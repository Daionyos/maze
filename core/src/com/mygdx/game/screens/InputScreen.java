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
import com.mygdx.game.physics.PhysicsSettings;

import java.util.ArrayList;

public class InputScreen implements Screen {
    private Stage stage;
    private Table table;
    private Skin skin;
    private FontGenerator generator;
    private GameMain game;
    private float uiScale;

    //inputfields
    private TextField heightFunctionField;
    private TextField greenKinFriction;
    private TextField greenStatFriction;
    private TextField ballStartPosXField;
    private TextField ballStartPosYField;
    private TextField holePosXField;
    private TextField holePosYField;
    private TextField holeRadiusField;
    private TextField obstaclesField;
    private TextArea mazeField;  // Added TextArea for maze input

    //inputs
    private String heightFunction = "";
    private String greenKinFrictionCoef = "";
    private String greenStatFrictionCoef = "";
    private String ballX = "";
    private String ballY = "";
    private String holeX = "";
    private String holeY = "";
    private String holeRad = "";
    private String mazeString = "";  // Added mazeString

    private ArrayList<Obstacle> obstacleList;

    public InputScreen(GameMain game) {
        this.game = game;
        generator = new FontGenerator();
    }

    public InputScreen(GameMain game, String heightFunction, String greenKinFrictionCoef, String greenStatFrictionCoef, String ballX, String ballY, String holeX, String holeY, String holeRad, ArrayList<Obstacle> obstacleList) {
        this.game = game;
        generator = new FontGenerator();
        this.heightFunction = heightFunction;
        this.greenKinFrictionCoef = greenKinFrictionCoef;
        this.greenStatFrictionCoef = greenStatFrictionCoef;
        this.ballX = ballX;
        this.ballY = ballY;
        this.holeX = holeX;
        this.holeY = holeY;
        this.holeRad = holeRad;
        this.obstacleList = obstacleList;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

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

        skin = new Skin(Gdx.files.internal("skins/skin1/uiskin.json"));
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

        // Height Function
        table.add(new Label("Height function:", skin)).width(labelWidth).pad(padSize);
        heightFunctionField = new TextField(heightFunction, skin);
        heightFunctionField.setSize(2 * fieldWidth + padSize, fieldHeight);
        table.add(heightFunctionField).width(2 * fieldWidth + padSize).height(fieldHeight).pad(padSize).colspan(2);
        table.row();

        table.add(new Label("Green Friction (kin, stat):", skin)).width(labelWidth).pad(padSize);
        greenKinFriction = new TextField(greenKinFrictionCoef, skin);
        greenStatFriction = new TextField(greenStatFrictionCoef, skin);
        table.add(greenKinFriction).width(fieldWidth).height(fieldHeight).pad(padSize);
        table.add(greenStatFriction).width(fieldWidth).height(fieldHeight).pad(padSize);
        table.row();

        // Ball Start Position
        ballStartPosXField = new TextField(ballX, skin);
        ballStartPosYField = new TextField(ballY, skin);
        table.add(new Label("Ball start Pos (x, y):", skin)).width(labelWidth).pad(padSize);
        table.add(ballStartPosXField).width(fieldWidth).height(fieldHeight).pad(padSize);
        table.add(ballStartPosYField).width(fieldWidth).height(fieldHeight).pad(padSize);
        table.row();

        // Hole Position and Radius
        holePosXField = new TextField(holeX, skin);
        holePosYField = new TextField(holeY, skin);
        holeRadiusField = new TextField(holeRad, skin);
        table.add(new Label("Hole Pos (x, y, rad):", skin)).width(labelWidth).pad(padSize);
        table.add(holePosXField).width(fieldWidth).height(fieldHeight).pad(padSize);
        table.add(holePosYField).width(fieldWidth).height(fieldHeight).pad(padSize);
        table.add(holeRadiusField).width(fieldWidth).height(fieldHeight).pad(padSize);
        table.row();

        // Maze Input Field
        table.add(new Label("Maze (# = wall, . = empty):", skin)).width(labelWidth).pad(padSize);
        mazeField = new TextArea(mazeString, skin);
        mazeField.setSize(2 * fieldWidth + padSize, 4 * fieldHeight);  // Adjust height to fit multiple lines
        table.add(mazeField).width(2 * fieldWidth + padSize).height(4 * fieldHeight).pad(padSize).colspan(2);
        table.row();

        // Number of Obstacles
        table.add(new Label("N. obstacles:", skin)).width(labelWidth).pad(padSize);
        obstaclesField = new TextField("", skin);
        table.add(obstaclesField).width(fieldWidth).height(fieldHeight).pad(padSize);

        TextButton setObstaclesButton = new TextButton("Set obstacle info", skin);
        setObstaclesButton.setSize(buttonWidth, buttonHeight);
        setObstaclesButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                heightFunction = heightFunctionField.getText();
                greenKinFrictionCoef = greenKinFriction.getText();
                greenStatFrictionCoef = greenStatFriction.getText();
                ballX = ballStartPosXField.getText();
                ballY = ballStartPosYField.getText();
                holeX = holePosXField.getText();
                holeY = holePosYField.getText();
                holeRad = holeRadiusField.getText();
                mazeString = mazeField.getText();  // Capture maze input
                game.setScreen(new ObstacleScreen(game, Integer.parseInt(obstaclesField.getText()), heightFunction, greenKinFrictionCoef, greenStatFrictionCoef, ballX, ballY, holeX, holeY, holeRad));
            }
        });
        table.add(setObstaclesButton).width(buttonWidth).height(buttonHeight).pad(padSize);
        table.row();

        // Submit and Back Buttons
        TextButton submitButton = new TextButton("Submit", skin);
        submitButton.setSize(buttonWidth, buttonHeight);
        submitButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                heightFunction = heightFunctionField.getText();
                greenKinFrictionCoef = greenKinFriction.getText();
                greenStatFrictionCoef = greenStatFriction.getText();
                ballX = ballStartPosXField.getText();
                ballY = ballStartPosYField.getText();
                holeX = holePosXField.getText();
                holeY = holePosYField.getText();
                holeRad = holeRadiusField.getText();
                mazeString = mazeField.getText();  // Capture maze input
                processInput();
            }
        });
        TextButton backButton = new TextButton("Back", skin);
        backButton.setSize(buttonWidth, buttonHeight);
        backButton.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                game.setScreen(new HomeScreen(game));
            }
        });
        table.add(submitButton).width(buttonWidth).height(buttonHeight).pad(padSize).fillX();
        table.add(backButton).width(buttonWidth).height(buttonHeight).pad(padSize).fillX();
        table.row();
    }

    private void processInput() {
        PhysicsSettings.KINETIC_FRIC_GRASS = Float.parseFloat(greenKinFrictionCoef);
        PhysicsSettings.STATIC_FRIC_GRASS = Float.parseFloat(greenStatFrictionCoef);
        game.getInput(heightFunction, Float.parseFloat(ballX), Float.parseFloat(ballY), Float.parseFloat(holeX), Float.parseFloat(holeY), Float.parseFloat(holeRad), obstacleList, mazeString);  // Pass mazeString to game input
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
