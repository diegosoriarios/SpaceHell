package com.spaceshooter.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Laser {

    Rectangle boundingBox;

    float movementSpeed;

    TextureRegion textureRegion;

    public Laser(float xPos, float yPos, float width, float height, float movementSpeed, TextureRegion textureRegion) {
        this.boundingBox = new Rectangle(xPos - width / 2, yPos, width, height);

        this.movementSpeed = movementSpeed;
        this.textureRegion = textureRegion;
    }

    public void draw(Batch batch) {
        batch.draw(textureRegion, boundingBox.x - boundingBox.width/2, boundingBox.y, boundingBox.width, boundingBox.height);
    }
}
