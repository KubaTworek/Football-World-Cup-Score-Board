package pl.jakubtworek;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("FootballScoreBoard unit tests")
class FootballScoreBoardTest {

    private MatchRepository repository;
    private FootballScoreBoard board;

    @BeforeEach
    void setup() {
        repository = mock(MatchRepository.class);
        board = new FootballScoreBoard(repository);
    }

    @Test
    @DisplayName("Should delegate match saving to repository on game start")
    void shouldSaveMatchOnStartGame() {
        // When
        board.startGame("TeamA", "TeamB");

        // Then
        verify(repository).save(any(Match.class));
    }

    @ParameterizedTest(name = "[{index}] homeTeam=''{0}'' awayTeam=''{1}'' → error: {2}")
    @DisplayName("Should throw on invalid or empty team names")
    @CsvSource(delimiter = '|', value = {
            "Team!|Rival|Field 'homeTeam' must contain only letters, digits or spaces",
            "Rival|Rival$|Field 'awayTeam' must contain only letters, digits or spaces",
            "|Opponent|Field 'homeTeam' cannot be empty",
            "Opponent| |Field 'awayTeam' cannot be empty",
            "Same|Same|Team cannot play against itself"
    })
    void shouldThrowIfTeamNameInvalid(String homeTeam, String awayTeam, String expectedMessage) {
        // When
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> board.startGame(homeTeam, awayTeam));

        // Then
        assertEquals(expectedMessage, ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should update score for an existing match")
    void shouldUpdateScoreIfMatchExists() {
        // Given
        final var match = new Match("A", "B");
        when(repository.findBy("A", "B")).thenReturn(Optional.of(match));

        // When
        board.updateScore("A", "B", 1, 1);

        // Then
        verify(repository).update(eq(match), eq(match.withUpdatedScore(1, 1)));
    }

    @Test
    @DisplayName("Should throw if updating score of a non-existent match")
    void shouldThrowIfUpdatingMissingMatch() {
        // Given
        when(repository.findBy("X", "Y")).thenReturn(Optional.empty());

        // When
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> board.updateScore("X", "Y", 2, 2));

        // Then
        assertEquals("Match not found", ex.getMessage());
        verify(repository, never()).update(any(), any());
    }

    @ParameterizedTest
    @DisplayName("Should throw if home or away score is negative")
    @CsvSource({
            "-1, 0",
            "0, -1",
            "-3, -2"
    })
    void shouldThrowIfScoreIsNegative(int homeScore, int awayScore) {
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> board.updateScore("X", "Y", homeScore, awayScore));

        assertEquals("Score cannot be negative", ex.getMessage());
    }

    @Test
    @DisplayName("Should call removeBy on repository when finishing a match")
    void shouldFinishGameCorrectly() {
        // Given
        when(repository.removeBy("A", "B")).thenReturn(true);

        // When
        board.finishGame("A", "B");

        // Then
        verify(repository).removeBy("A", "B");
    }

    @Test
    @DisplayName("Should throw if trying to finish a non-existent match")
    void shouldThrowWhenFinishingNonexistentGame() {
        // Given
        when(repository.removeBy("A", "B")).thenReturn(false);

        // When
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> board.finishGame("A", "B"));

        // Then
        assertEquals("Match not found", ex.getMessage());
    }


    @Test
    @DisplayName("Should cache and return summary once generated")
    void shouldReturnCachedSummary() {
        // Given
        when(repository.findAllByOrderByTotalScoreDescAddedAtDesc()).thenReturn(emptyList());

        // When
        final List<MatchRecord> firstCall = board.getSummary();
        final List<MatchRecord> secondCall = board.getSummary();

        // Then
        assertAll(
                () -> verify(repository, times(1)).findAllByOrderByTotalScoreDescAddedAtDesc(),
                () -> assertSame(firstCall, secondCall)
        );
    }
}