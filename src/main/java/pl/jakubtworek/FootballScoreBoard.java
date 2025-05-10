package pl.jakubtworek;

import java.util.*;

public class FootballScoreBoard {
    private final List<Match> matches = new LinkedList<>();

    public void startGame(String homeTeam, String awayTeam) {
        requireNonEmpty(homeTeam, "homeTeam");
        requireNonEmpty(awayTeam, "awayTeam");
        validateTeamName(homeTeam, "homeTeam");
        validateTeamName(awayTeam, "awayTeam");

        if (homeTeam.equalsIgnoreCase(awayTeam)) {
            throw new IllegalArgumentException("Team cannot play against itself: " + homeTeam);
        }

        if (getMatch(homeTeam, awayTeam).isPresent()) {
            throw new IllegalArgumentException("Match already exists: " + homeTeam + " vs " + awayTeam);
        }

        matches.add(new Match(homeTeam, awayTeam));
    }

    public void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        final Match match = getMatch(homeTeam, awayTeam)
                .orElseThrow(() -> new NoSuchElementException("Match not found: " + homeTeam + " vs " + awayTeam));
        match.setScore(homeScore, awayScore);
    }

    public void finishGame(String homeTeam, String awayTeam) {
        final Match match = getMatch(homeTeam, awayTeam)
                .orElseThrow(() -> new NoSuchElementException("Match not found: " + homeTeam + " vs " + awayTeam));
        matches.remove(match);
    }

    public List<Match> getSummary() {
        return matches.stream()
                .sorted(Comparator
                        .comparingInt(Match::getTotalScore)
                        .thenComparing(Match::getAddedAt).reversed())
                .map(Match::copy)
                .toList();
    }

    private Optional<Match> getMatch(String homeTeam, String awayTeam) {
        return matches.stream()
                .filter(m -> m.getHomeTeam().equalsIgnoreCase(homeTeam) && m.getAwayTeam().equalsIgnoreCase(awayTeam))
                .findFirst();
    }

    private void requireNonEmpty(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be empty");
        }
    }

    private void validateTeamName(String name, String field) {
        if (!name.matches("[A-Za-z0-9 ]+")) {
            throw new IllegalArgumentException("Field '" + field + "' must contain only letters, digits or spaces");
        }
    }
}
