package com.visellico.platty.leveleditor;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.farr.Events.Event;
import com.farr.Events.EventListener;
import com.visellico.graphics.Screen;
import com.visellico.graphics.ui.UIPanel;
import com.visellico.input.Focus;
import com.visellico.input.Keyboard;
import com.visellico.input.Mouse;
import com.visellico.platty.leveleditor.Level.Level;

public class Editor extends Canvas implements Runnable, EventListener {

	//This is to shut up eclipse warnings
	private static final long serialVersionUID = 1L;
	
	private Thread thread;
	private boolean running;
	
	private JFrame frame;
	private static final String TITLE = "Platty the Platformer | Level Editor";
	private static final String VERSION = "dev 0.0";

	
	public static Editor editor;
	public int width = 1600;
	public int height = 900;
	public int defaultScale = 4;
	
	public Keyboard key;
	public Mouse mouse;
	public Focus focus;
	
	private List<Renderable> screenRenderList = new ArrayList<>();
	private List<Layer> layerList = new ArrayList<>();
	
	public Screen screen;
	public UIPanel editorPanel;
	public UIPanel propertyPanel;
	
	
	public Level level;	//Level being edited. Should be created by deserializing a file, or as a new blank slate.
	
	public Editor() {
		
		Dimension size = new Dimension(width, height);
		setPreferredSize(size);
		
		frame = new JFrame();
		
		screen = new Screen((width - 200) / defaultScale, (height - 100) / defaultScale, defaultScale);
		screen.setOffset(-screen.width / 2, -screen.height/2);
		
		key = new Keyboard(this);
		mouse = new Mouse(this);
		focus = new Focus(this);
		
		addKeyListener(key);
		addMouseListener(mouse);
		addFocusListener(focus);
		
	}

	public void start() {
		running = true;
		
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
		
		screen.clear(0x7F7F7F);
		screen.renderPoint(0, 0, 0x0);
		screen.renderLine(50, 10, 100, 100, 0xAAEEAA);
		
		//render screen/uipanels
		for (int i = 0; i < screenRenderList.size(); i++)
			screenRenderList.get(i).render(screen);
		
		screen.pack();
		g.drawImage(screen.image, 200, 0, screen.width * screen.scale, screen.height * screen.scale, null);
		
		bs.show();
		g.dispose();
		
	}
	
	public void onEvent(Event event) {
		//Sends events top to bottom
		for (int i = layerList.size() - 1; i >= 0; i--) {
			layerList.get(i).onEvent(event);
		}
	}
	
	public void addLayer(Layer l) {
		layerList.add(l);
		if (l instanceof Renderable) screenRenderList.add((Renderable) l);
		l.init(layerList);
	}

	public void removeLayer(Layer l) {
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
		
		new Level();
		
	}

}
