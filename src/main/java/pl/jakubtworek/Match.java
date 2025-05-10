package pl.jakubtworek;

import java.util.Objects;

public class Match {
    private final String homeTeam;
    private final String awayTeam;
    private int homeScore;
    private int awayScore;
    private long addedAt;

    public Match(String homeTeam, String awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = 0;
        this.awayScore = 0;
        this.addedAt = System.nanoTime();
    }

    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public long getAddedAt() { return addedAt; }

    public int getTotalScore() {
        return Math.addExact(homeScore, awayScore);
    }

    public void setScore(int home, int away) {
        if (home < 0 || away < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
        this.homeScore = home;
        this.awayScore = away;
    }

    public Match copy() {
        Match copy = new Match(this.homeTeam, this.awayTeam);
        copy.homeScore = this.homeScore;
        copy.awayScore = this.awayScore;
        copy.addedAt = this.addedAt;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match m)) return false;
        return homeTeam.equalsIgnoreCase(m.homeTeam) && awayTeam.equalsIgnoreCase(m.awayTeam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(homeTeam.toLowerCase(), awayTeam.toLowerCase());
    }

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayTeam + " " + awayScore;
    }
}
