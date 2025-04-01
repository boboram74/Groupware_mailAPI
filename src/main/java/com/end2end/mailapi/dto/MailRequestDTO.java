package com.end2end.mailapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MailRequestDTO {
    private String from;
    private String to;
    private String subject;
    private String text;
}