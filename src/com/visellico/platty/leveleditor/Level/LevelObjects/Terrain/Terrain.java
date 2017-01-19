package com.visellico.platty.leveleditor.Level.LevelObjects.Terrain;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import com.farr.Events.Event;
import com.farr.Events.EventDispatcher;
import com.farr.Events.types.MouseMovedEvent;
import com.farr.Events.types.MousePressedEvent;
import com.farr.Events.types.MouseReleasedEvent;
import com.visellico.graphics.Screen;
import com.visellico.graphics.ui.UIButton;
import com.visellico.graphics.ui.UILabel;
import com.visellico.graphics.ui.UIPanel;
import com.visellico.graphics.ui.UITextField;
import com.visellico.platty.leveleditor.Editor;
import com.visellico.platty.leveleditor.LayerLE;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.platty.leveleditor.Level.LevelObjects.LevelObject;
import com.visellico.rainecloud.serialization.RCField;
import com.visellico.rainecloud.serialization.RCObject;
import com.visellico.rainecloud.serialization.RCString;
import com.visellico.util.MathUtils;
import com.visellico.util.Vector2i;

public abstract class Terrain extends LevelObject {

	int width, height;
	public static final String TERRAIN_NAME = "terrain";
	
	//These two ints are only used/updated when moveWithMouse is true.
	protected int mouseXDistFromX;
	protected int mouseYDistFromY;
	private boolean moveWithMouse = false;
	
	protected UIPanel panelEditor; 
	
	//------ UIComponents
	
	protected UITextField valX = new UITextField(new Vector2i(120,25), 100, "").setFont(Editor.fontEditField);
	protected UITextField valY = new UITextField(new Vector2i(120,65), 100, "").setFont(Editor.fontEditField);
	protected UITextField valWidth = new UITextField(new Vector2i(230,25), 100, "").setFont(Editor.fontEditField);
	protected UITextField valHeight = new UITextField(new Vector2i(230,65), 100, "").setFont(Editor.fontEditField);
	protected UIButton btnRemove = new UIButton(new Vector2i(350,25), new Vector2i(100,60), () -> {remove();}, "Delete");
	protected UILabel lblName = new UILabel(new Vector2i(10,25),"PLACEHOLDER",Editor.fontScrollList,() -> {System.out.println("God I love these");});
	
	//------

	public Terrain(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	private Terrain() {
	}
	
	
	
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
	
	
	public static Terrain deserializeSubClass(RCObject objTerrain, String type) {
		
		
		switch (type) {
		case (Floor.FLOOR_TYPE_NAME): return Floor.deserializeFloor(objTerrain);
		case (Wall.WALL_TYPE_NAME): return Wall.deserializeFloor(objTerrain);
		default: return null;
		}
		
	}

	public void render(Screen screen) {
	}

	public void update() {
		
//		System.out.println(this);
		//TODO CONDITION TO CHECK IF OBJECT IS OUTSIDE OF LEVEL BOUNDARY- IF SO, REMOVE
		
		height = MathUtils.parseInt(valHeight.getText());
		height = MathUtils.clamp(height, 5, l.height - 1);
		
		y = MathUtils.parseInt(valY.getText());
		y = MathUtils.clamp(y, height, l.height - 1);

		x = MathUtils.clamp(x, 1, l.width - width);
		x = MathUtils.parseInt(valX.getText());

		//Width is clamped before X because X clamp depends on the width clamp
		//And I put height before it too just because.
		width = MathUtils.parseInt(valWidth.getText());
		width = MathUtils.clamp(width, 2, l.width - x);
		
		if (!valX.getFocused()) valX.setText(Integer.toString(x));
		if (!valY.getFocused()) valY.setText(Integer.toString(y));
		if (!valWidth.getFocused()) valWidth.setText(Integer.toString(width));
		if (!valHeight.getFocused()) valHeight.setText(Integer.toString(height));
		
//		y = MathUtils.clamp(y,2, l.height);
//		height = MathUtils.clamp(height, 2, l.height);
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
	//	I read that it was more of a "trick" but, Im not sure what that is supposed to mean.
	//	Note: returns negative if the first object is less than the second,
	//	0 if they're the same,
	//	positive if the first one is greater. I reversed the subtraction order because I want it to do the ordering in reverse (high to low).
	public static void sort(List<Terrain> list) {
		
		Collections.sort(list, (o1, o2) -> {return o2.y - o1.y;});	//new HeightComparator()
		
	}
	
	public void onSelect() {
		//panelEditor = panelProperties;
//		panelEditor.size.x -= 100;
	}

	public void onEvent(Event event) {
		EventDispatcher dispatch = new EventDispatcher(event);
		
		dispatch.dispatch(Event.Type.MOUSE_PRESSED, (Event e) -> onMousePress((MousePressedEvent) event));
		dispatch.dispatch(Event.Type.MOUSE_MOVED, (Event e) -> onMouseMove((MouseMovedEvent) event));
		dispatch.dispatch(Event.Type.MOUSE_RELEASED, (Event e) -> onMouseRelease((MouseReleasedEvent) event));
	}
	
	protected boolean onMousePress(MousePressedEvent e) {

//		int levelX = l.editor.mouseXToScreenX(e.getX()) + l.editor.screenScrollX;
//		int levelY = l.editor.mouseYToScreenY(e.getY()) + l.editor.screenScrollY;
		int levelX = getLevelXFromMouse(e.getX());
		int levelY = getLevelYFromMouse(e.getY());
		
		if (levelX >= x && levelX < x + width) {
			if (levelY <= y && levelY > y - height) {
				
				moveWithMouse = true;
				
				mouseXDistFromX = levelX - x;	//Both of these should return the correct difference. Im starting to think I need a "difference" math function!
				mouseYDistFromY = y - levelY;	//It's a pain in the ass to have to use a different standard for Y because it is inverted.
//				System.out.println(mouseXDistFromX + " " + mouseYDistFromY);
				
				l.requestSelected(this);
				
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean onMouseRelease(MouseReleasedEvent e) {
		if (moveWithMouse) {
			moveWithMouse = false;
			l.sort();	//Restacks all of the level objects so we can't accidentally hide them behind other objects
			return true;
		}
		return false;
	}
	
	protected boolean onMouseMove(MouseMovedEvent e) {
		
		if (moveWithMouse) {
//			int mouseXInLevel = getLevelXFromMouse(e.getX());
//			int mouseYInLevel = getLevelXFromMouse(e.getY());
			
			int mouseXAdjusted = getLevelXFromMouse(e.getX()) - mouseXDistFromX;
			int mouseYAdjusted = getLevelYFromMouse(e.getY()) + mouseYDistFromY;	//Classic example of the problems of inverting one coordinate thingy.
			
//			x = MathUtils.clamp(getLevelXFromMouse(e.getX()),1, l.width - width);
//			y = MathUtils.clamp(getLevelYFromMouse(e.getY()),height, l.height - 1);
			x = MathUtils.clamp(mouseXAdjusted,1, l.width - width);
			y = MathUtils.clamp(mouseYAdjusted,height, l.height - 1);
						
			valX.setText(Integer.toString(x));
			valY.setText(Integer.toString(y));
			return true;
		}
		return false;
	}
	
	public void init(Level l) {
		super.init(l);
		
		panelEditor = this.l.editor.panelProperties;
//		this.l.editor.propertyPanel.size.x -= 100;
//		panelEditor.size.x /= 2;
		
		valX.setText(Integer.toString(x));
		valY.setText(Integer.toString(y));
		valWidth.setText(Integer.toString(width));
		valHeight.setText(Integer.toString(height));
		
		panelProperties = new UIPanel(new Vector2i(panelEditor.position.x, panelEditor.position.y), new Vector2i(panelEditor.size.x, panelEditor.size.y));
		panelProperties.add(valX);
		panelProperties.add(valY);
		panelProperties.add(valWidth);
		panelProperties.add(valHeight);
		panelProperties.add(btnRemove);
		panelProperties.add(lblName.setColor(Color.black));
		
	}
	
	public void init(List<LayerLE> l) {
	}

//	public void init(Level l) {
//		// TODO Auto-generated method stub
//		
//	}
	
}
