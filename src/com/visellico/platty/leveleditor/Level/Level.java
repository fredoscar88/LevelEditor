package com.visellico.platty.leveleditor.Level;

import java.io.File;
import java.util.List;

import com.farr.Events.Event;
import com.visellico.graphics.Screen;
import com.visellico.platty.Assets;
import com.visellico.platty.leveleditor.Layer;
import com.visellico.platty.leveleditor.Renderable;

/**
 * Container for all of the terrain/objects situated inside of a Platty level.
 * Vehicle for serialization. Editing tools largely affect this, i.e by adding new objects to a "level".
 * @author Henry
 *
 */
public class Level implements Renderable {
	
	public static final int MINIMUM_WIDTH = 500;
	public static final int MINIMUM_HEIGHT = 200;
	public static final String DEFAULT_LEVEL_TYPE = "Fields";
	
	public static String[] defaultLevelTypes;
	public static String[] customLevelTypes;
	
	//Load into memory the available level types
	static {
		System.out.println("Loading Level types");
		loadLevelTypes(System.getProperty("user.dir") + "/res" + Assets.dirDefaultLevelTypes, defaultLevelTypes);
		loadLevelTypes(System.getProperty("user.dir") + "/res" + Assets.dirCustomLevelTypes, customLevelTypes);
	}
	
	public String name;
	public String levelType;
	
	public int width, height;
	
	/**
	 * Creates a new, blank level, of a default type.
	 */
	public Level() {
		width = MINIMUM_WIDTH;
		height = MINIMUM_HEIGHT;
		levelType = DEFAULT_LEVEL_TYPE;
		
	}
	
	/**
	 * Deserializes a level, including all objects and meta data such as level type and width/height
	 * @param filePath
	 * @return
	 */
	public static Level deserializeFromFile(String filePath) {
		Level l = new Level();
		
		//Set member varuabkes
		
		return l;
	}
	
	/*
	 * Static method called when the program is loaded.
	 * Loads the names of each directory inside the default, and custom, level type directories into memory,
	 * in the defaultLEvelTypes list and customLevelTypes list respectively.
	 */
	private static void loadLevelTypes(String parentDirectoryPath, String[] loc) {
		
		File parentDirectory = new File(parentDirectoryPath);
		String[] fileNames = parentDirectory.list();
		loc = new String[fileNames.length];
		
		for (int i = 0; i < fileNames.length; i++) {
			loc[i] = fileNames[i];
			System.out.println(loc[i]);
		}
	}
	
	public void render(Screen screen) {
		
	}


	public void update() {
		
	}

	public void onEvent(Event e) {
		
	}

	public void init(List<Layer/*com.visellico.platty.leveleditor.Layer*/> l) {
		// TODO Auto-generated method stub
		
	}

	
	
}
