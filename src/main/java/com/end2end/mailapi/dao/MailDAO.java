package com.end2end.mailapi.dao;

import com.end2end.mailapi.dto.MailDTO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MailDAO {

    @Autowired
    private SqlSession mybatis;

    public int receive(MailDTO dto) {
        mybatis.insert("mail.insert", dto);
        return dto.getId();
    }

    public void insertState(int mailId, int employeeId) {
        Map<String, Object> input = new HashMap<>();
        input.put("mailId", mailId);
        input.put("employeeId", employeeId);
        mybatis.insert("mail.insertState", input);
    }

    public List<Integer> findByList(String receiveMail) {
        return mybatis.selectList("mail.findByList", receiveMail);
    }
}