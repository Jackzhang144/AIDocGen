package com.codecraft.documentationgenerator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 根路径欢迎接口
 */
@RestController
public class RootController {

    @GetMapping("/")
    public String welcome() {
        return "\uD83C\uDF43 Welcome to the Mintlify API";
    }
}
