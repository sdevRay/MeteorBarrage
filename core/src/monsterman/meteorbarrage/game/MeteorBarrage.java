package monsterman.meteorbarrage.game;

import com.badlogic.gdx.Game;

// Meteor Barrage
// 2018

public class MeteorBarrage extends Game {

	@Override
	public void create() {
		MeteorBarrageMenu sm = new MeteorBarrageMenu(this);
		setScreen(sm);
	}
}
