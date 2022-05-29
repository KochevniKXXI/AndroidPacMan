package ru.nomad.pacman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class GameMap implements Serializable {
    public enum CellType {
        EMPTY('0'), WALL('1'), FOOD('_'), CHERRY('*'), PLAYER('s'), BLUE('b'), PINK('p'),
        RED('r'), ORANGE('o');

        char datSymbol;

        CellType(char datSymbol) {
            this.datSymbol = datSymbol;
        }
    }

    public static final int CELL_SIZE_PX = 80;

    private int level;
    private int mapSizeX;
    private int mapSizeY;
    private int foodCount;
    private CellType[][] data;
    private transient TextureRegion textureGround;
    private transient TextureRegion textureWall;
    private transient TextureRegion textureFood;
    private transient TextureRegion textureCherry;
    private HashMap<Character, Vector2> startPositions;

    public int getFoodCount() {
        return foodCount;
    }

    public int getMapSizeX() {
        return mapSizeX;
    }

    public int getMapSizeY() {
        return mapSizeY;
    }

    public Vector2 getUnitPosition(char unitChar) {
        return startPositions.get(unitChar).cpy();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public GameMap() {
        this.level = 1;
        loadResources();
        loadMap("map.dat");
    }

    public void loadResources() {
        textureGround = Assets.getInstance().getAtlas().findRegion("ground");
        textureWall = Assets.getInstance().getAtlas().findRegion("wall");
        textureFood = Assets.getInstance().getAtlas().findRegion("food");
        textureCherry = Assets.getInstance().getAtlas().findRegion("energizer");
    }

    public void loadMap(String name) {
        startPositions = new HashMap<Character, Vector2>();
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = Gdx.files.internal(name).reader(8192);
            String str;
            while ((str = br.readLine()) != null) {
                list.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapSizeX = list.get(0).length();
        mapSizeY = list.size();
        data = new CellType[mapSizeX][mapSizeY];
        foodCount = 0;
        for (int y = 0; y < list.size(); y++) {
            for (int x = 0; x < list.get(y).length(); x++) {
                char currentSymb = list.get(y).charAt(x);
                for (int i = 0; i < CellType.values().length; i++) {
                    if (currentSymb == CellType.values()[i].datSymbol) {
                        data[x][mapSizeY - y - 1] = CellType.values()[i];
                        if (CellType.values()[i] == CellType.FOOD) {
                            foodCount++;
                        }
                        if (CellType.values()[i] == CellType.PLAYER || CellType.values()[i] == CellType.BLUE || CellType.values()[i] == CellType.PINK || CellType.values()[i] == CellType.RED || CellType.values()[i] == CellType.ORANGE) {
                            startPositions.put(currentSymb, new Vector2(x, mapSizeY - y - 1));
                        }
                        break;
                    }
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < mapSizeX; i++) {
            for (int j = 0; j < mapSizeY; j++) {
                batch.draw(textureGround, i * CELL_SIZE_PX, j * CELL_SIZE_PX);
                if (data[i][j] == CellType.WALL) {
                    batch.draw(textureWall, i * CELL_SIZE_PX, j * CELL_SIZE_PX);
                }
                if (data[i][j] == CellType.FOOD) {
                    batch.draw(textureFood, i * CELL_SIZE_PX, j * CELL_SIZE_PX);
                }
                if (data[i][j] == CellType.CHERRY) {
                    batch.draw(textureCherry, i * CELL_SIZE_PX, j * CELL_SIZE_PX);
                }
            }
        }
    }

    public boolean isCellEmpty(int cellX, int cellY) {
        if (cellX < 0) return data[mapSizeX - 1][cellY] != CellType.WALL;
        if (cellX > mapSizeX - 1) return data[0][cellY] != CellType.WALL;
        return data[cellX][cellY] != CellType.WALL;
    }

    public boolean checkFoodEating(float x, float y) {
        if (0 <= x && x <= mapSizeX - 1) {
            if (data[(int) x][(int) y] == CellType.FOOD) {
                data[(int) x][(int) y] = CellType.EMPTY;
                foodCount--;
                return true;
            }
        }
        return false;
    }

    public boolean checkCherryEating(float x, float y) {
        if (0 <= x && x <= mapSizeX - 1) {
            if (data[(int) x][(int) y] == CellType.CHERRY) {
                data[(int) x][(int) y] = CellType.EMPTY;
                return true;
            }
        }
        return false;
    }

    public void buildRoute(int srcX, int srcY, int dstX, int dstY, Vector2 destination) {
        int[][] arr = new int[mapSizeX][mapSizeY];
        for (int i = 0; i < mapSizeX; i++) {
            for (int j = 0; j < mapSizeY; j++) {
                if (data[i][j] == CellType.WALL) {
                    arr[i][j] = -1;
                }
            }
        }
        if (srcX < 0) srcX = 0;
        if (srcX > mapSizeX - 1) srcX = mapSizeX - 1;
        arr[srcX][srcY] = 1;
        updatePoint(arr, srcX, srcY, 2);
        int lastPoint = -1;
        for (int i = 2; i < 45; i++) {
            for (int x = 0; x < mapSizeX; x++) {
                for (int y = 0; y < mapSizeY; y++) {
                    if (arr[x][y] == i) {
                        updatePoint(arr, x, y, i + 1);
                    }
                }
            }
            if (arr[dstX][dstY] > 0) {
                lastPoint = arr[dstX][dstY];
                break;
            }
        }
        for (int i = 0; i < mapSizeX; i++) {
            for (int j = 0; j < mapSizeY; j++) {
                if (arr[i][j] == lastPoint - 1) {
                    destination.set(i, j);
                    return;
                }
            }
        }
    }

    public void updatePoint(int[][] arr, int x, int y, int number) {
        if (x - 1 > -1 && arr[x - 1][y] == 0) {
            arr[x - 1][y] = number;
        }
        if (x + 1 < mapSizeX && arr[x + 1][y] == 0) {
            arr[x + 1][y] = number;
        }
        if (y + 1 < mapSizeY && arr[x][y + 1] == 0) {
            arr[x][y + 1] = number;
        }
        if (y - 1 > -1 && arr[x][y - 1] == 0) {
            arr[x][y - 1] = number;
        }
    }
}
