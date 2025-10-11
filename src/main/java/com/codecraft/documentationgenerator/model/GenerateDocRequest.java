package com.codecraft.documentationgenerator.model;

import lombok.Data;

@Data
public class GenerateDocRequest {
    private String code;
    private String languageId;
    private String fileName;
    private String context;
    private Integer location;
    private String line;
    private Boolean isSelection;
    private String docFormat;
}