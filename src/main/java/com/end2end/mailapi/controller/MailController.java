package com.end2end.mailapi.controller;

import com.end2end.mailapi.dto.EmployeeDTO;
import com.end2end.mailapi.dto.MailRequestDTO;
import com.end2end.mailapi.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping(value = "/receive") //메일 수신
    public ResponseEntity<Void> receive(
            @RequestPart("email") String email,
            @RequestPart(name = "files", required = false) MultipartFile[] files) {
        System.out.println("메일 본문: " + email);
        System.out.println("첨부파일 개수: " + (files != null ? files.length : 0));
        mailService.receive(email, files);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send") //메일 발신
    public ResponseEntity<String> sendMail(@RequestBody MailRequestDTO dto) {
        System.out.println("dto = " + dto.getFrom() + " " + dto.getTo() + " " + dto.getSubject() + " " + dto.getText());
        mailService.sendMail(
                dto.getFrom(),
                dto.getTo(),
                dto.getSubject(),
                dto.getText()
        );
        return ResponseEntity.ok("메일 발송 완료");
    }


    @PostMapping("/employee") //계정 생성
    public String addMember(@RequestBody EmployeeDTO dto) {
        int result = mailService.addEmployee(dto);
        if (result == 1) {
            return "계정생성완료";
        } else {
            return "계정생성실패";
        }
    }
    @DeleteMapping("/employee") //게정 삭제
    public String deleteMail(String employee) {
        mailService.deleteEmployee(employee);
        return "계정삭제완료";
    }
}