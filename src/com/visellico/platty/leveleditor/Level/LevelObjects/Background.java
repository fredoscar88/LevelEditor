package com.visellico.platty.leveleditor.Level.LevelObjects;

import java.util.List;

import com.farr.Events.Event;
import com.farr.Events.EventDispatcher;
import com.farr.Events.types.MousePressedEvent;
import com.visellico.graphics.Screen;
import com.visellico.graphics.Sprite;
import com.visellico.graphics.ui.UIPanel;
import com.visellico.platty.leveleditor.LayerLE;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.rainecloud.serialization.RCObject;
import com.visellico.util.Vector2i;

public class Background extends LevelObject {

	public static Sprite spriteBackground;
	
	public int width, height;
	
	public Background() {
//		UIPanel panel = l.editor.propertyPanel;
//		panelProperties = new UIPanel(new Vector2i(panel.position.x,panel.position.y), new Vector2i(panel.size.x, panel.size.y)).setColor(panel.getColor());
	}

	public void init(Level l) {
		this.l = l;
		spriteBackground = l.levelType.spriteBackground;
		update();
		
	}

	public void update() {
		this.width = l.width;
		this.height = l.height;
	}

	public void onEvent(Event event) {
		EventDispatcher dispatch = new EventDispatcher(event);
		dispatch.dispatch(Event.Type.MOUSE_PRESSED, (Event e) -> onMousePress((MousePressedEvent) event));
	}
	
	protected boolean onMousePress(MousePressedEvent e) {
		int levelX = l.editor.mouseXToScreenX(e.getX()) + l.editor.screenScrollX;
		int levelY = l.editor.mouseYToScreenY(e.getY()) + l.editor.screenScrollY;
		
		if (levelX >= x && levelX < x + width) {
			if (levelY >= y && levelY < height) {
				l.requestSelected(this);
				
				return true;
			}
		}
		
		return false;
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

	
	
	public void onSelect() {
	}
	
}
