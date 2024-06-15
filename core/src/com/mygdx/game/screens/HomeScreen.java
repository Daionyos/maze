package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.GameMain;

public class HomeScreen implements Screen {
    private Stage stage;
    private Table table;
    private Skin skin;
    private FontGenerator generator;
    private GameMain game;
    private float uiScale;

    public HomeScreen(GameMain game) {
        this.game = game;
        generator = new FontGenerator();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        uiScale = calculateUIScale();
        generateFont();

        setupUI();
    }

    private void generateFont(){
        BitmapFont font = generator.createDynamicFont(14 * uiScale);
        skin = new Skin(Gdx.files.internal("skins/skin1/uiskin.json"));
        skin.add("default-font", font, BitmapFont.class);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = skin.getDrawable("button-orange");
        buttonStyle.down = skin.getDrawable("button-orange-down");
        buttonStyle.font = font;
        skin.add("default", buttonStyle);
    }

    private void setupUI() {
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        createButtons();
    }

    private void createButtons() {
        TextButton startGameButton = new TextButton("Start Game", skin);
        TextButton inputButton = new TextButton("Inputs", skin);
        TextButton howToButton = new TextButton("Guide", skin);

        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.startGame();
            }
        });

        inputButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new InputScreen(game));
            }
        });

        howToButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new HowToScreen());
            }
        });

        float buttonWidth = 200 * uiScale;
        float buttonHeight = 50 * uiScale;
        float padSize = 20 * uiScale;

        table.center();
        table.add(startGameButton).width(buttonWidth).height(buttonHeight).pad(padSize).row();
        table.add(inputButton).width(buttonWidth).height(buttonHeight).pad(padSize).row();
        table.add(howToButton).width(buttonWidth).height(buttonHeight).pad(padSize).row();
    }

    private float calculateUIScale() {
        float referenceWidth = 550;
        float referenceHeight = 400; // Example reference height, adjust as necessary
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float scaleWidth = screenWidth / referenceWidth;
        float scaleHeight = screenHeight / referenceHeight;
        return Math.min(scaleWidth, scaleHeight); // Use the smaller scale factor to ensure everything fits
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.2f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        uiScale = calculateUIScale(); // Recalculate the UI scale based on new size
        table.clear(); // Clear existing UI elements
        generateFont();
        createButtons(); // Recreate UI elements with new scale
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
        stage.dispose();
        skin.dispose();
    }

}
