package com.visellico.platty.leveleditor.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.farr.Events.Event;
import com.farr.Events.EventDispatcher;
import com.farr.Events.types.MousePressedEvent;
import com.visellico.graphics.Screen;
import com.visellico.graphics.ui.UIPanel;
import com.visellico.platty.Assets;
import com.visellico.platty.level.LevelType;
import com.visellico.platty.leveleditor.Editor;
import com.visellico.platty.leveleditor.LayerLE;
import com.visellico.platty.leveleditor.Renderable;
import com.visellico.platty.leveleditor.Level.LevelObjects.Background;
import com.visellico.platty.leveleditor.Level.LevelObjects.LevelObject;
import com.visellico.platty.leveleditor.Level.LevelObjects.Terrain.Terrain;
import com.visellico.platty.leveleditor.Level.LevelObjects.Terrain.Wall;
import com.visellico.rainecloud.serialization.RCDatabase;
import com.visellico.rainecloud.serialization.RCField;
import com.visellico.rainecloud.serialization.RCObject;
import com.visellico.rainecloud.serialization.RCString;
import com.visellico.util.FileUtils;
import com.visellico.util.MathUtils;
import com.visellico.util.Vector2i;

/**
 * Container for all of the terrain/objects situated inside of a Platty level.
 * Vehicle for serialization. Editing tools largely affect this, i.e by adding new objects to a "level".
 * @author Henry
 *
 */
public class Level implements Renderable {
	
	public Editor editor;
	public static final int MINIMUM_WIDTH = 500;
	public static final int MINIMUM_HEIGHT = 250;
	public static final int MAXIMUM_WIDTH = Integer.MAX_VALUE;
	public static final int MAXIMUM_HEIGHT = Integer.MAX_VALUE;
	
	public static final String DEFAULT_LEVEL_TYPE = "Fields";	
	
	public static List<String> defaultLevelTypes;
	public static List<String> customLevelTypes;
	
	public static List<String> defaultLevels;
	public static List<String> customLevels;
	
	public static LevelObjectFactory lof = new LevelObjectFactory();
	
	
	public volatile LevelType levelType;
	public boolean isDefault;
	public boolean usesDefaultAssets = true;
	public volatile List<LevelObject> levelObjects = new ArrayList<>();
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
	public Level(Editor editor, boolean isDefault) {
		width = MINIMUM_WIDTH;
		height = MINIMUM_HEIGHT;
		levelTypeName = DEFAULT_LEVEL_TYPE;
		this.isDefault = isDefault;
		name = "TEMPORARY NAME";
		
		this.editor = editor;
		
		loadLevelTypeAssets(levelTypeName, true);
		
		add(new Background());
//		add(new Floor(50, 50));
//		add(new Floor(30, 60));
//		add(new Floor(54, 54));
		
		sort();
		
	}
	
	private Level(String name, String levelTypeName, int width, int height, boolean isDefault, boolean usesDefaultAssets, Editor editor) {
		this.name = name;
		this.levelTypeName = levelTypeName;
		this.width = MathUtils.clamp(width, MINIMUM_WIDTH, MAXIMUM_WIDTH);
		this.height = MathUtils.clamp(height, MINIMUM_HEIGHT, MAXIMUM_HEIGHT);
		this.isDefault = isDefault;
		this.usesDefaultAssets = usesDefaultAssets;
		
		this.editor = editor;
		
		editor.textLevelWidth.setText(Integer.toString(width));
		editor.textLevelHeight.setText(Integer.toString(height));
		
		loadLevelTypeAssets(levelTypeName, usesDefaultAssets);
		
		add(new Background());
		
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
		lo.onSelect();
		if (lo.panelProperties != null) {
			editor.panelProperties = lo.panelProperties;
		} else {
			System.out.println("whaat");
			editor.panelProperties= new UIPanel(new Vector2i(200, 800), new Vector2i (1400, 100));
			editor.panelProperties.setColor(0x55BAE8);	//Magic values ayy
		}
	}
	
	/**
	 * Deserializes a level, including all objects and meta data such as level type and width/height
	 * @param filePath
	 * @return
	 */
	public static Level deserializeFromFile(String filePath, Editor editor) {
		
		RCDatabase db = RCDatabase.deserializeFromFile(filePath);
		String name = FileUtils.stripPath(FileUtils.stripExtension(filePath));
		
		if (db == null) return null;
		
		RCObject meta = db.objects.get(0);
		Level l = null;
		try {
			l = new Level(
				name,	//db.getName(), 
				meta.findString("type").getString(), 
				meta.findField("width").getInt(), 
				meta.findField("height").getInt(), 
				meta.findField("isDefault").getBoolean(),
				meta.findField("usesDefaultAssets").getBoolean(),
				editor);
		} catch (Exception e) {
			l = new Level(
				name,	//db.getName(), 
				Level.DEFAULT_LEVEL_TYPE, 
				meta.findField("width").getInt(), 
				meta.findField("height").getInt(), 
				meta.findField("isDefault").getBoolean(),
				true,
				editor);
		}
		System.out.println(l.isDefault);
		
		//THIS STARTS AT 1
		RCObject obj;
		for (int i = 1; i < db.objects.size(); i++) {
			LevelObject lo = null;
			obj = db.objects.get(i);
			
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
		
		RCObject lvlMeta = new RCObject("meta");
		lvlMeta.addString(RCString.Create("type", levelTypeName));
		lvlMeta.addField(RCField.Boolean("isDefault", isDefault));
		lvlMeta.addField(RCField.Boolean("usesDefaultAssets", usesDefaultAssets));
		lvlMeta.addField(RCField.Int("width", width));
		lvlMeta.addField(RCField.Int("height", height));
		db.addObject(lvlMeta);
		
		RCObject obj;
		for (LevelObject lo : levelObjects) {
			obj = lo.serialize();
			if (obj != null)
				db.addObject(obj);				
		}
		
//		System.out.println(directory);
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
//	private static void loadLevelTypes(String parentDirectoryPath, List<String> loc) {
//		
//		File parentDirectory = new File(parentDirectoryPath);
//		String[] fileNames = parentDirectory.list();
//		
//		for (int i = 0; i < fileNames.length; i++) {
//			loc.add(fileNames[i]);// = fileNames[i];
////			System.out.println(loc.get(i));
//		}
//	}
	
	/**
	 * This method could be used to load assets too, but it discards levelType each time so nah.
	 * @param levelTypeName
	 * @param isDefault
	 */
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
	
	public void switchAssets(String levelTypeName, boolean isDefaultAsset) {
		try {
			levelType = new LevelType(levelTypeName, isDefaultAsset);
			this.levelTypeName = levelTypeName;
			this.usesDefaultAssets = isDefaultAsset;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Rather than call the reloadAssets, Im going to re-init the LevelObjects here. Clearly there is some inefficiency, but I don't care to care.
		for (LevelObject o : levelObjects){
			o.init(this);
		}
	}
	
	public void reloadAssets() {
		
		try {
			levelType.loadAssets(levelType.levelTypeDirectoryPath);
		} catch (IOException e) {
			try {
				this.levelTypeName = Level.DEFAULT_LEVEL_TYPE;
				levelType = new LevelType(Level.DEFAULT_LEVEL_TYPE, true);
			} catch (IOException e1) {
				System.err.println("You did the wrong");
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		for (LevelObject o : levelObjects)
			o.init(this);
//			System.out.println(o);
			

	}
	
	public synchronized void render(Screen screen) {
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
//		editor.textLevelWidth;
//		editor.textLevelHeight;
		
		width = MathUtils.parseInt(editor.textLevelWidth.getText());
		height = MathUtils.parseInt(editor.textLevelHeight.getText());
		
		width = MathUtils.clamp(width, MINIMUM_WIDTH, MAXIMUM_WIDTH);
		height = MathUtils.clamp(height, MINIMUM_HEIGHT, MAXIMUM_HEIGHT);
		
		if (!editor.textLevelWidth.getFocused()) editor.textLevelWidth.setText(Integer.toString(width));
		if (!editor.textLevelHeight.getFocused()) editor.textLevelHeight.setText(Integer.toString(height));
		
		for (LevelObject lo : levelObjects) {
			lo.update();
		}
	}

	public void onEvent(Event event) {
		
		EventDispatcher dispatcher = new EventDispatcher(event);
		
		dispatcher.dispatch(Event.Type.MOUSE_PRESSED, (Event e) -> onMousePress((MousePressedEvent) e));
		
		for (int i = levelObjects.size() - 1; i >= 0; i--) {
			levelObjects.get(i).onEvent(event);
		}
	}
	
	public synchronized boolean onMousePress(MousePressedEvent e) {
		
		if (e.getButton() == 2) {
//			add(ct.create(editor.mouseXToScreenX(e.getX()) + editor.screenScrollX, editor.mouseYToScreenY(e.getY()) + editor.screenScrollY));
//			add(new Wall(editor.mouseXToScreenX(e.getX()) + editor.screenScrollX, editor.mouseYToScreenY(e.getY()) + editor.screenScrollY));
			add(lof.create(editor.mouseXToScreenX(e.getX()) + editor.screenScrollX, editor.mouseYToScreenY(e.getY()) + editor.screenScrollY));
		}
		
		return false;
	}

	public void init(List<LayerLE> l) {
		// TODO Auto-generated method stub
		
	}

	public static void initialize() {
		
		defaultLevelTypes = new ArrayList<>();
		customLevelTypes = new ArrayList<>();
		
		defaultLevels = new ArrayList<>();
		customLevels = new ArrayList<>();
//		allLevels = new ArrayList<>();
		
//		System.out.println("Loading Level types: Default");
//		loadLevelTypes(Assets.prgmDir + Assets.dirDefaultLevelTypes, defaultLevelTypes);
		FileUtils.loadNames(Assets.prgmDir + Assets.dirDefaultLevelTypes, defaultLevelTypes);
		System.out.println(defaultLevelTypes);

//		System.out.println("Loading Level types: Custom");
//		loadLevelTypes(Assets.prgmDir + Assets.dirCustomLevelTypes, customLevelTypes);
		FileUtils.loadNames(Assets.prgmDir + Assets.dirCustomLevelTypes, customLevelTypes);
		System.out.println(customLevelTypes);
		
		
		FileUtils.loadNames(Assets.prgmDir + Assets.dirDefaultLevels, defaultLevels);
		System.out.println(defaultLevels);
		FileUtils.loadNames(Assets.prgmDir + Assets.dirCustomLevels, customLevels);
		System.out.println(customLevels);
		
//		allLevels.addAll(defaultLevels);
//		allLevels.addAll(customLevels);
				
	}
	
}
