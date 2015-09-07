# LdapPwBulkReset

**Commandline tool to bulk reset the ldap password for a crowd of ldap accounts.**

Author: Stefan Schubert  
Requires Java8

Release-Info: Working SNAPSHOT Release. (Without big exception handling, yet) 

## PURPOSE

The reason to create this tool was, that one of the major wiki and issue-tracker vendor had an
own user directory implementation which was in usage. According to IT strategy the accounts
needed to be moved to an dedicate LDAP server. The tool vendor supports the account migration, but
because of the nature of password security there is no API to preserve the passwords of the account.
 
If you can think of offering an ldap self service in your environment you should stop reading here,
get the self-service, e.g. the one from the [LTP Project](http://ltb-project.org/wiki/documentation/self-service-password),
up and running and that's it.
 
However there may be circumstances that a self-service is not an option. Here you go.
You may take this tool, extract the migrated ldap accounts from the target ldap, choose
the user-set to bulk reset. Decide for which domain you can securely provide passwords 
via email and for which you need to generate files which you will deliver manually.

Drop me a comment if you could use this tool ;-)

## Building

*mvn package*

This will create a *LDAP-PW-Bulk-Reset-1.0-SNAPSHOT-jar-with-dependencies.jar* which is executable and a
*LDAP-PW-Bulk-Reset-1.0-SNAPSHOT.jar* which is quite smaller, but you have to ensure that the dependencies
are on your classpath before execution.

*mvn site*

Generates a detailed project report.

## Development Notes

You may need to add an -Djava.net.preferIPv4Stack=true for being able to connect to your mail server.

## Usage Example

See *java -jar LDAP-PW-Bulk-Reset-1.0-SNAPSHOT-jar-with-dependencies.jar -help* 

* You might require a mail server accepting anonymous connections for being able to send the notification mails. 
* You probably want to dry run especially the mail part before running it productive. For that purpose take a dummy mailer of your choice (e.g. smtp4dev).

### Required SSL Configuration

Due to security we use SSL or STARTTLS. However you might encounter SSL handshake problems.
These might occur when your ldap uses a self signed certificate and your java environment does not trust
the used root CA (for good reasons).

 (1) Get the used root certificate.

 If You don't have it at hand, you may use LDAP-ApacheDirectoryStudio, where you configured
the ldaps connection to your server and tested it successfully. Now choose your connection and go to
 the property dialog tab 'network parameter' and click the link to 'certificate management'. There you can
 download your root certificate. (Use X.509 CER Format).

(2) Lookup your java environment an import the certificate.

e.g. keytool -importcert -alias NEW_AD  -file "/opt/ire1.cer" -keystore "/opt/Java/jdk1.5.0_20/jre/lib/security/cacerts "
BTW: the default keystore password is 'changeit' if it does not suit contact your sysadmin.
You may need to delete a previously existing certificate like this:
e.g. keytool -delete -alias OLD_AD -keystore "/opt/Java/jdk1.5.0_21/jre/lib/security/cacerts"

## License 
APLv2.0, see license-apache2.txt

## Depends on 

* [Passay](https://github.com/vt-middleware/passay), dual-licensed: APL2 and LGPL, used for password generation and policy check  
* [jCommander](https://github.com/cbeust/jcommander), ASF2, used for commandline Parsing 
* [Apache Directory LDAP API](https://directory.apache.org/api/), APL2, for ldap API  

## Credits

*Thanks to our customer for sharing this piece of work as open source. Special thanks goes to Andreas!*
