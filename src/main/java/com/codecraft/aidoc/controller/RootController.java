package com.codecraft.aidoc.controller;

import com.codecraft.aidoc.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供简单的根路径探活接口，避免默认静态资源映射导致的 500。
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class RootController {

    @GetMapping({"", "/"})
    public ApiResponse<String> welcome() {
        return ApiResponse.ok("Aidoc backend is running", "Aidoc backend is running");
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("Aidoc backend is running", "Aidoc backend is running");
    }
}
