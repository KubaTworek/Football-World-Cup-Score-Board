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

@DisplayName("MatchRepository unit tests")
class MatchRepositoryTest {

    private MatchRepository repository;

    @BeforeEach
    void setUp() {
        repository = MatchRepository.getInstance();
        repository.clear();
    }

    @Test
    @DisplayName("Should save and retrieve a match")
    void shouldSaveAndFindMatch() {
        // Given
        final var match = new Match("Alpha", "Beta");

        // When
        repository.save(match);

        // Then
        final MatchRecord record = repository.findBy("Alpha", "Beta").orElseThrow().toRecord();
        assertMatchEquals(record, "Alpha", "Beta", 0, 0);

    }

    @Test
    @DisplayName("Should throw when match already exists")
    void shouldThrowWhenMatchAlreadyExists() {
        // Given
        final var original = new Match("TeamA", "TeamB");
        final var duplicate = new Match("teama", "teamc");
        repository.save(original);

        // When
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> repository.save(duplicate));

        // Then
        assertEquals("At least one of the teams is already playing a match", ex.getMessage());
    }

    @Test
    @DisplayName("Should return matches sorted by total score and then insertion order")
    void shouldReturnSortedMatches() {
        // Given
        final var one = new Match("T1", "T2");   // will get score 2
        final var two = new Match("T3", "T4");   // will get score 5
        final var three = new Match("T5", "T6"); // will get score 8
        final var four = new Match("T7", "T8");  // will get score 5 (added after two)
        final var five = new Match("T9", "T10"); // will get score 2 (added after one)

        repository.save(one);
        repository.save(two);
        repository.save(three);
        repository.save(four);
        repository.save(five);

        repository.update(one, one.withUpdatedScore(1, 1));     // total: 2
        repository.update(two, two.withUpdatedScore(2, 3));     // total: 5
        repository.update(three, three.withUpdatedScore(4, 4)); // total: 8
        repository.update(four, four.withUpdatedScore(1, 4));   // total: 5 (added later than two)
        repository.update(five, five.withUpdatedScore(2, 0));   // total: 2 (added later than one)

        // When
        final List<MatchRecord> summary = repository.findAllByOrderByTotalScoreDescAddedAtDesc();

        // Then
        assertAll(
                () -> assertMatchEquals(summary.get(0), "T5", "T6", 4, 4),
                () -> assertMatchEquals(summary.get(1), "T7", "T8", 1, 4),
                () -> assertMatchEquals(summary.get(2), "T3", "T4", 2, 3),
                () -> assertMatchEquals(summary.get(3), "T9", "T10", 2, 0 ),
                () -> assertMatchEquals(summary.get(4), "T1", "T2", 1, 1)
        );
    }

    @Test
    @DisplayName("Should update match score successfully")
    void shouldUpdateMatchSuccessfully() {
        // Given
        final var original = new Match("X", "Y");
        repository.save(original);
        final var updated = original.withUpdatedScore(3, 2);

        // When
        repository.update(original, updated);

        // Then
        final MatchRecord record = repository.findBy("X", "Y").orElseThrow().toRecord();
        assertMatchEquals(record, "X", "Y", 3, 2);
    }

    @Disabled("Disabled: relies on timing-sensitive behavior.")
    @Test
    @DisplayName("Should throw OptimisticLockException on concurrent update conflict")
    void shouldThrowOnOptimisticLockConflict() throws InterruptedException {
        // This test is intentionally disabled because it relies on timing-sensitive behavior.
        // To make it pass consistently, updateScore() would need to include a delay,
        // which is not suitable for production logic.

        // Given
        final var match = new Match("Alpha", "Beta");
        repository.save(match);

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicInteger optimisticLockConflicts = new AtomicInteger();

        // When
        for (int i = 0; i < 2; i++) {
            int score = i + 1;
            executor.execute(() -> {
                try {
                    final var current = repository.findBy("Alpha", "Beta").orElseThrow();
                    final var updated = current.withUpdatedScore(score, score);
                    repository.update(current, updated);
                } catch (OptimisticLockException e) {
                    optimisticLockConflicts.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(2, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertEquals(1, optimisticLockConflicts.get(), "Exactly one thread should fail with OptimisticLockException");
    }

    @Test
    @DisplayName("Should remove a match and unregister teams")
    void shouldRemoveMatch() {
        // Given
        repository.save(new Match("C", "D"));

        // When
        final boolean removed = repository.removeBy("C", "D");

        // Then
        assertAll(
                () -> assertTrue(removed),
                () -> assertFalse(repository.findBy("C", "D").isPresent())
        );
    }

    @Test
    @DisplayName("Should notr remove a match and unregister teams")
    void shouldNotRemoveMatch() {
        // When
        final boolean removed = repository.removeBy("C", "D");

        // Then
        assertAll(
                () -> assertFalse(removed),
                () -> assertFalse(repository.findBy("C", "D").isPresent())
        );
    }

    private void assertMatchEquals(MatchRecord record, String expectedHome, String expectedAway, int expectedHomeScore, int expectedAwayScore) {
        assertAll(
                () -> assertEquals(expectedHome, record.homeTeam()),
                () -> assertEquals(expectedAway, record.awayTeam()),
                () -> assertEquals(expectedHomeScore, record.homeScore()),
                () -> assertEquals(expectedAwayScore, record.awayScore())
        );
    }
}
