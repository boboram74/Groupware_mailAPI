package com.end2end.mailapi.dao;

import com.end2end.mailapi.dto.MailDTO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MailDAO {

    @Autowired
    private SqlSession mybatis;
    public int receive(MailDTO dto) {
        mybatis.insert("mail.insert", dto);
        return dto.getId();
    }
}