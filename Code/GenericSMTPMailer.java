import java.io.File;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * Maven repo artifact dependency: javax.mail:mail:1.5.0-b01
 *
 * Class for sending email messages through generic SMTP services
 *
 * In its current state hooks up to my csia69420@gmail.com account, some issues with gmail and by extension gjh servers.
 * Is not a great long term choice since gmail automatically revokes "less secure app" access. Work in progress on a proper
 * Oauth 2 gmail api implementation.
 */

public class GenericSMTPMailer {

    static Session session = null;

    /**
     *Initialization function to be run before sending first email
     *
     * Connects to SMTP server and authorizes the connection
     *
     * @param host SMTP server address
     * @param sender Email address to be logged into
     * @param password Password for the sender's email address
     */

    public static void initializeSession(String host, String sender, String password){
        if (session==null) {
            // Get system properties
            Properties properties = System.getProperties();

            // SMTP setup
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            // Pass username and password
            session = Session.getInstance(properties, new javax.mail.Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(sender, password);
                }

            });

            // SMTP debug for testing
            session.setDebug(true);
        }
    }


    /**
     * Once SMTP session is initialized the sendMail function sends a mail
     *
     * Takes about two seconds per mail from my testing
     *
     * @param to - String in form a@b.tld representing the recipient's email address
     * @param subject - String in email subject field
     * @param body - String to send in mail body
     * @param attachment - File to be appended as attachment
     */
        public static void sendMail (String to, String subject, String body, File attachment) {
            if (session != null) {

                // Sender's email required but overwritten
                String from = "generic@gmail.com";

                try {
                    // Construct default mimemessage.
                    MimeMessage message = new MimeMessage(session);

                    // Set From
                    message.setFrom(new InternetAddress(from));

                    // Set To
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

                    // Set Subject
                    message.setSubject(subject);

                    //Construct body encapsulation class
                    BodyPart messageBodyPart = new MimeBodyPart();

                    // Set body
                    messageBodyPart.setText(body);

                    // Construct message
                    Multipart multipart = new MimeMultipart();

                    // Set text message part
                    multipart.addBodyPart(messageBodyPart);

                    // Part two is attachment
                    if (attachment!=null) {
                        messageBodyPart = new MimeBodyPart();
                        String filename = attachment.getAbsolutePath();
                        DataSource source = new FileDataSource(filename);
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(filename);
                        multipart.addBodyPart(messageBodyPart);
                    }

                    // Send the complete message parts
                    message.setContent(multipart);

                    // Send message
                    Transport.send(message);
                } catch (MessagingException mex) {
                    mex.printStackTrace();
                }
            }
        }

        public static void main(String [] args) {

            //Sample code for an initialization
            initializeSession("smtp.gmail.com","csia69420@gmail.com", "thecsiasucks");
            sendMail("michalpalic1@gmail.com","Test subject", "body",new File("C:\\Users\\micha\\Desktop\\ayy.jpg"));
            sendMail("michalpalic1@gmail.com","Test subject2", "body4",null);


        }
}








