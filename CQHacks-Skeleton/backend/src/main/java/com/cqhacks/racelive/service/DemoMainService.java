package com.cqhacks.racelive.service;

import com.cqhacks.racelive.data.RelationalCsvDataService;
import com.cqhacks.racelive.demo.CsvDemoMain;
import org.springframework.stereotype.Service;

@Service
public class DemoMainService {

    private final RelationalCsvDataService relational;

    public DemoMainService(RelationalCsvDataService relational) {
        this.relational = relational;
    }

    public String runDemoForRound(int round) {
        return CsvDemoMain.summaryFromRaces(relational.getRacesGraph(), round);
    }
}
