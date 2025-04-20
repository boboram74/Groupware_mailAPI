package com.end2end.mailapi.controller;

import com.end2end.mailapi.dto.EmployeeDTO;
import com.end2end.mailapi.service.MailService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping(value = "/receive") //메일 수신
    public ResponseEntity<Void> receive(
            @RequestPart("email") String mail,
            @RequestPart(name = "files", required = false) MultipartFile[] files) {
        System.out.println("메일 본문: " + mail);
        System.out.println("첨부파일 개수: " + (files != null ? files.length : 0));
        mailService.receive(mail, files);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send") //메일 발신
    public ResponseEntity<String> sendMail(
            @RequestPart("email") String mail,
            @RequestPart(name = "files", required = false) MultipartFile[] files) throws Exception{
        mailService.sendMail(mail,files);
        return ResponseEntity.ok().build();
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

    @RequestMapping("/files")
    public ResponseEntity<Void> download(HttpServletResponse response, String path) {
        File file = new File(path);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        try (OutputStream os = response.getOutputStream();
            InputStream is = new FileInputStream(file)) {
            String encodedFilename = URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
            FileCopyUtils.copy(is, os);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/storage")
    public ResponseEntity<Map<String, String>> getStorageUsage() throws Exception {
        Process proc = new ProcessBuilder("df", "-Th", "/").start();
        if (!proc.waitFor(5, TimeUnit.SECONDS)) {
            throw new TimeoutException("df command timed out");
        }
        if (proc.exitValue() != 0) {
            throw new IOException("df command failed with exit code: " + proc.exitValue());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            reader.readLine();
            String line = reader.readLine();

            String[] parts = line.trim().split("\\s+");
            return ResponseEntity.ok(Map.of(
                    "size", parts[2],
                    "usePercent", parts[5]
            ));
        }
    }
}