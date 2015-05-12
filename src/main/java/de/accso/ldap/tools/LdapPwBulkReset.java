/*
 * Copyright 2015 Accso GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.accso.ldap.tools;

import com.beust.jcommander.JCommander;
import de.accso.common.PWPolicy;
import de.accso.common.PWStrength;
import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.password.PasswordUtil;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.StringTokenizer;

public class LdapPwBulkReset {
// ------------------------------ FIELDS ------------------------------

    private static PWPolicy pwPolicy;

    private static String notificationTemplate;

    private static Session session;


// --------------------------- main() method ---------------------------

    public static void main(final String[] args) throws LdapException, IOException, CursorException, MessagingException {
        final CmdLineParams cmdLine = new CmdLineParams();
        final JCommander jCommander = new JCommander(cmdLine, args);

        pwPolicy = new PWPolicy(PWStrength.MEDIUM);

        if (cmdLine.isHelp()) {
            printHelpAndUsageInfo(jCommander);
            return;
        }

        final LdapConnection connection = getLdapConnection(cmdLine);

        if (isExportSetting(cmdLine)) {
            dumpUserOnConsole(cmdLine, connection);
        }

        else if (isDoResetSetting(cmdLine)) {
            resetUserPasswords(cmdLine, connection);
        }
        else {
            // default
            printHelpAndUsageInfo(jCommander);
        }

        // Cleanup
        connection.unBind();
        connection.close();
    }


    private static LdapConnection getLdapConnection(final CmdLineParams pCmdLine) throws LdapException {
        final LdapConnection connection;
        final String ldapBindPW = pCmdLine.getLdapBindPW();
        final String ldapHost = pCmdLine.getLdapHost();
        final Integer ldapPort = pCmdLine.getLdapPort();
        final String ldapBindDN = pCmdLine.getLdapBindDN();


        final LdapConnectionConfig connectionConfig = new LdapConnectionConfig();
        connectionConfig.setLdapHost(ldapHost);
        connectionConfig.setLdapPort(ldapPort);
        connectionConfig.setName(ldapBindDN);
        connectionConfig.setCredentials(ldapBindPW);

        if (ldapPort == 636) {
            // if this fails see certificate import hints when calling this tool with -help as option.
            connectionConfig.setSslProtocol("SSLv3");
            connectionConfig.setUseSsl(true);
        }
        else {
            connectionConfig.setUseTls(true);
        }

        connection = new LdapNetworkConnection(connectionConfig);
        connection.setTimeOut(0);
        connection.connect();

        return connection;
    }


    private static boolean isExportSetting(final CmdLineParams pCmdLine) {
        final String ldapSearchBase = pCmdLine.getLdapSearchBase();
        return ((ldapSearchBase != null) && (!ldapSearchBase.isEmpty()));
    }


    private static void dumpUserOnConsole(final CmdLineParams pCmdLine, final LdapConnection pConnection) throws LdapException,
                                                                                                                 CursorException {
        final String ldapSearchBase = pCmdLine.getLdapSearchBase();
        final EntryCursor entryCursor = pConnection.search(ldapSearchBase, "(objectclass=inetOrgPerson)", SearchScope.ONELEVEL);

        final StringBuilder ldapUsers = new StringBuilder();
        while (entryCursor.next()) {
            final Entry entry = entryCursor.get();
            final String userInfo = "ID: " + entry.getDn() + "\n"
                                    + entry.get("mail") + "\n\n";
            ldapUsers.append(userInfo);
        }

        System.out.println(ldapUsers);
    }


    private static boolean isDoResetSetting(final CmdLineParams pCmdLine) {
        boolean result = true;

        if (nullOrEmptyString(pCmdLine.getNotifyDomain())
            || nullOrEmptyString(pCmdLine.getNotifyTemplateFilepath())
            || nullOrEmptyString(pCmdLine.getResetCandidateFile())
            || nullOrEmptyString(pCmdLine.getSmtpHost())
            || (pCmdLine.getSmtpPort() == null)) {
            result = false;
        }

        return result;
    }


    private static boolean nullOrEmptyString(final String pString) {
        return ((pString == null) || pString.isEmpty());
    }


    private static void resetUserPasswords(final CmdLineParams pCmdLine, final LdapConnection pConnection) throws
                                                                                                           IOException,
                                                                                                           LdapException,
                                                                                                           CursorException,
                                                                                                           MessagingException {
        final String notifyDomain = pCmdLine.getNotifyDomain();
        //   final String notifyTemplateFilepath = pCmdLine.getNotifyTemplateFilepath();
        final String resetCandidateFile = pCmdLine.getResetCandidateFile();
        final String ldapManagerDN = pCmdLine.getLdapBindDN();
        final String ldapManagerPW = pCmdLine.getLdapBindPW();


        String outputDir;
        if (nullOrEmptyString(pCmdLine.getOutputDir())) {
            outputDir = "";
        }
        else {
            outputDir = pCmdLine.getOutputDir() + File.separator;
        }

        pConnection.bind(ldapManagerDN, ldapManagerPW); // required for being able to change the passwords

        File file = new File(resetCandidateFile);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("ID:")) {
                final String strippedLine = line.substring(3);
                final String trimmedUserDN = strippedLine.trim();
                final String newPassword = pwPolicy.generatePassword();

                final byte[] cryptedPassword = PasswordUtil
                        .createStoragePassword(newPassword.getBytes(), LdapSecurityConstants.HASH_METHOD_SHA);

                // set password in ldap
                final ModifyRequest modifyRequest = new ModifyRequestImpl();
                final Dn userDN = new Dn(trimmedUserDN);
                modifyRequest.setName(userDN);
                modifyRequest.replace("userPassword", cryptedPassword);

                final ModifyResponse modifyResponse = pConnection.modify(modifyRequest);

                assert (modifyResponse.getLdapResult().getResultCode() == ResultCodeEnum.SUCCESS);

                // notify user via mail or create password file
                final String userMailAddress = readUsersMailAddressFromLdap(trimmedUserDN, pConnection);

                if (mailNotifyable(userMailAddress, notifyDomain)) {
                    sendPasswordNotification(userMailAddress, newPassword, pCmdLine);
                    System.out.println("send mail to " + userMailAddress);
                }
                else {
                    System.out.println("Generated PW file for: " + userMailAddress);
                    String filename = outputDir + determineFilename(userMailAddress, trimmedUserDN) + ".txt";
                    Files.write(Paths.get(filename), newPassword.getBytes());
                }
            }
        }
        br.close();
        fr.close();
    }


    private static void sendPasswordNotification(final String pUserMailAddress, final String pNewPassword,
                                                 final CmdLineParams pCmdLine) throws IOException, MessagingException {

        final String smtpHost = pCmdLine.getSmtpHost();
        final Integer smtpPort = pCmdLine.getSmtpPort();
        final String mailFrom = pCmdLine.getMailFrom();
        final String mailSubject = pCmdLine.getMailSubject();
        final String notifyTemplateFilepath = pCmdLine.getNotifyTemplateFilepath();


        if (notificationTemplate == null) {
            notificationTemplate = new String(Files.readAllBytes(Paths.get(notifyTemplateFilepath)));
        }


        if (session == null) {
            Properties properties=new Properties();
            properties.setProperty("mail.smtp.host", smtpHost);
            properties.setProperty("mail.smtp.port", smtpPort.toString());
            session = Session.getDefaultInstance(properties, null);
        }

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailFrom));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(pUserMailAddress));
        message.setSubject(mailSubject);

        message.setText(notificationTemplate.replace("${password}",pNewPassword));

        // Send message
        Transport.send(message);
        System.out.println("message sent successfully....");

    }


    private static String readUsersMailAddressFromLdap(final String searchDN, final LdapConnection pConnection) throws
                                                                                                                LdapException,
                                                                                                                CursorException {
        final EntryCursor entryCursor = pConnection.search(searchDN, "(objectclass=inetOrgPerson)", SearchScope.OBJECT);
        entryCursor.next();
        final Entry entry = entryCursor.get();
        return entry.get("mail").toString().substring(6);
    }


    private static boolean mailNotifyable(String mailAddress, String allowedMailDomain) {
        StringTokenizer token = new StringTokenizer(mailAddress, "@");
        token.nextToken();
        final String domain = token.nextToken();
        return domain.equalsIgnoreCase(allowedMailDomain);
    }


    /**
     * Returns the mail address if available (but converted for dealing as filename)
     * otherwise the alternative is used.
     *
     * @param mailadr
     * @return
     */
    private static String determineFilename(String mailadr, String alternative) {
        if (nullOrEmptyString(mailadr)) {
            return alternative;
        }
        else {
            StringTokenizer token = new StringTokenizer(mailadr, "@");
            String filename = token.nextToken() + "_AT_" + token.nextToken();
            return filename;
        }
    }


    private static void printHelpAndUsageInfo(final JCommander pJCommander) {
        pJCommander.usage();
        final String sslProps =
                "USAGE Examples:\n\n"
                + "1) Extract Users\n\n"
                + "java -jar LdapPwBulkReset -host 192.168.178.95 -port 389 -bindDN \"cn=admin, dc=testvm, dc=accso, dc=de\" -bindPW YOUR_SECRET -searchDN \"ou=users,dc=testvm,dc=accso,dc=de\"\n\n"
                + "Pipe the result into a file and delete the entries you don't want to change (The eMail line ist just to help grouping the set if required)\n\n"
                + "2) Reset Passwords\n\n"
                + "java -jar LdapPwBulkReset -host 192.168.178.95 -port 389 -bindDN \"cn=admin, dc=testvm, dc=accso, dc=de\" -bindPW YOUR_SECRET -userFile \"c:/spool/userList.txt\" -outputDir=\"C:\\tmp\\\" -notifyDomain accso.de -notifyTemplate \"c:/spool/notificationMail.txt\"\n\n"
                + "This will send notifications mails with new passwords to the specified domain and generates a password file per each other user"
                + " to issue it manually"
                + "\n\n"
                + "=== SSL ISSUES ??? =================================================================================\n"
                + "Due to security we use SSL or STARTTLS. However you might encounter SSL handshake problems.\n"
                + "These might occur when your ldap uses a self signed certificate and your java environment does not trust\n"
                + "the used root CA (for good reasons).\n\n "
                + "(1) Get the used root certificate.\n\n"
                + " If You don't have it at hand, you may use LDAP-ApacheDirectoryStudio, where you configured\n"
                + "the ldaps connection to your server and tested it successfully. Now choose your connection and go to\n"
                + " the property dialog tab 'network parameter' and click the link to 'certificate management'. There you can\n"
                + " download your root certificate. (Use X.509 CER Format).\n\n"
                + "(2) Lookup your java environment an import the certificate.\n\n"
                + "e.g. keytool -importcert -alias NEW_AD  -file \"/opt/ire1.cer\" -keystore \"/opt/Java/jdk1.5.0_20/jre/lib/security/cacerts \"\n"
                + "BTW: the default keystore password is 'changeit' if it does not suit contact your sysadmin.\n"
                + "You may need to delete a previously existing certificate like this:\n"
                + "e.g. keytool -delete -alias OLD_AD -keystore \"/opt/Java/jdk1.5.0_21/jre/lib/security/cacerts\"\n"
                + "====================================================================================================\n";
        System.out.println(sslProps);
    }
}
