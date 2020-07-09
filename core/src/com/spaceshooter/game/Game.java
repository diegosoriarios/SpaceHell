package com.spaceshooter.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Random;

public class Game extends com.badlogic.gdx.Game {
	GameScreen gameScreen;
	public static Random random = new Random();

	@Override
	public void create() {
		gameScreen = new GameScreen();
		setScreen(gameScreen);
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		gameScreen.resize(width, height);
	}

	@Override
	public void dispose() {
		super.dispose();
		gameScreen.dispose();
	}
}
