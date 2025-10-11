package com.codecraft.documentationgenerator.entity;

import lombok.Data;

@Data
public class ApiKey {
    private Long id;
    private String hashedKey;
    private String email;
    private String purpose;
}