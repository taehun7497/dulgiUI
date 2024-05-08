package com.korea.dulgiUI.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender){this.javaMailSender=javaMailSender;}

    public void sendMail(String email, String password){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("임시 비밀번호");
        message.setText("임시 비밀번호는 " + password + " 입니다람쥐.");
        javaMailSender.send(message);
    }


}
