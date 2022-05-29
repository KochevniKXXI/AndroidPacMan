package ru.nomad.pacman;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import ru.nomad.pacman.units.PacMan;

public class GameOverScreen implements Screen {
    private PacMan pacMan;
    private StringBuilder statisticBuilder;
    private StringBuilder highScoreBuilder;
    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;
    private Music music;
    private BitmapFont font32;
    private BitmapFont font96;

    public void setPacMan(PacMan pacMan) {
        this.pacMan = pacMan;
        this.statisticBuilder.setLength(0);
        this.statisticBuilder.append("Score: ").append(pacMan.getScore()).append("\n")
                .append("Food eated: ").append(pacMan.getFoodEaten()).append("\n");
    }

    public void setupResults() {
        HighScoreSystem.loadResults();
        highScoreBuilder.setLength(0);
        highScoreBuilder.append("Top Scores:").append("\n");
        for (int i = 0; i < 10; i++) {
            highScoreBuilder.append(HighScoreSystem.getNames()[i]).append(" ").append(HighScoreSystem.getScores()[i]).append("\n");
        }
    }

    public GameOverScreen(SpriteBatch batch) {
        this.batch = batch;
        this.statisticBuilder = new StringBuilder(200);
        this.highScoreBuilder = new StringBuilder(200);
    }

    @Override
    public void show() {
        font32 = Assets.getInstance().getAssetManager().get("zorque32.ttf", BitmapFont.class);
        font96 = Assets.getInstance().getAssetManager().get("zorque96.ttf", BitmapFont.class);
        music = Gdx.audio.newMusic(Gdx.files.internal("Jumping bat.wav"));
//        music.setLooping(true);
//        music.play();
        createGUI();
        setupResults();
        Assets.getInstance().playMusic();
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0.4f, 0.4f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font96.draw(batch, "Game Over", 0, 600, ScreenManager.WORLD_WIDTH, 1, false);
        font32.draw(batch, statisticBuilder, 0, 480, ScreenManager.WORLD_WIDTH / 2, 1, false);
        font32.draw(batch, highScoreBuilder, ScreenManager.WORLD_WIDTH / 2, 480, ScreenManager.WORLD_WIDTH / 2, 1, false);
        batch.end();
        stage.draw();
    }

    public void update(float dt) {
        stage.act(dt);
    }

    public void createGUI() {
        stage = new Stage(ScreenManager.getInstance().getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin();
        skin.addRegions(Assets.getInstance().getAtlas());
        skin.add("font32", font32);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.getDrawable("simpleButton");
        textButtonStyle.font = font32;
        skin.add("simpleSkin", textButtonStyle);

        TextButton.TextButtonStyle shortTextButtonStyle = new TextButton.TextButtonStyle();
        shortTextButtonStyle.up = skin.getDrawable("shortButton");
        shortTextButtonStyle.font = font32;
        skin.add("shortButtonStyle", shortTextButtonStyle);

        skin.add("nameField", new Texture("nameField.bmp"));
        skin.add("cursor", new Texture("cursor.bmp"));
        TextField.TextFieldStyle tfs = new TextField.TextFieldStyle();
        tfs.font = font32;
        tfs.background = skin.getDrawable("nameField");
        tfs.fontColor = Color.WHITE;
        tfs.cursor = skin.getDrawable("cursor");
        skin.add("textFieldStyle", tfs);

        final TextField field = new TextField("Player", skin, "textFieldStyle");
        field.setWidth(400);
        field.setPosition((ScreenManager.WORLD_WIDTH / 2) - (400 / 2) - 200, 280);

        Button btnRestartGame = new TextButton("Restart Game", skin, "simpleSkin");
        Button btnReturnToMenu = new TextButton("Return To Menu", skin, "simpleSkin");
        final Button btnSaveResults = new TextButton("OK", skin, "shortButtonStyle");
        btnRestartGame.setPosition((ScreenManager.WORLD_WIDTH / 2) - 160, 160);
        btnReturnToMenu.setPosition((ScreenManager.WORLD_WIDTH / 2) - 160, 40);
        btnSaveResults.setPosition((ScreenManager.WORLD_WIDTH / 2) + (400 / 2) + 40 - 200, 260);
        stage.addActor(btnRestartGame);
        stage.addActor(btnReturnToMenu);
        stage.addActor(field);
        stage.addActor(btnSaveResults);
        if (!HighScoreSystem.checkScore(pacMan.getScore())) {
            field.setVisible(false);
            btnSaveResults.setVisible(false);
        }
        btnRestartGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.GAME);
            }
        });
        btnReturnToMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.MENU);
            }
        });
        btnSaveResults.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                HighScoreSystem.addResult(field.getText(), pacMan.getScore());
                btnSaveResults.setVisible(false);
                field.setVisible(false);
                setupResults();
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        ScreenManager.getInstance().resize(width, height);
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

    }
}
