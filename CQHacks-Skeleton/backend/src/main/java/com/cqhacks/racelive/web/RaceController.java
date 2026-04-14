package com.cqhacks.racelive.web;

import com.cqhacks.racelive.data.RelationalCsvDataService;
import com.cqhacks.racelive.service.DemoMainService;
import com.cqhacks.racelive.service.RaceDataService;
import com.cqhacks.racelive.service.SessionPayloadService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RaceController {

    private final RaceDataService raceDataService;
    private final DemoMainService demoMainService;
    private final SessionPayloadService sessionPayloadService;
    private final RelationalCsvDataService relationalCsvDataService;

    public RaceController(
            RaceDataService raceDataService,
            DemoMainService demoMainService,
            SessionPayloadService sessionPayloadService,
            RelationalCsvDataService relationalCsvDataService) {
        this.raceDataService = raceDataService;
        this.demoMainService = demoMainService;
        this.sessionPayloadService = sessionPayloadService;
        this.relationalCsvDataService = relationalCsvDataService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    /** Calendar rows from {@code data/Race.csv} (round = {@code race_id}). */
    @GetMapping("/races")
    public List<RelationalCsvDataService.RaceCardDto> races() {
        return relationalCsvDataService.getRaceCards();
    }

    @GetMapping("/race-results")
    public ResponseEntity<?> raceResults() {
        return ResponseEntity.ok(raceDataService.loadRaceResults());
    }

    /**
     * Session view: standings from race results, insights + featured driver (fastest lap) from the same
     * demo pipeline as {@link DemoMainService}.
     */
    @GetMapping("/session/{round}")
    public ResponseEntity<?> sessionForRound(@PathVariable int round) {
        return sessionPayloadService
                .buildForRound(round)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No race data")));
    }

    @GetMapping("/lap-times")
    public ResponseEntity<?> lapTimes(
            @RequestParam int round,
            @RequestParam(defaultValue = "500") int limit) {
        if (limit < 1 || limit > 10_000) {
            return ResponseEntity.badRequest().body(Map.of("error", "limit must be between 1 and 10000"));
        }
        return ResponseEntity.ok(raceDataService.loadLapTimesForRound(round, limit));
    }

    /** Team Main.java reference + ported CsvDemoMain source (race session UI). */
    @GetMapping(value = "/demo/main-source", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> demoMainSource() throws IOException {
        String legacy = readDemoResource("demo/OriginalMain.java.txt");
        String ported = readDemoResource("demo/CsvDemoMain.java");
        String body =
                "/* ─── Original team Main.java (reference) ─── */\n\n" + legacy + "\n\n/* ─── Ported: CsvDemoMain.java (Spring Boot) ─── */\n\n" + ported;
        return ResponseEntity.ok(body);
    }

    /** Console-style race object dump + insights (lap-based lines empty without lap telemetry). */
    @GetMapping(value = "/demo/main-output", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> demoMainOutput(@RequestParam(defaultValue = "1") int round) {
        return ResponseEntity.ok(demoMainService.runDemoForRound(round));
    }

    private static String readDemoResource(String path) throws IOException {
        ClassPathResource res = new ClassPathResource(path);
        if (!res.exists()) {
            return "// (missing resource: " + path + ")\n";
        }
        return res.getContentAsString(StandardCharsets.UTF_8);
    }
}
