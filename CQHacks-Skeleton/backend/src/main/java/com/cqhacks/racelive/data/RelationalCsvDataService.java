package com.cqhacks.racelive.data;

import com.cqhacks.racelive.config.DataPathsProperties;
import com.cqhacks.racelive.demo.Driver;
import com.cqhacks.racelive.demo.Race;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Loads normalized F1 tables from {@code data/} (CSV) and exposes joined views compatible with the
 * legacy single-file API shape where needed.
 */
@Service
public class RelationalCsvDataService {

    private static final Logger log = LoggerFactory.getLogger(RelationalCsvDataService.class);

    private static final String[] TEAM_HEX = {
        "FF8000", "3671C6", "E80020", "B6BABD", "52E252", "FF8700", "27F4D2", "229971", "6692FF",
        "0093CC", "2D826D", "6CD3BF", "5E8FAA", "1E41FF", "980000", "00D2BE", "C30000"
    };

    private final Path dataDir;

    private List<Map<String, String>> legacyRaceResultRows = List.of();
    private ArrayList<Race> racesGraph = new ArrayList<>();
    private List<RaceCardDto> raceCards = List.of();

    public RelationalCsvDataService(DataPathsProperties paths) {
        this.dataDir = paths.getDataDirectory();
    }

    @PostConstruct
    public void reload() {
        try {
            loadInternal();
        } catch (Exception e) {
            log.error("Failed to load relational CSVs from {}", dataDir, e);
            legacyRaceResultRows = List.of();
            racesGraph = new ArrayList<>();
            raceCards = List.of();
        }
    }

    private void loadInternal() throws IOException, CsvException {
        if (!Files.isDirectory(dataDir)) {
            throw new IOException("Data directory not found: " + dataDir.toAbsolutePath());
        }
        Path driverPath = dataDir.resolve("Driver.csv");
        Path teamPath = dataDir.resolve("Team.csv");
        Path racePath = dataDir.resolve("Race.csv");
        Path resultsPath = dataDir.resolve("Race Results.csv");

        List<Map<String, String>> drivers = readAllRows(driverPath);
        List<Map<String, String>> teams = readAllRows(teamPath);
        List<Map<String, String>> races = readAllRows(racePath);
        List<Map<String, String>> results = readAllRows(resultsPath);

        Map<Integer, Map<String, String>> driverById = indexByIntKey(drivers, "driver_id");
        Map<Integer, Map<String, String>> teamById = indexByIntKey(teams, "team_id");
        Map<Integer, Map<String, String>> raceById = indexByIntKey(races, "race_id");

        TreeMap<Integer, List<Map<String, String>>> resultsByRace = new TreeMap<>();
        for (Map<String, String> row : results) {
            int rid = parseInt(row.get("race_id"));
            resultsByRace.computeIfAbsent(rid, k -> new ArrayList<>()).add(row);
        }
        for (List<Map<String, String>> group : resultsByRace.values()) {
            group.sort(
                    Comparator.<Map<String, String>, Double>comparing(r -> -parseDouble(r.get("points_scored")))
                            .thenComparing(r -> parseInt(r.get("driver_id"))));
        }

        List<Map<String, String>> legacyRows = new ArrayList<>();
        ArrayList<Race> graph = new ArrayList<>();
        List<RaceCardDto> cards = new ArrayList<>();

        for (Map<String, String> raceRow : races.stream()
                .sorted(Comparator.comparingInt(r -> parseInt(r.get("race_id"))))
                .collect(Collectors.toList())) {
            int raceId = parseInt(raceRow.get("race_id"));
            String raceName = nz(raceRow.get("race_name"));
            String country = nz(raceRow.get("country"));
            String lapsStr = nz(raceRow.get("laps"));
            Double lapsVal = parseDoubleNullable(lapsStr);

            cards.add(new RaceCardDto(raceId, raceName, country));

            List<Map<String, String>> group = resultsByRace.getOrDefault(raceId, List.of());
            if (group.isEmpty()) {
                continue;
            }
            Race race = new Race(raceId, country, country, raceName);
            int rank = 1;
            for (Map<String, String> res : group) {
                int driverId = parseInt(res.get("driver_id"));
                Map<String, String> d = driverById.get(driverId);
                if (d == null) {
                    continue;
                }
                int teamId = parseInt(d.get("team_id"));
                Map<String, String> t = teamById.getOrDefault(teamId, Map.of());
                String teamName = nz(t.get("team_name"));
                String first = nz(d.get("first_name"));
                String last = nz(d.get("last_name"));
                int driverNumber = parseInt(d.get("driver_number"));
                String driverCountry = nz(d.get("country"));
                double points = parseDouble(res.get("points_scored"));
                String abbrev = driverAbbrev(first, last);
                String broadcast = broadcastName(first, last);
                String fullName = (first + " " + last).trim();
                String teamColor = teamHex(teamId);

                Driver driver = new Driver(abbrev, driverNumber, teamName);
                driver.updateFromRaceResults(
                        broadcast,
                        abbrev,
                        String.valueOf(driverId),
                        teamColor,
                        String.valueOf(teamId),
                        first,
                        last,
                        fullName,
                        "",
                        driverCountry);
                driver.addRaceResult(
                        new Driver.RaceResult(
                                raceName,
                                raceId,
                                (double) rank,
                                String.valueOf(rank),
                                null,
                                points > 0 ? points + " pts" : "",
                                "",
                                "Finished",
                                points,
                                lapsVal,
                                "",
                                "",
                                ""));
                race.addDriver(driver);

                Map<String, String> legacy = new LinkedHashMap<>();
                legacy.put("Round", String.valueOf(raceId));
                legacy.put("Event Name", raceName);
                legacy.put("Country", country);
                legacy.put("Location", country);
                legacy.put("TeamName", teamName);
                legacy.put("TeamColor", teamColor);
                legacy.put("TeamId", String.valueOf(teamId));
                legacy.put("DriverId", String.valueOf(driverId));
                legacy.put("DriverNumber", String.valueOf(driverNumber));
                legacy.put("FirstName", first);
                legacy.put("LastName", last);
                legacy.put("FullName", fullName);
                legacy.put("BroadcastName", broadcast);
                legacy.put("Abbreviation", abbrev);
                legacy.put("ClassifiedPosition", String.valueOf(rank));
                legacy.put("Position", String.valueOf((double) rank));
                legacy.put("Points", String.valueOf(points));
                legacy.put("Time", points > 0 ? points + " pts" : "");
                legacy.put("ElapsedTime", "");
                legacy.put("Status", "Finished");
                legacy.put("Laps", lapsStr);
                legacy.put("GridPosition", "");
                legacy.put("HeadshotUrl", "");
                legacy.put("Q1", "");
                legacy.put("Q2", "");
                legacy.put("Q3", "");
                legacyRows.add(legacy);
                rank++;
            }
            graph.add(race);
        }

        this.legacyRaceResultRows = List.copyOf(legacyRows);
        this.racesGraph = graph;
        this.raceCards = List.copyOf(cards);
    }

    public List<Map<String, String>> getLegacyRaceResultRows() {
        return legacyRaceResultRows;
    }

    /** Lap-level telemetry is not in the normalized CSV set; returns an empty list. */
    public List<Map<String, String>> getLegacyLapTimesForRound(int round, int limit) {
        return List.of();
    }

    public ArrayList<Race> getRacesGraph() {
        return new ArrayList<>(racesGraph);
    }

    public List<RaceCardDto> getRaceCards() {
        return raceCards;
    }

    public Path getDataDirectory() {
        return dataDir;
    }

    public record RaceCardDto(int round, String name, String location) {}

    private static String teamHex(int teamId) {
        if (teamId < 1) {
            return "666666";
        }
        return TEAM_HEX[(teamId - 1) % TEAM_HEX.length];
    }

    private static String driverAbbrev(String first, String last) {
        String l = last.replaceAll("[^a-zA-Z]", "");
        if (l.length() >= 3) {
            return l.substring(0, 3).toUpperCase();
        }
        if (!l.isEmpty()) {
            return l.toUpperCase();
        }
        String f = first.replaceAll("[^a-zA-Z]", "");
        return f.length() >= 3 ? f.substring(0, 3).toUpperCase() : "UNK";
    }

    private static String broadcastName(String first, String last) {
        if (first.isBlank()) {
            return last.toUpperCase();
        }
        return first.charAt(0) + ". " + last.toUpperCase();
    }

    private static Map<Integer, Map<String, String>> indexByIntKey(
            List<Map<String, String>> rows, String keyName) {
        Map<Integer, Map<String, String>> m = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            int id = parseInt(row.get(keyName));
            if (id > 0) {
                m.put(id, row);
            }
        }
        return m;
    }

    private static List<Map<String, String>> readAllRows(Path path) throws IOException, CsvException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("Missing CSV: " + path.toAbsolutePath());
        }
        try (var br = Files.newBufferedReader(path);
                CSVReader reader = new CSVReader(br)) {
            String[] header = reader.readNext();
            if (header == null) {
                return List.of();
            }
            for (int i = 0; i < header.length; i++) {
                header[i] = Objects.toString(header[i], "").trim();
            }
            List<Map<String, String>> out = new ArrayList<>();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length == 1 && row[0] != null && row[0].isBlank()) {
                    continue;
                }
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < header.length; i++) {
                    String k = header[i];
                    String v = i < row.length && row[i] != null ? row[i].trim() : "";
                    map.put(k, v);
                }
                out.add(map);
            }
            return out;
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static int parseInt(String s) {
        if (s == null || s.isBlank()) {
            return 0;
        }
        String v = s.trim();
        int dot = v.indexOf('.');
        if (dot > 0) {
            v = v.substring(0, dot);
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(String s) {
        if (s == null || s.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Double parseDoubleNullable(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
