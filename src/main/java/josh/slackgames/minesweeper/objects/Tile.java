package josh.slackgames.minesweeper.objects;

/**
 * Tile object
 * @author Josh Ellis - neuroclast@gmail.com
 */
public class Tile {
    private final Integer state;
    private final int x;
    private final int y;

    public Tile(int x, int y, Integer state) {
        this.x = x;
        this.y = y;
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
