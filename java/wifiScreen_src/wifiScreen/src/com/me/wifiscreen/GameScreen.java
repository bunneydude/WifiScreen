package com.me.wifiscreen;

import java.io.FileOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Scaling;

public class GameScreen implements Screen {

	final WifiScreen game;
	private Stage stage;
	Skin skin;
	Label fpsLabel;
	public PlotWindow plotWindow;

	public GameScreen(final WifiScreen game){
		this.game = game;
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true, game.batch);
		Gdx.input.setInputProcessor(stage);
		fpsLabel = new Label("fps: ",skin);
		fpsLabel.setAlignment(0);
		plotWindow = new PlotWindow(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());	
		WifiScreen.plotWindow = plotWindow;
		System.out.println("Created game screen");
		WifiScreen.myMedium.signal();

		Window window = new Window("Wifi LCD", skin);
		window.debug();
		window.setPosition(0,0);
		window.defaults().spaceBottom(10);
		window.row();
		window.add(plotWindow);
		//window.row().expandX().fillX();
		//window.add(fpsLabel);			
		window.pack();		
		window.isDragging();
		window.setFillParent(true);
		/*Vector2 size = Scaling.fit.apply(800,480,window.getWidth(),window.getHeight());
		int viewportX = (int)(window.getWidth() - size.x) / 2;
        int viewportY = (int)(window.getHeight() - size.y) / 2;
        int viewportWidth = (int)size.x;
        int viewportHeight = (int)size.y;
        window.setWidth(viewportWidth);
        window.setHeight(viewportHeight);*/
		Gdx.graphics.setContinuousRendering(false);
		stage.addActor(plotWindow);
		System.out.println("after: H = " + window.getHeight() + ", W = " + window.getWidth());
	}


	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		fpsLabel.setText("fps: " + Gdx.graphics.getFramesPerSecond());
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		Table.drawDebug(stage);
	}

	@Override
	public void resize(int width, int height) {
//		stage.setViewport(800, 480, false);
		stage.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		for(Texture temp : WifiScreen.imageBuffer){
			temp.dispose();
		}
		WifiScreen.myMedium.disconnect();

	}


}