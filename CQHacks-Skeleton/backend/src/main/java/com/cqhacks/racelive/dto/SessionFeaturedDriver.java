package com.cqhacks.racelive.dto;

public record SessionFeaturedDriver(
        int driverNumber,
        String abbreviation,
        String firstName,
        String lastName,
        String team,
        String countryCode,
        String headshotUrl,
        String teamColor) {}
