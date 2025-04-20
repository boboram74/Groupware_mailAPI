package com.end2end.mailapi.dao;

import com.end2end.mailapi.dto.FileDetailDTO;
import com.end2end.mailapi.dto.FileMapperDTO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FileDAO {

    @Autowired
    private SqlSession mybatis;

    public void insert(FileMapperDTO dto) {
        mybatis.insert("file.insert", dto);
    }

    public void insertDetail (FileDetailDTO dto) {
        mybatis.insert("file.insertDetail", dto);
    }
}