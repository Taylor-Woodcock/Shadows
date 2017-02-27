package edu.lancs.game.scenes;

import edu.lancs.game.Debug;
import edu.lancs.game.Window;
import edu.lancs.game.entity.Chest;
import edu.lancs.game.entity.Enemy;
import edu.lancs.game.entity.Player;
import edu.lancs.game.generation.*;
import edu.lancs.game.gui.HUD;
import edu.lancs.game.gui.Lighting;
import edu.lancs.game.gui.MiniMap;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.View;
import org.jsfml.window.event.Event;

import java.io.*;
import java.util.Random;

import static edu.lancs.game.Constants.*;

public class GameScene extends Scene {
    private HUD hud;
    private MiniMap miniMap;
    private Lighting lighting;
    private Player player;
    private Level[][] levels;
    private Level currentLevel;
    private Level bossLevel;
    private Chest chest;

    public GameScene(Window window) {
        super(window);
        setTitle("Do Not Die");
        setMusic("game_music");
        setBackgroundColour(Color.BLACK);

        player = new Player(getWindow()); // creates a Player and passes the Window into it
        hud = new HUD(getWindow(), player); // creates a HUD and passes Window and the player just created into it for variables

        // currently only has one level TODO: add a 2D level array
        Random random = new Random();

        levels = new Level[GAME_LEVEL_WIDTH][GAME_LEVEL_HEIGHT]; // creates a 2D array to fill with levels

        chest = new Chest(getWindow(), 200, 200);

        for(int column = 0; column < GAME_LEVEL_WIDTH; column++)
            for(int row = 0; row < GAME_LEVEL_HEIGHT; row++)
                //FIXME: Make this a little less parameter heavy
                levels[column][row] = new Level(getWindow(), random.nextInt(ROOM_WIDTH_MAX + 1 - ROOM_WIDTH_MIN) + ROOM_WIDTH_MIN, random.nextInt(ROOM_HEIGHT_MAX + 1 - ROOM_HEIGHT_MIN) + ROOM_HEIGHT_MIN, random.nextInt(5), "green_stone", new Color(random.nextInt(192 + 1 - 64) + 64, random.nextInt(128 + 1 - 64) + 64, random.nextInt(64 + 1) + 10), column, row); // generates a level 7x5 with 0 complexity and using textures "green_stone"

        currentLevel = levels[random.nextInt(GAME_LEVEL_WIDTH)][random.nextInt(GAME_LEVEL_HEIGHT)]; // randomises the starting level
        currentLevel.discoverLevel();
        bossLevel = levels[random.nextInt(GAME_LEVEL_WIDTH)][random.nextInt(GAME_LEVEL_HEIGHT)]; // randomises the boss level

        miniMap = new MiniMap(getWindow(), levels); // creates the minimap
        lighting = new Lighting(getWindow(), player); // created the lighting instance
    }

    /***
     * Draws and updates the game objects such as decorations, player, HUD, Tiles, etc.
     *
     * @param window - Window that the objects will be drawn to
     */
    @Override
    public void draw(Window window) {
        player.update();
        // draws the level tiles
        for (Tile[] tileRow : currentLevel.getTiles()) {
            for (Tile tile : tileRow) {
                window.draw(tile);
                tileCollision(tile);
            }
        }


        // FIXME: View works, but maybe should really be done another way
        View view = (View) getWindow().getDefaultView();
        view.setCenter(player.getPosition());
        getWindow().setView(view);

        window.draw(chest); //FIXME: Just a text chest, remove once chest ransomisation is added

        // draws the enemies
        for(Enemy enemy : currentLevel.getEnemies()) {
            enemy.setTargetActor(player);
            enemy.update();
            window.draw(enemy);
        }

        // draws the player
        window.draw(player);

        // draw the lighting
        lighting.generateLighting(100 + player.getBatteryLevel());
        lighting.getLighting().forEach(window::draw);

        // draws the HUD
        hud.update();
        hud.getDecorations().forEach(window::draw);
        hud.getHearts().forEach(window::draw);
        hud.getTexts().forEach(window::draw);

        // display the minimap if the control key is pressed
        if(getWindow().getInputHandler().isCtrlKeyPressed()) {
            // draw the minimap
            miniMap.updateMap();
            miniMap.getMapTiles().forEach(window::draw);
        }
    }

    /***
     * VERY VERY VERY bad collision and level teleportation FIXME: NEEDS TO BE LOOKED AT AND COMPLETELY REWRITTEN
     * @param tile
     */
    private void tileCollision(Tile tile) {
        //FIXME: Some basic collision detection, worst way possible, needs changing (DON'T USE instanceOf, this was a last resort test)
        if (!(tile instanceof Floor)) {
            boolean teleport = false; //FIXME: Quick fix, should be rewritten
            Tile.Direction direction = tile.getDirection();
            if (tile.getGlobalBounds().intersection(new FloatRect(player.getPosition().x, player.getPosition().y, 1, 1)) != null) {
                if(tile instanceof Door) {
                    int destinationColumn = ((Door) tile).getDestinationColumn();
                    int destinationRow = ((Door) tile).getDestinationRow();

                    Debug.print("Teleporting to room: " + destinationRow + ", " + destinationColumn);
                    if((destinationColumn >= 0 && destinationColumn < GAME_LEVEL_WIDTH) && (destinationRow >= 0 && destinationRow < GAME_LEVEL_HEIGHT)) {
                        currentLevel.setCurrentLevel(false); // set the level loaded to not be the current level
                        currentLevel.unloadLevel();
                        currentLevel = levels[destinationRow][destinationColumn]; // change the level
                        currentLevel.discoverLevel(); // discover (minimap)
                        currentLevel.setCurrentLevel(true); // set the current level
                        player.resetCollision();
                        teleport = true;
                    }
                }
                switch (direction) {
                    case N:
                        if(teleport)
                            player.setPosition(currentLevel.getDoor(Tile.Direction.S).getPosition().x + MAP_TILE_WIDTH / 2,
                                    currentLevel.getDoor(Tile.Direction.S).getPosition().y - 100 + MAP_TILE_HEIGHT / 2);
                        else
                            player.setCollidingUp(true);
                        break;
                    case E:
                        if(teleport)
                            player.setPosition(currentLevel.getDoor(Tile.Direction.W).getPosition().x + 100 + MAP_TILE_WIDTH / 2,
                                    currentLevel.getDoor(Tile.Direction.W).getPosition().y + MAP_TILE_HEIGHT / 2);
                        else
                            player.setCollidingRight(true);
                        break;
                    case S:
                        if(teleport)
                            player.setPosition(currentLevel.getDoor(Tile.Direction.N).getPosition().x + MAP_TILE_WIDTH / 2,
                                    currentLevel.getDoor(Tile.Direction.N).getPosition().y + 100 + MAP_TILE_HEIGHT / 2);
                        else
                            player.setCollidingDown(true);
                        break;
                    case W:
                        if(teleport)
                            player.setPosition(currentLevel.getDoor(Tile.Direction.E).getPosition().x - 100 + MAP_TILE_WIDTH / 2,
                                    currentLevel.getDoor(Tile.Direction.E).getPosition().y + MAP_TILE_HEIGHT / 2);
                        else
                            player.setCollidingLeft(true);
                        break;
                    case NW:
                        player.setCollidingUp(true);
                        player.setCollidingLeft(true);
                        break;
                    case NE:
                        player.setCollidingUp(true);
                        player.setCollidingRight(true);
                        break;
                    case SW:
                        player.setCollidingDown(true);
                        player.setCollidingLeft(true);
                        break;
                    case SE:
                        player.setCollidingDown(true);
                        player.setCollidingRight(true);
                        break;
                }
            }
        }
    }

    /***
     * Window events happening while the GameScene is loaded are pushed to the InputHandler.
     *
     * @param event - Event that has been triggered
     */
    @Override
    public void executeEvent(Event event) {
        switch (event.type) {
            case KEY_PRESSED:
            case KEY_RELEASED:
                getWindow().getInputHandler().processInputs(event.asKeyEvent().key); // updates the InputHandler as to which key was pressed/released
                break;

            case JOYSTICK_MOVED:
                getWindow().getInputHandler().processInputs(event.asJoystickMoveEvent());
                break;

            case JOYSTICK_BUTTON_PRESSED:
            case JOYSTICK_BUTTON_RELEASED:
                getWindow().getInputHandler().processInputs(event.asJoystickButtonEvent());
                break;
        }
    }
}
