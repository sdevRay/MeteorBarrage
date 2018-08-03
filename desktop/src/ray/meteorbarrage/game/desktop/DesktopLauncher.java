package ray.meteorbarrage.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ray.meteorbarrage.game.MeteorBarrage;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new MeteorBarrage(), config);
		config.width = 480;
		config.height = 640;
	}
}
