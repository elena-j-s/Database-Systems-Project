package com.cqhacks.racelive.dto;

import java.util.List;

public record SessionPayload(
        int round,
        String eventName,
        String location,
        String country,
        List<SessionStandingsRow> standings,
        List<String> insights,
        SessionFeaturedDriver featuredDriver) {}
