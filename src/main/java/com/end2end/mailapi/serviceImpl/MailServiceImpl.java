package com.end2end.mailapi.serviceImpl;

import com.end2end.mailapi.dao.MailDAO;
import com.end2end.mailapi.dto.EmployeeDTO;
import com.end2end.mailapi.dto.MailDTO;
import com.end2end.mailapi.service.MailService;
import com.end2end.mailapi.util.MailUtil;
import com.end2end.mailapi.util.SecurityUtil;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    MailDAO mailDAO;
    @Autowired
    private JavaMailSender mailSender;

    public int addEmployee(EmployeeDTO dto) {
        if (!dto.getName().matches("^[a-z_][a-z0-9_-]{0,31}$")) {
            System.out.println("[에러]유효하지 않은 계정명: " + dto.getName());
            return 0;
        }
        String passwod = SecurityUtil.hashPassword(dto.getPassword());
        String cmd = String.format("useradd -m -s /sbin/nologin %s && echo '%s:%s' | chpasswd -e",
                dto.getName(), dto.getName(), passwod);
        try {
            Process process = new ProcessBuilder("/bin/bash", "-c", cmd).start();
            int exitCode = process.waitFor();
            process.getErrorStream().transferTo(System.err);
            return (exitCode == 0) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void receive(String mail, MultipartFile[] files) {
        System.out.println("메일 본문 : "+mail);
        try {
            InputStream is = new ByteArrayInputStream(mail.getBytes(StandardCharsets.UTF_8));
            Session session = Session.getDefaultInstance(new java.util.Properties());
            MimeMessage message = new MimeMessage(session, is);
            //제목
            String subject = message.getSubject();
            if (subject != null) {
                subject = MimeUtility.decodeText(subject);
            }
            //보낸사람
            String from = "(unknown)";
            Address[] fromAddresses = message.getFrom();
            if (fromAddresses != null && fromAddresses.length > 0) {
                from = ((InternetAddress) fromAddresses[0]).getAddress();
            }
            //받는사람
            String recipient = "(unknown)";
            Address[] toAddresses = message.getRecipients(Message.RecipientType.TO);
            if (toAddresses != null && toAddresses.length > 0) {
                recipient = ((InternetAddress) toAddresses[0]).getAddress();
            }
            String body = MailUtil.messageConvertText(message);
            MailDTO mailDTO = new MailDTO();
            mailDTO.setParents_mail_id(0);
            mailDTO.setSender_email(from);
            mailDTO.setRecipient_email(recipient);
            mailDTO.setTitle(subject);
            mailDTO.setContent(body);
            System.out.println("보낸 사람: " + mailDTO.getSender_email());
            System.out.println("받는 사람: " + mailDTO.getRecipient_email());
            System.out.println("제목: " + mailDTO.getTitle());
            System.out.println("본문: " + mailDTO.getContent());
            int mailId =  mailDAO.receive(mailDTO);
            System.out.println("ID : "+mailId);
            int atIndex = recipient.indexOf("@");
            String account = null;
            if (atIndex != -1) {
                account = recipient.substring(0, atIndex);
            }
            String uploadPath = "/home/"+account+"/file/";
            MailUtil.saveAttachments(message, uploadPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void deleteEmployee(String employee) {
        if (!employee.matches("^[a-z_][a-z0-9_-]{0,31}$")) {
            System.out.println("[에러] 유효하지 않은 계정명: " + employee);
            return;
        }
        String cmd = String.format("userdel -r %s", employee);
        try {
            Process process = new ProcessBuilder("/bin/bash", "-c", cmd).start();
            int exitCode = process.waitFor();
            process.getErrorStream().transferTo(System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMail(String from, String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}