package josh.slackgames.minesweeper.objects;

import josh.slackgames.minesweeper.MinesweeperService;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {

    private List<Point> mines;
    private int width, height, numMines;
    private int[][] field;
    private boolean[][] revealed;
    private boolean generated = false;


    /**
     * Initializes a new game state
     * @param level Difficulty level
     */
    public Game(MinesweeperService.Level level) {
        int width = 8;
        int height = 8;
        int numMines = 10;

        if(level == MinesweeperService.Level.INTERMEDIATE) {
            width = 16;
            height = 16;
            numMines = 40;
        }
        else if(level == MinesweeperService.Level.EXPERT) {
            width = 30;
            height = 16;
            numMines = 99;
        }

        this.field = new int[width][height];
        this.width = width;
        this.height = height;
        this.numMines = numMines;
        mines = new ArrayList<>();

        // set all tiles to not-revealed
        this.revealed = new boolean[width][height];
    }


    /**
     * Retrieves entire board for game
     * @return Board
     */
    public Board getBoard() {
        Board board = new Board(width, height);

        // add tiles to response object
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                if(revealed[x][y]) {
                    board.addTile(x, y, field[x][y]);
                }
            }
        }

        return board;
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumMines() {
        return numMines;
    }

    /**
     * Generates a new game field
     * @param clickX x-coordinate user starts with
     * @param clickY y-coordinate user starts with
     */
    public void generateField(int clickX, int clickY) {
        // place mines randomly in field
        for(int i = 0; i < numMines ; i++) {
            Random rand = new Random();
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);

            // retry if this spot already has a mine or if it's the starting spot
            if(field[x][y] != 0 || (x == clickX && y == clickY)) {
                i--;
                continue;
            }

            field[x][y] = 10;
            mines.add(new Point(x, y));
        }

        // generate integer distances to surrounding mines
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                if(field[x][y] == 10) {
                    continue;
                }

                for(Point p: mines) {
                    double distance = Math.hypot(x-p.x, y-p.y);

                    if(distance < 2) {
                        field[x][y]++;
                    }
                }
            }
        }
    }



    /**
     * Handle user click on tile
     * @param clickX x coordinate
     * @param clickY y coordinate
     * @return int
     */
    public int clickTile(int clickX, int clickY) {
        // do nothing if they click an already revealed tile
        if(revealed[clickX][clickY]) {
            return -2;
        }

        // generate board if first click
        if(!generated) {
            generateField(clickX, clickY);
            generated = true;
        }

        // reveal tile
        revealed[clickX][clickY] = true;

        // check for winning condition
        if(field[clickX][clickY] != 9) {
            int numRevealed = 0;
            int totalTiles = width * height;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (revealed[x][y]) {
                        numRevealed++;
                    }
                }
            }

            if(totalTiles - numRevealed == numMines) {
                // winner winner chicken dinner!
                return -1;
            }
        }

        return field[clickX][clickY];
    }
}
