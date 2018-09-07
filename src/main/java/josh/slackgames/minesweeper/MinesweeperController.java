package josh.slackgames.minesweeper;

import josh.slackgames.minesweeper.objects.Board;
import josh.slackgames.minesweeper.objects.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/minesweeper")
public class MinesweeperController {

    private final MinesweeperService msService;

    @Autowired
    public MinesweeperController(MinesweeperService msService) {
        this.msService = msService;
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

                // TODO: remove me
                game.clickTile(10, 10);

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
    @MessageMapping("/minesweeper/load/{channelId}")
    @SendToUser("/queue/minesweeper")
    public Board loadGame(@DestinationVariable String channelId) {
        // make sure game exists
        if(!msService.gameExists(channelId)) {
            return null;
        }

        Game game = msService.getGame(channelId);

        return game.getBoard();
    }


    @MessageMapping("/minesweeper/click/{channelId}/{clickType}/{x}/{y}")
    @SendTo("/topic/minesweeper/{channelId}")
    public Board clickTile(@DestinationVariable String channelId,
                           @DestinationVariable int clickType,
                           @DestinationVariable int x,
                           @DestinationVariable int y) {
        // make sure game exists
        if(!msService.gameExists(channelId)) {
            return null;
        }

        Game game = msService.getGame(channelId);

        if(clickType == 1) {
            game.clickTile(x, y);
        }

        return game.getBoard();
    }
}
