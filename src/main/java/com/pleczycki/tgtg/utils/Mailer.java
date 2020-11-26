package com.pleczycki.tgtg.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Scanner;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
public class Mailer {

    public void send(String to, String sub, String msg) {

        String pass = getPassword();

        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        //get Session
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("tgtgpolska@gmail.com", pass);
                    }
                });
        //compose message
        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(sub);
            message.setText(msg, "utf-8", "html");
            Transport.send(message);
            System.out.println("message sent successfully");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPassword() {
        File file = new File("/usr/local/bin/tgtg/data.txt");

//        File file = new File("C:\\Users\\Patryk\\Documents\\tgtg\\data.txt");
        String password = "";

        try (Scanner scan = new Scanner(file)) {
            //read lines from file
            password = scan.nextLine();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        return password;
    }
}
