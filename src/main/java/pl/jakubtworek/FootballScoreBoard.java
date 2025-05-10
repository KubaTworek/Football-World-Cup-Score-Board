package pl.jakubtworek;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FootballScoreBoard {
    private final Map<String, Match> matches = new ConcurrentHashMap<>();

    public String startGame(String homeTeam, String awayTeam) {
        requireNonEmpty(homeTeam, "homeTeam");
        requireNonEmpty(awayTeam, "awayTeam");
        validateTeamName(homeTeam, "homeTeam");
        validateTeamName(awayTeam, "awayTeam");

        if (homeTeam.equalsIgnoreCase(awayTeam)) {
            throw new IllegalArgumentException("Team cannot play against itself: " + homeTeam);
        }

        boolean duplicateExists = matches.values().stream()
                .anyMatch(m -> m.getHomeTeam().equalsIgnoreCase(homeTeam) && m.getAwayTeam().equalsIgnoreCase(awayTeam));
        if (duplicateExists) {
            throw new IllegalArgumentException("Match already exists: " + homeTeam + " vs " + awayTeam);
        }

        String uuid = UUID.randomUUID().toString();
        Match match = new Match(uuid, homeTeam, awayTeam);
        matches.put(uuid, match);
        return uuid;
    }

    public void updateScore(String uuid, int homeScore, int awayScore) {
        Match current = matches.get(uuid);
        if (current == null) throw new IllegalArgumentException("Match not found");

        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        if (current.getHomeScore() == homeScore && current.getAwayScore() == awayScore) {
            return;
        }

        Match updated = new Match(
                current.getUuid(),
                current.getHomeTeam(),
                current.getAwayTeam(),
                homeScore,
                awayScore,
                current.getAddedAt()
        );

        boolean replaced = matches.replace(uuid, current, updated);
        if (!replaced) {
            throw new OptimisticLockException("Match was modified concurrently. Please retry.");
        }
    }

    public void finishGame(String uuid) {
        if (matches.remove(uuid) == null) {
            throw new IllegalArgumentException("Match not found: " + uuid);
        }
    }

    public List<MatchRecord> getSummary() {
        final List<Match> snapshot = new ArrayList<>(matches.values());
        return snapshot.stream()
                .sorted(Comparator
                        .comparingInt(Match::getTotalScore)
                        .thenComparing(Match::getAddedAt).reversed())
                .map(Match::toRecord)
                .toList();
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
