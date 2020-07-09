package com.spaceshooter.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Particles {

    ParticleEffect pe;

    public Particles(Rectangle boundingBox) {
        this.pe = new ParticleEffect();
        this.pe.load(Gdx.files.internal("particle"),Gdx.files.internal(""));
        this.pe.getEmitters().first().setPosition(boundingBox.x,boundingBox.y);
        this.pe.start();
    }

    public void update(float delta) {
        pe.update(delta);
    }

    public void draw(Batch batch) {
        pe.draw(batch);
        if (pe.isComplete())
            pe.dispose();
    }
}
