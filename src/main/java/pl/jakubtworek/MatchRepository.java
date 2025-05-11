package pl.jakubtworek;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MatchRepository {
    private static final MatchRepository INSTANCE = new MatchRepository();

    private final Map<MatchKey, Match> matches;
    private final Set<String> teamsInUse;

    private MatchRepository() {
        this.matches = new ConcurrentHashMap<>();
        this.teamsInUse = ConcurrentHashMap.newKeySet();
    }

    public static MatchRepository getInstance() {
        return INSTANCE;
    }

    public void clear() {
        matches.clear();
        teamsInUse.clear();
    }

    public void save(Match match) {
        final var key = match.getKey();
        final var team1 = key.team1();
        final var team2 = key.team2();

        if (!registerTeams(team1, team2)) {
            throw new IllegalArgumentException("At least one of the teams is already playing a match");
        }

        if (matches.putIfAbsent(key, match) != null) {
            unregisterTeams(team1, team2);
            throw new IllegalArgumentException("Match already exists");
        }
    }

    public List<MatchRecord> findAllByOrderByTotalScoreDescAddedAtDesc() {
        final List<Match> snapshot = new ArrayList<>(matches.values());
        return snapshot.stream()
                .sorted(Match.SORT_BY_SCORE_THEN_TIME_DESC)
                .map(Match::toRecord)
                .toList();
    }

    public Optional<Match> findBy(String homeTeam, String awayTeam) {
        final var key = MatchKey.of(homeTeam, awayTeam);
        return Optional.ofNullable(matches.get(key));
    }

    public void update(Match current, Match updated) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!matches.replace(current.getKey(), current, updated)) {
            throw new OptimisticLockException("Match was modified concurrently. Please retry.");
        }
    }

    public boolean removeBy(String homeTeam, String awayTeam) {
        final var key = MatchKey.of(homeTeam, awayTeam);
        final Match removed = matches.remove(key);

        if (removed == null) {
            return false;
        }

        unregisterTeams(key.team1(), key.team2());
        return true;
    }

    private boolean registerTeams(String team1, String team2) {
        if (!teamsInUse.add(team1) || !teamsInUse.add(team2)) {
            unregisterTeams(team1, team2);
            return false;
        }
        return true;
    }

    private void unregisterTeams(String team1, String team2) {
        teamsInUse.remove(team1);
        teamsInUse.remove(team2);
    }
}
