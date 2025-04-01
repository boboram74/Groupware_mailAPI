package com.end2end.mailapi.service;

import com.end2end.mailapi.dto.EmployeeDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MailService {
    int addEmployee(EmployeeDTO dto);
    void receive(String mail, MultipartFile[] files);
    void deleteEmployee(String employee);
    void sendMail(String from, String to, String subject, String text);
}