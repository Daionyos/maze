package com.mygdx.game.screens;

import com.badlogic.gdx.Screen;
import com.mygdx.game.GameClass;
import com.mygdx.game.Obstacle;

import java.util.ArrayList;

public class GameScreen implements Screen {
    private GameClass gameClass;

    public GameScreen() {
        this.gameClass = new GameClass();
        this.gameClass.create();  // Assuming GameClass uses a create method for setup
    }

    public GameScreen(String function, float ballX, float ballY, float holeX, float holeY, float holeRadius, ArrayList<Obstacle> obstacles, String mazeString) {
        this.gameClass = new GameClass(function, ballX, ballY, holeX, holeY, holeRadius, obstacles, mazeString);
        this.gameClass.create();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        this.gameClass.render();  // Delegate rendering to GameClass
    }

    @Override
    public void resize(int width, int height) {
        this.gameClass.resize(width, height);
    }

    @Override
    public void pause() {
        this.gameClass.pause();
    }

    @Override
    public void resume() {
        this.gameClass.resume();
    }

    @Override
    public void hide() {
        // Optionally clean up resources that are only needed while this screen is active
    }

    @Override
    public void dispose() {
        this.gameClass.dispose();
    }
}
