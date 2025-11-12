package com.codecraft.aidoc.service.impl;

import com.codecraft.aidoc.service.TelemetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Logging backed telemetry implementation suitable for non-production environments.
 */
@Slf4j
@Service
public class TelemetryServiceImpl implements TelemetryService {

    @Override
    public void track(String userId, String eventName, Map<String, ?> properties) {
        log.info("[AIDocGen] Telemetry 事件 event={} user={} props={}", eventName, userId, properties);
    }
}
