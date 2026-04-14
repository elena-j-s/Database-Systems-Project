package com.cqhacks.racelive.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates drivers / laps for one event (ported from backend team).
 */
public class Race {

    private final String country;
    private final int rounds;
    private final String location;
    private final String eventName;
    private final List<Driver> drivers = new ArrayList<>();

    public Race(int rounds, String country, String location, String eventName) {
        this.rounds = rounds;
        this.country = country;
        this.location = location;
        this.eventName = eventName;
    }

    public int getRounds() {
        return rounds;
    }

    public String getEventName() {
        return eventName;
    }

    public String getCountry() {
        return country;
    }

    public String getLocation() {
        return location;
    }

    public void addDriver(Driver driver) {
        drivers.add(driver);
    }

    public List<Driver> getDrivers() {
        return drivers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════╗\n")
                .append("  Race   : ")
                .append(eventName)
                .append("\n")
                .append("  Round  : ")
                .append(rounds)
                .append("\n")
                .append("  Country: ")
                .append(country)
                .append("\n")
                .append("  Circuit: ")
                .append(location)
                .append("\n")
                .append("  Drivers: ")
                .append(drivers.size())
                .append("\n")
                .append("╚══════════════════════════════════════════════════════╝\n");
        for (Driver driver : drivers) {
            sb.append(driver.toString());
        }
        return sb.toString();
    }
}
