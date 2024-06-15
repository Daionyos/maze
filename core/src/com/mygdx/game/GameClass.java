package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.bot.AI_bot;
import com.mygdx.game.bot.calculateDistance;
import com.mygdx.game.bot.SimpleBot;
import com.mygdx.game.enums.CameraMode;
import com.mygdx.game.physics.NumericalMethods.Derivative;
import com.mygdx.game.physics.PhysicsEngine;
import com.mygdx.game.physics.parser.Parser;
import com.mygdx.game.shaders.CustomShaderProvider;
import com.mygdx.game.terrains.Terrain;
import com.mygdx.game.terrains.TerrainHeight;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameClass extends ApplicationAdapter implements InputProcessor {
	private SceneManager sceneManager;
	//private SceneAsset sceneAsset;
	private Scene playerScene;
	private PerspectiveCamera camera;
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private Texture brdfLUT;
	private float time;
	private SceneSkybox skybox;
	private DirectionalLightEx light;
	private FirstPersonCameraController cameraController;
	private InputMultiplexer inputMultiplexer;
	private ModelBuilder modelBuilder;

	//player objects
	private Ball ball;

	// Camera
	private float cameraHeight = 20f;
	private CameraMode cameraMode = CameraMode.GOLF_MODE;
	private float camPitch = Settings.CAMERA_START_PITCH;
	private float distanceFromPlayer = 35f;
	private float angleAroundPlayer = 0f;
	private float angleBehindPlayer = 0f;
	private boolean topDownToggle = false;

	// Terrain
	private Terrain terrain;
	private Scene terrainScene;
	private String heightfunction = "0.4*(0.9-e^(-(x^2+y^2)/8))";

	// Obstacles
	private ArrayList<Obstacle> obstacles = new ArrayList<>();
	private Vector2 normalToObstacle;

	private boolean isStartingPosition;
	private boolean isMoving;
	private float startTime;
	private final Vector3 lastPosition = new Vector3();

	// Physics
	private PhysicsEngine physicsEngine;
	private Parser parser;
	private float velocity;
	private Vector2 velocityVector = new Vector2();
	private float size_half = Settings.SIZE/2;
	private Derivative derivative;

	// Shooting
	private Stage stage;
	private Skin skin;
	private Slider powerSlider;
	private Label sliderValueLabel;
	private int numberOfShots;

	//Display
	Label labelShots;
	Label labelPosition;
	Table table;
	Label labelDist;

	//ai bot
	private double[][] AI_velocity;
	private boolean AIBot = false;
	private Label labelStateBot;
	private BitmapFont font = new BitmapFont();

	//simple bot
	private double[][] pathSimpleBot;
	private boolean simpleBot = false;
	private boolean player = true;

	//player Movement
	float speed = 60f;
	float rotationSpeed = 80f;
	private Matrix4 playerTransform = new Matrix4();
	private Vector3 moveTranslation = new Vector3();
	private Vector3 currentPosition = new Vector3(-3/Settings.SIZE_RATIO + size_half, 0, size_half);
	private final Vector3 initialPosition = new Vector3();


	// flag/hole
	private float holeRadius = 0.15F/Settings.SIZE_RATIO;
	private final Vector3 holePosition = new Vector3(4/Settings.SIZE_RATIO + size_half, 0, 1/Settings.SIZE_RATIO + size_half);

	//string to create maze
	private String mazeString;
	private ArrayList<Wall> boxes;



	public GameClass(String heightfunction, float ballX, float ballY, float holeX, float holeY, float holeRad, ArrayList<Obstacle> obstacles,String mazeString ){

		boxes = new ArrayList<>();

		if (heightfunction != null) {
			this.heightfunction = heightfunction;

			currentPosition.x = ballX / Settings.SIZE_RATIO + size_half;
			currentPosition.z = ballY / Settings.SIZE_RATIO + size_half;

			holePosition.x = holeX / Settings.SIZE_RATIO + size_half;
			holePosition.z = holeY / Settings.SIZE_RATIO + size_half;

			this.holeRadius = holeRad / Settings.SIZE_RATIO;
			if (obstacles != null) {
				this.obstacles = obstacles;
			} else {
				this.obstacles = new ArrayList<>();
			}
			processMazeString(mazeString);
		}
	}
	private void processMazeString(String mazeString) {
		// Split the mazeString into lines
		String[] lines = mazeString.split("\n");

		// Determine the dimensions of the maze
		int height = lines.length;
		int width = lines[0].length(); // assuming all lines have the same length

		// Create a 2D array to represent the maze
			char[][] maze = new char[height][width];

		// Populate the maze array and detect walls ('#')
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				maze[i][j] = lines[i].charAt(j);
				if (maze[i][j] == '#') {
					// Add a Wall object at position (i, j)
					boxes.add(new Wall(j, i, 1)); // Assuming Wall constructor is (x, y, length)
				}
			}
		}
	}

	public GameClass(){}


	@Override
	public void create() {
		ball = new Ball("golfBall/golf_ball_lp.glb");
		playerScene = ball.getSceneBall();
		isStartingPosition = true;
		velocityVector.x = 0;
		velocityVector.y = 0;

//		obstacles.add(new Obstacle(2,3,0.3f));
//		obstacles.add(new Obstacle(7,7,0.3f));

		parser = new Parser(heightfunction);
		derivative = new Derivative(parser, heightfunction);
		physicsEngine = new PhysicsEngine(heightfunction, obstacles,boxes);

		currentPosition.y = getHeightGame(currentPosition.x, currentPosition.z) + Settings.BALL_RADIUS;

		isMoving = false;

		sceneManager = new SceneManager(new CustomShaderProvider(), PBRShaderProvider.createDefaultDepth(36));
		sceneManager.addScene(playerScene);
		setInitialPosition();

		// setup camera
		camera = new PerspectiveCamera(75f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 0.1f;
		camera.far = 5000f;
		sceneManager.setCamera(camera);
		camera.position.set(0,0, 0);

		cameraController = new FirstPersonCameraController(camera);
		cameraController.setVelocity(100f);

		inputMultiplexer = new InputMultiplexer(this, cameraController);
		//Gdx.input.setInputProcessor(inputMultiplexer);

		// setup light
		light = new DirectionalLightEx();
		light.direction.set(1, -3, 1).nor();
		light.color.set(Color.WHITE);
		sceneManager.environment.add(light);

		// setup quick IBL (image based lighting)
		IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
		environmentCubemap = iblBuilder.buildEnvMap(1024);
		diffuseCubemap = iblBuilder.buildIrradianceMap(256);
		specularCubemap = iblBuilder.buildRadianceMap(10);
		iblBuilder.dispose();

		// This texture is provided by the library, no need to have it in your assets.
		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

		sceneManager.setAmbientLight(1f);
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

		// setup skybox
		skybox = new SceneSkybox(environmentCubemap);
		sceneManager.setSkyBox(skybox);
		modelBuilder = new ModelBuilder();

		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.internal("skins/skin1/uiskin.json"));

		inputMultiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(inputMultiplexer);
		table = new Table();

		holeBuilder();
		createTerrain();
		ObstaclesBuilder();
		WallsBuilder();
		setupLabels();
		setupSlider();
		setupTopViewButton();
		ObstaclesBuilder();
		WallsBuilder();
		setupAIBotButton();
		setupSimpleBotButton();
		setupNewPlayButton();

	}

	private void createTerrain() {
		if (terrain != null) {
			terrain.dispose();
			sceneManager.removeScene(terrainScene);
		}

		terrain = new TerrainHeight(parser, heightfunction);
		terrainScene = new Scene(terrain.getModelInstance());
		sceneManager.addScene(terrainScene);
		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
	}

	private void setInitialPosition(){
		initialPosition.set(currentPosition);
		playerScene.modelInstance.transform.setTranslation(currentPosition);
	}


	private void holeBuilder() {
		modelBuilder.begin();

		Material material = new Material();
		material.set(PBRColorAttribute.createBaseColorFactor(new Color(1, 0, 0, 0.5f))); // Red color with 50% transparency
		material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)); // Enable blending for transparency

		// Define the vertex attributes for the cylinder: position and normal are necessary
		int usageCode = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

		// Create a cylinder part
		MeshPartBuilder builder = modelBuilder.part("cylinder", GL20.GL_TRIANGLES, usageCode, material);

		// Build the cylinder with specified dimensions and properties
		float diameter = 2*holeRadius; // Diameter of the cylinder
		float height =2000f;   // Height of the cylinder
		int divisions = 16;  // Number of divisions around the cylinder

		// CylinderShapeBuilder requires diameter, height, and divisions
		CylinderShapeBuilder.build(builder, diameter, height, diameter, divisions);

		holePosition.y = getHeightGame(holePosition.x, holePosition.z);

		ModelInstance model  = new ModelInstance(modelBuilder.end());
		model.transform.setToTranslation(holePosition);
		sceneManager.addScene(new Scene(model));
	}

	private void ObstaclesBuilder(){
		if (obstacles.isEmpty()){
			return;
		} else {
			for (Obstacle obstacle : obstacles) {
				float height = getHeightFromReal(obstacle.x, obstacle.y);
				obstacle.setHeight(height);
				if (height < 0) continue;
				sceneManager.addScene(new Scene(obstacle.buildObstacle()));
			}
		}
	}
	private void WallsBuilder(){
		if (boxes.isEmpty()){
			System.out.println("NO WALLS");
			return;
		} else {
			for (Wall wall : boxes) {
				sceneManager.addScene(new Scene(wall.buildWall()));
			}
		}
	}
	private void setupSlider() {
		powerSlider = new Slider(0, 5f, 0.1f, false, skin);
		powerSlider.setValue(0); // Set a default value
		powerSlider.setTouchable(Touchable.enabled);

		velocity = powerSlider.getValue();
		sliderValueLabel = new Label(String.format("%.1f", powerSlider.getValue()) + " m/s", skin);
		sliderValueLabel.setColor(1, 2, 1, 1);

		powerSlider.getStyle().knob.setMinHeight(20);
		powerSlider.getStyle().knob.setMinWidth(20);
		powerSlider.getStyle().background.setMinHeight(5);

		sliderValueLabel.setFontScale(1.5f);

		table.setFillParent(true);
		table.add(labelStateBot).padTop(100);
		labelStateBot.setVisible(false);
		table.row();
		table.add(powerSlider).width(300).padTop(600);
		table.row();
		table.add(sliderValueLabel).padTop(10);



		stage.addActor(table);


		powerSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				velocity = powerSlider.getValue();
				sliderValueLabel.setText(String.format("%.1f", powerSlider.getValue())+ " m/s");

			}
		});
	}

	public void setupLabels(){
		Label.LabelStyle labelStyle = new Label.LabelStyle();
		labelStyle.font = font;
		String position = "Position: (" + currentPosition.x+ ", " + currentPosition.z + ")";
		labelPosition = new Label(position, labelStyle);
		labelPosition.setPosition(50, 100);
		labelShots = new Label("Number of shots: " + numberOfShots, labelStyle);
		labelShots.setPosition(50, 150);
		labelStateBot = new Label("Searching for best path...", labelStyle);
		calculateDistance calc = new calculateDistance((currentPosition.x- size_half)*Settings.SIZE_RATIO , (currentPosition.z- size_half)*Settings.SIZE_RATIO , (holePosition.x- size_half)*Settings.SIZE_RATIO , (holePosition.z- size_half)*Settings.SIZE_RATIO );
		labelDist = new Label("Distance from hole: " + Math.round(calc.distance()*100.0)/100.0, labelStyle);
		labelDist.setPosition(50, 200);

		stage.addActor(labelDist);
		stage.addActor(labelStateBot);
		stage.addActor(labelPosition);
		stage.addActor(labelShots);
	}

	public void updateLabels(){
		labelPosition.setText("Position: (" + (currentPosition.x-size_half)*Settings.SIZE_RATIO + ", " + (currentPosition.z-size_half)*Settings.SIZE_RATIO + ")");
		labelShots.setText("Number of shots: " + numberOfShots);
		calculateDistance calc = new calculateDistance((currentPosition.x- size_half)*Settings.SIZE_RATIO , (currentPosition.z- size_half)*Settings.SIZE_RATIO , (holePosition.x- size_half)*Settings.SIZE_RATIO , (holePosition.z- size_half)*Settings.SIZE_RATIO );
		labelDist.setText("Distance from hole: " + Math.round(calc.distance()*100.0)/100.0);
	}

	public void setupTopViewButton(){
		TextButton topViewButton = new TextButton("  Top View  ", skin);
		topViewButton.setPosition(20, Gdx.graphics.getHeight() - 50);
		topViewButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
                updateLabelView(topViewButton);
                if (topDownToggle == false){
					cameraMode = CameraMode.TOP_DOWN_GAME;
					topDownToggle = true;
					powerSlider.setVisible(false);
					sliderValueLabel.setVisible(false);
				} else {
					cameraMode = CameraMode.GOLF_MODE;
					topDownToggle = false;
					powerSlider.setVisible(true);
					sliderValueLabel.setVisible(true);
				}
			}
		});

		stage.addActor(topViewButton);
	}

	public void updateLabelView(TextButton button){
		if(cameraMode != CameraMode.TOP_DOWN_GAME){
			button.setText("Normal view");
		} else{
			button.setText("  Top view  ");
		}
	}

	public void setupNewPlayButton(){
		TextButton newPlayButton = new TextButton("New Play", skin);
		newPlayButton.setPosition(20, Gdx.graphics.getHeight() - 100);
		newPlayButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				labelStateBot.setVisible(false);
				currentPosition.set(initialPosition);
				playerScene.modelInstance.transform.setTranslation(currentPosition);
				numberOfShots = 0;
				simpleBot = false;
				AIBot = false;
				player = true;
				powerSlider.setVisible(true);
				sliderValueLabel.setText(String.format("%.1f", powerSlider.getValue()) + " m/s");

			}
		});

		stage.addActor(newPlayButton);
	}

	public void setupAIBotButton(){
		TextButton AiBotButton = new TextButton("AI Bot", skin); // 'skin' is your Skin instance
		AiBotButton.setPosition(20, Gdx.graphics.getHeight() - 200); // Example position
		AiBotButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				labelStateBot.setVisible(false);
				labelStateBot.setText("Searching for best path...");
				labelStateBot.setVisible(true);
				powerSlider.setVisible(false);
                currentPosition.set(initialPosition);
                playerScene.modelInstance.transform.setTranslation(currentPosition);
				numberOfShots = 0;
				simpleBot = false;
				player = false;
				AIBot = true;

				AI_bot bot = new AI_bot(heightfunction,(currentPosition.x- size_half)*Settings.SIZE_RATIO , (currentPosition.z- size_half)*Settings.SIZE_RATIO, (holePosition.x- size_half)*Settings.SIZE_RATIO , (holePosition.z- size_half) * Settings.SIZE_RATIO , holeRadius*Settings.SIZE_RATIO);
				AI_velocity = bot.findBestShot();
				String string = "AI Bot: path found with " + (int) AI_velocity[0][2] + " shot";
				if(AI_velocity[0][2] >= 2) {
					string += "s";
				}
				labelStateBot.setText(string);

			}
		});
		stage.addActor(AiBotButton);
	}

	public void setupSimpleBotButton(){
		TextButton simpleBotButton = new TextButton("Simple Bot", skin); // 'skin' is your Skin instance
		simpleBotButton.setPosition(20, Gdx.graphics.getHeight() - 150); // Example position
		simpleBotButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				labelStateBot.setVisible(false);
				labelStateBot.setText("Searching for best path...");
				labelStateBot.setVisible(true);
				powerSlider.setVisible(false);
                currentPosition.set(initialPosition);
                playerScene.modelInstance.transform.setTranslation(currentPosition);
				numberOfShots=0;
				AIBot = false;
				player = false;
				simpleBot = true;
				SimpleBot bot = new SimpleBot(heightfunction, (currentPosition.x- size_half)*Settings.SIZE_RATIO , (currentPosition.z- size_half)*Settings.SIZE_RATIO, (holePosition.x- size_half)*Settings.SIZE_RATIO , (holePosition.z- size_half) * Settings.SIZE_RATIO , holeRadius*Settings.SIZE_RATIO);
				pathSimpleBot = bot.getVelocityVectors();
				bot.printPath(bot.getPath());
				String string = "Simple Bot: path found with " + (int) pathSimpleBot.length + " shot";
				if(pathSimpleBot.length >= 2) {
					string += "s";
				}
				labelStateBot.setText(string);
			}
		});
		stage.addActor(simpleBotButton);
	}



	@Override
	public void resize(int width, int height) {
		sceneManager.updateViewport(width, height);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		time += deltaTime;
		processInput(deltaTime);
		updateCamera();
		updateLabels();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		sceneManager.update(deltaTime);
		sceneManager.render();
		stage.act(deltaTime);
		stage.draw();
	}

	private void updateCamera(){
		if(cameraMode == CameraMode.FLY_MODE){
			cameraController.update();
			return;
		}

		float horDistance = calculateHorizontalDistance(distanceFromPlayer);
		float vertDistance = calculateVerticalDistance(distanceFromPlayer);

		calculatePitch();
		calculateAngleAroundPlayer();
		calculateCameraPosition(currentPosition, horDistance, vertDistance);


		if (cameraMode != CameraMode.TOP_DOWN_GAME) {
			camera.lookAt(currentPosition);
			camera.up.set(Vector3.Y);
		} else {
			camera.lookAt(size_half, 0, size_half);
			camera.up.set(0,1,0);
		}
		camera.update();
	}


	private void calculateCameraPosition(Vector3 currentPosition, float horDistance, float vertDistance){
		if (cameraMode != CameraMode.TOP_DOWN_GAME) {
			float offsetX = (float) (horDistance * Math.sin(Math.toRadians(angleAroundPlayer)));
			float offsetZ = (float) (horDistance * Math.cos(Math.toRadians(angleAroundPlayer)));

			camera.position.x = currentPosition.x - offsetX;
			camera.position.y = currentPosition.y + vertDistance;
			camera.position.z = currentPosition.z - offsetZ;

			if (cameraMode == CameraMode.GOLF_MODE) {
				float height = getHeightGame(camera.position.x, camera.position.z);
				camera.position.y = Math.max(height + 25f, currentPosition.y);
				if (isMoving) {
					Vector3 desiredPosition = calculateCameraMovingPosition(currentPosition, horDistance, camera.position.y+200);
					float lerpFactor = computeLerpFactor();
       				camera.position.lerp(desiredPosition, lerpFactor);
					updateFOV();
				}
			}
		} else {
			camera.position.set(size_half-1,  700f, size_half);
		}
	}


	private Vector3 calculateCameraMovingPosition(Vector3 currentPosition, float horDistance, float vertDistance) {
		Vector3 position = new Vector3();
		float theta = (float) Math.toRadians(angleAroundPlayer);

		position.x = (float) (currentPosition.x - horDistance * Math.sin(theta));
		position.z = (float) (currentPosition.z - horDistance * Math.cos(theta));
		position.y = currentPosition.y + vertDistance;  // Adjust the height based on the pitch and current action
		return position;
	}

	private float computeLerpFactor() {
		// Lerp factor could be a function of the velocity of the ball to make camera movements smoother
		float velocity = velocityVector.len(); // Assume velocityVector is the velocity of the ball
		float lerpBase = 0.1f;  // Base lerp factor
		float velocityFactor = 0.05f;  // How much the ball's speed affects the lerp factor
		return Math.min(1.0f, lerpBase + (velocityFactor * velocity));
	}

	private void updateFOV() {
		float baseFOV = 70; // Default FOV
		float speed = velocityVector.len();
		camera.fieldOfView = baseFOV + speed * 0.1f; // Dynamically adjust FOV based on speed
	}

	private void calculateAngleAroundPlayer(){
		if (cameraMode == CameraMode.FREE_LOOK || cameraMode == CameraMode.GOLF_MODE){
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
				float angleChange = Gdx.input.getDeltaX()/5;
				angleAroundPlayer -= angleChange;
			}
		} else {
			angleAroundPlayer = angleBehindPlayer;
		}
	}

	private void calculatePitch(){
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && cameraMode != CameraMode.GOLF_MODE) {
			float pitchChange = -Gdx.input.getDeltaY() * Settings.CAM_PITCH_FACTOR;
			camPitch -= pitchChange;
		}
		if(camPitch < Settings.CAMERA_MIN_PITCH){
			camPitch = Settings.CAMERA_MIN_PITCH;
		} else if (camPitch > Settings.CAMERA_MAX_PITCH){
			camPitch = Settings.CAMERA_MAX_PITCH;
		}
	}

	private float calculateHorizontalDistance(float distanceFromPlayer){
		return (float) (distanceFromPlayer * Math.cos(Math.toRadians(camPitch)));
	}

	private float calculateVerticalDistance(float distanceFromPlayer){
		return (float) (distanceFromPlayer * Math.sin(Math.toRadians(camPitch)));
	}

	private float getHeightGame(float x, float y){
		float converted_x = (x - Settings.SIZE/2) * Settings.SIZE_RATIO;
		float converted_y = (y - Settings.SIZE/2) * Settings.SIZE_RATIO;
		Map<String, Double> map = new HashMap<>();
		map.put("x", (double) converted_x);
		map.put("y", (double) converted_y);
		float evaluated = (float) parser.evaluateAt(map, heightfunction);
		return evaluated / Settings.SIZE_RATIO;
	}

	private float getHeightFromReal(float x ,float y){
		Map<String, Double> map = new HashMap<>();
		map.put("x", (double) x);
		map.put("y", (double) y);
		float evaluated = (float) parser.evaluateAt(map, heightfunction);
		return evaluated / Settings.SIZE_RATIO;
	}

	private void processInput(float deltaTime){

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			setLastPosition();
			isMoving = true;
			if(AIBot) {
				velocityVector.x = (float) AI_velocity[numberOfShots][0];
				velocityVector.y = (float) AI_velocity[numberOfShots][1];
				sliderValueLabel.setText("Velocity: " + Math.round(Math.sqrt( Math.pow(AI_velocity[0][numberOfShots], 2) + Math.pow(AI_velocity[1][numberOfShots], 2))*100.0)/100.0 + " m/s");
			} else if(simpleBot){
				velocityVector.x = (float) pathSimpleBot[numberOfShots][1];
				velocityVector.y = (float) pathSimpleBot[numberOfShots][0];
				sliderValueLabel.setText("Velocity: " + Math.round(Math.sqrt( Math.pow(pathSimpleBot[numberOfShots][0], 2) + Math.pow(pathSimpleBot[numberOfShots][1], 2))*100.0)/100.0 + " m/s");
			} else {
				float angleRadians = (float) (angleAroundPlayer * Math.PI/180);
				velocityVector.x = velocity * MathUtils.sin(angleRadians);
				velocityVector.y = velocity * MathUtils.cos(angleRadians);
			}
			numberOfShots++;
		}

		gamePhysics(deltaTime);

		playerScene.modelInstance.transform.setTranslation(currentPosition);

		if(isMoving) {
			cameraMode = CameraMode.GOLF_MODE;
		}
	}

	private void setLastPosition(){
		lastPosition.set(currentPosition);
	}

	private boolean outOfBounds(float x, float y){
		return x + 10 > Settings.SIZE || y + 10 > Settings.SIZE || x < 0 || y < 0;
	}

	private void gamePhysics(float deltaTime){
		if(isMoving){
			double [] newPos = physicsEngine.computeNewVectorState(deltaTime, (currentPosition.x-size_half)*Settings.SIZE_RATIO, (currentPosition.z-size_half)*Settings.SIZE_RATIO, velocityVector.x, velocityVector.y);
			float x = (float) ((newPos[0] / Settings.SIZE_RATIO) + size_half);
			float y = (float) ((newPos[1] / Settings.SIZE_RATIO) + size_half);

			if (outOfBounds(x ,y)) {
				isMoving = false;
			} else if (physicsEngine.hitWater) {
				currentPosition.set(lastPosition);
				isMoving = false;
				physicsEngine.hitWater = false;
			} else {
				currentPosition.x = x;
				currentPosition.z = y;

				velocityVector.x = (float) newPos[2];
				velocityVector.y = (float) newPos[3];
				isMoving = !physicsEngine.isAtRest;

				float height = getHeightGame(currentPosition.x, currentPosition.z);
				currentPosition.y = height + Settings.BALL_RADIUS;
			}
		}
	}

	private float convertToRealUnits(float num){
		return (num - Settings.SIZE/2) * Settings.SIZE_RATIO;
	}

	private float convertToWorldUnits(float num){
		return num/Settings.SIZE_RATIO + Settings.SIZE/2;
	}


	@Override
	public void dispose() {
		sceneManager.dispose();
		//sceneAsset.dispose();
		ball.dispose();
		environmentCubemap.dispose();
		diffuseCubemap.dispose();
		specularCubemap.dispose();
		brdfLUT.dispose();
		skybox.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {

		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		float zoomLevel = amountY * Settings.CAMERA_ZOOM_LEVEL_FACTOR;
		distanceFromPlayer += zoomLevel;
		if(distanceFromPlayer < Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER){
			distanceFromPlayer = Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER;
		}
		return false;
	}
}
