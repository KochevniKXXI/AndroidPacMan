package ru.nomad.pacman.units;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

import ru.nomad.pacman.Assets;
import ru.nomad.pacman.GameMap;
import ru.nomad.pacman.GameScreen;

public class Monster extends Unit implements Serializable {
    PacMan target;
    int type;
    char unitChar;
    transient TextureRegion[] whiteRegions;
    Vector2 v; // вектор для гонки за пакманом

    public Monster(GameScreen gameScreen, GameMap gameMap, PacMan target, int type, char unitChar) {
        this.gameScreen = gameScreen;
        this.position = gameMap.getUnitPosition(unitChar);
        this.destination = gameMap.getUnitPosition(unitChar);
        this.target = target;
        this.gameMap = gameMap;
        this.animationTimer = 0.0f;
        this.secPerFrame = 0.1f;
        this.rotation = 0;
        this.tmp = new Vector2(0, 0);
        this.unitChar = unitChar;
        this.v = new Vector2(-1, -1);
        this.type = type;
        this.speed = 2.0f;
        checkLevel();
        loadResources(gameScreen);
    }

    public void checkLevel() {
        this.speed = 2.0f + gameMap.getLevel() * 0.05f;
        if (speed > 3.0f) {
            speed = 3.0f;
        }
    }

    @Override
    public void restart(boolean full) {
        resetPosition();
        checkLevel();
    }

    @Override
    public void loadResources(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.textureRegions = Assets.getInstance().getAtlas().findRegion("ghosts").split(SIZE, SIZE)[type];
        this.whiteRegions = Assets.getInstance().getAtlas().findRegion("ghosts").split(SIZE, SIZE)[4];
    }

    @Override
    public void resetPosition() {
        this.position = gameMap.getUnitPosition(unitChar);
        this.destination = gameMap.getUnitPosition(unitChar);
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion currentRegion = textureRegions[getCurrentFrame()];
        if (gameScreen.checkHuntTimer() && gameScreen.getHuntTimer() % 0.4f > 0.2f) {
            currentRegion = whiteRegions[getCurrentFrame()];
        }
        if (flipX != currentRegion.isFlipX()) {
            currentRegion.flip(true, false);
        }
        batch.draw(currentRegion, position.x * GameScreen.WORLD_CELL_PX, position.y * GameScreen.WORLD_CELL_PX, HALF_SIZE, HALF_SIZE, SIZE, SIZE, 1, 1, rotation);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (Vector2.dst(position.x, position.y, destination.x, destination.y) < 0.001f) {
            Direction dir = Direction.values()[MathUtils.random(0, 3)];
            if (Vector2.dst(position.x, position.y, target.getPosition().x, target.getPosition().y) < 8.0f && !gameScreen.checkHuntTimer()) {
                gameMap.buildRoute((int) target.getPosition().x, (int) target.getPosition().y, (int) position.x, (int) position.y, v);
                if (v.x < position.x) {
                    dir = Direction.LEFT;
                }
                if (v.x > position.x) {
                    dir = Direction.RIGHT;
                }
                if (v.y < position.y) {
                    dir = Direction.DOWN;
                }
                if (v.y > position.y) {
                    dir = Direction.UP;
                }
            }
            move(dir, true);
        } else {
            tmp.set(destination).sub(position).nor().scl(speed * dt);
            position.add(tmp);
            if (Vector2.dst(position.x, position.y, destination.x, destination.y) < tmp.len()) {
                position.set(destination);
            }
        }
    }
}
