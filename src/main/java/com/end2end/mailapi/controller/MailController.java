package com.end2end.mailapi.controller;

import com.end2end.mailapi.dto.MemberDTO;
import com.end2end.mailapi.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public String listMail() {
        return "Mail GET";
    }

    @PostMapping("/receive")
    public String listoneMail(@RequestBody String mailContent) {
        memberService.receive(mailContent);
        return "메일 처리 성공";
    }

    @PostMapping("/member")
    public String addMember(@RequestBody MemberDTO dto) {
        System.out.println(dto.getName() + " : " + dto.getPassword());
        int result = memberService.addMember(dto);
        if (result == 1) {
            System.out.println("계정생성완료");
            return "계정생성완료";
        } else {
            System.out.println("계정생성실패");
            return "계정생성실패";
        }
    }

    @PutMapping
    public String updateMail() {
        return "Mail PUT";
    }
    @DeleteMapping
    public String deleteMail() {
        return "Mail DELETE";
    }
}
