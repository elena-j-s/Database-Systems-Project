package com.cqhacks.racelive.demo;

/** One lap row from LapTimes.csv — mutable fields for {@link InsightGenerator}. */
public class Lap {

    public String time;
    public String lapTime;
    public Double lapNumber;
    public Double stint;
    public String pitOutTime;
    public String pitInTime;
    public String sector1Time;
    public String sector2Time;
    public String sector3Time;
    public String sector1SessionTime;
    public String sector2SessionTime;
    public String sector3SessionTime;
    public Double speedI1;
    public Double speedI2;
    public Double speedFL;
    public Double speedST;
    public boolean personalBest;
    public String compound;
    public Double tyreLife;
    public boolean freshTyre;
    public String lapStartTime;
    public String lapStartDate;
    public String trackStatus;
    public Double position;
    public boolean deleted;
    public String deletedReason;
    public boolean fastF1Generated;
    public boolean isAccurate;

    public Lap(
            String time,
            String lapTime,
            Double lapNumber,
            Double stint,
            String pitOutTime,
            String pitInTime,
            String sector1Time,
            String sector2Time,
            String sector3Time,
            String sector1SessionTime,
            String sector2SessionTime,
            String sector3SessionTime,
            Double speedI1,
            Double speedI2,
            Double speedFL,
            Double speedST,
            boolean personalBest,
            String compound,
            Double tyreLife,
            boolean freshTyre,
            String lapStartTime,
            String lapStartDate,
            String trackStatus,
            Double position,
            boolean deleted,
            String deletedReason,
            boolean fastF1Generated,
            boolean isAccurate) {
        this.time = time;
        this.lapTime = lapTime;
        this.lapNumber = lapNumber;
        this.stint = stint;
        this.pitOutTime = pitOutTime;
        this.pitInTime = pitInTime;
        this.sector1Time = sector1Time;
        this.sector2Time = sector2Time;
        this.sector3Time = sector3Time;
        this.sector1SessionTime = sector1SessionTime;
        this.sector2SessionTime = sector2SessionTime;
        this.sector3SessionTime = sector3SessionTime;
        this.speedI1 = speedI1;
        this.speedI2 = speedI2;
        this.speedFL = speedFL;
        this.speedST = speedST;
        this.personalBest = personalBest;
        this.compound = compound;
        this.tyreLife = tyreLife;
        this.freshTyre = freshTyre;
        this.lapStartTime = lapStartTime;
        this.lapStartDate = lapStartDate;
        this.trackStatus = trackStatus;
        this.position = position;
        this.deleted = deleted;
        this.deletedReason = deletedReason;
        this.fastF1Generated = fastF1Generated;
        this.isAccurate = isAccurate;
    }

    @Override
    public String toString() {
        return String.format(
                "      Lap %-4s  %s  pos=%s  %s%n",
                lapNumber != null ? String.format("%.0f", lapNumber) : "—",
                lapTime != null && !lapTime.isBlank() ? lapTime : "—",
                position != null ? String.format("%.0f", position) : "—",
                compound != null && !compound.isBlank() ? compound : "");
    }
}
