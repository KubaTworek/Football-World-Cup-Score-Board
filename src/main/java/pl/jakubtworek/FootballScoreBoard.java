package pl.jakubtworek;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class FootballScoreBoard {
    private final AtomicReference<List<MatchRecord>> cachedSummary;
    private final MatchRepository repository;

    public FootballScoreBoard(MatchRepository matchRepository) {
        this.cachedSummary = new AtomicReference<>();
        this.repository = matchRepository;
    }

    public void startGame(String homeTeam, String awayTeam) {
        validateTeams(homeTeam, awayTeam);
        repository.save(new Match(homeTeam, awayTeam));
        invalidateCache();
    }

    public void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        validateScores(homeScore, awayScore);

        repository.findBy(homeTeam, awayTeam).ifPresentOrElse(current -> {
            final Match updated = current.withUpdatedScore(homeScore, awayScore);
            repository.update(current, updated);
            invalidateCache();
        }, () -> {
            throw new IllegalArgumentException("Match not found");
        });
    }

    public void finishGame(String homeTeam, String awayTeam) {
        if (!repository.removeBy(homeTeam, awayTeam)) {
            throw new IllegalArgumentException("Match not found");
        }
        invalidateCache();
    }

    public List<MatchRecord> getSummary() {
        return cachedSummary.updateAndGet(existing ->
                existing != null ? existing : repository.findAllByOrderByTotalScoreDescAddedAtDesc()
        );
    }

    private void invalidateCache() {
        cachedSummary.set(null);
    }

    private void validateScores(int homeScore, int awayScore) {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
    }

    private void validateTeams(String home, String away) {
        requireNonEmpty(home, "homeTeam");
        requireNonEmpty(away, "awayTeam");
        validateTeamName(home, "homeTeam");
        validateTeamName(away, "awayTeam");

        if (home.equalsIgnoreCase(away)) {
            throw new IllegalArgumentException("Team cannot play against itself");
        }
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
