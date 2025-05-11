package pl.jakubtworek;

import java.util.Objects;

public record MatchKey(String team1, String team2) {
    public static MatchKey of(String t1, String t2) {
        String a = t1.toLowerCase();
        String b = t2.toLowerCase();
        return a.compareTo(b) <= 0 ? new MatchKey(a, b) : new MatchKey(b, a);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MatchKey other &&
                team1.equals(other.team1) &&
                team2.equals(other.team2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(team1.hashCode(), team2.hashCode());
    }
}
