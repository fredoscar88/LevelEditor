package com.visellico.platty.leveleditor.Level.LevelObjects.Terrain;

import com.farr.Events.Event;
import com.farr.Events.EventDispatcher;
import com.farr.Events.types.MouseMovedEvent;
import com.farr.Events.types.MousePressedEvent;
import com.farr.Events.types.MouseReleasedEvent;
import com.visellico.graphics.Screen;
import com.visellico.graphics.Sprite;
import com.visellico.platty.leveleditor.Level.Level;
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
	
	private int trimWidthLeft, trimWidthRight, topHeight;
	
	public static final int DEFAULT_WIDTH = 15;
	public static final String FLOOR_TYPE_NAME = "floor";
	
//	public UIPanel panelFloorProps;
	
//	public static Comparator<Floor> floorComparator;
	
	
	
	/**
	 * Constructs a new floor with default width and height set to y
	 * Calls to this from level, or whatever part of the program that creates new objects (not when loaded), should always set it as selected.
	 * @param x x coordinate of floor
	 * @param y y coordinate of floor
	 */
	public Floor(int x, int y) {
		super(x, y, DEFAULT_WIDTH, y);
		
		this.serialName = FLOOR_TYPE_NAME;
		
//		this.width = DEFAULT_WIDTH;
//		this.height = y;
		
	}
	
	public Floor createNew(int x, int y) {
		System.out.println("Floor.createNew()");
		return new Floor(x, y);
		
	}
	//@devnote floor does not need to implement Serialize, since it is an uncomplicated Terrain instance.
//	/**
//	 * Serialize a floor for saving into a file
//	 * @return instance of this floor saved as an RCObject
//	 * @devnote <p>It may be more beneficial to serialize at the direct 
//	 * superclass of floor (Terrain) because all of the serialized
//	 * information is at the Terrain level anyway</p>
//	 */
//	public RCObject serialize() {
//		
//		RCObject objFloor = new RCObject("floor");
//		
//		objFloor.addField(RCField.Int("x", x));
//		objFloor.addField(RCField.Int("y", y));
//		objFloor.addField(RCField.Int("width", width));
//		objFloor.addField(RCField.Int("height", height));
//		
//		return objFloor;
//		
//	}
	
	/**
	 * Deserializes a floor
	 * @param objFloor
	 * @return
	 * @devnote <p>How could we move this deserialize method, or atleast most of it, to Terrain?
	 * My thinking is that. like with serializing, all of the important configuration data is
	 * specific to Terrain and not all of its subclasses. Perhaps have the deserialize </p>
	 */
	public static Floor deserializeFloor(RCObject objFloor) {
		
		int x, y, width, height;
		x = objFloor.findField("x").getInt();
		y = objFloor.findField("y").getInt();
		width = objFloor.findField("width").getInt();
		height = objFloor.findField("height").getInt();
//		
		Floor f = new Floor(x, y);
		f.width = width;
		f.height = height;	//MUST be same as y
		
		return f;
	}
	
	public void render(Screen screen) {
		//TODO clamp the dimensions that trimmings are drawn, so that they cant be drawn bigger than the actual object and thus exceed the boundaries, like that of the level >_>
		screen.renderSpriteTiled(x, y, width, height, spriteFloor);
		screen.renderSpriteTiled(x, y, trimWidthLeft, height, spriteTrimLeft);
		screen.renderSpriteTiled(x + width - trimWidthRight, y, trimWidthRight, height, spriteTrimRight);
		screen.renderSpriteTiled(x, y, width, topHeight, spriteTop);
	}
	
	public void update() {
		super.update();
		
	}

	public void onEvent(Event event) {
		EventDispatcher dispatch = new EventDispatcher(event);
		
		dispatch.dispatch(Event.Type.MOUSE_PRESSED, (Event e) -> onMousePress((MousePressedEvent) event));
		dispatch.dispatch(Event.Type.MOUSE_MOVED, (Event e) -> onMouseMove((MouseMovedEvent) event));
		dispatch.dispatch(Event.Type.MOUSE_RELEASED, (Event e) -> onMouseRelease((MouseReleasedEvent) event));
	}
	
	protected boolean onMouseMove(MouseMovedEvent e) {
		
		if (super.onMouseMove(e)) {
			height = y;
			super.valHeight.setText(Integer.toString(y));
			return true;
		}
		
		return false;
	}
	
	public void init(Level l) {
		super.init(l);
		spriteFloor = l.levelType.spriteFloor;
		spriteTop = l.levelType.spriteFloorTop;
		spriteTrimLeft = l.levelType.spriteFloorTrimLeft;
		spriteTrimRight = l.levelType.spriteFloorTrimRight;
		
		trimWidthLeft = spriteTrimLeft.getWidth();
		trimWidthRight = spriteTrimRight.getWidth();
		topHeight = spriteTop.getHeight();
		//Nothing to add to the properties panel
	}
	
}
