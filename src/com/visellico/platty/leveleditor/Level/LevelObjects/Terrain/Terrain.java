package com.visellico.platty.leveleditor.Level.LevelObjects.Terrain;

import java.util.Collections;
import java.util.List;

import com.farr.Events.Event;
import com.farr.Events.EventDispatcher;
import com.farr.Events.types.MousePressedEvent;
import com.visellico.graphics.Screen;
import com.visellico.platty.leveleditor.LayerLE;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.platty.leveleditor.Level.LevelObjects.LevelObject;
import com.visellico.rainecloud.serialization.RCField;
import com.visellico.rainecloud.serialization.RCObject;
import com.visellico.rainecloud.serialization.RCString;

public class Terrain extends LevelObject {

	int width, height;
	public static final String TERRAIN_NAME = "terrain";
	
	/**
	 * serializes all aspects of Terrain that are non-specific
	 * @param t
	 * @return
	 */
	public RCObject serialize() {
		
		RCObject objTerrain = new RCObject(TERRAIN_NAME);
		
		objTerrain.addString(RCString.Create("type", serialName));
		
		objTerrain.addField(RCField.Int("x", x));
		objTerrain.addField(RCField.Int("y", y));
		objTerrain.addField(RCField.Int("width", width));
		objTerrain.addField(RCField.Int("height", height));
		
		return objTerrain;
		
	}
	
	/**
	 * Ugh never mind
	 * @param objTerrain
	 * @return
	 * @note I cant the java
	 * @DONOTUSE
	 */
	public static Terrain deserializeTerrain(RCObject objTerrain) {
	
		Terrain t = new Terrain();
		
		t.x = objTerrain.findField("x").getInt();
		t.y = objTerrain.findField("y").getInt();
		t.width = objTerrain.findField("width").getInt();
		t.height = objTerrain.findField("height").getInt();
		
		return t;
	}
	
	public static Terrain deserializeSubClass(RCObject objTerrain, String type) {
		Terrain t = new Terrain();
		
		switch (type) {
		case (Floor.FLOOR_TYPE_NAME): return Floor.deserializeFloor(objTerrain);
		}
		
		return t;
	}

	public void render(Screen screen) {
	}

	public void update() {
	}
	
//	public static class HeightComparator implements Comparator<Terrain> {
//
//		public int compare(Terrain o1, Terrain o2) {
//			return o2.height - o1.height;
//			//Sends the larger height back
//		}
//		
//	}
	
	
	//Small note, I may want to replace this with if statement conditionals, i,e if 01 < 02 return -1 or whatever, instead of this subtraction method.
	//	I readthat it was more of a "trick" but, Im not sure what that is supposed to mean.
	//	Note: returns negative if the first object is less than the second,
	//	0 if they're the same,
	//	positive if the first one is greater. I reversed the subtraction order because I want it to do the ordering in reverse (high to low).
	public static void sort(List<Terrain> list) {
		
		Collections.sort(list, (o1, o2) -> {return o2.height - o1.height;});	//new HeightComparator()
		
	}

	public void onEvent(Event event) {
		EventDispatcher dispatch = new EventDispatcher(event);
		
		dispatch.dispatch(Event.Type.MOUSE_PRESSED, (Event e) -> onMousePress((MousePressedEvent) event));
		
	}

	private boolean onMousePress(MousePressedEvent e) {
		System.out.println(this.serialName + " was clicked");
		return true;
	}
	
	public void init(List<LayerLE> l) {
	}

	public void init(Level l) {
		// TODO Auto-generated method stub
		
	}
	
}
