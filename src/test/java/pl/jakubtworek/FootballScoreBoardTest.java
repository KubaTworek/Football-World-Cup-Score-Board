package pl.jakubtworek;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

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
        final List<Match> summary = board.getSummary();

        // Then
        assertAll(
                () -> assertEquals(1, summary.size()),
                () -> assertEquals(home, summary.getFirst().getHomeTeam()),
                () -> assertEquals(away, summary.getFirst().getAwayTeam()),
                () -> assertEquals(0, summary.getFirst().getHomeScore()),
                () -> assertEquals(0, summary.getFirst().getAwayScore())
        );
    }

    @Test
    @DisplayName("Should update the score of an existing match")
    void givenExistingMatch_whenUpdateScore_thenScoreIsUpdated() {
        // Given
        board.startGame("Germany", "France");

        // When
        board.updateScore("Germany", "France", 2, 3);
        final Match match = board.getSummary().getFirst();

        // Then
        assertAll(
                () -> assertEquals(2, match.getHomeScore()),
                () -> assertEquals(3, match.getAwayScore())
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
        board.startGame("Mexico", "Canada");
        board.startGame("Spain", "Brazil");
        board.startGame("Germany", "France");
        board.startGame("Uruguay", "Italy");
        board.startGame("Argentina", "Australia");

        board.updateScore("Mexico", "Canada", 0, 5);      // 5
        board.updateScore("Spain", "Brazil", 10, 2);      // 12
        board.updateScore("Germany", "France", 2, 2);     // 4
        board.updateScore("Uruguay", "Italy", 6, 6);      // 12
        board.updateScore("Argentina", "Australia", 3, 1);// 4

        // When
        final List<Match> summary = board.getSummary();

        // Then
        assertAll(
                () -> assertEquals("Uruguay", summary.get(0).getHomeTeam()),     // 12 (inserted later)
                () -> assertEquals("Spain", summary.get(1).getHomeTeam()),       // 12
                () -> assertEquals("Mexico", summary.get(2).getHomeTeam()),      // 5
                () -> assertEquals("Argentina", summary.get(3).getHomeTeam()),   // 4
                () -> assertEquals("Germany", summary.get(4).getHomeTeam())      // 4
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
        assertTrue(ex.getMessage().contains("Match already exists"));
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
        final var ex = assertThrows(NoSuchElementException.class, () ->
                board.updateScore("Foo", "Bar", 1, 1)
        );

        // Then
        assertTrue(ex.getMessage().contains("Match not found"));
    }

    @Test
    @DisplayName("Should throw when finishing non-existent match")
    void givenNonExistingMatch_whenFinishGame_thenThrowException() {
        // When
        final var ex = assertThrows(NoSuchElementException.class, () ->
                board.finishGame("NoTeam", "OtherNoTeam")
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
    @DisplayName("Should allow maximum possible score without overflow")
    void givenMaxIntScore_whenUpdateScore_thenTotalScoreIsCorrect() {
        // Given
        board.startGame("Germany", "France");

        // When
        board.updateScore("Germany", "France", Integer.MAX_VALUE, 0);

        // Then
        final Match match = board.getSummary().getFirst();
        assertEquals(Integer.MAX_VALUE, match.getTotalScore());
    }

    @Test
    @DisplayName("Should throw ArithmeticException on integer overflow in totalScore")
    void givenOverflowScore_whenTotalScore_thenThrowException() {
        // Given
        board.startGame("Germany", "France");

        // When
        board.updateScore("Germany", "France", Integer.MAX_VALUE, 1);

        // Then
        final var ex = assertThrows(ArithmeticException.class, () ->
                board.getSummary().getFirst().getTotalScore()
        );
        assertEquals("integer overflow", ex.getMessage());
    }

    @Test
    @DisplayName("Should treat team names case-insensitively for equality")
    void givenSameTeamsWithDifferentCase_whenGetMatch_thenMatchFound() {
        // Given
        board.startGame("Germany", "France");

        // When
        assertDoesNotThrow(() ->
                board.updateScore("GERMANY", "france", 3, 2)
        );

        // Then
        final Match match = board.getSummary().getFirst();
        assertEquals(5, match.getTotalScore());
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
        final List<Match> summary = board.getSummary();

        // Then
        assertTrue(summary.isEmpty());
    }

    @Test
    @DisplayName("Should return copies of matches in summary")
    void givenSummaryReturned_whenModifyingMatch_thenOriginalUnchanged() {
        // Given
        board.startGame("Spain", "Brazil");
        board.updateScore("Spain", "Brazil", 1, 2);

        // When
        final List<Match> summary = board.getSummary();
        final Match returned = summary.getFirst();
        returned.setScore(99, 99);

        // Then
        final Match original = board.getSummary().getFirst();
        assertEquals(3, original.getTotalScore());
    }
}
