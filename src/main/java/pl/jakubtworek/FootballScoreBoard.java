package pl.jakubtworek;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class FootballScoreBoard {
    private final Map<MatchKey, Match> matches = new ConcurrentHashMap<>();
    private final AtomicReference<List<MatchRecord>> cachedSummary = new AtomicReference<>();
    private final Map<String, Boolean> teamsInUse = new ConcurrentHashMap<>();

    public void startGame(String homeTeam, String awayTeam) {
        requireNonEmpty(homeTeam, "homeTeam");
        requireNonEmpty(awayTeam, "awayTeam");
        validateTeamName(homeTeam, "homeTeam");
        validateTeamName(awayTeam, "awayTeam");

        String home = homeTeam.toLowerCase();
        String away = awayTeam.toLowerCase();

        if (home.equals(away)) {
            throw new IllegalArgumentException("Team cannot play against itself: " + homeTeam);
        }

        if (teamsInUse.putIfAbsent(home, true) != null || teamsInUse.putIfAbsent(away, true) != null) {
            teamsInUse.remove(home);
            teamsInUse.remove(away);
            throw new IllegalArgumentException("At least one of the teams is already playing a match");
        }

        MatchKey key = new MatchKey(homeTeam, awayTeam);
        Match match = new Match(homeTeam, awayTeam);

        Match existing = matches.putIfAbsent(key, match);
        if (existing != null) {
            teamsInUse.remove(homeTeam.toLowerCase());
            teamsInUse.remove(awayTeam.toLowerCase());
            throw new IllegalArgumentException("Match already exists: " + homeTeam + " vs " + awayTeam);
        }
        invalidateCache();
    }

    public void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        MatchKey key = new MatchKey(homeTeam, awayTeam);
        Match current = matches.get(key);
        if (current == null) throw new IllegalArgumentException("Match not found");

        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        if (current.getHomeScore() == homeScore && current.getAwayScore() == awayScore) {
            return;
        }

        Match updated = new Match(
                current.getKey(),
                current.getHomeTeam(),
                current.getAwayTeam(),
                homeScore,
                awayScore,
                current.getAddedAt()
        );

        boolean replaced = matches.replace(key, current, updated);
        if (!replaced) {
            throw new OptimisticLockException("Match was modified concurrently. Please retry.");
        }
        invalidateCache();
    }

    public void finishGame(String homeTeam, String awayTeam) {
        MatchKey key = new MatchKey(homeTeam, awayTeam);
        if (matches.remove(key) == null) {
            throw new IllegalArgumentException("Match not found");
        }
        teamsInUse.remove(homeTeam.toLowerCase());
        teamsInUse.remove(awayTeam.toLowerCase());
        invalidateCache();
    }

    public List<MatchRecord> getSummary() {
        return cachedSummary.updateAndGet(existing -> {
            if (existing != null) return existing;

            List<Match> snapshot = new ArrayList<>(matches.values());
            return snapshot.stream()
                    .sorted(Comparator
                            .comparingInt(Match::getTotalScore)
                            .thenComparing(Match::getAddedAt).reversed())
                    .map(Match::toRecord)
                    .toList();
        });
    }

    private void invalidateCache() {
        cachedSummary.set(null);
    }

    private void requireNonEmpty(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be empty");
        }
    }

    private void validateTeamName(String name, String field) {
        if (!Pattern.matches("[A-Za-z0-9 ]+", name)) {
            throw new IllegalArgumentException("Field '" + field + "' must contain only letters, digits or spaces");
        }
    }
}
