package com.spaceshooter.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spaceshooter.game.objects.Explosion;
import com.spaceshooter.game.objects.Laser;
import com.spaceshooter.game.objects.Particles;
import com.spaceshooter.game.objects.PlayerShip;
import com.spaceshooter.game.objects.enemies.EnemyShip;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

public class GameScreen implements Screen {

    private Camera camera;
    private Viewport viewport;

    //graphics
    private SpriteBatch batch;
    private TextureAtlas textureAtlas;
    private Texture explosionTexture;
    private TextureRegion[] backgrounds;
    private float backgroundHeight;
    private TextureRegion playerShipTextureRegion, playerShieldTextureRegion,
            enemyShipTextureRegion, enemyShieldTextureRegion,
            playerLaserTextureRegion, enemyLaserTextureRegion;

    //timing
    private float[] backgroundOffsets = {0,0,0,0};
    private float backgroundMaxScrollingSpeed;
    private float timeBetweenEnemySpawns = 3f;
    private float enemySpawnTimer = 0;

    //world parameters
    private final float WORLD_WIDTH = 72;
    private final float WORLD_HEIGHT = 128;
    private final float TOUCH_MOVEMENT_THRESHOLD = 0.5f;

    //gameObjects
    private PlayerShip playerShip;
    private LinkedList<EnemyShip> enemyShips;
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;
    private LinkedList<Explosion> explosionList;
    private LinkedList<Particles> particles;

    private int score = 0;

    //Heads Up Display
    BitmapFont font;
    float hudVerticalMargin, hudLeftX, hudRightX, hudCenterX, hudRow1, hudRow2, hudSectionWidth;

    public GameScreen() {
        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        //background = new Texture("darkPurpleStarscape.png");
        //backgroundOffset = 0;

        textureAtlas = new TextureAtlas("images.atlas");

        backgrounds = new TextureRegion[4];
        backgrounds[0] = textureAtlas.findRegion("Starscape00");
        backgrounds[1] = textureAtlas.findRegion("Starscape01");
        backgrounds[2] = textureAtlas.findRegion("Starscape02");
        backgrounds[3] = textureAtlas.findRegion("Starscape03");

        backgroundHeight = WORLD_HEIGHT * 2;
        backgroundMaxScrollingSpeed = (float)(WORLD_HEIGHT) / 4;
        explosionTexture = new Texture("explosion.png");

        playerShipTextureRegion = textureAtlas.findRegion("playerShip2_blue");
        enemyShipTextureRegion = textureAtlas.findRegion("enemyRed3");
        playerShieldTextureRegion = textureAtlas.findRegion("shield2");
        enemyShieldTextureRegion = textureAtlas.findRegion("shield1");
        enemyShieldTextureRegion.flip(false, true);
        playerLaserTextureRegion = textureAtlas.findRegion("laserBlue03");
        enemyLaserTextureRegion = textureAtlas.findRegion("laserRed02");

        playerShip = new PlayerShip(48,3,10,10, WORLD_WIDTH/ 2, WORLD_HEIGHT/4,  0.4f, 4, 45, 0.5f,playerShipTextureRegion, playerShieldTextureRegion, playerLaserTextureRegion);
        enemyShips = new LinkedList<>();

        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();

        explosionList = new LinkedList<>();
        particles = new LinkedList<>();

        batch = new SpriteBatch();

        prepareHud();
    }

    @Override
    public void show() {

    }

    private void prepareHud() {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("EdgeOfTheGalaxyRegular-OVEa6.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        fontParameter.size = 72;
        fontParameter.borderWidth = 3.6f;
        fontParameter.color = new Color(1, 1, 1, 0.3f);
        fontParameter.borderColor = new Color(0, 0, 0, 0.3f);

        font = fontGenerator.generateFont(fontParameter);

        font.getData().setScale(0.08f);

        hudVerticalMargin = font.getCapHeight() / 2;
        hudLeftX = hudVerticalMargin;
        hudRightX = WORLD_WIDTH * 2 / 3 - hudLeftX;
        hudCenterX = WORLD_WIDTH / 3;
        hudRow1 = WORLD_HEIGHT - hudVerticalMargin;
        hudRow2 = hudRow1 - hudVerticalMargin - font.getCapHeight();
        hudSectionWidth = WORLD_WIDTH / 3;

    }

    @Override
    public void render(float delta) {
        batch.begin();

        renderBackground(delta);
        detectInput(delta);

        spawnEnemyShips(delta);

        ListIterator<EnemyShip> enemyShipListIterator = enemyShips.listIterator();
        while (enemyShipListIterator.hasNext()) {
            EnemyShip enemyShip = enemyShipListIterator.next();

            moveEnemies(enemyShip, delta);

            enemyShip.update(delta);

            enemyShip.draw(batch);
        }

        playerShip.update(delta);

        playerShip.draw(batch);

        renderLaser(delta);

        updateAndRenderExplosions(delta);
        updateAndRenderParticles(delta);

        detectCollisions();

        updateAndRenderHUD();

        batch.end();
    }

    private void updateAndRenderHUD() {
        font.draw(batch, "Score", hudLeftX, hudRow1, hudSectionWidth, Align.left, false);
        font.draw(batch, "Shield", hudCenterX, hudRow1, hudSectionWidth, Align.center, false);
        font.draw(batch, "Lives", hudRightX, hudRow1, hudSectionWidth, Align.right, false);

        font.draw(batch, String.format(Locale.getDefault(), "%06d", score), hudLeftX, hudRow2, hudSectionWidth, Align.left, false);
        font.draw(batch, String.format(Locale.getDefault(), "%02d", playerShip.shield), hudCenterX, hudRow2, hudSectionWidth, Align.center, false);
        font.draw(batch, String.format(Locale.getDefault(), "%02d", playerShip.lives), hudRightX, hudRow2, hudSectionWidth, Align.right, false);
    }

    private void renderBackground(float deltaTime){
        backgroundOffsets[0] += deltaTime * backgroundMaxScrollingSpeed / 8;
        backgroundOffsets[1] += deltaTime * backgroundMaxScrollingSpeed / 4;
        backgroundOffsets[2] += deltaTime * backgroundMaxScrollingSpeed / 2;
        backgroundOffsets[3] += deltaTime * backgroundMaxScrollingSpeed;

        for (int layer = 0; layer < backgroundOffsets.length; layer++) {
            if (backgroundOffsets[layer] > WORLD_HEIGHT) {
                backgroundOffsets[layer] = 0;
            }
            batch.  draw(backgrounds[layer], 0, -backgroundOffsets[layer], WORLD_WIDTH, backgroundHeight);
        }
    }

    public void renderLaser(float delta) {
        if (playerShip.canShoot()) {
            Laser[] lasers = playerShip.fireLaser();
            for (Laser laser: lasers) {
                playerLaserList.add(laser);
            }
        }

        ListIterator<EnemyShip> enemyShipListIterator = enemyShips.listIterator();
        while (enemyShipListIterator.hasNext()) {
            EnemyShip enemyShip = enemyShipListIterator.next();

            if (enemyShip.canShoot()) {
                Laser[] lasers = enemyShip.fireLaser();
                for (Laser laser: lasers) {
                    enemyLaserList.add(laser);
                }
            }
        }

        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed*delta;
            if (laser.boundingBox.y > WORLD_HEIGHT) {
                iterator.remove();
            }
        }

        iterator = enemyLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed*delta;
            if (laser.boundingBox.y + laser.boundingBox.height < 0) {
                iterator.remove();
            }
        }
    }

    private void updateAndRenderParticles(float delta) {
        ListIterator<Particles> particlesListIterator = particles.listIterator();

        while(particlesListIterator.hasNext()) {
            Particles particle = particlesListIterator.next();

            particle.draw(batch);
            particle.update(delta);
        }
    }

    private void updateAndRenderExplosions(float delta) {
        ListIterator<Explosion> explosionListIterator = explosionList.listIterator();

        while(explosionListIterator.hasNext()) {
            Explosion explosion = explosionListIterator.next();

            explosion.update(delta);

            if (explosion.isFinished()) {
                explosionListIterator.remove();
            } else {
                explosion.draw(batch);
            }
        }
    }

    private void detectCollisions() {
        ListIterator<Laser> laserListIterator = playerLaserList.listIterator();
        while(laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            ListIterator<EnemyShip> enemyShipListIterator = enemyShips.listIterator();
            while (enemyShipListIterator.hasNext()) {
                EnemyShip enemyShip = enemyShipListIterator.next();
                if (enemyShip.intersects(laser.boundingBox)) {
                    if(enemyShip.hitAndCheckDestroyed(laser)) {
                        enemyShipListIterator.remove();
                        //explosionList.add(new Explosion(explosionTexture, new Rectangle(enemyShip.boundingBox), 0.7f));
                        particles.add(new Particles(new Rectangle(enemyShip.boundingBox)));
                        score += 100;
                    }
                    laserListIterator.remove();
                    break;
                }
            }
        }

        laserListIterator = enemyLaserList.listIterator();
        while(laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            if (playerShip.intersects(laser.boundingBox)) {
                if(playerShip.hitAndCheckDestroyed(laser)) {
                    explosionList.add(new Explosion(explosionTexture, new Rectangle(playerShip.boundingBox), 1.6f));
                    playerShip.shield = 10;
                    playerShip.lives--;
                };
                laserListIterator.remove();
            }
        }
    }

    private void spawnEnemyShips(float delta) {
        enemySpawnTimer += delta;

        if (enemySpawnTimer > timeBetweenEnemySpawns) {
            enemyShips.add(
                    new EnemyShip(48,1,10,10, Game.random.nextFloat()*(WORLD_WIDTH-10)+5, WORLD_HEIGHT - 5, 0.3f, 5, 50, 0.8f, enemyShipTextureRegion, enemyShieldTextureRegion, enemyLaserTextureRegion)
            );

            enemySpawnTimer -= timeBetweenEnemySpawns;
        }
    }

    public void detectInput(float delta) {
        float leftLimit, rightLimit, upLimit, downLimit;

        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
        upLimit = (float)(WORLD_HEIGHT / 2) - playerShip.boundingBox.y - playerShip.boundingBox.height;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0) {
            playerShip.translate(Math.min(playerShip.movementSpeed*delta, rightLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0) {
            playerShip.translate(0f, Math.min(playerShip.movementSpeed*delta, upLimit));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0) {
            playerShip.translate(Math.max(playerShip.movementSpeed*delta, leftLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0) {
            playerShip.translate(0f, Math.max(playerShip.movementSpeed*delta, downLimit));
        }

        //TOUCH INPUT
        if (Gdx.input.isTouched()) {
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            Vector2 touchPoint = new Vector2(xTouchPixels, yTouchPixels);
            touchPoint = viewport.unproject(touchPoint);

            Vector2 playerShipCenter = new Vector2(
                    playerShip.boundingBox.x + playerShip.boundingBox.width/2,
                    playerShip.boundingBox.y + playerShip.boundingBox.height/2);

            float touchDistance = touchPoint.dst(playerShipCenter);

            if (touchDistance > TOUCH_MOVEMENT_THRESHOLD) {
                float xTouchDifference = touchPoint.x - playerShipCenter.x;
                float yTouchDifference = touchPoint.y - playerShipCenter.y;

                float xMove = xTouchDifference / touchDistance * playerShip.movementSpeed * delta;
                float yMove = yTouchDifference / touchDistance * playerShip.movementSpeed * delta;

                if (xMove > 0) xMove = Math.min(xMove, rightLimit);
                else xMove = Math.max(xMove, leftLimit);

                if (yMove > 0) yMove = Math.min(yMove, upLimit);
                else yMove = Math.max(yMove, downLimit);

                playerShip.translate(xMove,yMove);
            }
        }
    }

    private  void moveEnemies(EnemyShip enemyShip, float delta) {
        float leftLimit, rightLimit, upLimit, downLimit;

        leftLimit = -enemyShip.boundingBox.x;
        downLimit = (float)WORLD_HEIGHT/2-enemyShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - enemyShip.boundingBox.x - enemyShip.boundingBox.width;
        upLimit = WORLD_HEIGHT - enemyShip.boundingBox.y - enemyShip.boundingBox.height;

        float xMove = enemyShip.getDirectionVector().x * enemyShip.movementSpeed * delta;
        float yMove = enemyShip.getDirectionVector().y * enemyShip.movementSpeed * delta;

        if (xMove > 0) xMove = Math.min(xMove, rightLimit);
        else xMove = Math.max(xMove, leftLimit);

        if (yMove > 0) yMove = Math.min(yMove, upLimit);
        else yMove = Math.max(yMove, downLimit);

        enemyShip.translate(xMove,yMove);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
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

    }
}
