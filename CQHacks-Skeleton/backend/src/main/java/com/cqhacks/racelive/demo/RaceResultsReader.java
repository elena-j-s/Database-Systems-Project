package com.cqhacks.racelive.demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class RaceResultsReader {

    private final String fileName;

    public RaceResultsReader(String fileName) {
        this.fileName = fileName;
    }

    private static final int DRIVER_NUMBER = 0;
    private static final int BROADCAST_NAME = 1;
    private static final int ABBREVIATION = 2;
    private static final int DRIVER_ID = 3;
    private static final int TEAM_NAME = 4;
    private static final int TEAM_COLOR = 5;
    private static final int TEAM_ID = 6;
    private static final int FIRST_NAME = 7;
    private static final int LAST_NAME = 8;
    private static final int FULL_NAME = 9;
    private static final int HEADSHOT_URL = 10;
    private static final int COUNTRY_CODE = 11;
    private static final int POSITION = 12;
    private static final int CLASSIFIED_POSITION = 13;
    private static final int GRID_POSITION = 14;
    private static final int TIME = 15;
    private static final int ELAPSED_TIME = 16;
    private static final int STATUS = 17;
    private static final int POINTS = 18;
    private static final int LAPS = 19;
    private static final int ROUND = 20;
    private static final int COUNTRY = 21;
    private static final int LOCATION = 22;
    private static final int EVENT_NAME = 23;
    private static final int Q1 = 24;
    private static final int Q2 = 25;
    private static final int Q3 = 26;

    public void readCSV(List<Race> races) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] t = line.split(",", -1);
                int driverNumber = parseInt(t, DRIVER_NUMBER);
                String eventName = get(t, EVENT_NAME);

                Race race = null;
                for (Race r : races) {
                    if (r.getEventName().equals(eventName)) {
                        race = r;
                        break;
                    }
                }
                if (race == null) {
                    race = new Race(parseInt(t, ROUND), get(t, COUNTRY), get(t, LOCATION), eventName);
                    races.add(race);
                }

                Driver driver = null;
                for (Driver d : race.getDrivers()) {
                    if (d.driverNumber == driverNumber) {
                        driver = d;
                        break;
                    }
                }
                if (driver == null) {
                    driver = new Driver(get(t, ABBREVIATION), driverNumber, get(t, TEAM_NAME));
                    race.addDriver(driver);
                }

                driver.updateFromRaceResults(
                        get(t, BROADCAST_NAME),
                        get(t, ABBREVIATION),
                        get(t, DRIVER_ID),
                        get(t, TEAM_COLOR),
                        get(t, TEAM_ID),
                        get(t, FIRST_NAME),
                        get(t, LAST_NAME),
                        get(t, FULL_NAME),
                        get(t, HEADSHOT_URL),
                        get(t, COUNTRY_CODE));

                Driver.RaceResult result =
                        new Driver.RaceResult(
                                eventName,
                                parseInt(t, ROUND),
                                parseDouble(t, POSITION),
                                get(t, CLASSIFIED_POSITION),
                                parseDouble(t, GRID_POSITION),
                                get(t, TIME),
                                get(t, ELAPSED_TIME),
                                get(t, STATUS),
                                parseDouble(t, POINTS),
                                parseDouble(t, LAPS),
                                get(t, Q1),
                                get(t, Q2),
                                get(t, Q3));
                driver.addRaceResult(result);
            }
        } catch (IOException e) {
            System.err.println("RaceResults CSV: " + e.getMessage());
        }
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
}
