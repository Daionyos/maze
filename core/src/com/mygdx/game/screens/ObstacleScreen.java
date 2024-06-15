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
import com.mygdx.game.Obstacle;

import java.util.ArrayList;

public class ObstacleScreen implements Screen {
    private Stage stage;
    private Table table;
    private Skin skin;
    private FontGenerator generator;
    private GameMain game;
    private float uiScale;
    private int numObstacles;

    private ArrayList<Obstacle> obstacles;
    private ArrayList<TextField> objTextFields;

    private String heightFunction;
    private String greenKinFrictionCoef;
    private String greenStatFrictionCoef;
    private String ballX;
    private String ballY;
    private String holeX;
    private String holeY;
    private String holeRad;

    public ObstacleScreen(GameMain game, int numObstacles, String heightFunction, String greenKinFrictionCoef, String reenStatFrictionCoef, String ballX, String ballY, String holeX, String holeY, String holeRad) {
        this.game = game;
        generator = new FontGenerator();
        this.numObstacles = numObstacles;
        if (heightFunction != null) {

            this.heightFunction = heightFunction;
            this.greenKinFrictionCoef = greenKinFrictionCoef;
            this.greenStatFrictionCoef = greenStatFrictionCoef;
            this.ballX = ballX;
            this.ballY = ballY;
            this.holeX = holeX;
            this.holeY = holeY;
            this.holeRad = holeRad;
        }

        this.heightFunction = "";
        this.greenKinFrictionCoef = "";
        this.greenStatFrictionCoef = "";
        this.ballX = "";
        this.ballY = "";
        this.holeX = "";
        this.holeY = "";
        this.holeRad = "";


        obstacles = new ArrayList<>();
        objTextFields = new ArrayList<>();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("skins/skin1/uiskin.json"));

        for (int i = 0; i < 3*numObstacles; i++) {
            objTextFields.add(new TextField("", skin));
        }

        uiScale = calculateUIScale();
        generateFont();

        setupUI();
    }

    private void setupUI() {
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        createInputFields();
    }

    private void generateFont(){
        BitmapFont font = generator.createDynamicFont(14 * uiScale);

        skin.add("default-font", font, BitmapFont.class);

        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = font;
        skin.add("default", labelStyle, Label.LabelStyle.class);

        TextField.TextFieldStyle textFieldStyle = skin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font = font;
        skin.add("default", textFieldStyle, TextField.TextFieldStyle.class);

        TextButton.TextButtonStyle buttonStyle = skin.get(TextButton.TextButtonStyle.class);
        buttonStyle.font = font;
        skin.add("default", buttonStyle, TextButton.TextButtonStyle.class);
    }


    private void createInputFields() {
        float baseWidth = 120 * uiScale; // Base width for all elements, scaled
        float baseHeight = 30 * uiScale; // Base height for buttons and text fields
        float padSize = 5 * uiScale;

        // Configure widths and heights with scaling
        float labelWidth = baseWidth;
        float fieldWidth = baseWidth;
        float fieldHeight = baseHeight; // Ensure text fields have a height factor
        float buttonWidth = baseWidth;
        float buttonHeight = baseHeight; // Ensure buttons don't just elongate

        // Clear previous contents of the table
        table.clear();

        // Obstacle Details Input Field
        table.add(new Label("Obstacle (x, y, radius):", skin)).width(labelWidth).pad(padSize).colspan(3);
        for (int i = 0; i < 3*numObstacles; i = i+3) {
            table.row();
            table.add(objTextFields.get(i)).width(fieldWidth).height(fieldHeight).pad(padSize);
            table.add(objTextFields.get(i+1)).width(fieldWidth).height(fieldHeight).pad(padSize);
            table.add(objTextFields.get(i+2)).width(fieldWidth).height(fieldHeight).pad(padSize);
            table.row();
        }

        // Submit and Back Buttons
        TextButton submitButton = new TextButton("Submit", skin);
        submitButton.setSize(buttonWidth, buttonHeight);
        submitButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                // Handle submission
            }
        });
        TextButton backButton = new TextButton("Back", skin);
        backButton.setSize(buttonWidth, buttonHeight);
        backButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                if (!objTextFields.get(0).getText().equals("")) {
                    for (int i = 0; i < 3 * numObstacles; i = i + 3) {
                        obstacles.add(new Obstacle(Float.parseFloat(objTextFields.get(i).getText()), Float.parseFloat(objTextFields.get(i + 1).getText()), Float.parseFloat(objTextFields.get(i + 2).getText())));
                    }
                }
                game.setScreen(new InputScreen(game, heightFunction, greenKinFrictionCoef, greenStatFrictionCoef, ballX, ballY, holeX, holeY, holeRad, obstacles)); // Ensure you navigate back appropriately
            }
        });
        table.add(submitButton).width(buttonWidth).height(buttonHeight).pad(padSize).fillX();
        table.add(backButton).width(buttonWidth).height(buttonHeight).pad(padSize).fillX();
        table.row();
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
        createInputFields(); // Recreate UI elements with new scale

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
