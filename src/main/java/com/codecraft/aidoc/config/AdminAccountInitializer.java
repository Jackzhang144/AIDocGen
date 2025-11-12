package com.codecraft.aidoc.config;

import com.codecraft.aidoc.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Seeds the administrator account based on configuration so deployments have a deterministic admin login.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer {

    private final AdminProperties adminProperties;
    private final UserService userService;

    @PostConstruct
    public void ensureAdminAccount() {
        if (!StringUtils.hasText(adminProperties.getUsername()) || !StringUtils.hasText(adminProperties.getPassword())) {
            log.warn("[Aidoc] 未配置 security.admin.username/password，无法自动创建管理员账号");
            return;
        }
        userService.upsertAdmin(adminProperties.getUsername(), adminProperties.getEmail(), adminProperties.getPassword());
        log.info("[Aidoc] 管理员账号 {} 已确保存在", adminProperties.getUsername());
    }
}
