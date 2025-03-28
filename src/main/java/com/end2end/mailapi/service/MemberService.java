package com.end2end.mailapi.service;

import com.end2end.mailapi.dto.MemberDTO;

public interface MemberService {
    int addMember(MemberDTO dto);
    void receive(String mailContent);
}
