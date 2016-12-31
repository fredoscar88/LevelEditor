package com.visellico.platty.leveleditor;

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
import com.visellico.graphics.ui.UILabel;
import com.visellico.graphics.ui.UIPanel;
import com.visellico.graphics.ui.UIPromptOption;
import com.visellico.graphics.ui.UIScrollList;
import com.visellico.input.Focus;
import com.visellico.input.Keyboard;
import com.visellico.input.Mouse;
import com.visellico.platty.Assets;
import com.visellico.platty.leveleditor.Level.Level;
import com.visellico.util.MathUtils;
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
	private boolean running;
	
	private JFrame frame;
	private static final String TITLE = "Platty the Platformer | Level Editor";
	private static final String VERSION = "dev 0.0";
	Font scrollListFont;
	private UIScrollList listDefaultLevelTypes;
	private UIScrollList listCustomLevelTypes;
	
	public static Editor editor;
	public int width = 1600;
	public int height = 900;
	public int defaultScale = 4;
	
	public Keyboard key;
	public Mouse mouse;
	public Focus focus;
	
	public UIPromptOption currentPrompt;
	public Thread promptThread;
	
	private List<Renderable> screenRenderList = new ArrayList<>();
	private List<LayerLE> layerList = new ArrayList<>();
	private List<com.farr.Events.Layer> layerListClassic = new ArrayList<>();
	
	public Screen screen;
	public int screenCoordX = 200;
	public int screenCoordY = 0;
	public int screenScrollX;
	public int screenScrollY;
	public UIPanel editorPanel;
	public UIPanel propertyPanel;
	
	//Mouse drag vars
	boolean dragging = false;
	int mouseDragXStart;
	int mouseDragYStart;
	int screenScrollStartX;
	int screenScrollStartY;
	int curMouseX;
	int curMouseY;
	
	public volatile Level level;	//Level being edited. Should be created by deserializing a file, or as a new blank slate.
	
	//TODO this constructor does an awful lot for a constructor.
	//	A lot of initialization things, but it is beginning to feel more like something that would run at the start... not when constructed!
	public Editor() {
		
		Level.initialize();

		UIPromptOption startup = new UIPromptOption(width, height, "Select one", "New Level", "Load Level");
		UIPromptOption exit = new UIPromptOption(width, height, "Do you want to save?", "Yes", "No");
		
		Dimension size = new Dimension(width, height);
		setPreferredSize(size);
		
		frame = new JFrame() {
			protected void processWindowEvent(final WindowEvent e) {
				if (e.getID() == WindowEvent.WINDOW_CLOSING) {
					save(level);
				}
				super.processWindowEvent(e);
		    }
		};
			
		
		screen = new Screen((width - 200) / defaultScale, (height - 100) / defaultScale, defaultScale);
		screen.setOffset(-screen.width / 2, -screen.height/2);
		
		editorPanel = new UIPanel(new Vector2i(0,0), new Vector2i(200, height));
		editorPanel.setColor(0xE8BA55);
		propertyPanel = new UIPanel(new Vector2i(200, height - 100), new Vector2i (width - 200, 100));
		propertyPanel.setColor(0x55BAE8);
		
		startup.init(layerListClassic);
		currentPrompt = startup;
		promptThread = new Thread(() -> {
			currentPrompt.value = -1;
			int response = currentPrompt.awaitResponse();
			switch (response) {
			case 0: level = new Level(); break;
			case 1: level = Level.deserializeFromFile("res/Levels/Default/New Level.lvl"); break;

			}
			currentPrompt = null;
		}, "I BETTER NOT BE RUNNING LONG");
		promptThread.start();
		
		
		scrollListFont = new Font("Times New Roman", Font.PLAIN, 18);
		listDefaultLevelTypes = new UIScrollList(new Vector2i(10,75), new Vector2i(180,100));
		listCustomLevelTypes = new UIScrollList(new Vector2i(10,185), new Vector2i(180,100));
		
		populateTypeSelectList(scrollListFont, listDefaultLevelTypes, Level.defaultLevelTypes);
		populateTypeSelectList(scrollListFont, listCustomLevelTypes, Level.customLevelTypes);

		editorPanel.add(listDefaultLevelTypes);
		editorPanel.add(listCustomLevelTypes);
		
//		for (String lType : Level.defaultLevelTypes) {
//			listDefaultLevelTypes.add(new UILabel(new Vector2i(0,0), lType, scrollListFont,() -> {level.switchAssets(lType, true);}).setYPaddingOffset(2).setColor(Color.black));
//		}
//		for (String lType : Level.customLevelTypes) {
//			listCustomLevelTypes.add(new UILabel(new Vector2i(0,0), lType, scrollListFont, () -> {level.switchAssets(lType, false);}).setYPaddingOffset(2).setColor(Color.black));
//		}
		
//		level = new Level();
//		level = Level.deserializeFromFile("res/Levels/Default/New Level.lvl");
//		save(level);
		//BLAAH
		
		key = new Keyboard(this);
		mouse = new Mouse(this);
		focus = new Focus(this);
		
		addKeyListener(key);
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		addMouseWheelListener(mouse);
		addFocusListener(focus);
		
	}
	
	public void populateTypeSelectList(Font font, UIScrollList scrList, List<String> strings) {
		scrList.clear();
		for (String lType : strings) {
			scrList.add(new UILabel(new Vector2i(0,0), lType, font,() -> {level.switchAssets(lType, true);}).setYPaddingOffset(2).setColor(Color.black));
		}
	}
	
	public static void save(Level level) {
		level.sort();
		
//		String directory = Assets.prgmDir + (level.isDefault ? Assets.dirDefaultLevels : Assets.dirCustomLevels);
//		System.out.println(directory);
//		
//		level.serialize(directory);
		level.serialize(Assets.prgmDir + (level.isDefault ? Assets.dirDefaultLevels : Assets.dirCustomLevels));
	}

	public void start() {
		running = true;
		
		screenScrollX = screen.width/2;
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
		
//		if (layerListClassic.size() > 0) {
//			layerListClassic.get(0).update();
//			if (currentPrompt.value != -1)
//				currentPrompt.remove();
//			return;
//		}
		editorPanel.update();
		
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
		editorPanel.render(g);
		propertyPanel.render(g);
		
		g.drawImage(screen.image, screenCoordX, screenCoordY, screen.width * screen.scale, screen.height * screen.scale, null);
		
		//render layers. This is going to be interesting because screen is not technically a layer, but stuff it displays.. IS.
		//	hoo boy
		for (int i = 0; i < screenRenderList.size(); i++)
			screenRenderList.get(i).render(screen);
		
		if (currentPrompt != null) {
			currentPrompt.render(g);
		}
		
		bs.show();
		g.dispose();
		
	}
	
	public void onEvent(Event event) {
		
		if (currentPrompt != null) {
			currentPrompt.onEvent(event);
			return;
		}
		
		editorPanel.onEvent(event);
		propertyPanel.onEvent(event);
		
		
		EventDispatcher dispatcher = new EventDispatcher(event);
		
		//Sends events top to bottom
		for (int i = layerList.size() - 1; i >= 0; i--) {
			layerList.get(i).onEvent(event);
		}
		
		dispatcher.dispatch(Event.Type.MOUSE_PRESSED, (Event e) -> onMousePress((MousePressedEvent) e));
		dispatcher.dispatch(Event.Type.MOUSE_RELEASED, (Event e) -> onMouseRelease((MouseReleasedEvent) e));
		dispatcher.dispatch(Event.Type.MOUSE_MOVED, (Event e) -> onMouseMove((MouseMovedEvent) e));
		dispatcher.dispatch(Event.Type.KEY_TYPED, (Event e) -> onKeyType((KeyTypedEvent) e));
		
	}
	
	/**
	 * This even fires when we don't click on the panels to the side. So the screen basically which Im starting to wish I'd made into a layer.
	 * @param e
	 * @return
	 */
	public boolean onMousePress(MousePressedEvent e) {
		
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
		final char charShftR = '\u0052';
		final char charCtrlS = '\u0013';
		
//		System.out.printf("%x\n", (short) e.getKeyChar());
		
		switch (e.getKeyChar()) {
		case (charShftR):
			Level.initialize();
			populateTypeSelectList(scrollListFont, listDefaultLevelTypes, Level.defaultLevelTypes);
			populateTypeSelectList(scrollListFont, listCustomLevelTypes, Level.customLevelTypes);
			level.reloadAssets();
			return true;
		case (charCtrlS):
			save(level);
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
