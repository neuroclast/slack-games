package josh.slackgames.minesweeper;

import josh.slackgames.WSMessage;
import josh.slackgames.minesweeper.objects.Game;
import josh.slackgames.minesweeper.objects.Tile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/minesweeper")
public class MinesweeperController {

    private final MinesweeperService msService;
    private final SimpMessagingTemplate template;


    @Autowired
    public MinesweeperController(MinesweeperService msService, SimpMessagingTemplate template) {
        this.msService = msService;
        this.template = template;
    }


    @PostMapping("/start-game")
    public ResponseEntity startGame(@RequestParam String channelId, @RequestParam(required = false, defaultValue = "BEGINNER") String level) {
        // attempt to start game
        if(!msService.gameExists(channelId)) {
            try {
                Game game = msService.startGame(channelId, MinesweeperService.Level.valueOf(level));
                if (game == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating game.");
                }

                return ResponseEntity.ok().build();
            }
            catch(IllegalArgumentException iae) {
                return ResponseEntity.badRequest().body("Invalid level specified.");
            }
        }
        // game already exists
        else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(String.format("Game already exists for channel %s.", channelId));
        }
    }


    /**
     * Does the initial load of a game when client first connects to WS
     * @param channelId Channel ID
     * @return Board
     */
    @MessageMapping("/minesweeper/load/{channelId}/{userId}")
    @SendToUser("/queue/minesweeper")
    public WSMessage loadGame(@DestinationVariable String channelId,
                              @DestinationVariable String userId) {
        // make sure game exists
        if(!msService.gameExists(channelId)) {
            return new WSMessage("error", "Game does not exist for channel.");
        }

        Game game = msService.getGame(channelId);

        this.template.convertAndSend(String.format("/topic/minesweeper/%s", channelId), new WSMessage("log", String.format("%s has joined the game.", userId)));

        if(game.isGameOver()) {
            return new WSMessage("loss", null, game.getBoard());
        }

        return new WSMessage("initial", null, game.getBoard());
    }


    /**
     * Resets game when user clicks new game button
     * @param channelId Channel ID
     * @param userId User Id
     * @return Board
     */
    @MessageMapping("/minesweeper/reset/{channelId}/{userId}")
    @SendTo("/topic/minesweeper/{channelId}")
    public WSMessage resetGame(@DestinationVariable String channelId,
                               @DestinationVariable String userId) {
        // make sure game exists
        if(!msService.gameExists(channelId)) {
            return new WSMessage("error", "Game does not exist for channel.");
        }

        Game game = msService.resetGame(channelId);

        if(game == null) {
            return new WSMessage("error", "Unable to reset game!");
        }

        this.template.convertAndSend(String.format("/topic/minesweeper/%s", channelId), new WSMessage("log", String.format("Game restarted by %s.", userId)));

        return new WSMessage("initial", null, game.getBoard());
    }


    @MessageMapping("/minesweeper/click/{channelId}/{userId}/{clickType}/{x}/{y}")
    @SendTo("/topic/minesweeper/{channelId}")
    public WSMessage clickTile(@DestinationVariable String channelId,
                               @DestinationVariable String userId,
                               @DestinationVariable int clickType,
                               @DestinationVariable int x,
                               @DestinationVariable int y) {
        // make sure game exists
        if(!msService.gameExists(channelId)) {
            return new WSMessage("error", "Game does not exist for channel.");
        }

        Game game = msService.getGame(channelId);

        Integer clickResult = 0;

        if(clickType == 1) {
            clickResult = game.revealTile(x, y);
            this.template.convertAndSend(String.format("/topic/minesweeper/%s", channelId), new WSMessage("log", String.format("%s revealed tile at %d,%d.", userId, x, y)));
        }
        else if(clickType == 2) {
            clickResult = game.flagTile(x, y);
            this.template.convertAndSend(String.format("/topic/minesweeper/%s", channelId), new WSMessage("log", String.format("%s flagged tile at %d,%d.", userId, x, y)));
        }

        if(clickResult != null) {
            // already clicked here
            if (clickResult == -2) {
                return null;
            }
            // update whole board
            else if (clickResult == -3) {
                return new WSMessage("initial", null, game.getBoard());
            }
            // report loss
            else if (clickResult == -4) {
                this.template.convertAndSend(String.format("/topic/minesweeper/%s", channelId), new WSMessage("log", String.format("%s lost the game!", userId)));
                return new WSMessage("loss", null, game.getBoard());
            }
        }

        return new WSMessage("update", null, new Tile(x, y, clickResult));
    }
}
