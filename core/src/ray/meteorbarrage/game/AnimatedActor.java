package ray.meteorbarrage.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimatedActor extends ray.meteorbarrage.game.BaseActor {
    public float elapsedTime;
    public Animation<TextureRegion> anim;

    public AnimatedActor() {
        super();
        elapsedTime = 0;
    }

    public void setAnimation(Animation<TextureRegion> a) {
        Texture t = a.getKeyFrame(0).getTexture();
        setTexture(t);
        anim = a;
    }

    public void act(float dt) {
        super.act(dt);
        elapsedTime += dt;
    }

    public void draw(Batch batch, float parentAlpha) {
        region.setRegion(anim.getKeyFrame(elapsedTime));
        super.draw(batch, parentAlpha);
    }
}
