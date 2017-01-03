package com.visellico.platty.leveleditor.Level.LevelObjects.Terrain;

import com.visellico.graphics.Screen;
import com.visellico.graphics.Sprite;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.rainecloud.serialization.RCObject;

/**
 * Wall object in its most basic form
 * X, Y, Width, Height, and four sprites
 * @author Henry
 *
 */
public class Wall extends Terrain {

	public static Sprite spriteWall;
	public static Sprite spriteTop;
	public static Sprite spriteTrimLeft;
	public static Sprite spriteTrimRight;
	
	private int trimWidthLeft, trimWidthRight, topHeight;
	
	public static final int DEFAULT_WIDTH = 15;
	public static final int DEFAULT_HEIGHT = 15;
	public static final String WALL_TYPE_NAME = "wall";
	
//	public UIPanel panelFloorProps;
	
//	public static Comparator<Floor> floorComparator;
	
	
	
	/**
	 * Constructs a new floor with default width and height set to y
	 * Calls to this from level, or whatever part of the program that creates new objects (not when loaded), should always set it as selected.
	 * @param x x coordinate of floor
	 * @param y y coordinate of floor
	 */
	public Wall(int x, int y) {
		super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		
		this.serialName = WALL_TYPE_NAME;
		
//		this.width = DEFAULT_WIDTH;
//		this.height = y;
		
	}
	
	public Wall createNew(int x, int y) {
		
		System.out.println("Wall.createNew(");
		return new Wall(x, y);
		
	}
	
	/**
	 * Deserializes a floor
	 * @param objWall
	 * @return
	 * @devnote <p>How could we move this deserialize method, or atleast most of it, to Terrain?
	 * My thinking is that. like with serializing, all of the important configuration data is
	 * specific to Terrain and not all of its subclasses. Perhaps have the deserialize </p>
	 */
	public static Wall deserializeFloor(RCObject objWall) {
		
		int x, y, width, height;
		x = objWall.findField("x").getInt();
		y = objWall.findField("y").getInt();
		width = objWall.findField("width").getInt();
		height = objWall.findField("height").getInt();
//		
		Wall w = new Wall(x, y);
		w.width = width;
		w.height = height;	//MUST be same as y
		
		return w;
	}
	
	public void render(Screen screen) {
		//TODO clamp the dimensions that trimmings are drawn, so that they cant be drawn bigger than the actual object and thus exceed the boundaries, like that of the level >_>
		screen.renderSpriteTiled(x, y, width, height, spriteWall);
		screen.renderSpriteTiled(x, y, trimWidthLeft, height, spriteTrimLeft);
		screen.renderSpriteTiled(x + width - trimWidthRight, y, trimWidthRight, height, spriteTrimRight);
		screen.renderSpriteTiled(x, y, width, topHeight, spriteTop);
	}
	
	public void update() {
		super.update();
		
	}
	
	public void init(Level l) {
		super.init(l);
		spriteWall = l.levelType.spriteWall;
		spriteTop = l.levelType.spriteWallTop;
		spriteTrimLeft = l.levelType.spriteWallTrimLeft;
		spriteTrimRight = l.levelType.spriteWallTrimRight;
		
		trimWidthLeft = spriteTrimLeft.getWidth();
		trimWidthRight = spriteTrimRight.getWidth();
		topHeight = spriteTop.getHeight();
		//Nothing to add to the properties panel
	}
}
