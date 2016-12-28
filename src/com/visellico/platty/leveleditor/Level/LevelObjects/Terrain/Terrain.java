package com.visellico.platty.leveleditor.Level.LevelObjects.Terrain;

import com.visellico.graphics.Screen;
import com.visellico.platty.level.Level;
import com.visellico.platty.leveleditor.Level.LevelObjects.LevelObject;
import com.visellico.rainecloud.serialization.RCField;
import com.visellico.rainecloud.serialization.RCObject;
import com.visellico.rainecloud.serialization.RCString;

public class Terrain extends LevelObject {

	int width, height;
	protected String serialName;
	
	/**
	 * serializes all aspects of Terrain that are non-specific
	 * @param t
	 * @return
	 */
	public RCObject serialize(Terrain t) {
		
		RCObject objTerrain = new RCObject("terrain");
		
		objTerrain.addString(RCString.Create("type", t.serialName));
		
		objTerrain.addField(RCField.Int("x", t.x));
		objTerrain.addField(RCField.Int("y", t.y));
		objTerrain.addField(RCField.Int("width", t.width));
		objTerrain.addField(RCField.Int("height", t.height));
		
		return objTerrain;
		
	}
	
	protected Terrain deserialize(RCObject objTerrain) {
	
		Terrain t = new Terrain();
		
		x = objTerrain.findField("x").getInt();
		y = objTerrain.findField("y").getInt();
		width = objTerrain.findField("width").getInt();
		height = objTerrain.findField("height").getInt();
		
		return t;
	}

	public void init(Level l) {
		
	}

	public void render(Screen screen) {
		
	}

	

	
}
