package com.visellico.platty.leveleditor.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.farr.Events.Event;
import com.visellico.graphics.Screen;
import com.visellico.platty.Assets;
import com.visellico.platty.level.LevelType;
import com.visellico.platty.leveleditor.Layer;
import com.visellico.platty.leveleditor.Renderable;
import com.visellico.platty.leveleditor.Level.LevelObjects.LevelObject;

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
	
	
	public LevelType levelType;
	public List<LevelObject> levelObjects = new ArrayList<>();
	public LevelObject selectedLevelObject = null;
	
	
	//Load into memory the available level types
	static {
		System.out.println("Loading Level types: Default");
		loadLevelTypes(System.getProperty("user.dir") + "/res" + Assets.dirDefaultLevelTypes, defaultLevelTypes);
		System.out.println("Loading Level types: Custom");
		loadLevelTypes(System.getProperty("user.dir") + "/res" + Assets.dirCustomLevelTypes, customLevelTypes);
	}
	
	public String name;
	public String levelTypeName;
	
	public int width, height;
	
	/**
	 * Creates a new, blank level, of a default type.
	 */
	public Level() {
		width = MINIMUM_WIDTH;
		height = MINIMUM_HEIGHT;
		levelTypeName = DEFAULT_LEVEL_TYPE;
		try {
			levelType = new LevelType(levelTypeName, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
	
	public void requestSelected(LevelObject lo) {
		if (lo.isSelected) return;
		deselect();
		lo.isSelected = true;
		selectedLevelObject = lo;
	}
	
	public void deselect() {
		for (LevelObject lo : levelObjects) {
			lo.isSelected = false;
		}
		selectedLevelObject = null;
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
		screen.renderPoint(-10, -10, 0x0);
		System.out.println("Hey");
	}


	public void update() {
		
	}

	public void onEvent(Event e) {
		
	}

	public void init(List<Layer/*com.visellico.platty.leveleditor.Layer*/> l) {
		// TODO Auto-generated method stub
		
	}

	
	
}
