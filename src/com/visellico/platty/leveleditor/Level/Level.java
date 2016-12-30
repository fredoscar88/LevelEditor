package com.visellico.platty.leveleditor.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.farr.Events.Event;
import com.visellico.graphics.Screen;
import com.visellico.platty.Assets;
import com.visellico.platty.level.LevelType;
import com.visellico.platty.leveleditor.LayerLE;
import com.visellico.platty.leveleditor.Renderable;
import com.visellico.platty.leveleditor.Level.LevelObjects.Background;
import com.visellico.platty.leveleditor.Level.LevelObjects.LevelObject;
import com.visellico.platty.leveleditor.Level.LevelObjects.Terrain.Terrain;
import com.visellico.rainecloud.serialization.RCDatabase;
import com.visellico.rainecloud.serialization.RCObject;

/**
 * Container for all of the terrain/objects situated inside of a Platty level.
 * Vehicle for serialization. Editing tools largely affect this, i.e by adding new objects to a "level".
 * @author Henry
 *
 */
public class Level implements Renderable {
	
	public static final int MINIMUM_WIDTH = 500;
	public static final int MINIMUM_HEIGHT = 250;
	public static final String DEFAULT_LEVEL_TYPE = "Fields";	
	
	public static List<String> defaultLevelTypes;
	public static List<String> customLevelTypes;
	
	
	public LevelType levelType;
	public boolean isDefault;
	public List<LevelObject> levelObjects = new ArrayList<>();
	public List<Terrain> terrains = new ArrayList<>();
	public LevelObject selectedLevelObject = null;
	
	public Background background;
	
	
	//Load into memory the available level types
//	static {
//		System.out.println("Loading Level types: Default");
//		loadLevelTypes(System.getProperty("user.dir") + "/res" + Assets.dirDefaultLevelTypes, defaultLevelTypes);
//		System.out.println("Loading Level types: Custom");
//		loadLevelTypes(System.getProperty("user.dir") + "/res" + Assets.dirCustomLevelTypes, customLevelTypes);
//	}
	
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
		isDefault = true;
		name = "New Level";
		
		loadLevelTypeAssets(levelTypeName, isDefault);
		
		add(new Background());
//		add(new Floor(50, 50));
//		add(new Floor(30, 60));
//		add(new Floor(54, 54));
		
		sort();
		
	}
	
	public void add(LevelObject lo) {
		levelObjects.add(lo);
		
		if (lo instanceof Background) background = (Background) lo;
		if (lo instanceof Terrain) terrains.add((Terrain) lo);
		
		lo.init(this);
	}
	
	//Sorts array list in this order:
	/**
	 * Sorts levelObject list into proper order
	 * Background, floor, wall, misc terrain,
	 * Moving terrain, items, entities, foreground objects
	 */
	public void sort() {
		
//		System.out.println("Debug, Level Class, Sort method\nSorting level objects...");
		
		Terrain.sort(terrains);
		
		levelObjects = new ArrayList<>();
		
		levelObjects.add(background);
		for (LevelObject lo : terrains)
			levelObjects.add(lo);
		
	}
	
	public void requestSelected(LevelObject lo) {
		if (lo.isSelected) return;
		deselect();
		lo.isSelected = true;
		selectedLevelObject = lo;
	}
	
	/**
	 * Deserializes a level, including all objects and meta data such as level type and width/height
	 * @param filePath
	 * @return
	 */
	public static Level deserializeFromFile(String filePath) {
		
		RCDatabase db = RCDatabase.deserializeFromFile(filePath);
		System.out.println(db);
		
		if (db == null) return null;
		
		Level l = new Level();
		
		//Set member variables
		l.name = db.getName();
		
		for (RCObject obj : db.objects) {
			
			LevelObject lo = null;
			
			switch (obj.getName()) {
			case (Terrain.TERRAIN_NAME): 
				lo = Terrain.deserializeSubClass(obj, obj.findString("type").getString()); break;
				
			}
			l.add(lo);
			
		}
		
		l.sort();
		
		return l;
	}
	
	public void serialize(String directory) {
		
		directory = directory + "/"+ name + ".lvl";
		File lvlFile = new File(directory);
		if (!lvlFile.exists()) {
			try {
				lvlFile.createNewFile();
				System.out.println("psst new file " + lvlFile.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		RCDatabase db = new RCDatabase(name);
		RCObject obj;
		
		for (LevelObject lo : levelObjects) {
			obj = lo.serialize();
			if (obj != null)
				db.addObject(obj);				
		}
		
		
		
		System.out.println(directory);
		db.serializeToFile(directory);
		
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
	private static void loadLevelTypes(String parentDirectoryPath, List<String> loc) {
		
		File parentDirectory = new File(parentDirectoryPath);
		String[] fileNames = parentDirectory.list();
		
		for (int i = 0; i < fileNames.length; i++) {
			loc.add(fileNames[i]);// = fileNames[i];
			System.out.println(loc.get(i));
		}
	}
	
	private void loadLevelTypeAssets(String levelTypeName, boolean isDefault) {
		
		try {
			levelType = new LevelType(levelTypeName, isDefault);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (LevelObject lo : levelObjects) {
			lo.init(this);
		}
		
	}
	
	public void render(Screen screen) {
		screen.renderFilledRec(0, 0, width, height, 0x202020);	
		
//		if (background != null) background.render(screen);
		
		for (LevelObject lo : levelObjects) {
			lo.render(screen);
		}
		
		screen.renderLine(0, 0, width, 0, 0xFF0000);
		screen.renderLine(0, 0, 0, height, 0xFF0000);
		screen.renderLine(width, 0, width, height, 0xFF0000);
		screen.renderLine(0, height, width, height, 0xFF0000);
		screen.renderPoint(width, height, 0xFF0000);
		
	}


	public void update() {
		
	}

	public void onEvent(Event e) {
		
	}

	public void init(List<LayerLE/*com.visellico.platty.leveleditor.Layer*/> l) {
		// TODO Auto-generated method stub
		
	}

	public static void initialize() {
		
		defaultLevelTypes = new ArrayList<>();
		customLevelTypes = new ArrayList<>();
		
		System.out.println("Loading Level types: Default");
		loadLevelTypes(System.getProperty("user.dir") + "/res" + Assets.dirDefaultLevelTypes, defaultLevelTypes);
		System.out.println("Loading Level types: Custom");
		loadLevelTypes(System.getProperty("user.dir") + "/res" + Assets.dirCustomLevelTypes, customLevelTypes);
		System.out.println("Debug from Level.initialize()");
		System.out.println(defaultLevelTypes);
		System.out.println(customLevelTypes);
	}
	
}
