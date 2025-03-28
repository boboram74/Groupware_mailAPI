package com.end2end.mailapi.serviceImpl;

import com.end2end.mailapi.dto.MemberDTO;
import com.end2end.mailapi.service.MemberService;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    public int addMember(MemberDTO dto) {
        if (!dto.getName().matches("^[a-z_][a-z0-9_-]{0,31}$")) {
            System.err.println("[에러]유효하지 않은 계정명: " + dto.getName());
            return 0;
        }
        String cmd = String.format("useradd -m %s && echo '%s:%s' | chpasswd",
                dto.getName(), dto.getName(), dto.getPassword());
        try {
            Process process = new ProcessBuilder("/bin/bash", "-c", cmd).start();
            int exitCode = process.waitFor();
            process.getErrorStream().transferTo(System.err);
            System.out.println("[에러] " + exitCode);
            return (exitCode == 0) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void receive(String mailContent) {
        System.out.println(mailContent+"이 내용은 메일 본문 내용입니다.");
    }
}