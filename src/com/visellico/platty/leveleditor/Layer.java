package com.visellico.platty.leveleditor;

import java.util.List;

import com.farr.Events.Event;

/**
 * Personalized layer to extend the Event handling API. Since a larger focus of this is on actual layer stuffs, I felt it important to 
 * use a different style of layering, one that doesn't require the renderer to explicitly use graphics. It's inconvenient.
 * @author Henry
 *
 */
public interface Layer {
	
	public void update();

	public void onEvent(Event e);
	
	public void init(List<Layer> l);
	
}
