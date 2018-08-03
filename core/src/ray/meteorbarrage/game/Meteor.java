package ray.meteorbarrage.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.Random;

public class Meteor extends ray.meteorbarrage.game.BaseActor {

    private float virtualScreenWidth = 480;
    private float virtualScreenHeight = 640;
    private float meteorVelocity;
    private Texture[] meteorTexture;
    private Random randomNumberGenerator;
    private int randomIndex;
    private boolean isDestroyed;


    public Meteor() {
        super();
        buildMeteor();
        meteorVelocity = 250;

        setPosition(randomMeteorSpawnX(), randomMeteorSpawnY());
        velocityY -= meteorVelocity;
    }

    public Meteor(float meteorVelocity) {
        super();
        this.meteorVelocity = meteorVelocity;
        buildMeteor();

        setPosition(randomMeteorSpawnX(), randomMeteorSpawnY());
        velocityY -= meteorVelocity;

    }

    public void buildMeteor() {
        randomNumberGenerator = new Random();

        meteorTexture = new Texture[3];
        for (int i = 0; i < meteorTexture.length; i++) {
            String fileName = "meteor" + i + ".png";
            Texture tex = new Texture(fileName);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            meteorTexture[i] = tex;
        }

        randomIndex = randomNumberGenerator.nextInt(meteorTexture.length);
        setTexture(meteorTexture[randomIndex]);

        for (int i = 0; i < meteorTexture.length; i++) {
            if (i != randomIndex)
                meteorTexture[i].dispose();
        }

        setSize(75, 75);
        setOrigin(getWidth() / 2, getHeight() / 2);
        addAction(Actions.forever(Actions.rotateBy(-2f)));

        isDestroyed = false;
    }

    public void destroyed() {
        clearActions();
        setTexture(new Texture("meteorDestroyed.png"));
        setSize(75, 75);
        addAction(Actions.forever(Actions.parallel((Actions.scaleTo(3, 2, 5f)), (Actions.fadeOut(3f)))));
        isDestroyed = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    private int randomMeteorSpawnX() {
        return randomNumberGenerator.nextInt((int) (virtualScreenWidth - getWidth()));
    }

    private int randomMeteorSpawnY() {
        return (int) virtualScreenHeight + randomNumberGenerator.nextInt((int) (virtualScreenHeight));
    }

    public void dispose() {
        meteorTexture[randomIndex].dispose();
    }
}
