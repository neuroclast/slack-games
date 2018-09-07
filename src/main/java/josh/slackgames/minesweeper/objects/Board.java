package josh.slackgames.minesweeper.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Game board object
 * @author Josh Ellis - neuroclast@gmail.com
 */
public class Board {

    private int width;
    private int height;
    private List<Tile> tiles = new ArrayList<>();

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void addTile(int x, int y, Integer state) {
        tiles.add(new Tile(x, y, state));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Tile> getTiles() {
        return tiles;
    }
}
