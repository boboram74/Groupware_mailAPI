package com.end2end.mailapi.util;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MailUtil {

    public static String messageConvertText(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof Multipart) {
            String text = getTextFromMultipart((Multipart) content);
            return (text != null && !text.trim().isEmpty()) ? text : "(본문 없음)";
        }
        return "(본문 없음)";
    }

    private static String getTextFromMultipart(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            String disposition = part.getDisposition();
            if (disposition == null || disposition.equalsIgnoreCase(Part.INLINE)) {
                if (part.isMimeType("text/plain")) {
                    return (String) part.getContent();
                } else if (part.isMimeType("text/html")) {
                    return (String) part.getContent();
                } else if (part.getContent() instanceof Multipart) {
                    String result = getTextFromMultipart((Multipart) part.getContent());
                    if (result != null && !result.trim().isEmpty()) {
                        return result;
                    }
                }
            }
        }
        return null;
    }
    public static void saveAttachments(Message message, String uploadPath) throws Exception {
        Object content = message.getContent();
        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                String disposition = part.getDisposition();
                if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                    String fileName = part.getFileName();
                    if (fileName != null) {
                        fileName = MimeUtility.decodeText(fileName);
                    } else {
                        fileName = "attachment_" + i;
                    }
                    String oriName = fileName;
                    System.out.println(oriName+"설정한 oriName입니다.");
                    String sysName = java.util.UUID.randomUUID() + "_" + fileName;
                    File file = new File(uploadDir, sysName);
                    long totalBytes = 0;
                    try (InputStream is = part.getInputStream();
                         FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            totalBytes += bytesRead;
                        }
                        double actualSizeMB = totalBytes / (1024.0 * 1024.0);
                        System.out.println("첨부파일 저장됨: " + file.getAbsolutePath());
                        System.out.println(String.format("첨부파일 실제 사이즈: %.2f MB", actualSizeMB));
                    }
                }
            }
        }
    }
}