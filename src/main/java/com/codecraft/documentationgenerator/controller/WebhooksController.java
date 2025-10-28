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
        log.info("Received Stripe webhook event: {}", event.getType());
        if (event.getData() == null || event.getData().getObject() == null) {
            log.warn("Stripe webhook missing payload body");
            return ResponseEntity.ok().build();
        }

        String email = event.getData().getObject().getCustomerEmail();
        String customerId = event.getData().getObject().getCustomer();
        if (email == null) {
            log.warn("Stripe webhook missing customer email for event {}", event.getType());
            return ResponseEntity.ok().build();
        }

        switch (event.getType()) {
            case "checkout.session.completed":
            case "invoice.paid":
                log.info("Activating premium plan for {} (customerId={})", maskEmail(email), customerId);
                updatePlan(email, customerId, "premium");
                break;
            case "customer.subscription.deleted":
            case "invoice.payment_failed":
                log.info("Deactivating premium plan for {}", maskEmail(email));
                updatePlan(email, null, null);
                break;
            default:
                log.debug("Ignoring unsupported Stripe event type: {}", event.getType());
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
        log.debug("Updated subscription for {} => plan={}, customerId={}", maskEmail(email), plan, customerId);
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "<empty>";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + (atIndex == -1 ? "" : email.substring(atIndex));
        }
        return email.substring(0, Math.min(2, atIndex)) + "***" + email.substring(atIndex);
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
