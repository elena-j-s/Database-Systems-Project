package com.cqhacks.racelive.service;

import com.cqhacks.racelive.data.RelationalCsvDataService;
import com.cqhacks.racelive.demo.Driver;
import com.cqhacks.racelive.demo.InsightGenerator;
import com.cqhacks.racelive.demo.Race;
import com.cqhacks.racelive.dto.SessionFeaturedDriver;
import com.cqhacks.racelive.dto.SessionPayload;
import com.cqhacks.racelive.dto.SessionStandingsRow;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SessionPayloadService {

    private final RelationalCsvDataService relational;

    public SessionPayloadService(RelationalCsvDataService relational) {
        this.relational = relational;
    }

    public Optional<SessionPayload> buildForRound(int round) {
        ArrayList<Race> races = relational.getRacesGraph();
        if (races.isEmpty()) {
            return Optional.empty();
        }

        Race target = null;
        if (round > 0) {
            for (Race r : races) {
                if (r.getRounds() == round) {
                    target = r;
                    break;
                }
            }
        }
        if (target == null) {
            target = races.get(0);
        }

        InsightGenerator gen = new InsightGenerator(target);
        List<String> insights = new ArrayList<>(gen.generateAll());
        Driver featured = gen.fastestLapDriver();

        List<SessionStandingsRow> standings = buildStandings(target);
        if (featured == null && !standings.isEmpty()) {
            featured = findDriverByNumber(target, standings.get(0).driverNumber());
        }

        SessionFeaturedDriver featuredDto = toFeatured(featured);

        return Optional.of(
                new SessionPayload(
                        target.getRounds(),
                        target.getEventName(),
                        target.getLocation(),
                        target.getCountry(),
                        standings,
                        insights,
                        featuredDto));
    }

    private static Driver findDriverByNumber(Race race, int num) {
        for (Driver d : race.getDrivers()) {
            if (d.driverNumber == num) {
                return d;
            }
        }
        return null;
    }

    private static SessionFeaturedDriver toFeatured(Driver d) {
        if (d == null) {
            return new SessionFeaturedDriver(0, "—", "", "", "", "", "", "#888888");
        }
        String color = normalizeCssHex(nz(d.teamColor));
        if (color.isEmpty()) {
            color = "#888888";
        }
        return new SessionFeaturedDriver(
                d.driverNumber,
                nz(d.abbreviation),
                nz(d.firstName),
                nz(d.lastName),
                nz(d.team),
                nz(d.countryCode),
                nz(d.headshotUrl),
                color);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** Accepts "RRGGBB" or "#RRGGBB" for session UI CSS. */
    private static String normalizeCssHex(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String t = s.trim();
        if (t.startsWith("#")) {
            return t.length() == 7 ? t : "#888888";
        }
        if (t.matches("(?i)[0-9a-f]{6}")) {
            return "#" + t;
        }
        return t;
    }

    private static List<SessionStandingsRow> buildStandings(Race race) {
        int r = race.getRounds();
        String event = race.getEventName();
        record SortRow(Driver d, double sortKey, double points) {}

        List<SortRow> rows = new ArrayList<>();
        for (Driver d : race.getDrivers()) {
            for (Driver.RaceResult rr : d.getRaceResults()) {
                if (rr.round == r && event.equals(rr.eventName)) {
                    double pos = rr.position != null ? rr.position : 9999;
                    double pts = rr.points != null ? rr.points : 0;
                    rows.add(new SortRow(d, pos, pts));
                    break;
                }
            }
        }
        rows.sort(Comparator.comparingDouble(SortRow::sortKey));

        List<SessionStandingsRow> out = new ArrayList<>();
        int rank = 1;
        for (SortRow row : rows) {
            Driver d = row.d;
            String name = (d.fullName != null && !d.fullName.isBlank()) ? d.fullName : d.name;
            String tc = normalizeCssHex((d.teamColor != null && !d.teamColor.isBlank()) ? d.teamColor : "#888888");
            out.add(
                    new SessionStandingsRow(
                            rank++,
                            name,
                            row.points,
                            d.driverNumber,
                            tc,
                            nz(d.abbreviation),
                            nz(d.firstName),
                            nz(d.lastName),
                            nz(d.team),
                            nz(d.countryCode),
                            nz(d.headshotUrl)));
        }
        return out;
    }
}
