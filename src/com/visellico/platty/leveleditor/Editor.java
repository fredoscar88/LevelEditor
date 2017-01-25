package com.visellico.platty.leveleditor;

import static com.visellico.platty.leveleditor.Level.LevelObjectFactory.Type.FLOOR;
import static com.visellico.platty.leveleditor.Level.LevelObjectFactory.Type.WALL;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.farr.Events.Event;
import com.farr.Events.EventDispatcher;
import com.farr.Events.EventListener;
import com.farr.Events.types.KeyTypedEvent;
import com.farr.Events.types.MouseMovedEvent;
import com.farr.Events.types.MousePressedEvent;
import com.farr.Events.types.MouseReleasedEvent;
import com.visellico.graphics.Screen;
import com.visellico.graphics.ui.UIButton;
import com.visellico.graphics.ui.UILabel;
import com.visellico.graphics.ui.UIPanel;
import com.visellico.graphics.ui.UIPromptOption;
import com.visellico.graphics.ui.UIScrollList;
import com.visellico.graphics.ui.UITextField;
import com.visellico.input.Focus;
import com.visellico.input.Keyboard;
import com.visellico.input.Mouse;
import com.visellico.platty.Assets;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.platty.leveleditor.Level.LevelObjectFactory;
import com.visellico.util.FileUtils;
import com.visellico.util.MathUtils;
import com.visellico.util.StringUtils;
import com.visellico.util.Vector2i;

/**
 * <p>Editor object for creating/editing levels.</p>
 * This class itself is not very object oriented, I blame my inexperience, and legacy stuff like UI panels and the conflictual
 * layer hierarchies that make it oh so fun to render and distribute events. All of the other classes however should look okay.
 * @author Henry
 *
 */
public class Editor extends Canvas implements Runnable, EventListener {

	//This is to shut up eclipse warnings
	private static final long serialVersionUID = 1L;
	
	private Thread thread;
	public Thread promptThread;
	public Thread saveThreadLol;
	public Thread loadThreadLol;
	private boolean running;
	
	private JFrame frame;
	private static final String TITLE = "Platty the Platformer | Level Editor";
	private static final String VERSION = "dev 1.0";
	private boolean isDevVersion = true;
	public static Font fontScrollList;
	public static Font fontEditField = new Font("Times New Roman", Font.PLAIN, 12);
	private UIScrollList listLevelObjects;
	private UIScrollList listDefaultLevelTypes;
	private UIScrollList listCustomLevelTypes;
	private UIScrollList listDefaultLevels;
	private UIScrollList listCustomLevels;
	private UITextField textFieldSaveNameAs;
	private UIButton buttonSaveNameAs;
	//No, I don't need anyone to tell me how terriblez this is.
	public UIPanel screenCover;
	public UIPanel panelLevelSelect;
	public UIPanel panelSaveAs;
	
	public static Editor editor;
	public int width = 1600;
	public int height = 900;
	public int defaultScale = 4;
	
	public Keyboard key;
	public Mouse mouse;
	public Focus focus;
	
	public UIPromptOption currentPrompt;
	
	private List<Renderable> screenRenderList = new ArrayList<>();
	private List<LayerLE> layerList = new ArrayList<>();
	private List<com.farr.Events.Layer> layerListClassic = new ArrayList<>();
	
	public Screen screen;
	public int screenCoordX = 200;
	public int screenCoordY = 0;
	public int screenScrollX;
	public int screenScrollY;
	public UIPanel panelEditor;
		public UITextField textLevelWidth;
		public UITextField textLevelHeight;
	public UIPanel panelProperties;
	
	//Mouse drag vars
	boolean dragging = false;
	int mouseDragXStart;
	int mouseDragYStart;
	int screenScrollStartX;
	int screenScrollStartY;
	int curMouseX;
	int curMouseY;
	
	public volatile Level level;	//Level being edited. Should be created by deserializing a file, or as a new blank slate.
	public volatile static String saveName;	//Name of the level we LOADED. if we created a NEW level, then this is null.
	
	//TODO this constructor does an awful lot for a constructor.
	//	A lot of initialization things, but it is beginning to feel more like something that would run at the start... not when constructed!
	//	ALSO TODO editor does NOT have support to edit multiple levels at once! shame really.
	public Editor() {
		
		Level.initialize();

		UIPromptOption startup = new UIPromptOption(width, height, "Select one", "New Level", "Load Level");
		
		Dimension size = new Dimension(width, height);
		setPreferredSize(size);
		
		frame = new JFrame() {
			protected void processWindowEvent(final WindowEvent e) {
				if (e.getID() == WindowEvent.WINDOW_CLOSING) {
					if (layerListClassic.size() > 0) super.processWindowEvent(e);
					setNonClashingLevelSaveName();
					save();
				}
				super.processWindowEvent(e);
		    }
		};
		
		
		screenCover = new UIPanel(new Vector2i(0,0), new Vector2i(width, height)).setColor(0x7f202020, true);
		
		panelLevelSelect = new UIPanel(new Vector2i(screenCover.size.x / 2 - 250, screenCover.size.y / 2 - 250), new Vector2i(500,500)).setColor(0x55BAE8, false);
		panelSaveAs = new UIPanel(new Vector2i(screenCover.size.x / 2 - 250, screenCover.size.y / 2 - 250), new Vector2i(500,500)).setColor(0x55BAE8, false);
		
		screen = new Screen((width - 200) / defaultScale, (height - 100) / defaultScale, defaultScale);
		screen.setOffset(-screen.width / 2, -screen.height/2);
		
		panelEditor = new UIPanel(new Vector2i(0,0), new Vector2i(200, height));
		panelEditor.setColor(0xE8BA55);
		panelProperties = new UIPanel(new Vector2i(200, height - 100), new Vector2i (width - 200, 100));
		panelProperties.setColor(0x55BAE8);
		
		startup.init(layerListClassic);
		currentPrompt = startup;
		promptThread = new Thread(() -> {
			currentPrompt.value = -1;
			int response = currentPrompt.awaitResponse();
			
			if (listDefaultLevels.size() == 0 && listCustomLevels.size() == 0) response = 0;
			if (listCustomLevels.size() == 0 && !isDevVersion) response = 0;
			
			switch (response) {
			case 0: 
				level = new Level(this, isDevVersion); //Dev version makes default levels. Really I should have a way to switch runtime. yeah..
				break;
			case 1: 
				load();
//				level = Level.deserializeFromFile("res/Levels/Default/Test Level.lvl"); 
//				saveName = level.name;
				break;

			}
			currentPrompt = null;
		}, "I BETTER NOT BE RUNNING LONG");
		promptThread.start();
		
//		saveThreadCreate();
		
		
		fontScrollList = new Font("Times New Roman", Font.PLAIN, 18);
		listDefaultLevels = new UIScrollList(new Vector2i(10,30), new Vector2i(180,100));
		listCustomLevels = new UIScrollList(new Vector2i(10, 160), new Vector2i(180,100));
		listDefaultLevelTypes = new UIScrollList(new Vector2i(10,75), new Vector2i(180,60));
		listCustomLevelTypes = new UIScrollList(new Vector2i(10,160), new Vector2i(180,60));
		listLevelObjects = new UIScrollList(new Vector2i(10,300), new Vector2i(180,60));
		
		populateScrollList(fontScrollList, listDefaultLevelTypes, Level.defaultLevelTypes, true);
		populateScrollList(fontScrollList, listCustomLevelTypes, Level.customLevelTypes, false);
		
		//Yes this is bad. TODO in the postmortem, point out how shite my ScrollList thingies are. And all of my supposedly OO shit is :c
		setScrollListLevel();
		setScrollListLevelObjects();

		textLevelWidth = new UITextField(new Vector2i(10,400), 85, "Width").setFont(fontEditField);
		textLevelHeight = new UITextField(new Vector2i(105,400), 85, "Height").setFont(fontEditField);
		
		
		textFieldSaveNameAs = new UITextField(new Vector2i(30,30),460,"Enter level name here");
		buttonSaveNameAs = new UIButton(new Vector2i(30,100), new Vector2i(115,30), () -> {saveName = textFieldSaveNameAs.getText(); layerListClassic.remove(screenCover);}, "Save");
//		panelSaveAs.addFocusable(textFieldSaveNameAs);
		panelSaveAs.add(textFieldSaveNameAs);
		panelSaveAs.add(buttonSaveNameAs);

		panelEditor.add(listDefaultLevelTypes);
		panelEditor.add(listCustomLevelTypes);
		panelEditor.add(listLevelObjects);
		panelEditor.add(textLevelWidth);
		panelEditor.add(textLevelHeight);
		
		panelLevelSelect.add(listDefaultLevels);
		panelLevelSelect.add(listCustomLevels);
				
		
		key = new Keyboard(this);
		System.out.println("Whats all this now?");
		try {
			mouse = new Mouse(this);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		focus = new Focus(this);
		
		addKeyListener(key);
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		addMouseWheelListener(mouse);
		addFocusListener(focus);
		
	}
	
	public void populateScrollList(Font font, UIScrollList scrList, List<String> strings, boolean isDefault) {
		scrList.clear();
		for (String lType : strings) {
			scrList.add(new UILabel(new Vector2i(0,0), lType, font,() -> {level.switchAssets(lType, isDefault);}).setYPaddingOffset(2).setColor(Color.black));
		}
	}
	public void setScrollListLevel() {
		listDefaultLevels.clear();
		listCustomLevels.clear();
		if (isDevVersion) 
			for (String levelName : Level.defaultLevels) {
				listDefaultLevels.add(new UILabel(new Vector2i(0,0), levelName, fontScrollList, () -> {listDefaultLevels.selectedItem = levelName;}).setYPaddingOffset(2).setColor(Color.black));
			}
//		else {
		for (String levelName : Level.customLevels) {
			listCustomLevels.add(new UILabel(new Vector2i(0,0), levelName, fontScrollList, () -> {listCustomLevels.selectedItem = levelName;}).setYPaddingOffset(2).setColor(Color.black));
		}
//		}
	}
	public void setScrollListLevelObjects() {	//Woo-ee I really love me some arbitrary methods
		listLevelObjects.clear();
		System.out.println(LevelObjectFactory.Type.values().length);
		for (LevelObjectFactory.Type t : LevelObjectFactory.Type.values()) {
			listLevelObjects.add(new UILabel(new Vector2i(0,0), StringUtils.capOnlyFirst(t.toString()), fontScrollList, () -> {Level.lof.setType(t);}).setYPaddingOffset(2).setColor(Color.black));
		}
	}
	
	//TODO In the future, or if I were a better programmer, this would either A) belong to level, or B) be static or C) both!
	public void save() {
		level.sort();
		
		//sa|veName is null if we created a newLevel
		if (saveName == null) {
			screenCover = new UIPanel(new Vector2i(0,0), new Vector2i(width, height)).setColor(0x7f202020, true);
			screenCover.add(panelSaveAs);
			layerListClassic.add(screenCover);
			while (saveName == null) {
				
			}
			level.name = saveName;
		}
		
		level.serialize(Assets.prgmDir + (level.isDefault ? Assets.dirDefaultLevels : Assets.dirCustomLevels));
		textFieldSaveNameAs.setText("");
	}
	
	public void setNonClashingLevelSaveName() {
		if (saveName == null) {
			String tempStr = "NewLevel";
			saveName = FileUtils.availableName(
					(Assets.prgmDir + (level.isDefault ? Assets.dirDefaultLevels : Assets.dirCustomLevels)),
					tempStr,
					".lvl");
			level.name = saveName;
		}
	}
	
	public void saveThreadCreate() {
		saveThreadLol = new Thread(() -> {save();}, "Save Thread");
	}
	public void loadThreadCreate() {
		loadThreadLol = new Thread(() -> {load();}, "Load Thread");
	}
	
	public void load() {
		
		Level.initialize();
		setScrollListLevel();
		listDefaultLevels.selectedItem = null;
		listCustomLevels.selectedItem = null;
//		listCustomLevels
		
		screenCover = new UIPanel(new Vector2i(0,0), new Vector2i(width, height)).setColor(0x7f202020, true);
		screenCover.add(panelLevelSelect);
		layerListClassic.add(screenCover);
		
		while (listDefaultLevels.selectedItem == null && listCustomLevels.selectedItem == null) {
		}
		
		layerListClassic.remove(screenCover);
//		System.out.println("res/Levels/Default/" + listEditableLevels.selectedItem + ".lvl");
		if (listDefaultLevels.selectedItem != null)
			level = Level.deserializeFromFile("res/Levels/Default/" + listDefaultLevels.selectedItem + ".lvl", this); 
		else
			level = Level.deserializeFromFile("res/Levels/Custom/" + listCustomLevels.selectedItem + ".lvl", this);
		saveName = level.name;
	}

	public void start() {
		running = true;
		
		screenScrollX = screen.width/2 + 1;
		screenScrollY = screen.height/2;
		
		thread = new Thread(this, "Level Editor");
		thread.start();
		
	}
	
	public void stop() {
		running = false;
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private int updates;
	private int frames;
	
	public void run() {
		final double updatesPerSec = 60.0;
		final double updatesPerSecRatio = 1_000_000_000/updatesPerSec;	// Every one billion nano seconds has 60 updates per second
		
//		final double framesPerSec = 60.0;	// Frame Rate cap
//		final double framesPerSecRatio = (framesPerSec == 0.0) ? 0 : 1_000_000_000/framesPerSec;

		long lastTime = System.nanoTime();
		long currentTime;
		double updateDelta = 0;
		
		long timer = System.currentTimeMillis();
		updates = 0; 
		frames = 0;
		
		while (running) {
			currentTime = System.nanoTime();
			updateDelta += (currentTime - lastTime) / updatesPerSecRatio;
			lastTime = currentTime;
			
			while(updateDelta >= 1) {
				update();
				updates++;
				updateDelta--;
			}
			
			//Frames are uncapped at the moment
			render();
			frames++;
			
			//Display the framerate and update rate every second
			while(System.currentTimeMillis() - timer >= 0) {
				timer += 1000;
				if (level != null) 
					frame.setTitle(TITLE + " | " + VERSION + " | " + level.name + " | DEBUG: FPS: " + frames + " UPS: " + updates);
				else 
					frame.setTitle(TITLE + " | DEBUG: FPS: " + frames + " UPS: " + updates);
				updates = 0;
				frames = 0;
			}
			
		}
		stop();
	}
	
	public void update() {
		
		if (layerListClassic.size() > 0) {
			layerListClassic.get(0).update();
//			if (currentPrompt.value != -1)
//				currentPrompt.remove();
			return;
		}
		panelEditor.update();
		panelProperties.update();
		
		if (level != null) 
			level.update();
		
		//Updates top to bottom
		for (int i = layerList.size() - 1; i >= 0; i--) {
			layerList.get(i).update();
		}
		
	}
	
	public void render() {
		
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(new Color(0xFF00FF));
		g.fillRect(0, 0, width, height);
		
		screen.setOffset(screenScrollX - screen.width / 2, Screen.getScreenY(screenScrollY) - screen.height / 2);
		
		screen.clear(0x0);
		if (level != null) 
			level.render(screen);
		screen.renderInvertedPoint(screenScrollX, screenScrollY);
		screen.renderInvertedPoint(curMouseX, curMouseY);
		
		screen.pack();
		
		//UIPanels and screen can't be rendered traditionally. Screen isn't even a layer, and the layer typiarchy for panels (draw to graphics g) is incompatible 
		//	with the typiarchy for everything that draws to the screen, so we render them out here like dis.
		panelEditor.render(g);
		panelProperties.render(g);
		
		g.drawImage(screen.image, screenCoordX, screenCoordY, screen.width * screen.scale, screen.height * screen.scale, null);
		
		//render layers. This is going to be interesting because screen is not technically a layer, but stuff it displays.. IS.
		//	hoo boy
		for (int i = 0; i < screenRenderList.size(); i++)
			screenRenderList.get(i).render(screen);
		
		if (layerListClassic.size() > 0) {
			layerListClassic.get(layerListClassic.size() - 1).render(g);
		}
		
		bs.show();
		g.dispose();
		
	}
	
	public void onEvent(Event event) {
		
		EventDispatcher dispatcher = new EventDispatcher(event);
		dispatcher.dispatch(Event.Type.MOUSE_PRESSED, (Event e) -> onMousePress((MousePressedEvent) e));
		dispatcher.dispatch(Event.Type.MOUSE_RELEASED, (Event e) -> onMouseRelease((MouseReleasedEvent) e));
		dispatcher.dispatch(Event.Type.MOUSE_MOVED, (Event e) -> onMouseMove((MouseMovedEvent) e));
		dispatcher.dispatch(Event.Type.KEY_TYPED, (Event e) -> onKeyType((KeyTypedEvent) e));
		
		if (layerListClassic.size() > 0) {
			layerListClassic.get(layerListClassic.size() - 1).onEvent(event);
			return;
		}
		
		panelEditor.onEvent(event);
		panelProperties.onEvent(event);
		//Sends events top to bottom
		for (int i = layerList.size() - 1; i >= 0; i--) {
			layerList.get(i).onEvent(event);
		}
		
		if (level != null) 
			level.onEvent(event);
		
	}
	
	/**
	 * This even fires when we don't click on the panels to the side. So the screen basically which Im starting to wish I'd made into a layer.
	 * @param e
	 * @return
	 */
	public boolean onMousePress(MousePressedEvent e) {
		
		panelEditor.clearAllFocus();
		panelProperties.clearAllFocus();
		
		int mouseButton = e.getButton();
		
		if (mouseButton == MouseEvent.BUTTON3) {
			
			//Mouse values, divided into the screen scale, and translated onto the screen region, then set to screen coords
			screenScrollStartX = screenScrollX;
			screenScrollStartY = screenScrollY;
			mouseDragXStart = mouseXToScreenX(e.getX());
			mouseDragYStart = mouseYToScreenY(e.getY());
			
			dragging = true;
			return true;
		}
		
		return false;
	}
	
	public boolean onMouseRelease(MouseReleasedEvent e) {
		
//		level.sort();
		
		int mouseButton = e.getButton();
		
		if (mouseButton == MouseEvent.BUTTON3) {
			screenScrollStartX = screenScrollX;
			screenScrollStartY = screenScrollY;
			dragging = false;
			return true;
		}
		
		return false;
	}
	
	//Currently ignoring the code I wrote for handling drags
	public boolean onMouseMove(MouseMovedEvent e) {
		
		curMouseX = mouseXToScreenX(e.getX());
		curMouseY = mouseYToScreenY(e.getY());
		
		int deltaX = (mouseDragXStart - curMouseX);
		int deltaY = (mouseDragYStart - curMouseY);
		
		curMouseX += screenScrollX;
		curMouseY += screenScrollY;
		
		if (dragging) {
			screenScrollX = MathUtils.clamp(screenScrollStartX + deltaX, 0, level.width);
			screenScrollY =  MathUtils.clamp(screenScrollStartY + deltaY, 0, level.height);
			
			return true;
		}
		return false;
	}
	
	public boolean onKeyType(KeyTypedEvent e) {
		
		//Shortcuts!
		final char charShftR	= '\u0052';
		final char charCtrlS	= '\u0013';
		final char charCtrlN	= '\u000E';
		final char charCtrlL	= '\u000C';
		final char charW		= '\u0077';
		final char charF		= '\u0066';
		
//		System.out.printf("%04X\n", (short) e.getKeyChar());
		
		switch (e.getKeyChar()) {
		case (charShftR):
			Level.initialize();
			populateScrollList(fontScrollList, listDefaultLevelTypes, Level.defaultLevelTypes, true);
			populateScrollList(fontScrollList, listCustomLevelTypes, Level.customLevelTypes, false);
			//TODO Should probably reload list of levels too 
			level.reloadAssets();
			return true;
		case (charCtrlS):
			saveThreadCreate();
			saveThreadLol.start();
			return true;
		case (charCtrlN):
			setNonClashingLevelSaveName();
			save();
			level = new Level(this, isDevVersion);
			saveName = null;
			return true;
		case (charCtrlL):
			loadThreadCreate();
			loadThreadLol.start();
			return true;
		case (charW):
			Level.lof.setType(WALL);
			return true;
		case (charF):
			Level.lof.setType(FLOOR);
			return true;
		default: return false;
		}
		
	}
	
	/**
	 * Gets the x coordinate of the pixel on the screen
	 * To get the coordinate of the pixel in the level environment, add screenScrollX
	 * @param mouseX
	 * @return
	 */
	public int mouseXToScreenX(int mouseX) {
		return ((mouseX/screen.scale - (screenCoordX / screen.scale)) - screen.width / 2);// + screenScrollStartX;
	}
	
	/**
	 * Gets the y coordinate of the pixel in the screen
	 * To get the coordinate of the pixel in the level environment, add screenScrollX
	 * @param mouseX
	 * @return
	 */
	public int mouseYToScreenY(int mouseY) {
		//Important note, this ptus Y into screen stuffs and disregards all else
		return Screen.getScreenY((mouseY/screen.scale - (screenCoordY / screen.scale)) - screen.height / 2);// + screenScrollStartY;
	}
	
	public void addLayer(LayerLE l) {
		layerList.add(l);
		if (l instanceof Renderable) screenRenderList.add((Renderable) l);
		l.init(layerList);
	}

	public void removeLayer(LayerLE l) {
		layerList.remove(l);
	}
	
	public void removeLayer(int index) {
		if (index < layerList.size() - 1)
			layerList.remove(index);
	}
	
	public static void main(String args[]) {
		
		editor = new Editor();
		
		editor.frame.setResizable(false);
		editor.frame.setTitle(TITLE + " | " + VERSION);
		editor.frame.add(editor);
		editor.frame.pack();
		editor.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		editor.frame.setLocationRelativeTo(null);
		editor.frame.setVisible(true);
		
		editor.start();
		
	}

}