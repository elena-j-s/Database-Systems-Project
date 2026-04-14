package com.cqhacks.racelive.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsightGenerator {

    public final ArrayList<String> insights = new ArrayList<>();
    private final List<Driver> drivers;
    private final Race race;

    public InsightGenerator(Race race) {
        this.race = race;
        this.drivers = race.getDrivers();
    }

    /** Driver who set the overall fastest lap in this race, or {@code null} if none. */
    public Driver fastestLapDriver() {
        Lap best = null;
        Driver bestDriver = null;
        for (Driver driver : drivers) {
            for (Lap lap : driver.getLaps()) {
                if (!hasValue(lap.lapTime)) {
                    continue;
                }
                if (best == null || toMillis(lap.lapTime) < toMillis(best.lapTime)) {
                    best = lap;
                    bestDriver = driver;
                }
            }
        }
        return bestDriver;
    }

    public ArrayList<String> generateAll() {
        insights.clear();

        insights.add(fastestLap());
        insights.add(slowestLap());
        insights.add(mostConsistentDriver());
        insights.add(biggestPaceImprovement());

        insights.addAll(fastestSectors());
        insights.add(bestTheoreticalLap());

        insights.add(highestTopSpeed());
        insights.addAll(highestTopSpeedPerTeam());

        insights.add(longestStint());
        insights.add(mostPitStops());
        insights.add(fastestCompound());
        insights.add(freshVsWornLapDelta());

        insights.add(mostPositionsGained());
        insights.addAll(mostLapsLed());

        insights.add(mostDeletedLaps());
        insights.add(accuracyRate());

        insights.removeIf(s -> s == null || s.isBlank());

        return insights;
    }

    String fastestLap() {
        Lap best = null;
        Driver bestDriver = null;
        for (Driver driver : drivers) {
            for (Lap lap : driver.getLaps()) {
                if (!hasValue(lap.lapTime)) {
                    continue;
                }
                if (best == null || toMillis(lap.lapTime) < toMillis(best.lapTime)) {
                    best = lap;
                    bestDriver = driver;
                }
            }
        }
        if (best == null) {
            return null;
        }
        return "Fastest lap: "
                + formatTime(best.lapTime)
                + " by "
                + driverName(bestDriver)
                + " (Lap "
                + fmt(best.lapNumber)
                + ")";
    }

    String slowestLap() {
        Lap worst = null;
        Driver worstDriver = null;
        for (Driver driver : drivers) {
            for (Lap lap : driver.getLaps()) {
                if (!hasValue(lap.lapTime)) {
                    continue;
                }
                if (worst == null || toMillis(lap.lapTime) > toMillis(worst.lapTime)) {
                    worst = lap;
                    worstDriver = driver;
                }
            }
        }
        if (worst == null) {
            return null;
        }
        return "Slowest lap: "
                + formatTime(worst.lapTime)
                + " by "
                + driverName(worstDriver)
                + " (Lap "
                + fmt(worst.lapNumber)
                + ")";
    }

    String mostConsistentDriver() {
        String bestDriver = null;
        long bestRange = Long.MAX_VALUE;

        for (Driver driver : drivers) {
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            int count = 0;
            for (Lap lap : driver.getLaps()) {
                if (!hasValue(lap.lapTime)) {
                    continue;
                }
                long ms = toMillis(lap.lapTime);
                if (ms < min) {
                    min = ms;
                }
                if (ms > max) {
                    max = ms;
                }
                count++;
            }
            if (count < 2) {
                continue;
            }
            long range = max - min;
            if (range < bestRange) {
                bestRange = range;
                bestDriver = driverName(driver);
            }
        }
        if (bestDriver == null) {
            return null;
        }
        return "Most consistent driver: " + bestDriver + " (fastest–slowest lap gap: " + msToDisplay(bestRange) + ")";
    }

    String biggestPaceImprovement() {
        String bestDriver = null;
        long bestDelta = Long.MIN_VALUE;

        for (Driver driver : drivers) {
            Lap firstValid = null;
            Lap lastValid = null;
            for (Lap lap : driver.getLaps()) {
                if (!hasValue(lap.lapTime)) {
                    continue;
                }
                if (firstValid == null) {
                    firstValid = lap;
                }
                lastValid = lap;
            }
            if (firstValid == null || lastValid == null || firstValid == lastValid) {
                continue;
            }
            long delta = toMillis(firstValid.lapTime) - toMillis(lastValid.lapTime);
            if (delta > bestDelta) {
                bestDelta = delta;
                bestDriver = driverName(driver);
            }
        }
        if (bestDriver == null || bestDelta <= 0) {
            return null;
        }
        return "Biggest pace improvement lap 1 → last lap: " + bestDriver + " improved by " + msToDisplay(bestDelta);
    }

    List<String> fastestSectors() {
        Lap bestS1 = null;
        Lap bestS2 = null;
        Lap bestS3 = null;
        Driver dS1 = null;
        Driver dS2 = null;
        Driver dS3 = null;

        for (Driver driver : drivers) {
            for (Lap lap : driver.getLaps()) {
                if (hasValue(lap.sector1Time)
                        && (bestS1 == null || toMillis(lap.sector1Time) < toMillis(bestS1.sector1Time))) {
                    bestS1 = lap;
                    dS1 = driver;
                }
                if (hasValue(lap.sector2Time)
                        && (bestS2 == null || toMillis(lap.sector2Time) < toMillis(bestS2.sector2Time))) {
                    bestS2 = lap;
                    dS2 = driver;
                }
                if (hasValue(lap.sector3Time)
                        && (bestS3 == null || toMillis(lap.sector3Time) < toMillis(bestS3.sector3Time))) {
                    bestS3 = lap;
                    dS3 = driver;
                }
            }
        }

        List<String> results = new ArrayList<>();
        if (bestS1 != null) {
            results.add("Fastest S1: " + formatTime(bestS1.sector1Time) + " by " + driverName(dS1));
        }
        if (bestS2 != null) {
            results.add("Fastest S2: " + formatTime(bestS2.sector2Time) + " by " + driverName(dS2));
        }
        if (bestS3 != null) {
            results.add("Fastest S3: " + formatTime(bestS3.sector3Time) + " by " + driverName(dS3));
        }
        return results;
    }

    String bestTheoreticalLap() {
        String bestDriver = null;
        long bestTotal = Long.MAX_VALUE;

        for (Driver driver : drivers) {
            long minS1 = Long.MAX_VALUE;
            long minS2 = Long.MAX_VALUE;
            long minS3 = Long.MAX_VALUE;
            for (Lap lap : driver.getLaps()) {
                if (hasValue(lap.sector1Time)) {
                    minS1 = Math.min(minS1, toMillis(lap.sector1Time));
                }
                if (hasValue(lap.sector2Time)) {
                    minS2 = Math.min(minS2, toMillis(lap.sector2Time));
                }
                if (hasValue(lap.sector3Time)) {
                    minS3 = Math.min(minS3, toMillis(lap.sector3Time));
                }
            }
            if (minS1 == Long.MAX_VALUE || minS2 == Long.MAX_VALUE || minS3 == Long.MAX_VALUE) {
                continue;
            }
            long total = minS1 + minS2 + minS3;
            if (total < bestTotal) {
                bestTotal = total;
                bestDriver = driverName(driver);
            }
        }
        if (bestDriver == null) {
            return null;
        }
        return "Best theoretical lap (sum of personal best sectors): " + msToDisplay(bestTotal) + " by " + bestDriver;
    }

    String highestTopSpeed() {
        Double maxSpeed = null;
        String topDriver = null;
        for (Driver driver : drivers) {
            for (Lap lap : driver.getLaps()) {
                if (lap.speedST == null) {
                    continue;
                }
                if (maxSpeed == null || lap.speedST > maxSpeed) {
                    maxSpeed = lap.speedST;
                    topDriver = driverName(driver);
                }
            }
        }
        if (maxSpeed == null) {
            return null;
        }
        return "Highest top speed: " + String.format("%.1f km/h", maxSpeed) + " by " + topDriver;
    }

    List<String> highestTopSpeedPerTeam() {
        Map<String, Double> teamBest = new HashMap<>();
        for (Driver driver : drivers) {
            String team = hasValue(driver.team) ? driver.team : "Unknown";
            for (Lap lap : driver.getLaps()) {
                if (lap.speedST == null) {
                    continue;
                }
                teamBest.merge(team, lap.speedST, Math::max);
            }
        }
        List<String> results = new ArrayList<>();
        teamBest.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(
                        e -> results.add("Top speed [" + e.getKey() + "]: " + String.format("%.1f km/h", e.getValue())));
        return results;
    }

    String longestStint() {
        String bestDriver = null;
        String bestCompound = null;
        int bestLength = 0;

        for (Driver driver : drivers) {
            int currentLength = 0;
            String currentCompound = null;
            Double currentStint = null;

            for (Lap lap : driver.getLaps()) {
                boolean sameStint = lap.stint != null && lap.stint.equals(currentStint);
                if (sameStint) {
                    currentLength++;
                } else {
                    currentStint = lap.stint;
                    currentCompound = lap.compound;
                    currentLength = 1;
                }
                if (currentLength > bestLength) {
                    bestLength = currentLength;
                    bestDriver = driverName(driver);
                    bestCompound = currentCompound;
                }
            }
        }
        if (bestDriver == null) {
            return null;
        }
        return "Longest stint: " + bestLength + " laps on " + fmt(bestCompound) + " by " + bestDriver;
    }

    String mostPitStops() {
        String mostDriver = null;
        int mostPits = 0;
        for (Driver driver : drivers) {
            int pits = 0;
            for (Lap lap : driver.getLaps()) {
                if (hasValue(lap.pitInTime)) {
                    pits++;
                }
            }
            if (pits > mostPits) {
                mostPits = pits;
                mostDriver = driverName(driver);
            }
        }
        if (mostDriver == null) {
            return null;
        }
        return "Most pit stops: " + mostDriver + " (" + mostPits + " stops)";
    }

    String fastestCompound() {
        Map<String, Long> totalMs = new HashMap<>();
        Map<String, Integer> count = new HashMap<>();

        for (Driver driver : drivers) {
            for (Lap lap : driver.getLaps()) {
                if (!hasValue(lap.lapTime) || !hasValue(lap.compound)) {
                    continue;
                }
                totalMs.merge(lap.compound, toMillis(lap.lapTime), Long::sum);
                count.merge(lap.compound, 1, Integer::sum);
            }
        }

        String bestCompound = null;
        long bestAvg = Long.MAX_VALUE;
        for (String compound : totalMs.keySet()) {
            long avg = totalMs.get(compound) / count.get(compound);
            if (avg < bestAvg) {
                bestAvg = avg;
                bestCompound = compound;
            }
        }
        if (bestCompound == null) {
            return null;
        }
        return "Fastest average lap by compound: " + bestCompound + " (" + msToDisplay(bestAvg) + " avg)";
    }

    String freshVsWornLapDelta() {
        String biggestDeltaDriver = null;
        long biggestDelta = Long.MIN_VALUE;

        for (Driver driver : drivers) {
            long freshTotal = 0;
            long wornTotal = 0;
            int freshCount = 0;
            int wornCount = 0;

            for (Lap lap : driver.getLaps()) {
                if (!hasValue(lap.lapTime)) {
                    continue;
                }
                long ms = toMillis(lap.lapTime);
                if (lap.freshTyre) {
                    freshTotal += ms;
                    freshCount++;
                } else {
                    wornTotal += ms;
                    wornCount++;
                }
            }
            if (freshCount == 0 || wornCount == 0) {
                continue;
            }
            long delta = (wornTotal / wornCount) - (freshTotal / freshCount);
            if (delta > biggestDelta) {
                biggestDelta = delta;
                biggestDeltaDriver = driverName(driver);
            }
        }
        if (biggestDeltaDriver == null || biggestDelta <= 0) {
            return null;
        }
        return "Biggest fresh vs worn tyre delta: "
                + biggestDeltaDriver
                + " ("
                + msToDisplay(biggestDelta)
                + " slower on worn tyres on average)";
    }

    String mostPositionsGained() {
        String bestDriver = null;
        double bestGain = Double.MIN_VALUE;

        for (Driver driver : drivers) {
            Double startPos = null;
            Double endPos = null;
            for (Lap lap : driver.getLaps()) {
                if (lap.position == null) {
                    continue;
                }
                if (startPos == null) {
                    startPos = lap.position;
                }
                endPos = lap.position;
            }
            if (startPos == null || endPos == null) {
                continue;
            }
            double gain = startPos - endPos;
            if (gain > bestGain) {
                bestGain = gain;
                bestDriver = driverName(driver);
            }
        }
        if (bestDriver == null || bestGain <= 0) {
            return null;
        }
        return "Most positions gained: " + bestDriver + " (+" + (int) bestGain + " positions)";
    }

    List<String> mostLapsLed() {
        Map<String, Integer> lapsInLead = new HashMap<>();
        for (Driver driver : drivers) {
            int led = 0;
            for (Lap lap : driver.getLaps()) {
                if (lap.position != null && lap.position == 1.0) {
                    led++;
                }
            }
            if (led > 0) {
                lapsInLead.put(driverName(driver), led);
            }
        }
        List<String> results = new ArrayList<>();
        lapsInLead.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> results.add("Laps led: " + e.getKey() + " — " + e.getValue() + " laps"));
        return results;
    }

    String mostDeletedLaps() {
        String mostDriver = null;
        int mostDeleted = 0;
        String lastReason = "";

        for (Driver driver : drivers) {
            int deleted = 0;
            String reason = "";
            for (Lap lap : driver.getLaps()) {
                if (lap.deleted) {
                    deleted++;
                    if (hasValue(lap.deletedReason)) {
                        reason = lap.deletedReason;
                    }
                }
            }
            if (deleted > mostDeleted) {
                mostDeleted = deleted;
                mostDriver = driverName(driver);
                lastReason = reason;
            }
        }
        if (mostDriver == null || mostDeleted == 0) {
            return null;
        }
        return "Most deleted laps: "
                + mostDriver
                + " ("
                + mostDeleted
                + " laps"
                + (lastReason.isBlank() ? "" : ", e.g. " + lastReason)
                + ")";
    }

    String accuracyRate() {
        String bestDriver = null;
        double bestRate = -1;

        for (Driver driver : drivers) {
            long accurate = driver.getLaps().stream().filter(l -> l.isAccurate).count();
            int total = driver.getLaps().size();
            if (total == 0) {
                continue;
            }
            double rate = (double) accurate / total * 100;
            if (rate > bestRate) {
                bestRate = rate;
                bestDriver = driverName(driver);
            }
        }
        if (bestDriver == null) {
            return null;
        }
        return "Highest lap accuracy rate: " + bestDriver + " (" + String.format("%.0f%%", bestRate) + " accurate laps)";
    }

    private long toMillis(String time) {
        if (time == null || time.isBlank()) {
            return Long.MAX_VALUE;
        }
        try {
            String[] parts = time.trim().split(" ");
            String[] timePart = parts[2].split("[:.]");
            long hours = Long.parseLong(timePart[0]);
            long minutes = Long.parseLong(timePart[1]);
            long seconds = Long.parseLong(timePart[2]);
            long micros = Long.parseLong(timePart[3]);
            return hours * 3_600_000L + minutes * 60_000L + seconds * 1_000L + micros / 1_000L;
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private String formatTime(String time) {
        if (time == null || time.isBlank()) {
            return "—";
        }
        long ms = toMillis(time);
        if (ms == Long.MAX_VALUE) {
            return time;
        }
        long minutes = ms / 60_000;
        long seconds = (ms % 60_000) / 1_000;
        long millis = ms % 1_000;
        return String.format("%d:%02d.%03d", minutes, seconds, millis);
    }

    private String msToDisplay(long ms) {
        if (ms <= 0) {
            return "0:00.000";
        }
        long minutes = ms / 60_000;
        long seconds = (ms % 60_000) / 1_000;
        long millis = ms % 1_000;
        return String.format("%d:%02d.%03d", minutes, seconds, millis);
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }

    private String driverName(Driver d) {
        if (d == null) {
            return "Unknown";
        }
        return hasValue(d.fullName) ? d.fullName : d.name;
    }

    private String fmt(Double d) {
        return d != null ? String.format("%.0f", d) : "—";
    }

    private String fmt(String s) {
        return hasValue(s) ? s : "—";
    }
}
