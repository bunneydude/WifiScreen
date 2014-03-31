package com.me.wifiscreen;


import static com.me.wifiscreen.Medium.BACKGROUND;
import static com.me.wifiscreen.Medium.STROKE;
import static com.me.wifiscreen.Medium.NOSTROKE;
import static com.me.wifiscreen.Medium.FILL;
import static com.me.wifiscreen.Medium.NOFILL;
import static com.me.wifiscreen.Medium.TEXT;
import static com.me.wifiscreen.Medium.SETTEXTSIZE;
import static com.me.wifiscreen.Medium.POINT;
import static com.me.wifiscreen.Medium.LINE;
import static com.me.wifiscreen.Medium.RECT;
import static com.me.wifiscreen.Medium.WIDTH;
import static com.me.wifiscreen.Medium.HEIGHT;
import static com.me.wifiscreen.Medium.CIRCLE;
import static com.me.wifiscreen.Medium.IMAGE;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PlotWindow extends Actor {

	private Color color;
	private Color strokeColor;
	private Color fillColor;
	private Color batchColor;
	private int textSize;
	private boolean fill = true;
	private boolean stroke = true;

	private ShapeRenderer shapeRenderer;	
	private float baseX;
	private float baseY;
	private int lastSize = 0;
	private float lastW = 0;
	private float lastH = 0;
	private BitmapFont[] font;

	private Texture bunneyLogo;

	private boolean debugPrint = false;


	public PlotWindow(float width, float height) {

		this.setWidth(width);
		this.setHeight(height);
		shapeRenderer = new ShapeRenderer();
		color = new Color(0,0,0,0);
		strokeColor = new Color(0,0,0,0);
		fillColor = new Color(0,0,0,0);
		batchColor = new Color(0,0,0,0);
		
		textSize = 0;
		font = new BitmapFont[]{		
				new BitmapFont(Gdx.files.internal("data/fonts/arial_16.fnt")),
				new BitmapFont(Gdx.files.internal("data/fonts/arial_24.fnt")),
				new BitmapFont(Gdx.files.internal("data/fonts/arial_32.fnt")),
				new BitmapFont(Gdx.files.internal("data/fonts/arial_40.fnt")),
				new BitmapFont(Gdx.files.internal("data/fonts/arial_48.fnt"))
		};

		bunneyLogo = new Texture(Gdx.files.internal("data/bunneyGreen2.png"));
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		batchColor.set(batch.getColor());
		batch.end();
		//baseX = this.getParent().getX() + this.getX();
		//baseY = this.getParent().getY() + this.getY();
		baseX = 0;
		baseY = 0;
		color.a = parentAlpha;
		strokeColor.a = parentAlpha;
		fillColor.a = parentAlpha;

		if(lastW != this.getWidth()){
			System.out.println("W = " + this.getWidth());
			lastW = this.getWidth();
		}
		if(lastH != this.getHeight()){
			System.out.println("H = " + this.getHeight());
			lastH = this.getHeight();
		}
		//batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		synchronized(WifiScreen.commandBuffer){
			if(WifiScreen.commandBuffer.size() != lastSize){
				//System.out.println(WifiScreen.commandBuffer.size());
				//System.out.printf("width = %d, height = %d\n",Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				lastSize = WifiScreen.commandBuffer.size();
				debugPrint = false;
			}
			for(float[] temp : WifiScreen.commandBuffer){		

				switch((int)temp[0]){
				case BACKGROUND:			
					color.r = temp[1];
					color.g = temp[2];
					color.b = temp[3];
					shapeRenderer.begin(ShapeType.Filled);
					shapeRenderer.setColor(color);
					shapeRenderer.rect(baseX, baseY, this.getWidth(), this.getHeight());
					shapeRenderer.end();
					break;
				case STROKE:
					stroke = true;
					strokeColor.set(temp[1], temp[2], temp[3], parentAlpha);
					break;
				case NOSTROKE:
					stroke = false;
					break;
				case FILL:
					fill = true;
					fillColor.set(temp[1], temp[2], temp[3], parentAlpha);
					break;
				case NOFILL:
					fill = false;
					break;
				case TEXT:
					batch.begin();
					font[textSize].setColor(strokeColor);
					font[textSize].draw(batch, WifiScreen.textBuffer.get((int)temp[3]), baseX + temp[1], baseY + temp[2]);					
					batch.end();
					break;
				case SETTEXTSIZE: //pixel height: 0 = 16px, 1 = 24px, 2 = 32px, 3 = 40px, 4 = 48px 
					textSize = (int)temp[1];
					break;
				case POINT:
					shapeRenderer.begin(ShapeType.Point);
					shapeRenderer.setColor(strokeColor);
					shapeRenderer.point(baseX + temp[1], baseY + temp[2], 0);
					shapeRenderer.end();
					break;
				case LINE:
					shapeRenderer.begin(ShapeType.Line);				
					shapeRenderer.setColor(strokeColor);
					shapeRenderer.line(baseX + temp[1], baseY + temp[2], baseX + temp[3], baseY + temp[4] );
					shapeRenderer.end();
					break;
				case RECT:
					if(fill == true){
						shapeRenderer.begin(ShapeType.Filled);
						shapeRenderer.setColor(fillColor);
						shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
						shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
						shapeRenderer.rect(baseX + temp[1], baseY + temp[2], temp[3], temp[4] );
						shapeRenderer.end();
					}
					if(stroke == true){
						shapeRenderer.begin(ShapeType.Line);
						shapeRenderer.setColor(strokeColor);
						shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
						shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
						shapeRenderer.rect(baseX + temp[1], baseY + temp[2], temp[3], temp[4] );
						shapeRenderer.end();
					}																								
					break;
				case CIRCLE:
					if(fill == true){
						shapeRenderer.begin(ShapeType.Filled);
						shapeRenderer.setColor(fillColor);
						shapeRenderer.circle(baseX + temp[1], baseY + temp[2], temp[3] );
						shapeRenderer.end();	
					}
					if(stroke == true){
						shapeRenderer.begin(ShapeType.Line);
						shapeRenderer.setColor(strokeColor);
						shapeRenderer.circle(baseX + temp[1], baseY + temp[2], temp[3] );
						shapeRenderer.end();	
					}				
					break;
				case IMAGE:
					if(debugPrint){
						System.out.println("index = " + temp[0]);
					}
					batch.begin();
					//batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
					batch.setColor(batchColor.r, batchColor.g, batchColor.b, 1);
					batch.draw(WifiScreen.imageBuffer.get((int)temp[3]), baseX + temp[1], baseY + temp[2] );					
					batch.end();
					break;
				default:
					System.out.println("Not valid value: " + temp[0]);
					break;
				}//end switch							
			}//end for int
			debugPrint = false;
		}//end sync			
		batch.begin();
	}//end draw

	public Color getStroke(){
		return strokeColor;		
	}

	public Color getFill(){
		return fillColor;		
	}
	
	public int getTextSize(){
		return textSize;		
	}	

}





