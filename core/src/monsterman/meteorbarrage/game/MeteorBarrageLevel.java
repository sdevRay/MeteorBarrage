package monsterman.meteorbarrage.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class MeteorBarrageLevel implements Screen, InputProcessor {

    public Game game;
    private OrthographicCamera cam;

    private Stage mainStage;
    private Stage uiStage;

    private AnimatedActor spaceship;

    private BaseActor planet;
    private BaseActor restart;

    private BitmapFont font;

    private Label timeLabel;
    private Label destroyLabel;
    private Label scoreLabel;

    private ArrayList<BaseActor> projectileManager = new ArrayList<BaseActor>();
    private ArrayList<BaseActor> backgroundManager = new ArrayList<BaseActor>();
    private ArrayList<Meteor> meteorManager = new ArrayList<Meteor>();

    private boolean gameStop;
    private boolean drawMeteor;
    private boolean fireProjectile;

    private int incrementInterval;
    private int meteorDestroyed;
    private int meteorImpacts;
    private int meteors;

    private float timeElapsed;
    private float rotationDegrees;
    private float projectileTimer;
    private float virtualScreenWidth = 480;
    private float virtualScreenHeight = 640;

    private Vector3 lastTouch;
    private Vector3 newTouch;

    private Music music_level;

    public MeteorBarrageLevel(Game g) {
        this.game = g;
        create();
    }

    private void create() {

        cam = new OrthographicCamera();
        Viewport viewport = new StretchViewport(virtualScreenWidth, virtualScreenHeight);
        viewport.setCamera(cam);

        mainStage = new Stage(viewport);
        uiStage = new Stage(viewport);

        music_level = Gdx.audio.newMusic(Gdx.files.internal("backgroundmusic.mp3"));
        music_level.setVolume(.20f);
        music_level.setLooping(true);
        music_level.play();

        // Build background texture array
        for (int i = 0; i < 2; i++) {
            backgroundManager.add(new BaseActor());
            backgroundManager.get(i).setTexture(new Texture("background.png"));

            if (i < 1)
                backgroundManager.get(i).setPosition(0, 0);
            else
                backgroundManager.get(i).setPosition(0, virtualScreenHeight);

            backgroundManager.get(i).setSize(virtualScreenWidth, virtualScreenHeight);
            backgroundManager.get(i).velocityY -= 15f;
            mainStage.addActor(backgroundManager.get(i));
        }

        planet = new BaseActor();
        planet.setTexture(new Texture("planet.png"));
        planet.setPosition(-planet.getWidth(), virtualScreenHeight);
        planet.setSize(200, 150);
        planet.velocityX += 5f;
        planet.velocityY -= 10f;
        mainStage.addActor(planet);

        // Build spaceship
        // Assign textures to spaceship animation
        TextureRegion[] spaceshipFrames = new TextureRegion[4];
        for (int i = 0; i < spaceshipFrames.length; i++) {
            String fileName = "spaceship" + i + ".png";
            Texture tex = new Texture(fileName);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            spaceshipFrames[i] = new TextureRegion(tex);
        }
        Array<TextureRegion> framesArray = new Array<TextureRegion>(spaceshipFrames);
        Animation<TextureRegion> anim = new Animation(0.1f, framesArray, Animation.PlayMode.LOOP_PINGPONG);
        spaceship = new AnimatedActor();
        spaceship.setAnimation(anim);
        spaceship.setSize(50, 50);
        spaceship.setPosition(virtualScreenWidth / 2 - spaceship.getWidth() / 2, 140 - spaceship.getHeight() / 2);
        spaceship.setOrigin(spaceship.getWidth() / 2, spaceship.getHeight() / 2);
        uiStage.addActor(spaceship);

        restart = new BaseActor();
        restart.setTexture(new Texture("restart.png"));
        restart.setPosition(0, 0);
        restart.setSize(256, 64);
        restart.setPosition(virtualScreenWidth / 2 - restart.getWidth() / 2, virtualScreenHeight / 2);
        restart.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                startGame();
                return true;
            }
        });
        restart.addAction(Actions.alpha(0));
        restart.setTouchable(Touchable.disabled);
        uiStage.addActor(restart);

        font = new BitmapFont();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        String text = "Time: " + (int) timeElapsed;
        timeLabel = new Label(text, style);
        timeLabel.setFontScale(2);
        timeLabel.setPosition(virtualScreenWidth - timeLabel.getWidth() * 2 - 50, virtualScreenHeight - timeLabel.getHeight() * 2);

        text = " Destroyed: " + meteorDestroyed;
        destroyLabel = new Label(text, style);
        destroyLabel.setFontScale(2);
        destroyLabel.setPosition(0, virtualScreenHeight - destroyLabel.getHeight() * 2);

        text = "Score: ";
        scoreLabel = new Label(text, style);
        scoreLabel.setFontScale(2);
        scoreLabel.setPosition(virtualScreenWidth / 2 - scoreLabel.getWidth() - 20, virtualScreenHeight / 2 - restart.getHeight());
        scoreLabel.addAction(Actions.alpha(0));

        uiStage.addActor(scoreLabel);
        uiStage.addActor(timeLabel);
        uiStage.addActor(destroyLabel);

        meteorDestroyed = 0;
        timeElapsed = 0;
        projectileTimer = 0;
        incrementInterval = 2;
        rotationDegrees = 0;
        meteors = 1;

        gameStop = false;
        drawMeteor = false;

        lastTouch = new Vector3(spaceship.getX() + (spaceship.getWidth() / 2), spaceship.getY() + (spaceship.getHeight() / 2), 0);

    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    // SPACESHIP MOVEMENTS
    // Spaceship moves to touch location and gradually rotates left or right depending on movement direction
    private void updateMotion() {
        spaceship.addAction(Actions.moveTo(lastTouch.x - spaceship.getWidth() / 2, lastTouch.y + spaceship.getHeight() / 2, .25f));

        if (lastTouch.x < spaceship.getX()) {

            if (spaceship.getRotation() < 30f)
                rotationDegrees += 10f;
            else
                rotationDegrees = 30f;

        } else {

            if (spaceship.getRotation() <= 30f && spaceship.getRotation() > 0f) {
                rotationDegrees -= 5f;
            }

        }

        if (lastTouch.x > spaceship.getX() + spaceship.getWidth()) {

            if (spaceship.getRotation() > -30f)
                rotationDegrees -= 10f;
            else
                rotationDegrees = -30f;

        } else {

            if (spaceship.getRotation() >= -30f && spaceship.getRotation() < 0f) {
                rotationDegrees += 5f;
            }

        }

        spaceship.setRotation(rotationDegrees);

        if (fireProjectile) {
            BaseActor projectile = new BaseActor();
            projectile.setTexture(new Texture("projectile.png"));
            projectile.setSize(25, 25);
            projectile.setPosition(spaceship.getX() + spaceship.getOriginX() / 2, spaceship.getY() + spaceship.getOriginY() / 2);
            projectile.velocityY += 1000;
            projectileManager.add(projectile);
            mainStage.addActor(projectile);
            fireProjectile = false;
        }
    }

    private void keyboardControls() {
        if (!Gdx.input.isTouched()) {
            if (gameStop)
                if (Gdx.input.isKeyJustPressed(Input.Keys.R))
                    startGame();
        }
    }

    @Override
    public void render(float dt) {
        // Update
        mainStage.act(dt);
        uiStage.act(dt);

        updateMotion();
        keyboardControls();

        // Manage timers
        if (!gameStop) {
            timeElapsed += dt;
            projectileTimer += dt;
        }

        // Auto fire projectiles every 15th of a second
        if (projectileTimer >= .15f) {
            projectileTimer = 0;
            setFireProjectile(true);
        } else {
            setFireProjectile(false);
        }

        // Display labels and change their colors as meteors impact, stop game on 3rd impact
        timeLabel.setText(" Time: " + (int) timeElapsed);
        destroyLabel.setText(" Destroyed: " + meteorDestroyed);
        if (meteorImpacts == 1) {
            timeLabel.addAction(Actions.forever(Actions.color(new Color(Color.YELLOW), 0.5f)));
            destroyLabel.addAction(Actions.forever(Actions.color(new Color(Color.YELLOW), 0.5f)));
        } else if (meteorImpacts == 2) {
            timeLabel.addAction(Actions.forever(Actions.color(new Color(Color.RED), 0.5f)));
            destroyLabel.addAction(Actions.forever(Actions.color(new Color(Color.RED), 0.5f)));
        } else if (meteorImpacts == 3) {
            scoreLabel.addAction(Actions.alpha(1));
            restart.addAction(Actions.alpha(1));
            restart.setTouchable(Touchable.enabled);
            scoreLabel.setText("Score: " + (int) timeElapsed * meteorDestroyed);
            gameStop = true;
        }

        // Increment meteor count as they are destroyed
        if (meteorDestroyed >= incrementInterval) {
            if (drawMeteor) {
                meteors++;
                incrementInterval += 1;
                drawMeteor = false;
            }
        }

        // Add meteors to the stage
        int meteorCounter = 0;
        if (meteorManager.isEmpty() && !gameStop) {
            for (int i = 0; i < meteors; i++) {
                meteorManager.add(new Meteor());
                mainStage.addActor(meteorManager.get(i));
                meteorCounter = i;
            }
            drawMeteor = true;
        }

        // MAIN GAME LOOP
        // Outer loop cycles the meteors
        // Inner loop cycles the projectiles
        // Check for meteors and projectiles to leave boundaries or collision
        while (meteorCounter < meteorManager.size()) {
            for (int x = 0; x < meteorManager.size(); x++) {
                for (int i = 0; i < projectileManager.size(); i++) {
                    if (projectileManager.get(i).getY() > virtualScreenHeight) {
                        projectileManager.get(i).remove();
                        projectileManager.remove(i);
                    } else if (meteorManager.get(x).getBoundingRectangle().contains(projectileManager.get(i).getBoundingRectangle())) {

                        if (!meteorManager.get(x).isDestroyed()) {
                            projectileManager.get(i).remove();
                            projectileManager.remove(i);
                            meteorManager.get(x).destroyed();
                            if (!gameStop)
                                meteorDestroyed++;
                            break;
                        }
                    }
                }

                if (meteorManager.get(x).getY() + meteorManager.get(x).getHeight() < 0) {
                    if (!meteorManager.get(x).isDestroyed())
                        meteorImpacts++;
                    meteorManager.get(x).remove();
                    meteorManager.get(x).dispose();
                    meteorManager.remove(x);
                    meteorCounter--;
                    break;
                }
            }

            meteorCounter++;
        }

        // Set world boundaries for the spaceship
        spaceship.setX(MathUtils.clamp(spaceship.getX(), 0, virtualScreenWidth - spaceship.getWidth()));
        spaceship.setY(MathUtils.clamp(spaceship.getY(), 0, virtualScreenHeight - spaceship.getHeight()));

        // Cycle between the background array as it scrolls
        for (int i = 0; i < backgroundManager.size(); i++) {
            if ((int) (backgroundManager.get(0).getY() + backgroundManager.get(0).getHeight()) <= 0)
                backgroundManager.get(0).setPosition(0, backgroundManager.get(1).getY() + backgroundManager.get(1).getHeight());

            if ((int) (backgroundManager.get(1).getY() + backgroundManager.get(1).getHeight()) <= 0)
                backgroundManager.get(1).setPosition(0, backgroundManager.get(0).getY() + backgroundManager.get(0).getHeight());
        }

        // Draw graphics
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mainStage.draw();
        uiStage.draw();
    }

    private void startGame() {
        dispose();
        game.setScreen(new MeteorBarrageMenu(game));
    }

    private void setFireProjectile(boolean t) {
        fireProjectile = t;
    }

    @Override
    public void resize(int width, int height) {
        mainStage.getViewport().update(width, height);
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastTouch = new Vector3(screenX, screenY, 0);
        cam.unproject(lastTouch);

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        newTouch = new Vector3(screenX, screenY, 0);
        cam.unproject(newTouch);
        lastTouch = newTouch;
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void dispose() {
        mainStage.dispose();
        uiStage.dispose();
        font.dispose();
        music_level.dispose();
    }
}
