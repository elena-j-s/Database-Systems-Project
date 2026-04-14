package com.cqhacks.racelive.demo;

import java.util.ArrayList;

public class Driver {

    public String name;
    public int driverNumber;
    public String team;

    public String broadcastName;
    public String abbreviation;
    public String driverID;
    public String teamColor;
    public String teamID;
    public String firstName;
    public String lastName;
    public String fullName;
    public String headshotUrl;
    public String countryCode;

    public final ArrayList<Lap> laps = new ArrayList<>();
    public final ArrayList<RaceResult> raceResults = new ArrayList<>();

    public Driver(String name, int driverNumber, String team) {
        this.name = name;
        this.driverNumber = driverNumber;
        this.team = team;
    }

    public void updateFromRaceResults(
            String broadcastName,
            String abbreviation,
            String driverID,
            String teamColor,
            String teamID,
            String firstName,
            String lastName,
            String fullName,
            String headshotUrl,
            String countryCode) {
        this.broadcastName = broadcastName;
        this.abbreviation = abbreviation;
        this.driverID = driverID;
        this.teamColor = teamColor;
        this.teamID = teamID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.headshotUrl = headshotUrl;
        this.countryCode = countryCode;
    }

    public void addLap(Lap lap) {
        laps.add(lap);
    }

    public void addRaceResult(RaceResult result) {
        raceResults.add(result);
    }

    public ArrayList<Lap> getLaps() {
        return laps;
    }

    public ArrayList<RaceResult> getRaceResults() {
        return raceResults;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    ┌─ Driver : ")
                .append(fullName != null && !fullName.isBlank() ? fullName : name)
                .append("  (#")
                .append(driverNumber)
                .append(")")
                .append(abbreviation != null && !abbreviation.isBlank() ? "  [" + abbreviation + "]" : "")
                .append("  Team: ")
                .append(team)
                .append("\n")
                .append("    │  Laps driven : ")
                .append(laps.size())
                .append("\n");
        if (!raceResults.isEmpty()) {
            sb.append("    │  Race results : ").append(raceResults.size()).append(" races\n");
            for (RaceResult rr : raceResults) {
                sb.append("    │  ").append(rr.toString()).append("\n");
            }
        }
        for (Lap lap : laps) {
            sb.append(lap.toString());
        }
        sb.append("    └─────────────────────────────────────────────────────\n");
        return sb.toString();
    }

    public static class RaceResult {

        public String eventName;
        public int round;
        public Double position;
        public String classifiedPosition;
        public Double gridPosition;
        public String time;
        public String elapsedTime;
        public String status;
        public Double points;
        public Double laps;
        public String q1;
        public String q2;
        public String q3;

        public RaceResult(
                String eventName,
                int round,
                Double position,
                String classifiedPosition,
                Double gridPosition,
                String time,
                String elapsedTime,
                String status,
                Double points,
                Double laps,
                String q1,
                String q2,
                String q3) {
            this.eventName = eventName;
            this.round = round;
            this.position = position;
            this.classifiedPosition = classifiedPosition;
            this.gridPosition = gridPosition;
            this.time = time;
            this.elapsedTime = elapsedTime;
            this.status = status;
            this.points = points;
            this.laps = laps;
            this.q1 = q1;
            this.q2 = q2;
            this.q3 = q3;
        }

        @Override
        public String toString() {
            return String.format(
                    "        R%02d %-30s  P%-3s (Grid: %s)  %s  %.0f pts",
                    round,
                    eventName,
                    classifiedPosition != null && !classifiedPosition.isEmpty() ? classifiedPosition : "—",
                    gridPosition != null ? String.format("%.0f", gridPosition) : "—",
                    status,
                    points != null ? points : 0.0);
        }
    }
}
