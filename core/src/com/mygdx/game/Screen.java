package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.mygdx.game.enums.CameraMode;

public class Screen extends ScreenAdapter {

    private final Vector3 currentPosition;
    private PerspectiveCamera camera;


    public Screen(Vector3 currentPosition, int numberShots, PerspectiveCamera camera){
        this.currentPosition = currentPosition;
        this.camera = camera;

        Stage stage = new Stage(new ScreenViewport());

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        TextButton topViewButton = new TextButton("Top View", skin); // 'skin' is your Skin instance
        topViewButton.setPosition(20, Gdx.graphics.getHeight() - 50); // Example position
        topViewButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Toggle top view mode when button is clicked
                changeTopView();
            }
        });

        LabelStyle labelStyle = new LabelStyle();
        String position = "Position: ("+ currentPosition.x + ", " + currentPosition.y + ")";
        Label labelPosition = new Label(position, labelStyle);
        labelPosition.setAlignment(Align.center);
        // Set position and size of the label
        labelPosition.setPosition(100, 100);
        labelPosition.setSize(20, 50);


        Label labelShots = new Label("Number of shots: "+ numberShots , labelStyle);
        labelShots.setAlignment(Align.center);
        // Set position and size of the label
        labelShots.setPosition(100, 100);
        labelShots.setSize(20, 50);


        stage.addActor(labelShots);
        stage.addActor(labelPosition);
        stage.addActor(topViewButton);

        Gdx.input.setInputProcessor(stage);
    }

    public void changeTopView(){
        camera.position.set(1, 2, 3); // Set the position to a suitable top-down position
        camera.lookAt(1, 2, 3); // Set the lookAt vector to the center of your scene or desired focus point

        // Update the camera's view matrix
        camera.update();
    }
}
