package pl.jakubtworek;

import java.util.Objects;

public class Match {
    private final MatchKey key;
    private final String homeTeam;
    private final String awayTeam;
    private final int homeScore;
    private final int awayScore;
    private final int totalScore;
    private final long addedAt;

    public Match(String homeTeam, String awayTeam) {
        this.key = new MatchKey(homeTeam, awayTeam);
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = 0;
        this.awayScore = 0;
        this.totalScore = 0;
        this.addedAt = System.nanoTime();
    }

    public Match(MatchKey key, String homeTeam, String awayTeam, int homeScore, int awayScore, long addedAt) {
        this.key = key;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.totalScore = homeScore + awayScore;
        this.addedAt = addedAt;
    }

    public MatchKey getKey() {
        return key;
    }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public int getTotalScore() {
        return totalScore;
    }
    public long getAddedAt() { return addedAt; }

    public MatchRecord toRecord() {
        return new MatchRecord(homeTeam, awayTeam, homeScore, awayScore);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match other)) return false;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayTeam + " " + awayScore;
    }
}
