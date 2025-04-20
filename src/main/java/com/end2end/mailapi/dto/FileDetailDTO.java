package com.end2end.mailapi.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDetailDTO {
    private int fileId;
    private String originFilename;
    private String systemFilename;
    private String path;
    private double filesize;
}