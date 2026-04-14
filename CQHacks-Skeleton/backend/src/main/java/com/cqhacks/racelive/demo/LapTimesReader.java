package com.cqhacks.racelive.demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LapTimesReader {

    private final String fileName;

    public LapTimesReader(String fileName) {
        this.fileName = fileName;
    }

    private static final int TIME = 0;
    private static final int DRIVER = 1;
    private static final int DRIVER_NUMBER = 2;
    private static final int LAP_TIME = 3;
    private static final int LAP_NUMBER = 4;
    private static final int STINT = 5;
    private static final int PIT_OUT_TIME = 6;
    private static final int PIT_IN_TIME = 7;
    private static final int SECTOR_1_TIME = 8;
    private static final int SECTOR_2_TIME = 9;
    private static final int SECTOR_3_TIME = 10;
    private static final int SECTOR_1_SESSION_TIME = 11;
    private static final int SECTOR_2_SESSION_TIME = 12;
    private static final int SECTOR_3_SESSION_TIME = 13;
    private static final int SPEEDI1 = 14;
    private static final int SPEEDI2 = 15;
    private static final int SPEEDFL = 16;
    private static final int SPEEDST = 17;
    private static final int IS_PERSONAL_BEST = 18;
    private static final int COMPOUND = 19;
    private static final int TYRE_LIFE = 20;
    private static final int FRESH_TYRE = 21;
    private static final int TEAM = 22;
    private static final int LAP_START_TIME = 23;
    private static final int LAP_START_DATE = 24;
    private static final int TRACK_STATUS = 25;
    private static final int POSITION = 26;
    private static final int DELETED = 27;
    private static final int DELETED_REASON = 28;
    private static final int FAST_F1_GENERATED = 29;
    private static final int IS_ACCURATE = 30;
    private static final int ROUND = 31;
    private static final int COUNTRY = 32;
    private static final int LOCATION = 33;
    private static final int EVENT_NAME = 34;

    public List<Race> readCSV() {
        Map<String, Race> racesByEvent = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] t = line.split(",", -1);
                String eventName = get(t, EVENT_NAME);
                Race race = racesByEvent.computeIfAbsent(
                        eventName,
                        key -> new Race(parseInt(t, ROUND), get(t, COUNTRY), get(t, LOCATION), key));
                int driverNumber = parseInt(t, DRIVER_NUMBER);
                String driverName = get(t, DRIVER);
                String team = get(t, TEAM);
                Driver driver = race.getDrivers().stream()
                        .filter(d -> d.driverNumber == driverNumber)
                        .findFirst()
                        .orElseGet(() -> {
                            Driver d = new Driver(driverName, driverNumber, team);
                            race.addDriver(d);
                            return d;
                        });
                Lap lap = new Lap(
                        get(t, TIME),
                        get(t, LAP_TIME),
                        parseDouble(t, LAP_NUMBER),
                        parseDouble(t, STINT),
                        get(t, PIT_OUT_TIME),
                        get(t, PIT_IN_TIME),
                        get(t, SECTOR_1_TIME),
                        get(t, SECTOR_2_TIME),
                        get(t, SECTOR_3_TIME),
                        get(t, SECTOR_1_SESSION_TIME),
                        get(t, SECTOR_2_SESSION_TIME),
                        get(t, SECTOR_3_SESSION_TIME),
                        parseDouble(t, SPEEDI1),
                        parseDouble(t, SPEEDI2),
                        parseDouble(t, SPEEDFL),
                        parseDouble(t, SPEEDST),
                        parseBool(t, IS_PERSONAL_BEST),
                        get(t, COMPOUND),
                        parseDouble(t, TYRE_LIFE),
                        parseBool(t, FRESH_TYRE),
                        get(t, LAP_START_TIME),
                        get(t, LAP_START_DATE),
                        get(t, TRACK_STATUS),
                        parseDouble(t, POSITION),
                        parseBool(t, DELETED),
                        get(t, DELETED_REASON),
                        parseBool(t, FAST_F1_GENERATED),
                        parseBool(t, IS_ACCURATE));
                driver.addLap(lap);
            }
        } catch (IOException e) {
            System.err.println("File not found! " + e.getMessage());
        }
        return new ArrayList<>(racesByEvent.values());
    }

    private static String get(String[] tokens, int index) {
        if (index >= tokens.length) {
            return "";
        }
        String v = tokens[index].trim();
        return v.equalsIgnoreCase("nan") ? "" : v;
    }

    private static Double parseDouble(String[] tokens, int index) {
        String v = get(tokens, index);
        if (v.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseInt(String[] tokens, int index) {
        String v = get(tokens, index);
        if (v.isEmpty()) {
            return 0;
        }
        if (v.contains(".")) {
            v = v.substring(0, v.indexOf('.'));
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean parseBool(String[] tokens, int index) {
        return get(tokens, index).equalsIgnoreCase("true");
    }
}
