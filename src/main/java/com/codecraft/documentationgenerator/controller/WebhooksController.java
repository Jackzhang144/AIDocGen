package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Stripe Webhook 处理
 */
@Slf4j
@RestController
@RequestMapping("/webhooks")
public class WebhooksController {

    private final UserServiceInterface userService;

    public WebhooksController(UserServiceInterface userService) {
        this.userService = userService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripe(@RequestBody StripeEvent event) {
        if (event.getData() == null || event.getData().getObject() == null) {
            return ResponseEntity.ok().build();
        }

        String email = event.getData().getObject().getCustomerEmail();
        String customerId = event.getData().getObject().getCustomer();
        if (email == null) {
            return ResponseEntity.ok().build();
        }

        switch (event.getType()) {
            case "checkout.session.completed":
            case "invoice.paid":
                updatePlan(email, customerId, "premium");
                break;
            case "customer.subscription.deleted":
            case "invoice.payment_failed":
                updatePlan(email, null, null);
                break;
            default:
                break;
        }

        return ResponseEntity.ok().build();
    }

    private void updatePlan(String email, String customerId, String plan) {
        User user = userService.findByEmailOrNull(email);
        if (user == null) {
            log.warn("Received Stripe event for unknown user {}", email);
            return;
        }
        user.setPlan(plan);
        user.setStripeCustomerId(customerId);
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateSubscriptionInfo(user);
    }

    @Data
    public static class StripeEvent {
        private String type;
        private StripeEventData data;
    }

    @Data
    public static class StripeEventData {
        private StripeSession object;
    }

    @Data
    public static class StripeSession {
        @com.fasterxml.jackson.annotation.JsonProperty("customer_email")
        private String customerEmail;
        private String customer;
    }
}
