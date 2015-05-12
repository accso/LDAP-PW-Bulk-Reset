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

import com.beust.jcommander.Parameter;

/**
 * JCommander "Config" for PwResetTool
 *
 * @author Stefan Schubert
 */
public class CmdLineParams {
// ------------------------------ FIELDS ------------------------------

    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-notifyTemplate",
            description =
                    "File which contains the email template for the users for which an email notification is considered safe. "
                    + "(See Usage Example (1) to generate this file) "
                    + "The domain must be set through the -notifyDomain Option and in your template you can use the following "
                    + "substitution keyword ${password} for the mail body. For security "
                    + "reasons we do not send the login alongside with the password, as the user already knows it.")
    private String notifyTemplateFilepath;


    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-outputDir",
            description =
                    "Directory in which the password files will be written for the users, who are not to be notified" 
                    + " via email, i.e. their mail domain does not match the -notifyDomain Flag. "
                    + "(See Usage Example 'Reset Passwords') ")
    private String outputDir;

    
    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-userFile",
            description =
                    "File which contains the users for which the password will be reset. "
                    + "(See Usage Example (1) to generate this file) "
                    + "Users with the -notifyDomain will be notified via email (Note that the email stored within the"
                    + " ldap will be used, not the one from the file. For users which do not belong to the domain a text file will"
                    + " be generated which contains the new password so you may provide it manually.")
    private String resetCandidateFile;


    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-notifyDomain",
            description = "Domain of the users for which an email notification is considered safe. Must be used in conjunction"
                          + "with the -notifyTemplate and -userFile option.")
    private String notifyDomain;


    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-bindPW",
            description = "Password of the manger used for the -bindDN.",
            required = true)
    private String ldapBindPW;

    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-host",
            description = "Ldap-Host to connect to, e.g. 'localhost'",
            required = true)
    private String ldapHost;

    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-port",
            description = "Port of the LDAP Connection, usally 389 which my be used in conjunction with STARTTLS, or the old ldaps way via 636",
            required = true)
    private Integer ldapPort;

    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-bindDN",
            description = "Bind DN for UserExtraction, like ou=users,dc=testvm,dc=accso,dc=de",
            required = true
    )
    private String ldapBindDN;


    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-searchDN",
            description = "Searchbase for UserExtraction, like ou=users,dc=testvm,dc=accso,dc=de"
    )
    private String ldapSearchBase;

    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-smtpHost",
            description = "resolvable hostname or ip address of the smtp mailer which will be used for the notifications."
    )
    private String smtpHost;


    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-smtpPort",
            description = "port of the smtp mailer which will be used for the notifications."
    )
    private Integer smtpPort;

    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-mailFrom",
            description = "Email address which will be used as sender for the mail notifications."
    )
    private String mailFrom;

    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-mailSubject",
            description = "Email Subject which will be used for the mail notifications."
    )
    private String mailSubject;


    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = "-help", help = true)
    private boolean help;

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getLdapBindDN() {
        return this.ldapBindDN;
    }

    public String getLdapBindPW() {
        return ldapBindPW;
    }

    public String getLdapHost() {
        return ldapHost;
    }

    public Integer getLdapPort() {
        return this.ldapPort;
    }

    public String getLdapSearchBase() {
        return this.ldapSearchBase;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public String getNotifyDomain() {
        return this.notifyDomain;
    }

    public String getNotifyTemplateFilepath() {
        return this.notifyTemplateFilepath;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getResetCandidateFile() {
        return this.resetCandidateFile;
    }

    public String getSmtpHost() {
        return this.smtpHost;
    }

    public Integer getSmtpPort() {
        return this.smtpPort;
    }

    public final boolean isHelp() {
        return help;
    }
}
