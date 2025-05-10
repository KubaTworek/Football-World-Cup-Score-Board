package pl.jakubtworek;

import java.util.*;

public class FootballScoreBoard {

    private final List<Match> matches = new LinkedList<>();

    public void startGame(String homeTeam, String awayTeam) {
        final var match = new Match(homeTeam, awayTeam);
        matches.add(match);
    }

    public void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        getMatch(homeTeam, awayTeam).ifPresent(match -> {
            match.setHomeScore(homeScore);
            match.setAwayScore(awayScore);
        });
    }

    public void finishGame(String homeTeam, String awayTeam) {
        getMatch(homeTeam, awayTeam).ifPresent(matches::remove);
    }

    public List<Match> getSummary() {
        return matches.stream()
                .sorted(Comparator
                        .comparingInt(Match::getTotalScore).reversed()
                        .thenComparing((m1, m2) -> Integer.compare(
                                matches.lastIndexOf(m2), matches.lastIndexOf(m1))))
                .toList();
    }

    private Optional<Match> getMatch(String homeTeam, String awayTeam) {
        return matches.stream()
                .filter(m -> m.getHomeTeam().equals(homeTeam) && m.getAwayTeam().equals(awayTeam))
                .findFirst();
    }
}
