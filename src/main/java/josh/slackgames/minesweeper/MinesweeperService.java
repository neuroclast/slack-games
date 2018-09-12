package josh.slackgames.minesweeper;

import josh.slackgames.minesweeper.objects.Game;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores games and their objects
 */
@Service
public class MinesweeperService {
    private Map<String, Game> games = new HashMap<>();

    public enum Level {
        BEGINNER, INTERMEDIATE, EXPERT
    }


    /**
     * Checks if a game has already started for a channelId
     * @param channelId Channel ID to check
     * @return boolean
     */
    boolean gameExists(String channelId) {
        return games.containsKey(channelId);
    }


    /**
     * Starts a new game for a channel
     * @param channelId Channel to start game for
     * @param level Game level
     * @return Game object
     */
    Game startGame(String channelId, Level level) {
        if(gameExists(channelId)) {
            return null;
        }

        Game game = new Game(level);
        games.put(channelId, game);

        return game;
    }


    /**
     * Resets a game
     * @param channelId channel to reset game for
     * @return Game object
     */
    Game resetGame(String channelId) {
        if(!gameExists(channelId)) {
            return null;
        }

        Game oldGame = getGame(channelId);
        Game newGame = new Game(oldGame.getLevel());
        endGame(channelId);
        games.put(channelId, newGame);

        return newGame;
    }


    /**
     * Ends a game
     * @param channelId Channel ID
     */
    void endGame(String channelId) {
        if(gameExists(channelId)) {
            games.remove(channelId);
        }
    }


    /**
     * Retrieves game object for channel
     * @param channelId Channel ID
     * @return Game object or null
     */
    Game getGame(String channelId) {
        return games.getOrDefault(channelId, null);
    }
}
