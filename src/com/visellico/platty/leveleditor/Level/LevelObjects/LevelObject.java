package com.visellico.platty.leveleditor.Level.LevelObjects;

import com.visellico.graphics.ui.UIPanel;
import com.visellico.platty.leveleditor.Renderable;
import com.visellico.platty.leveleditor.Level.Addable;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.platty.leveleditor.Level.LevelObjects.Terrain.Floor;
import com.visellico.rainecloud.serialization.RCObject;

public abstract class LevelObject implements Addable, Renderable {

	public int x, y;
	public String serialName;
	
	//Really should handle more stuff in this super level class, but I think Im going to distribute work to the immediate subclasses for stuff like dat x and y
	public UIPanel panelProperties;
	
//	public int listID;
//	public static int nextListID = 0;
//	protected Sprite sprite; //Sprites are handled for each object individually, as some have several, its inconsistent to say that they all have specifically 1.
	//Some may not even render any sprites!
	
	public boolean isSelected;
	public Level l;	
	
	public void init(Level l) {
		this.l = l;
	}
	
//	public abstract void render(Screen screen);
//	
//	public abstract void update();
//	
//	public abstract boolean onEvent(Event e);
//	
	public abstract RCObject serialize();
	
	public int getLevelXFromMouse(int x) {
		return l.editor.mouseXToScreenX(x) + l.editor.screenScrollX;
	}
	public int getLevelYFromMouse(int y) {
		return l.editor.mouseYToScreenY(y) + l.editor.screenScrollY;
	}
	
	public abstract void onSelect();
	
	public LevelObject createNew(int x, int y) {
		//Default thing to return.
		return new Floor(x, y);
	}
	
//	public abstract LevelObject deserialize(RCObject obj);
	
//	public void remove() {
//		l.remove(this);
//	}
	
	//Would love to put info about serializing/deserializing, but I don't think I can fit it here,
	//because of the different return types for deserializing. Serialize, maybe. But it would feel incomplete :c
	
}
