package com.end2end.mailapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private int id;
    private String oriName;
    private String sysName;
    private String path;
    private double size;
}