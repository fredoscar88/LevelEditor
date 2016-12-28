package com.visellico.platty.leveleditor.Level.LevelObjects.Terrain;

import com.visellico.graphics.Screen;
import com.visellico.graphics.Sprite;
import com.visellico.platty.level.Level;
import com.visellico.rainecloud.serialization.RCField;
import com.visellico.rainecloud.serialization.RCObject;

/**
 * Floor object in its most basic form
 * X, Y, Width, Height, and four sprites
 * @author Henry
 *
 */
public class Floor extends Terrain {

	public static Sprite spriteFloor;
	public static Sprite spriteTop;
	public static Sprite spriteTrimLeft;
	public static Sprite spriteTrimRight;
	
	public static final int DEFAULT_WIDTH = 8;
	
	/**
	 * Constructs a new floor with default width and height set to y
	 * Calls to this from level, or whatever part of the program that creates new objects (not when loaded), should always set it as selected.
	 * @param x x coordinate of floor
	 * @param y y coordinate of floor
	 */
	public Floor(int x, int y) {
	
		this.x = x;
		this.y = y;
		this.width = DEFAULT_WIDTH;
		this.height = y;
		
	}
	
	/**
	 * Serialize a floor for saving into a file
	 * @return instance of this floor saved as an RCObject
	 * @devnote <p>It may be more beneficial to serialize at the direct 
	 * superclass of floor (Terrain) because all of the serialized
	 * information is at the Terrain level anyway</p>
	 */
	public RCObject serialize() {
		
		RCObject objFloor = new RCObject("floor");
		
		objFloor.addField(RCField.Int("x", x));
		objFloor.addField(RCField.Int("y", y));
		objFloor.addField(RCField.Int("width", width));
		objFloor.addField(RCField.Int("height", height));
		
		return objFloor;
		
	}
	
	public static Floor deserialize(RCObject objFloor) {
		
		int x, y, width, height;
		x = objFloor.findField("x").getInt();
		y = objFloor.findField("y").getInt();
		width = objFloor.findField("width").getInt();
		height = objFloor.findField("height").getInt();
		
		Floor f = new Floor(x, y);
		f.width = width;
		f.height = height;	//MUST be same as y
		return f;
	}
	
	public void render(Screen screen) {
		screen.renderSpriteTiled(x, y, width, height, spriteFloor);
		screen.renderSpriteTiled(x, y, 2, height, spriteTrimLeft);
		screen.renderSpriteTiled(x + width - 2, y, 2, height, spriteTrimRight);
		screen.renderSpriteTiled(x, y, width, 3, spriteTop);
	}

	public void init(Level l) {
		spriteFloor = l.levelType.spriteFloor;
		spriteTop = l.levelType.spriteFloorTop;
		spriteTrimLeft = l.levelType.spriteFloorTrimLeft;
		spriteTrimRight = l.levelType.spriteFloorTrimRight;
	}
	
}
