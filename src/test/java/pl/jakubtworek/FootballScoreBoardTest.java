package pl.jakubtworek;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
}
