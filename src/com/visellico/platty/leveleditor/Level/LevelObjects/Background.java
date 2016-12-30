package com.visellico.platty.leveleditor.Level.LevelObjects;

import java.util.List;

import com.farr.Events.Event;
import com.visellico.graphics.Screen;
import com.visellico.graphics.Sprite;
import com.visellico.platty.leveleditor.LayerLE;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.rainecloud.serialization.RCObject;

public class Background extends LevelObject {

	public static Sprite spriteBackground;
	
	public int width, height;
	Level level;
	
	public Background() {
	}

	public void init(Level l) {
		this.level = l;
		spriteBackground = l.levelType.spriteBackground;
		update();
		
	}

	public void update() {
		this.width = level.width;
		this.height = level.height;
	}

	public void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}

	public void init(List<LayerLE> l) {
		// TODO Auto-generated method stub
		
	}

	public void render(Screen screen) {
		screen.renderSpriteTiled(x + 1, height - 1, width - 1, height - 1, spriteBackground);
	}

	public RCObject serialize() {
		return null;
	}

	public LevelObject deserialize(RCObject obj) {
		System.err.println("Backgrounds should not be deserialized, you're doing something wrong");
		return null;
	}
	
}
