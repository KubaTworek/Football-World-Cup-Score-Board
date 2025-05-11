package pl.jakubtworek;

import java.util.Comparator;
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
        this(MatchKey.of(homeTeam, awayTeam), homeTeam, awayTeam, 0, 0, System.nanoTime());
    }

    private Match(MatchKey key, String homeTeam, String awayTeam, int homeScore, int awayScore, long addedAt) {
        this.key = key;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.totalScore = homeScore + awayScore;
        this.addedAt = addedAt;
    }

    public Match withUpdatedScore(int homeScore, int awayScore) {
        if (this.homeScore == homeScore && this.awayScore == awayScore) {
            throw new IllegalArgumentException("New score is identical to the current score");
        }

        return new Match(key, homeTeam, awayTeam, homeScore, awayScore, addedAt);
    }

    public MatchKey getKey() {
        return key;
    }

    public MatchRecord toRecord() {
        return new MatchRecord(homeTeam, awayTeam, homeScore, awayScore);
    }

    public static final Comparator<Match> SORT_BY_SCORE_THEN_TIME_DESC =
            Comparator.comparingInt(Match::getTotalScore)
                    .thenComparingLong(Match::getAddedAt)
                    .reversed();

    private int getTotalScore() {
        return totalScore;
    }

    private long getAddedAt() { return addedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match other)) return false;
        return homeScore == other.homeScore &&
                awayScore == other.awayScore &&
                key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, homeScore, awayScore, addedAt);
    }

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayTeam + " " + awayScore;
    }
}
