package com.mcelrea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Stack;

public class SimScreen implements Screen {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont defaultFont;

    private static final int GROUND = 0, GOLD = 1, PIT = 2, SPIDER = 3, WUMPUS = 4, GLITTER = 11, WIND = 12, WEB = 13, STINK = 14, BLACK = 20;

    private int world[][];
    private boolean visible[][];
    private boolean visited[][];
    private Stack<Location> moveHistory;

    private int currentSelection = -1;
    private int playerRow = 9;
    private int playerCol = 0;
    private boolean showCave = false;
    private int steps = 0;
    private boolean runningSim = false;

    private int toolBarOffset = 500;

    private Texture groundTexture;
    private Texture goldTexture;
    private Texture glitterTexture;
    private Texture pitTexture;
    private Texture windTexture;
    private Texture spiderTexture;
    private Texture webTexture;
    private Texture wumpusTexture;
    private Texture stinkTexture;
    private Texture playerTexture;
    private Texture blackTexture;

    private Texture upArrowTexture;
    private Texture downArrowTexture;
    private Texture leftArrowTexture;
    private Texture rightArrowTexture;
    private Texture plusTexture;
    private Texture questionTexture;
    private Texture trophyTexture;


    @Override
    public void show() {
        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera(WIDTH, HEIGHT);
        viewport = new FitViewport(WIDTH, HEIGHT, camera);
        defaultFont = new BitmapFont();

        world = new int[10][10];
        visible = new boolean[10][10];
        visited = new boolean[10][10];
        visible[playerRow][playerCol] = true;
        visited[playerRow][playerCol] = true;
        moveHistory = new Stack<Location>();
        moveHistory.push(new Location(playerRow, playerCol));

        groundTexture = new Texture("groundTile.png");
        goldTexture = new Texture("goldTile.png");
        glitterTexture = new Texture("glitterTile.png");
        pitTexture = new Texture("pitTile.png");
        windTexture = new Texture("windTile.png");
        spiderTexture = new Texture("spiderTile.png");
        webTexture = new Texture("webTile.png");
        wumpusTexture = new Texture("wumpusTile.png");
        stinkTexture = new Texture("stinkTile.png");
        playerTexture = new Texture("guy.png");
        blackTexture = new Texture("blackTile.png");

        upArrowTexture = new Texture("upArrow.png");
        downArrowTexture = new Texture("downArrow.png");
        leftArrowTexture = new Texture("leftArrow.png");
        rightArrowTexture = new Texture("rightArrow.png");
        plusTexture = new Texture("plus.png");
        questionTexture = new Texture("question.png");
        trophyTexture = new Texture("trophy.png");
    }

    public void renderToolBar(SpriteBatch spriteBatch) {
        defaultFont.draw(spriteBatch,"Toolbar", 650, toolBarOffset+80);
        spriteBatch.draw(groundTexture, 650, toolBarOffset);
        spriteBatch.draw(spiderTexture, 650, toolBarOffset-60);
        spriteBatch.draw(pitTexture, 650, toolBarOffset-120);
        spriteBatch.draw(wumpusTexture, 650, toolBarOffset-180);
        spriteBatch.draw(goldTexture, 650, toolBarOffset-240);

        spriteBatch.draw(upArrowTexture, 575, toolBarOffset-320); //300
        spriteBatch.draw(leftArrowTexture, 530, toolBarOffset-360); //340
        spriteBatch.draw(downArrowTexture, 575, toolBarOffset-400); //380
        spriteBatch.draw(rightArrowTexture, 620, toolBarOffset-360); //340
        spriteBatch.draw(questionTexture, 700, toolBarOffset-320);
        spriteBatch.draw(plusTexture, 700, toolBarOffset-380);
        spriteBatch.draw(trophyTexture, 700, toolBarOffset-440);

        defaultFont.draw(spriteBatch, "Stats", 550, toolBarOffset);
        defaultFont.draw(spriteBatch, "Steps: " + steps, 550, toolBarOffset-30);
    }

    public void renderTile(SpriteBatch spriteBatch, int tileType, int x, int y) {
        if(tileType == GROUND)
            spriteBatch.draw(groundTexture, x, y);
        else if(tileType == WUMPUS)
            spriteBatch.draw(wumpusTexture, x, y);
        else if(tileType== STINK)
            spriteBatch.draw(stinkTexture, x, y);
        else if(tileType == SPIDER)
            spriteBatch.draw(spiderTexture, x, y);
        else if(tileType == WEB)
            spriteBatch.draw(webTexture, x, y);
        else if(tileType == PIT)
            spriteBatch.draw(pitTexture, x, y);
        else if(tileType == WIND)
            spriteBatch.draw(windTexture, x, y);
        else if(tileType == GOLD)
            spriteBatch.draw(goldTexture, x, y);
        else if(tileType == GLITTER)
            spriteBatch.draw(glitterTexture, x, y);
    }

    private void renderPlayer(SpriteBatch spriteBatch) {
        spriteBatch.draw(playerTexture, convertColToX(playerCol), convertRowToY(playerRow));
    }

    private int convertYClickToScreenCoords(int y) {
        return 600-y;
    }

    public void move() {
        System.out.println();
        System.out.println("--------------------------------------");
        ArrayList<Location> moveLocs = getPossibleMoveLocs();
        System.out.println("Possible Moves: " + moveLocs);

        if(moveLocs.size() != 0) {
            int choice = (int)(Math.random()*moveLocs.size());
            System.out.println("Choosing index...." + choice);
            movePlayer(moveLocs.get(choice));
        }
        else {
            System.out.println("No where to go, moving to...." + moveHistory.peek());
            moveHistory.pop();
            movePlayer(moveHistory.pop());
        }

        System.out.println("stack: " + moveHistory);
        System.out.println("--------------------------------------");
        System.out.println();
    }

    public void movePlayer(Location location) {
        playerRow = location.getRow();
        playerCol = location.getCol();
        visited[playerRow][playerCol] = true;
        visible[playerRow][playerCol] = true;
        moveHistory.push(location);
        steps++;
    }

    public boolean notVisited(Location loc) {
        return visited[loc.getRow()][loc.getCol()] == false;
    }

    public ArrayList<Location> getPossibleMoveLocs() {
        ArrayList<Location> locs = new ArrayList<Location>(4);

        Location above = new Location(playerRow-1, playerCol);
        Location below = new Location(playerRow+1, playerCol);
        Location left = new Location(playerRow, playerCol-1);
        Location right = new Location(playerRow, playerCol+1);

        if(isValid(above) && notVisited(above))
            locs.add(above);
        if(isValid(below) && notVisited(below))
            locs.add(below);
        if(isValid(left) && notVisited(left))
            locs.add(left);
        if(isValid(right) && notVisited(right))
            locs.add(right);

        return locs;
    }

    public boolean completelySafe(Location loc) {
        return world[loc.getRow()][loc.getCol()] == 0;
    }

    public void renderWorld(SpriteBatch spriteBatch) {
        for(int row=0; row < world.length; row++) {
            for(int col=0; col < world[row].length; col++) {
                if(showCave == true || visible[row][col] == true) {
                    renderTile(spriteBatch, world[row][col], 10 + col * 50, 500 - row * 50);
                }
                else {
                    renderTile(spriteBatch, -1, 10 + col * 50, 500 - row * 50);
                }
                defaultFont.draw(spriteBatch, "["+row+","+col+"]", 10 + col * 50,520 - row * 50);
            }
        }
    }

    public void renderTileSelected(SpriteBatch spriteBatch) {
        if(currentSelection != -1) {
            int x = Gdx.input.getX();
            int y = Gdx.input.getY();
            renderTile(spriteBatch, currentSelection, x-20, convertYClickToScreenCoords(y)-20);
        }
    }

    public Location getWorldCell(int x, int y) {
        Location loc = new Location();
        int col = (x-20)/50;
        int row = (550-y)/50;
        System.out.println("(" + x + "," + y + ")");
        System.out.println(row + ", " + col);
        loc.setRow(row);
        loc.setCol(col);
        return loc;
    }

    public void handleMouseClick() {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX();
            int y = Gdx.input.getY();
            int size = 50;

            y = convertYClickToScreenCoords(y);
            //GROUND Tool
            if(x >= 650 && x <= 700 && y >=toolBarOffset && y <= toolBarOffset+size) {
                currentSelection = GROUND;
            }
            //SPIDER Tool
            else if(x >= 650 && x <= 700 && y >=toolBarOffset-60 && y <= toolBarOffset-60+size) {
                currentSelection = SPIDER;
            }
            //PIT Tool
            else if(x >= 650 && x <= 700 && y >=toolBarOffset-120 && y <= toolBarOffset-120+size) {
                currentSelection = PIT;
            }
            //WUMPUS Tool
            else if(x >= 650 && x <= 700 && y >=toolBarOffset-180 && y <= toolBarOffset-180+size) {
                currentSelection = WUMPUS;
            }
            //GOLD Tool
            else if(x >= 650 && x <= 700 && y >=toolBarOffset-240 && y <= toolBarOffset-240+size) {
                currentSelection = GOLD;
            }
            //up arrow
            else if (x >= 575 && x <= 625 && y >=toolBarOffset-320 && y <= toolBarOffset-320+size) {
                if(isValid(playerRow-1, playerCol)) {
                    playerRow = playerRow - 1;
                    visible[playerRow][playerCol] = true;
                    steps++;
                }
            }
            //right arrow
            else if (x >= 620 && x <= 670 && y >=toolBarOffset-360 && y <= toolBarOffset-360+size) {
                if(isValid(playerRow, playerCol+1)) {
                    playerCol = playerCol + 1;
                    visible[playerRow][playerCol] = true;
                    steps++;
                }
            }
            //down arrow
            else if (x >= 575 && x <= 625 && y >=toolBarOffset-400 && y <= toolBarOffset-400+size) {
                if(isValid(playerRow+1, playerCol)) {
                    playerRow = playerRow + 1;
                    visible[playerRow][playerCol] = true;
                    steps++;
                }
            }
            //left arrow
            else if (x >= 530 && x <= 580 && y >=toolBarOffset-360 && y <= toolBarOffset-360+size) {
                if(isValid(playerRow, playerCol-1)) {
                    playerCol = playerCol - 1;
                    visible[playerRow][playerCol] = true;
                    steps++;
                }
            }
            //question mark
            else if (x >= 700 && x <= 750 && y >=toolBarOffset-320 && y <= toolBarOffset-320+size) {
                showCave = !showCave;
            }
            //trophy
            else if (x >= 700 && x <= 750 && y >=toolBarOffset-440 && y <= toolBarOffset-440+size) {
                runningSim = !runningSim;
            }
            //plus
            else if (x >= 700 && x <= 750 && y >=toolBarOffset-380 && y <= toolBarOffset-380+size) {
                move();
            }
            else {
                Location cell = getWorldCell(x, y);
                if(currentSelection != -1 && cell.getRow() >= 0 && cell.getRow() < 10 && cell.getCol() >= 0 && cell.getCol() < 10) {
                    world[cell.getRow()][cell.getCol()] = currentSelection;
                    placeHints(currentSelection, cell.getRow(), cell.getCol());
                }
                currentSelection = -1;
            }
        }
    }

    public boolean isValid(int row, int col) {
        return row >= 0 && row < world.length && col >= 0 && col < world[row].length;
    }

    public boolean isValid(Location loc) {
        return loc.getRow() >= 0 && loc.getRow() < world.length && loc.getCol() >= 0 && loc.getCol() < world[loc.getRow()].length;
    }

    private void setPlayerVision() {
        Location above = new Location(playerRow-1, playerCol);
        Location below = new Location(playerRow+1, playerCol);
        Location left = new Location(playerRow, playerCol-1);
        Location right = new Location(playerRow, playerCol+1);

        if(isValid(above))
            visible[above.getRow()][above.getCol()] = true;
        if(isValid(below))
            visible[below.getRow()][below.getCol()] = true;
        if(isValid(left))
            visible[left.getRow()][left.getCol()] = true;
        if(isValid(right))
            visible[right.getRow()][right.getCol()] = true;
    }

    private void placeHints(int tileType, int row, int col) {
        Location above = new Location(row-1, col);
        Location below = new Location(row+1, col);
        Location left = new Location(row, col-1);
        Location right = new Location(row, col+1);

        if(tileType == GROUND) {
            eraseTile(row, col);
            return;
        }

        if(isValid(above))
            world[above.getRow()][above.getCol()] = tileType + 10;
        if(isValid(below))
            world[below.getRow()][below.getCol()] = tileType + 10;
        if(isValid(left))
            world[left.getRow()][left.getCol()] = tileType + 10;
        if(isValid(right))
            world[right.getRow()][right.getCol()] = tileType + 10;
    }

    private void eraseTile(int row, int col) {
        Location above = new Location(row-1, col);
        Location below = new Location(row+1, col);
        Location left = new Location(row, col-1);
        Location right = new Location(row, col+1);

        if(isValid(above))
            world[above.getRow()][above.getCol()] = GROUND;
        if(isValid(below))
            world[below.getRow()][below.getCol()] = GROUND;
        if(isValid(left))
            world[left.getRow()][left.getCol()] = GROUND;
        if(isValid(right))
            world[right.getRow()][right.getCol()] = GROUND;
    }

    public int convertColToX(int col) {
        return 10+col*50;
    }

    public int convertRowToY(int row) {
        return 500-row*50;
        //0 = 450;
        //1 = 400;
        //2 = 350;
    }

    @Override
    public void render(float delta) {
        clearScreen();

        handleMouseClick();

        if(runningSim) {
            move();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //setPlayerVision();

        spriteBatch.begin();
        renderWorld(spriteBatch);
        renderToolBar(spriteBatch);
        renderTileSelected(spriteBatch);
        renderPlayer(spriteBatch);
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        defaultFont.dispose();
        groundTexture.dispose();
        goldTexture.dispose();
        glitterTexture.dispose();
        pitTexture.dispose();
        windTexture.dispose();
        spiderTexture.dispose();
        webTexture.dispose();
        wumpusTexture.dispose();
        stinkTexture.dispose();
        playerTexture.dispose();
        blackTexture.dispose();
    }

    public void clearScreen() {
        Gdx.gl20.glClearColor(0,0,0,1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
