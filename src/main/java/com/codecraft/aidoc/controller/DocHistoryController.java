package com.codecraft.aidoc.controller;

import com.codecraft.aidoc.common.ApiResponse;
import com.codecraft.aidoc.common.PageResponse;
import com.codecraft.aidoc.enums.UserRole;
import com.codecraft.aidoc.pojo.request.DocHistoryQueryRequest;
import com.codecraft.aidoc.pojo.response.DocHistoryItem;
import com.codecraft.aidoc.security.UserPrincipal;
import com.codecraft.aidoc.service.DocHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes paginated history pages for authenticated users.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/docs/history")
public class DocHistoryController {

    private final DocHistoryService docHistoryService;

    @GetMapping
    public ApiResponse<PageResponse<DocHistoryItem>> pageHistory(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @Valid DocHistoryQueryRequest request) {
        if (principal.getRole() != UserRole.ADMIN) {
            request.setUserId(principal.getUsername());
        }
        PageResponse<DocHistoryItem> response = docHistoryService.pageHistory(request);
        return ApiResponse.ok("获取历史记录成功", response);
    }
}
