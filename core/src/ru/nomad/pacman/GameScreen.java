package ru.nomad.pacman;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ru.nomad.pacman.units.Monster;
import ru.nomad.pacman.units.PacMan;
import ru.nomad.pacman.units.Unit;

public class GameScreen implements Screen {
    public static final int WORLD_CELL_PX = 80;

    private SpriteBatch batch;

    private PacMan pacMan;
    private GameMap gameMap;
    private Camera camera;
    private Monster[] monsters;
    private BitmapFont font48;
    private float huntTimer;

    private Stage stage;
    private Skin skin;

    private boolean paused;

    public void resetHuntTimer() {
        huntTimer = 0.0f;
    }

    public void activateHuntTimer() {
        huntTimer = 5.0f - gameMap.getLevel() * 0.2f;
        if (huntTimer < 0.0f) {
            huntTimer = 0.0f;
        }
    }

    public boolean checkHuntTimer() {
        return huntTimer > 0.0f;
    }

    public float getHuntTimer() {
        return huntTimer;
    }

    public GameScreen(SpriteBatch batch, Camera camera) {
        this.batch = batch;
        this.camera = camera;
    }

    @Override
    public void show() {
        this.font48 = Assets.getInstance().getAssetManager().get("zorque48.ttf");
        this.gameMap = new GameMap();
        this.pacMan = new PacMan(this, gameMap);
        this.monsters = new Monster[4];
        this.monsters[0] = new Monster(this, gameMap, pacMan, 0, 'r');
        this.monsters[1] = new Monster(this, gameMap, pacMan, 1, 'b');
        this.monsters[2] = new Monster(this, gameMap, pacMan, 2, 'o');
        this.monsters[3] = new Monster(this, gameMap, pacMan, 3, 'p');
        this.camera.position.set(640, 360, 0);
        this.camera.update();
        this.huntTimer = 0.0f;
        this.createGUI();
        this.paused = false;
    }

    public void createGUI() {
        stage = new Stage(ScreenManager.getInstance().getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin();
        skin.addRegions(Assets.getInstance().getAtlas());
        skin.add("font48", font48);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.getDrawable("shortButton");
        textButtonStyle.font = font48;
        skin.add("simpleSkin", textButtonStyle);

        final Button btnPause = new TextButton("II", skin, "simpleSkin");

        btnPause.setPosition(884, 628);

        stage.addActor(btnPause);

        final Group pausePanel = new Group();

        btnPause.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paused = true;
                pausePanel.setVisible(true);
                btnPause.setVisible(false);
            }
        });

        // ------------ PAUSE PANEL --------------
        Pixmap pixmap = new Pixmap(440, 320, Pixmap.Format.RGB888);
        pixmap.setColor(0.0f, 0.0f, 0.2f, 1.0f);
        pixmap.fill();
        Texture texturePanel = new Texture(pixmap);
        skin.add("texturePanel", texturePanel);
        pausePanel.setVisible(false);
        Button btnMenu = new TextButton("M", skin, "simpleSkin");
        Button btnContinue = new TextButton("C>", skin, "simpleSkin");
        Button btnRestart = new TextButton("R", skin, "simpleSkin");
        Label.LabelStyle ls = new Label.LabelStyle(font48, Color.WHITE);
        Label pauseLabel = new Label("PAUSED", ls);
        pauseLabel.setPosition(120, 240);
        Image image = new Image(skin, "texturePanel");
        pausePanel.addActor(image);
        pausePanel.setPosition(ScreenManager.WORLD_WIDTH / 2 - 220, ScreenManager.WORLD_HEIGHT / 2 - 160);
        pausePanel.addActor(btnMenu);
        pausePanel.addActor(btnContinue);
        pausePanel.addActor(btnRestart);
        pausePanel.addActor(pauseLabel);
        btnMenu.setPosition(40, 40);
        btnRestart.setPosition(180, 40);
        btnContinue.setPosition(320, 40);
        stage.addActor(pausePanel);
        btnMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.MENU);
            }
        });
        btnContinue.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paused = false;
                pausePanel.setVisible(false);
                btnPause.setVisible(true);
            }
        });
        btnRestart.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                restartGame();
                paused = false;
                pausePanel.setVisible(false);
                btnPause.setVisible(true);
            }
        });

//        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            Button btnLeft = new TextButton("<", skin, "simpleSkin");
            Button btnRight = new TextButton(">", skin, "simpleSkin");
            Button btnUp = new TextButton("^", skin, "simpleSkin");
            Button btnDown = new TextButton("v", skin, "simpleSkin");
            btnLeft.setPosition(40, 120);
            btnRight.setPosition(240, 120);
            btnUp.setPosition(140, 220);
            btnDown.setPosition(140, 20);
            stage.addActor(btnLeft);
            stage.addActor(btnRight);
            stage.addActor(btnUp);
            stage.addActor(btnDown);
            btnLeft.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    pacMan.setPrefferedDirection(Unit.Direction.LEFT);
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    pacMan.setPrefferedDirection(Unit.Direction.NONE);
                }
            });
            btnRight.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    pacMan.setPrefferedDirection(Unit.Direction.RIGHT);
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    pacMan.setPrefferedDirection(Unit.Direction.NONE);
                }
            });
            btnUp.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    pacMan.setPrefferedDirection(Unit.Direction.UP);
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    pacMan.setPrefferedDirection(Unit.Direction.NONE);
                }
            });
            btnDown.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    pacMan.setPrefferedDirection(Unit.Direction.DOWN);
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    pacMan.setPrefferedDirection(Unit.Direction.NONE);
                }
            });
//        }
    }

    public void restartGame() {
        gameMap.loadMap("map.dat");
        pacMan.restart(true);
        for (int i = 0; i < monsters.length; i++) {
            monsters[i].restart(true);
        }
        resetHuntTimer();
    }

    public void levelUp() {
        gameMap.setLevel(gameMap.getLevel() + 1);
        gameMap.loadMap("map.dat");
        pacMan.restart(false);
        for (int i = 0; i < monsters.length; i++) {
            monsters[i].restart(false);
        }
        resetHuntTimer();
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        gameMap.render(batch);
        pacMan.render(batch);
        for (int i = 0; i < monsters.length; i++) {
            monsters[i].render(batch);
        }
        resetCamera();
        batch.setProjectionMatrix(camera.combined);
        pacMan.renderGUI(batch, font48);
        if (paused) {
            font48.draw(batch, "PAUSED", 0, 400, ScreenManager.WORLD_WIDTH, 1, false);
        }
        batch.end();
        stage.draw();
    }

    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            new GameSession(pacMan, gameMap, monsters).saveSession();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            GameSession gs = new GameSession();
            gs.loadSession();
            this.pacMan = gs.getPacMan();
            this.gameMap = gs.getGameMap();
            this.monsters = gs.getMonsters();
            this.pacMan.loadResources(this);
            this.gameMap.loadResources();
            for (int i = 0; i < this.monsters.length; i++) {
                this.monsters[i].loadResources(this);
            }
        }

        if (!paused) {
            pacMan.update(dt);
            for (int i = 0; i < monsters.length; i++) {
                monsters[i].update(dt);
            }
            checkCollisions();
            if (checkHuntTimer()) {
                huntTimer -= dt;
            }
            if (gameMap.getFoodCount() == 0) {
                levelUp();
            }
        }
        cameraTrackPacMan();
        stage.act(dt);
    }

    public void checkCollisions() {
        for (int i = 0; i < monsters.length; i++) {
            if (Vector2.dst(pacMan.getPosition().x + 0.5f, pacMan.getPosition().y + 0.5f, monsters[i].getPosition().x + 0.5f, monsters[i].getPosition().y + 0.5f) < 0.5f) {
                if (!pacMan.checkSafe() && !checkHuntTimer()) {
                    pacMan.resetPosition();
                    pacMan.minusLife();
                    if (pacMan.getLives() < 0) {
                        ScreenManager.getInstance().transferPacmanToGameOverScreen(pacMan);
                        ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.GAMEOVER);
                    }
                }
                if (checkHuntTimer()) {
                    pacMan.addScore(200);
                    monsters[i].resetPosition();
                }
            }
        }
    }

    public void cameraTrackPacMan() {
        camera.position.set(pacMan.getPosition().x * 80 + 40, pacMan.getPosition().y * 80 + 40, 0);
        if (camera.position.x < ScreenManager.WORLD_WIDTH / 2) {
            camera.position.x = ScreenManager.WORLD_WIDTH / 2;
        }
        if (camera.position.y < ScreenManager.WORLD_HEIGHT / 2) {
            camera.position.y = ScreenManager.WORLD_HEIGHT / 2;
        }
        if (camera.position.x > gameMap.getMapSizeX() * GameMap.CELL_SIZE_PX - ScreenManager.WORLD_WIDTH / 2) {
            camera.position.x = gameMap.getMapSizeX() * GameMap.CELL_SIZE_PX - ScreenManager.WORLD_WIDTH / 2;
        }
        if (camera.position.y > gameMap.getMapSizeY() * GameMap.CELL_SIZE_PX - ScreenManager.WORLD_HEIGHT / 2) {
            camera.position.y = gameMap.getMapSizeY() * GameMap.CELL_SIZE_PX - ScreenManager.WORLD_HEIGHT / 2;
        }
        camera.update();
    }

    public void resetCamera() {
        camera.position.set(ScreenManager.WORLD_WIDTH / 2, ScreenManager.WORLD_HEIGHT / 2, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        ScreenManager.getInstance().resize(width, height);
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
