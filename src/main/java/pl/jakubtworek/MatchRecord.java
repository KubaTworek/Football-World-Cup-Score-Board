package pl.jakubtworek;

public record MatchRecord(
        String homeTeam,
        String awayTeam,
        int homeScore,
        int awayScore
) {}
