package com.visellico.platty.leveleditor.Level.LevelObjects;

import com.visellico.graphics.Screen;
import com.visellico.platty.leveleditor.Renderable;
import com.visellico.platty.leveleditor.Level.Addable;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.rainecloud.serialization.RCObject;

public abstract class LevelObject implements Addable, Renderable {

	public int x, y;
	public String serialName;
	
	
//	public int listID;
//	public static int nextListID = 0;
//	protected Sprite sprite; //Sprites are handled for each object individually, as some have several, its inconsistent to say that they all have specifically 1.
	//Some may not even render any sprites!
	
	public boolean isSelected;
	public Level l;	//May not be needed
	
	public abstract void render(Screen screen);
	
	public abstract RCObject serialize();
	
//	public abstract LevelObject deserialize(RCObject obj);
	
//	public void remove() {
//		l.remove(this);
//	}
	
	//Would love to put info about serializing/deserializing, but I don't think I can fit it here,
	//because of the different return types for deserializing. Serialize, maybe. But it would feel incomplete :c
	
}
