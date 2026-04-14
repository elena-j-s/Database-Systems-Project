package com.cqhacks.racelive.service;

import com.cqhacks.racelive.data.RelationalCsvDataService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RaceDataService {

    private final RelationalCsvDataService relational;

    public RaceDataService(RelationalCsvDataService relational) {
        this.relational = relational;
    }

    public List<Map<String, String>> loadRaceResults() {
        return relational.getLegacyRaceResultRows();
    }

    public List<Map<String, String>> loadLapTimesForRound(int round, int limit) {
        return relational.getLegacyLapTimesForRound(round, limit);
    }
}
