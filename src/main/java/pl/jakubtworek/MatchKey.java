package pl.jakubtworek;

public record MatchKey(String team1, String team2) {
    public MatchKey(String team1, String team2) {
        String t1 = team1.toLowerCase();
        String t2 = team2.toLowerCase();
        if (t1.compareTo(t2) <= 0) {
            this.team1 = t1;
            this.team2 = t2;
        } else {
            this.team1 = t2;
            this.team2 = t1;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MatchKey other
                && team1.equals(other.team1)
                && team2.equals(other.team2);
    }
}
