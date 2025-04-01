package com.end2end.mailapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class MailDTO {
    private int id;
    private int parents_mail_id;
    private String sender_email;
    private String recipient_email;
    private String title;
    private String content;
}
