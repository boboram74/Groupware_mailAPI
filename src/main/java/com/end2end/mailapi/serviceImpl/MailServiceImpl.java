package com.end2end.mailapi.serviceImpl;

import com.end2end.mailapi.dao.FileDAO;
import com.end2end.mailapi.dao.MailDAO;
import com.end2end.mailapi.dto.*;
import com.end2end.mailapi.service.MailService;
import com.end2end.mailapi.util.MailUtil;
import com.end2end.mailapi.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.util.StringUtils.cleanPath;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private MailDAO mailDAO;

    @Autowired
    private FileDAO fileDAO;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public int addEmployee(EmployeeDTO dto) {
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
    public void sendMail(String mail, MultipartFile[] files) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        MailRequestDTO dto = mapper.readValue(mail, MailRequestDTO.class);

        boolean hasFiles = files != null && files.length > 0;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, hasFiles, "UTF-8");

        mimeMessageHelper.setFrom(dto.getFrom());
        mimeMessageHelper.setTo(dto.getTo());
        mimeMessageHelper.setSubject(dto.getSubject());
        mimeMessageHelper.setText(dto.getText(), false);

        if(hasFiles) {
            for (MultipartFile file : files) {
                if(!file.isEmpty()) {
                    String fileName = cleanPath(file.getOriginalFilename());
                    mimeMessageHelper.addAttachment(fileName, new ByteArrayResource(file.getBytes()));
                }
            }
        }
        mailSender.send(mimeMessage);
    }

    @Transactional
    @Override
    public void receive(String mail, MultipartFile[] files) {
        //System.out.println("메일 본문 : "+mail);
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
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://34.47.99.32/mail/alarm?mailId=" + mailId + "&email=" + mailDTO.getRecipient_email();
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("응답: " + response);

            List<Integer> result = mailDAO.findByList(mailDTO.getRecipient_email());
            for(int i=0; i<result.size(); i++) {
                mailDAO.insertState(mailId, result.get(i));
            }
            int atIndex = recipient.indexOf("@");
            String account = null;
            if (atIndex != -1) {
                account = recipient.substring(0, atIndex);
            }
            String uploadPath = "/home/"+account+"/file/";
            List<FileDTO> fileList = MailUtil.saveAttachments(message, uploadPath);
            List<Integer> fileIds = new ArrayList<>();
            for (FileDTO fileDto : fileList) {
                FileMapperDTO dtoMapper = FileMapperDTO.builder()
                        .mailId(mailId)
                        .build();
                fileDAO.insert(dtoMapper);
                int fileId = dtoMapper.getId();
                fileIds.add(fileId);

                FileDetailDTO detailDto = FileDetailDTO.builder()
                        .fileId(fileId)
                        .originFilename(fileDto.getOriName())
                        .systemFilename(fileDto.getSysName())
                        .path(fileDto.getPath())
                        .filesize(fileDto.getSize())
                        .build();
                fileDAO.insertDetail(detailDto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void deleteEmployee(String employee) {
        String cmd = String.format("userdel -r %s", employee);
        try {
            Process process = new ProcessBuilder("/bin/bash", "-c", cmd).start();
            int exitCode = process.waitFor();
            process.getErrorStream().transferTo(System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}