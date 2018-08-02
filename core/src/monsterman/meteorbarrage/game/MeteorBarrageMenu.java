package monsterman.meteorbarrage.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MeteorBarrageMenu implements Screen {

    private Stage uiStage;
    private Game game;

    private BitmapFont font;

    private BaseActor background;
    private BaseActor title;
    private BaseActor spaceship;
//    private BaseActor badlogic;

    private float virtualScreenWidth = 480;
    private float virtualScreenHeight = 640;

    public MeteorBarrageMenu(Game g) {
        game = g;
        create();
    }

    private void create() {

        OrthographicCamera cam = new OrthographicCamera();
        Viewport viewport = new StretchViewport(virtualScreenWidth, virtualScreenHeight);
        viewport.setCamera(cam);

        uiStage = new Stage(viewport);

        background = new BaseActor();
        background.setTexture(new Texture("background.png"));
        background.setPosition(0, 0);
        background.setSize(virtualScreenWidth, virtualScreenHeight);
        uiStage.addActor(background);

//        badlogic = new BaseActor();
//        badlogic.setTexture(new Texture("badlogic.jpg"));
//        badlogic.setPosition(0, virtualScreenHeight - 100);
//        badlogic.setSize(100, 100);
//        uiStage.addActor(badlogic);

        spaceship = new BaseActor();
        spaceship.setTexture(new Texture("spaceship.png"));
        spaceship.setSize(50, 50);
        spaceship.setPosition(virtualScreenWidth / 2 - spaceship.getWidth() / 2, 140 - spaceship.getHeight() / 2);
        spaceship.setBounds(spaceship.getX(), spaceship.getY(), spaceship.getWidth(), spaceship.getHeight());
        spaceship.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                startGame();
                return false;
            }
        });
        uiStage.addActor(spaceship);

        title = new BaseActor();
        title.setTexture(new Texture("title.png"));
        title.setPosition(virtualScreenWidth / 2 - title.getWidth() / 2, virtualScreenHeight / 2);
        title.setSize(383, 96);
        uiStage.addActor(title);

        font = new BitmapFont();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.TEAL);

        String descriptionStr = "Choose your spaceship";
        Label descriptionText = new Label(descriptionStr, style);
        descriptionText.setFontScale(2);
        descriptionText.setPosition(virtualScreenWidth / 2 - descriptionText.getWidth(), spaceship.getY() + spaceship.getHeight() * 2);
        descriptionText.addAction(Actions.forever(Actions.sequence(Actions.color(new Color(Color.PINK), 0.5f), Actions.delay(0.5f), Actions.color(new Color(199, 21, 133, 1), 0.5f))));

        uiStage.addActor(descriptionText);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    private void keyboardControls() {
        if (!Gdx.input.isTouched()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                startGame();
        }
    }

    @Override
    public void render(float dt) {
        // Update
        uiStage.act(dt);

        keyboardControls();

        // Draw graphics
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        uiStage.draw();
    }

    private void startGame() {
        dispose();
        game.setScreen(new MeteorBarrageLevel(game));
    }


    @Override
    public void resize(int width, int height) {

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
        uiStage.dispose();
        font.dispose();
    }
}
