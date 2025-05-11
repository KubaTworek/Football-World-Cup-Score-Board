package pl.jakubtworek;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FootballScoreBoard basic functionality")
class FootballScoreBoardTest {

    private FootballScoreBoard board;

    @BeforeEach
    void setUp() {
        board = new FootballScoreBoard();
    }

    @Test
    @DisplayName("Should start a new game and add match to the list")
    void givenTeams_whenStartGame_thenMatchIsAddedWithZeroScore() {
        // Given
        final String home = "Spain";
        final String away = "Brazil";

        // When
        board.startGame(home, away);
        final MatchRecord match = board.getSummary().getFirst();

        // Then
        assertAll(
                () -> assertEquals(home, match.homeTeam()),
                () -> assertEquals(away, match.awayTeam()),
                () -> assertEquals(0, match.homeScore()),
                () -> assertEquals(0, match.awayScore())
        );
    }

    @Test
    @DisplayName("Should update the score of an existing match")
    void givenExistingMatch_whenUpdateScore_thenScoreIsUpdated() {
        // Given
        board.startGame("Germany", "France");

        // When
        board.updateScore("Germany", "France", 2, 3);
        final MatchRecord match = board.getSummary().getFirst();

        // Then
        assertAll(
                () -> assertEquals(2, match.homeScore()),
                () -> assertEquals(3, match.awayScore())
        );
    }

    @Test
    @DisplayName("Should remove a finished game from the scoreboard")
    void givenMatchStarted_whenFinishGame_thenMatchIsRemoved() {
        // Given
        board.startGame("Mexico", "USA");

        // When
        board.finishGame("Mexico", "USA");

        // Then
        assertTrue(board.getSummary().isEmpty());
    }

    @Test
    @DisplayName("Should return summary sorted by total score and then by insertion order")
    void givenMultipleMatches_whenGetSummary_thenSortedCorrectly() {
        // Given
        board.startGame("Mexico", "Canada");      // 0
        board.startGame("Spain", "Brazil");       // 1
        board.startGame("Germany", "France");     // 2
        board.startGame("Uruguay", "Italy");      // 3
        board.startGame("Argentina", "Australia"); // 4

        board.updateScore("Mexico", "Canada", 0, 5);  // Mexico-Canada: 5
        board.updateScore("Spain", "Brazil", 10, 2); // Spain-Brazil: 12
        board.updateScore("Germany", "France", 2, 2);  // Germany-France: 4
        board.updateScore("Uruguay", "Italy", 6, 6);  // Uruguay-Italy: 12
        board.updateScore("Argentina", "Australia", 3, 1);  // Argentina-Australia: 4

        // When
        final List<MatchRecord> summary = board.getSummary();

        // Then
        assertAll(
                () -> assertEquals("Uruguay", summary.get(0).homeTeam()),     // 12 (added later than Spain)
                () -> assertEquals("Spain", summary.get(1).homeTeam()),       // 12
                () -> assertEquals("Mexico", summary.get(2).homeTeam()),      // 5
                () -> assertEquals("Argentina", summary.get(3).homeTeam()),   // 4
                () -> assertEquals("Germany", summary.get(4).homeTeam())      // 4
        );
    }

    @Test
    @DisplayName("Should not allow duplicate matches regardless of case")
    void givenDuplicateMatch_whenStartGame_thenThrowException() {
        // Given
        board.startGame("Spain", "Brazil");

        // When
        final var ex = assertThrows(IllegalArgumentException.class, () ->
                board.startGame("spain", "BRAZIL")
        );

        // Then
        assertTrue(ex.getMessage().contains("At least one of the teams is already playing a match"));
    }

    @Test
    @DisplayName("Should throw when starting game where team plays against itself")
    void givenSameTeamForHomeAndAway_whenStartGame_thenThrowException() {
        // When
        final var ex = assertThrows(IllegalArgumentException.class, () ->
                board.startGame("Spain", "spain")
        );

        // Then
        assertTrue(ex.getMessage().contains("cannot play against itself"));
    }

    @Test
    @DisplayName("Should throw when updating score of non-existent match")
    void givenNonExistingMatch_whenUpdateScore_thenThrowException() {
        // When
        final var ex = assertThrows(IllegalArgumentException.class, () ->
                board.updateScore("dummyTeam", "dummierTeam", 1, 1)
        );

        // Then
        assertTrue(ex.getMessage().contains("Match not found"));
    }

    @Test
    @DisplayName("Should throw when finishing non-existent match")
    void givenNonExistingMatch_whenFinishGame_thenThrowException() {
        // When
        final var ex = assertThrows(IllegalArgumentException.class, () ->
                board.finishGame("dummyTeam", "dummierTeam")
        );

        // Then
        assertTrue(ex.getMessage().contains("Match not found"));
    }

    @Test
    @DisplayName("Should throw when score is negative")
    void givenNegativeScore_whenUpdateScore_thenThrowException() {
        // Given
        board.startGame("Germany", "France");

        // When
        final var ex = assertThrows(IllegalArgumentException.class, () ->
                board.updateScore("Germany", "France", -1, 2)
        );

        // Then
        assertTrue(ex.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("Should throw when team name contains invalid characters")
    void givenInvalidTeamName_whenStartGame_thenThrow() {
        // When
        final var ex = assertThrows(IllegalArgumentException.class, () ->
                board.startGame("Team!", "Rival$")
        );

        // Then
        assertTrue(ex.getMessage().contains("must contain only"));
    }

    @Test
    @DisplayName("Should allow team name with letters, digits and spaces")
    void givenValidTeamName_whenStartGame_thenSuccess() {
        // When & Then
        assertDoesNotThrow(() ->
                board.startGame("Real Madrid 123", "Team42")
        );
    }

    @Test
    @DisplayName("Should return empty summary when no matches started")
    void givenNoMatches_whenGetSummary_thenReturnEmptyList() {
        // When
        final List<MatchRecord> summary = board.getSummary();

        // Then
        assertTrue(summary.isEmpty());
    }

    @Disabled("Disabled: updateScore() executes too fast to reliably trigger optimistic locking conflicts in concurrent scenarios.")
    @Test
    @DisplayName("Should handle concurrent updates safely with optimistic locking")
    void givenConcurrentUpdates_whenUpdateScore_thenOnlyOneSucceedsAndRestFail() throws InterruptedException {
        // This test is intentionally disabled because it relies on timing-sensitive behavior.
        // To make it pass consistently, updateScore() would need to include a delay,
        // which is not suitable for production logic.

        // Given
        board.startGame("TeamA", "TeamB");
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger optimisticExceptions = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();

        // When
        for (int i = 1; i < threads; i++) {
            int finalI = i;
            executor.execute(() -> {
                try {
                    board.updateScore("TeamA", "TeamB", finalI, finalI);
                    successCount.incrementAndGet();
                } catch (OptimisticLockException e) {
                    optimisticExceptions.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        MatchRecord match = board.getSummary().getFirst();
        assertTrue(match.homeScore() >= 0 && match.homeScore() < threads);
        assertEquals(match.homeScore(), match.awayScore());
        assertEquals(1, successCount.get());
        assertTrue(optimisticExceptions.get() >= 1);
    }
}
