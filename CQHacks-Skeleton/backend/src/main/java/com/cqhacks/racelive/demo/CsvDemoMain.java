package com.cqhacks.racelive.demo;

import java.util.ArrayList;

/**
 * Race summary + {@link InsightGenerator} output (lap-based insights are empty when no lap CSV).
 */
public final class CsvDemoMain {

    private CsvDemoMain() {}

    /** {@code round <= 0} uses the first race in list order. */
    public static String summaryFromRaces(ArrayList<Race> races, int round) {
        if (races.isEmpty()) {
            return "No races loaded (check data/ CSVs: Race.csv, Race Results.csv, Driver.csv, Team.csv).";
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

        StringBuilder sb = new StringBuilder();
        sb.append(target.toString());
        sb.append("\n──────── Insights ────────\n");
        InsightGenerator gen = new InsightGenerator(target);
        for (String line : gen.generateAll()) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }
}
