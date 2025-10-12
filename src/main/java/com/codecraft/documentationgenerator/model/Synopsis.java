package com.codecraft.documentationgenerator.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 代码概要信息模型类
 * <p>
 * 用于封装代码的概要信息，包括参数、返回值等
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Data
public class Synopsis {
    /**
     * 代码类型(function, class, typedef, unspecified)
     */
    private String kind;

    /**
     * 参数列表
     */
    private List<Param> params;

    /**
     * 返回值描述
     */
    private String returns;

    /**
     * 返回值类型
     */
    private String returnsType;

    /**
     * 属性列表
     */
    private List<Property> properties;

    /**
     * 参数内部类
     */
    @Data
    public static class Param {
        /**
         * 参数名称
         */
        private String name;

        /**
         * 参数类型
         */
        private String type;

        /**
         * 参数说明
         */
        private String explanation;
    }

    /**
     * 属性内部类
     */
    @Data
    public static class Property {
        /**
         * 属性名称
         */
        private String name;

        /**
         * 属性类型
         */
        private String type;

        /**
         * 属性说明
         */
        private String explanation;
    }
}