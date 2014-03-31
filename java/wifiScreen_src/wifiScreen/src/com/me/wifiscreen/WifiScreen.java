package com.me.wifiscreen;
import static com.me.wifiscreen.Medium.STROKE;
import static com.me.wifiscreen.Medium.FILL;
import static com.me.wifiscreen.Medium.SETTEXTSIZE;

import java.util.LinkedList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.me.wifiscreen.Medium;

public class WifiScreen extends Game{
	
	SpriteBatch batch;
	BitmapFont font;
	public static Medium myMedium;
	public static LinkedList<float[]> commandBuffer;
	public static LinkedList<String> textBuffer;
	public static LinkedList<Texture> imageBuffer;
	public static PlotWindow plotWindow;	
	
	public WifiScreen(Medium myMedium){
		WifiScreen.myMedium = myMedium;	
		commandBuffer = new LinkedList<float[]>();	
		textBuffer = new LinkedList<String>();
		imageBuffer = new LinkedList<Texture>();
		
		WifiScreen.commandBuffer.add( new float[] {STROKE,0,0,0} );
		WifiScreen.commandBuffer.add( new float[] {FILL,1,1,1} );
		WifiScreen.commandBuffer.add( new float[] {SETTEXTSIZE,0} );
		Texture.setEnforcePotImages(false);		
	}
	
	
	public void create(){
		//myMedium.connect(); //start interface	
		batch = new SpriteBatch();
		font = new BitmapFont(); //default Arial
		this.setScreen(new MainMenuScreen(this));
		
	}
	
	public void render(){
		super.render();
	}
	
	public void dispose(){
		System.out.println("App closed");
		batch.dispose();
		font.dispose();		
		myMedium.disconnect();
	}
	
	
	
}