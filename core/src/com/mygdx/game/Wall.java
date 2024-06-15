package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import java.util.Random;

public class Wall {
    public float x;
    public float y;
    public float length;
    public float heightC;
    public float gameXC;
    public float gameYC;
    public float gameLengthC;


    public Wall(float x, float y, float length) {
        this.x = x;
        this.y = y;
        this.length = length;
        this.gameXC = (x + 10) / Settings.SIZE_RATIO;
        this.gameYC = (y + 10) / Settings.SIZE_RATIO;
        this.gameLengthC = length / Settings.SIZE_RATIO;
    }
    public void setHeight(float height){
        this.heightC = height;
    }

    public ModelInstance buildWall() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        Material material = new Material();
        material.set(PBRColorAttribute.createBaseColorFactor(new Color(0f, 0.29f, 0.24f, 1)));
        int usageCode = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        MeshPartBuilder meshPartBuilder = modelBuilder.part("box", GL20.GL_TRIANGLES, usageCode, material);

        float width = gameLengthC;
        float height = gameLengthC;
        float depth = gameLengthC;

        BoxShapeBuilder.build(meshPartBuilder, width, height+40, depth);

        Model model = modelBuilder.end();
        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(gameXC + width / 2, height / 2, gameYC + depth / 2);

        return modelInstance;
    }
}
