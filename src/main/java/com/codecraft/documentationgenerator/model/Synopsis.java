package com.codecraft.documentationgenerator.model;

import lombok.Data;
import java.util.List;

@Data
public class Synopsis {
    private String kind; // function, class, typedef, unspecified
    private List<Param> params;
    private String returns;
    private String returnsType;
    private List<Property> properties;
    
    @Data
    public static class Param {
        private String name;
        private String type;
        private String explanation;
    }
    
    @Data
    public static class Property {
        private String name;
        private String type;
        private String explanation;
    }
}