package pl.jakubtworek;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Match unit tests")
class MatchTest {

    @Test
    @DisplayName("Should initialize match with zero score")
    void shouldInitializeWithZeroScore() {
        // When
        final var match = new Match("A", "B");

        // Then
        assertAll(
                () -> assertEquals(0, match.toRecord().homeScore()),
                () -> assertEquals(0, match.toRecord().awayScore()),
                () -> assertEquals("A", match.toRecord().homeTeam()),
                () -> assertEquals("B", match.toRecord().awayTeam())
        );
    }

    @Test
    @DisplayName("Should update score with new values")
    void shouldUpdateScore() {
        // Given
        final var original = new Match("A", "B");

        // When
        final var updated = original.withUpdatedScore(2, 3);

        // Then
        assertAll(
                () -> assertEquals("A", updated.toRecord().homeTeam()),
                () -> assertEquals("B", updated.toRecord().awayTeam()),
                () -> assertEquals(2, updated.toRecord().homeScore()),
                () -> assertEquals(3, updated.toRecord().awayScore()),
                () -> assertEquals(original.getKey(), updated.getKey())
        );
    }

    @Test
    @DisplayName("Should throw if score update is identical")
    void shouldThrowIfScoreIsSame() {
        // Given
        final var match = new Match("A", "B");

        // When
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> match.withUpdatedScore(0, 0));

        // Then
        assertEquals("New score is identical to the current score", ex.getMessage());
    }

    @Test
    @DisplayName("Should have consistent equals and hashCode")
    void shouldRespectEqualsAndHashCode() {
        // Given
        final var a1 = new Match("X", "Y");
        final var a2 = a1.withUpdatedScore(1, 2);
        final var b1 = new Match("X", "Y");

        // When & Then
        assertNotEquals(a1, a2);
        assertNotEquals(a1.hashCode(), a2.hashCode());
        assertEquals(a1, b1);
    }

    @Test
    @DisplayName("Should generate correct string representation")
    void shouldGenerateToString() {
        // Given
        final var match = new Match("Lions", "Tigers").withUpdatedScore(3, 2);

        // When
        final var result = match.toString();

        // Then
        assertEquals("Lions 3 - Tigers 2", result);
    }
}
