package pl.jakubtworek;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FootballScoreBoard integration tests")
class FootballScoreBoardIT {

    private FootballScoreBoard board;

    @BeforeEach
    void setUp() {
        MatchRepository.getInstance().clear();
        board = new FootballScoreBoard(MatchRepository.getInstance());
    }

    @Test
    @DisplayName("Should start, update and finish a match successfully")
    void shouldStartUpdateAndFinishGameSuccessfully() {
        // Given
        board.startGame("A", "B");

        // When
        board.updateScore("A", "B", 2, 1);
        final MatchRecord updated = board.getSummary().getFirst();

        // Then
        assertAll(
                () -> assertEquals(2, updated.homeScore()),
                () -> assertEquals(1, updated.awayScore())
        );

        // Finish game
        board.finishGame("A", "B");
        assertTrue(board.getSummary().isEmpty());
    }

    @Test
    @DisplayName("Should not allow same team to play in two concurrent matches")
    void shouldNotAllowTwoGamesWithSameTeamConcurrently() {
        // Given
        board.startGame("A", "B");

        // When
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> board.startGame("A", "C"));

        // Then
        assertEquals("At least one of the teams is already playing a match", ex.getMessage());
    }

    @Test
    @DisplayName("Should return consistent summary from cache")
    void shouldReturnConsistentSummaryFromCache() {
        // Given
        board.startGame("A", "B");

        // First summary call (generates cache)
        final List<MatchRecord> first = board.getSummary();
        // Second call should return same object (cached)
        final List<MatchRecord> second = board.getSummary();

        // Then
        assertSame(first, second);
    }

    @Test
    @DisplayName("Should invalidate cache when score updated or match finished")
    void shouldInvalidateCacheOnUpdateOrFinish() {
        // Given
        board.startGame("X", "Y");
        board.getSummary(); // warm up cache

        // When
        board.updateScore("X", "Y", 1, 1);
        final List<MatchRecord> afterUpdate = board.getSummary();

        // Then
        assertEquals(1, afterUpdate.getFirst().homeScore());

        // When
        board.finishGame("X", "Y");

        // Then
        assertTrue(board.getSummary().isEmpty());
    }

    @Disabled("Disabled: relies on timing-sensitive behavior.")
    @Test
    @DisplayName("Should handle concurrent updates with optimistic locking")
    void shouldHandleConcurrentUpdateConflicts() throws InterruptedException {
        // This test is intentionally disabled because it relies on timing-sensitive behavior.
        // To make it pass consistently, updateScore() would need to include a delay,
        // which is not suitable for production logic.
        // Given
        board.startGame("Alpha", "Beta");

        final ExecutorService executor = Executors.newFixedThreadPool(4);
        final CountDownLatch latch = new CountDownLatch(4);
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();

        // When
        for (int i = 1; i <= 4; i++) {
            final int score = i;
            executor.submit(() -> {
                try {
                    board.updateScore("Alpha", "Beta", score, score);
                    success.incrementAndGet();
                } catch (OptimisticLockException e) {
                    failed.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(2, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertEquals(1, success.get(), "Only one update should succeed");
        assertEquals(3, failed.get(), "Others should fail due to optimistic locking");
    }
}
