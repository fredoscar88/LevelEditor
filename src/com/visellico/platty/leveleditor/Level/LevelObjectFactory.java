package com.visellico.platty.leveleditor.Level;

import static com.visellico.platty.leveleditor.Level.LevelObjectFactory.Type.FLOOR;

import com.visellico.platty.leveleditor.Level.LevelObjects.Terrain.*;
import com.visellico.platty.leveleditor.Level.LevelObjects.LevelObject;
//import com.visellico.platty.leveleditor.Level.LevelObjects.Terrain.Floor;



public class LevelObjectFactory {

	public enum Type {
		FLOOR,
		WALL
	}
	
	private Type t;
	
	public LevelObjectFactory() {
		t = FLOOR;
	}
	
	public void setType(Type t) {
		this.t = t;
	}
	
	public LevelObject create(int x, int y) {
		
		System.out.println(FLOOR.toString());
		
		switch (t) {
		case FLOOR: return new Floor(x, y);
		case WALL: return new Wall(x, y);
		default: return null;
		}
	}
	
	
}
