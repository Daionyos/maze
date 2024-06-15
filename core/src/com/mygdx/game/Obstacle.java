package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import java.util.Random;

public class Obstacle {
    public float x;
    public float y;
    public float radius;
    float height;
    float length;

    float gameX;
    float gameY;
    float gameRadius;

    public Obstacle(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;

        this.gameX = (x +10)/Settings.SIZE_RATIO;
        this.gameY = (y +10)/Settings.SIZE_RATIO;
        this.gameRadius = radius/Settings.SIZE_RATIO;
        setLength();
    }

    public void setHeight(float height){
        this.height = height;
    }

    private void setLength(){
        Random random = new Random();
        length = random.nextFloat(100) + 100;
    }

    public ModelInstance buildObstacle(){
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Material material = new Material();
        material.set(PBRColorAttribute.createBaseColorFactor(new Color(0.4f, 0.29f, 0.14f, 1)));
        material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));

        int usageCode = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        MeshPartBuilder builder = modelBuilder.part("cylinder", GL20.GL_TRIANGLES, usageCode, material);

        float diameter = 2 * gameRadius;
        float length = this.length;
        int divisions = 16;

        CylinderShapeBuilder.build(builder, diameter, length, diameter, divisions);

        ModelInstance model  = new ModelInstance(modelBuilder.end());
        model.transform.setToTranslation(gameX, height+0.25f*length, gameY);
        return model;
    }
}
