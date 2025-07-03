package com.example.chatappadmin;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class GmailSender extends Authenticator {
    private final String user;
    private final String password;

    public GmailSender(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public void sendMail(String subject, String body, String sender, String recipients) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
