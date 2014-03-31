package com.me.wifiscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;


public class MainMenuScreen implements Screen {
	final WifiScreen game;

	Texture bunneyLogo;
	Texture ssmuLogo;

	OrthographicCamera camera;
	private Stage stage;

	Skin skin;
	Texture texture1;
	Texture texture2;
	Label fpsLabel;
	
    ScrollPane logPane;
    ScrollPaneStyle scrollPaneStyle;
    Label logScroll;

	public MainMenuScreen(final WifiScreen game) {
		this.game = game;		

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		texture1 = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		texture2 = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		bunneyLogo = new Texture(Gdx.files.internal("data/bunneyGreen.png"));		
		
		TextureRegion sig = new TextureRegion(bunneyLogo);		
		
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, game.batch);		
		System.out.printf("width = %d, height = %d\n",Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.input.setInputProcessor(stage);		

		ImageButtonStyle style = new ImageButtonStyle(skin.get(ButtonStyle.class));
		style.imageUp = new TextureRegionDrawable(sig);
		style.imageDown = new TextureRegionDrawable(sig);
		ImageButton bunneyButton = new ImageButton(style);
		
		Button launchButton = new TextButton("Launch", skin);
		Button connectButton = new TextButton("Connect", skin);
		//Button miscToggleButton = new Button(new Image(sig), skin, "toggle");
		//Button imgButton = new Button(new Image(sig), skin);
				
        BitmapFont font = new BitmapFont(Gdx.files.internal("data/fonts/arial_24.fnt"));
        Color fontColor = new Color(1,1,1,1);
        LabelStyle labelStyle = new LabelStyle(font,fontColor);
        
        logScroll = new Label("Enter IP and port for target.", skin);
        logScroll.setAlignment(Align.center);
        logScroll.setWrap(true);
        
        logPane = new ScrollPane(logScroll,skin);
        logPane.setScrollingDisabled(true, false);
        
		final TextField platformTextField = new TextField("", skin);
		platformTextField.setMessageText("Enter IP here");
		
		final TextField portTextField = new TextField("", skin);
		
		portTextField.setMessageText("Enter port here");
				
		fpsLabel = new Label("fps:", skin);
		
        
        
        
		Window window = new Window("Wifi Screen", skin);
		//window.debug();
		window.setPosition(0, 0);
		window.defaults().spaceBottom(10);	
		window.row();
		window.add(platformTextField).minWidth(100).expandX().minHeight(100).fill().colspan(3);
		window.row();
		window.add(portTextField).expandX().fill().minHeight(100).colspan(3);
		window.row().expandX().fillX();	
		window.add(connectButton).fill().left();
		window.add(bunneyButton).fill().left();
		window.add(launchButton).fill().left();				
		window.row();
		window.add(logPane).fill().expand().colspan(4);
		window.row();
		window.add(fpsLabel).colspan(4);
		window.pack();
        window.setFillParent(true);
        
		stage.addActor(window);

		
		System.out.println("H = " + stage.getHeight() + ", W = " + stage.getWidth());
		
		platformTextField.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField textField, char key) {
				if (key == '\n') textField.getOnscreenKeyboard().show(false);
			}
		});
		

		bunneyButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				new Dialog("Mark Bunney, Jr.", skin, "dialog").text("2014").key(Keys.ESCAPE, false).button("Close",false).show(stage);
			}
		});
		
		launchButton.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor){
				if(WifiScreen.myMedium.isConnected()){
					Gdx.app.log("UITest", "Launching data screen.");
					game.setScreen(new GameScreen(game));				
					dispose();
				}else{
					logScroll.setText("Connect before launching.");
				}
			}
		});
		
		connectButton.addListener(new ChangeListener(){
			public void changed (ChangeEvent event, Actor actor){
				Gdx.app.log("UITest", "Connecting to port.");				
				int result = WifiScreen.myMedium.connect(platformTextField.getText(),portTextField.getText());
				switch(result){
				case(0):
					logScroll.setText("Connected.");
					break;
				case(-1):
					logScroll.setText("IP numbers must be in range [0,255].");
					break;
				case(-2):
					logScroll.setText("IP can only be numbers.");
					break;
				case(-3):
					logScroll.setText("Error creating InetAddress object.");
					break;
				case(-4):
					logScroll.setText("Error creating socket object.");
					break;
				}
			}
		});
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
		/*Vector2 size = Scaling.fit.apply(800,480,width,height);
		int viewportX = (int)(width - size.x) / 2;
        int viewportY = (int)(height - size.y) / 2;
        int viewportWidth = (int)size.x;
        int viewportHeight = (int)size.y;
        Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
        stage.setViewport(800, 480, true, viewportX, viewportY, viewportWidth, viewportHeight);*/
        stage.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);	
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
        texture1.dispose();
        texture2.dispose();
        bunneyLogo.dispose();
	}

}
