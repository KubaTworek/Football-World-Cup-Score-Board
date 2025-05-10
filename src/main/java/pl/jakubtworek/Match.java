package pl.jakubtworek;

import java.util.Objects;

public class Match {
    private final String uuid;
    private final String homeTeam;
    private final String awayTeam;
    private final int homeScore;
    private final int awayScore;
    private final long addedAt;

    public Match(String uuid, String homeTeam, String awayTeam) {
        this.uuid = uuid;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = 0;
        this.awayScore = 0;
        this.addedAt = System.nanoTime();
    }

    public Match(String uuid, String homeTeam, String awayTeam, int homeScore, int awayScore, long addedAt) {
        this.uuid = uuid;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.addedAt = addedAt;
    }

    public String getUuid() {
        return uuid;
    }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public long getAddedAt() { return addedAt; }

    public int getTotalScore() {
        return homeScore + awayScore;
    }

    public MatchRecord toRecord() {
        return new MatchRecord(homeTeam, awayTeam, homeScore, awayScore);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match other)) return false;
        return uuid.equals(other.uuid) &&
                homeScore == other.homeScore &&
                awayScore == other.awayScore &&
                homeTeam.equalsIgnoreCase(other.homeTeam) &&
                awayTeam.equalsIgnoreCase(other.awayTeam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, homeTeam.toLowerCase(), awayTeam.toLowerCase(), homeScore, awayScore);
    }

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayTeam + " " + awayScore;
    }
}
