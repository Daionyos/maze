package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.screens.HomeScreen;

import java.util.ArrayList;

public class GameMain extends Game {
    private HomeScreen homeScreen;
    private GameScreen gameScreen;

    private String function;
    private float ballX;
    private float ballY;
    private float holeX;
    private float holeY;
    private float holeRadius;
    private ArrayList<Obstacle> obstacles;
    private String mazeString;  // Added mazeString

    @Override
    public void create() {
        homeScreen = new HomeScreen(this);
        this.setScreen(homeScreen);
    }

    public void getInput(String function, float ballX, float ballY, float holeX, float holeY, float holeRadius, ArrayList<Obstacle> obstacles, String mazeString) {
        this.function = function;
        this.ballX = ballX;
        this.ballY = ballY;
        this.holeX = holeX;
        this.holeY = holeY;
        this.holeRadius = holeRadius;
        this.obstacles = obstacles;
        this.mazeString = mazeString;  // Capture maze input
    }

    public void startGame() {
        this.setScreen(new GameScreen(function, ballX, ballY, holeX, holeY, holeRadius, obstacles, mazeString));  // Pass mazeString to GameScreen
    }
}
