package com.cqhacks.racelive.dto;

public record SessionStandingsRow(
        int position,
        String name,
        double points,
        int driverNumber,
        String teamColor,
        String abbreviation,
        String firstName,
        String lastName,
        String team,
        String countryCode,
        String headshotUrl) {}
