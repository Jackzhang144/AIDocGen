package com.codecraft.aidoc.service;

import java.util.Map;

/**
 * Facade for analytics providers. The default implementation simply logs structured events;
 * it can be replaced with Segment, Mixpanel, etc. in production deployments.
 */
public interface TelemetryService {

    /**
     * Tracks an event for the supplied user identifier.
     *
     * @param userId unique user identifier (can be hashed)
     * @param eventName event name
     * @param properties event payload
     */
    void track(String userId, String eventName, Map<String, ?> properties);
}
