package com.me.wifiscreen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public abstract class Medium {	
	public static final int BACKGROUND = 20;
	public static final int STROKE = 21;
	public static final int NOSTROKE = 22;
	public static final int FILL = 23;
	public static final int NOFILL = 24;
	public static final int TEXT = 25;
	public static final int SETTEXTSIZE = 26;
	public static final int POINT = 27;
	public static final int LINE = 28;
	public static final int RECT = 29;
	public static final int WIDTH = 30;
	public static final int HEIGHT = 31;
	public static final int CIRCLE = 32;
	public static final int IMAGE = 33;
	public static final int NEWIMAGE = 34;
	public static final int GETIMAGE = 35;
	public static final int ACK = 36;
	public static final int NACK = 37;
	public static final int BUFFERSIZE = 38;


	private DataProcessor dataProcessor;
	private Thread dataProcessorThread;

	private Socket client;
	private int port = 2000;
	private InetAddress addr;
	byte[] ip = new byte[] {(byte) 192, (byte) 168, 1, (byte) 66};
	static DataOutputStream wifiOut;	
	static DataInputStream wifiIn;
	private boolean connected = false;

	static BufferedOutputStream imageWriter = null;
	static int imageCounter = 0;
	static String imageName = "";
	static int imageID = 0;
	static boolean imageOverwrite = false;

	public static LinkedList<String> imageNameList = new LinkedList<String>();
	private static Hashtable<Short, Integer> objectIDToText = new Hashtable<Short, Integer>();


	public int connect(String portName, String portNumber) {
		String ipSplit[] = portName.split("\\.");

		if(ipSplit.length == 0){
			return -1;
		}

		for(int i=0;i<ipSplit.length;i++){
			try{
				int temp = Integer.parseInt(ipSplit[i]);
				if( (temp < 0) || (temp > 255) ){
					return -1;
				}
				ip[i] = (byte)temp;
			}catch(NumberFormatException e){
				return -2;
			}
		}

		port = Integer.parseInt(portNumber);



		try {
			addr = InetAddress.getByAddress(ip);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return -3;
		}
		try {
			//server = new ServerSocket(port,0,addr);
			client = new Socket(addr,port);
			wifiOut = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
			wifiIn = new DataInputStream(new BufferedInputStream(client.getInputStream()));
			connected = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -4;
		}

		deleteCachedFiles();

		dataProcessor = new DataProcessor(this);
		dataProcessorThread = new Thread(dataProcessor);
		dataProcessorThread.start();

		return 0;
	}

	public void disconnect() {
		if(connected){
			try {
				dataProcessor.running = false;
				try {
					dataProcessorThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				client.close();
				deleteCachedFiles();
				connected = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String[] listPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	public int send(int cmd) {
		//wifiOut.println("Hello world");
		return 0;
	}

	public void status() {
		boolean temp = false;
		temp = client.isConnected();
		System.out.println("Status = " + temp);
	}

	public boolean isConnected() { 
		return connected;
	}

	public void signal() {
		try {
			wifiOut.write(ACK);
			wifiOut.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public abstract FileOutputStream getFileOutputStream(String image);
	public abstract FileHandle getFileHandle(String image);
	public abstract void deleteCachedFiles();

	private static class DataProcessor implements Runnable{
		public boolean running = true;
		float plotWidth;
		float plotHeight;
		private Medium myMedium = null;

		int cmd;
		short numberLength;
		short textLength;
		short objectName;
		int numRead = 0;
		short[] numberPayload = null;
		char[] textPayload = null;

		public DataProcessor(Medium medium) {
			this.myMedium = medium;
		}

		@Override
		public void run() {
			while(running){
				//if input, get it, process it, signal w/ runnable
				try {
					if( wifiIn.available() > 0 ){
						//System.out.println("About to read");
						cmd = wifiIn.read(); //cmd
						numberLength = wifiIn.readShort();
						textLength = wifiIn.readShort(); 
						objectName = wifiIn.readShort();
						numberPayload = new short[numberLength];
						textPayload = new char[textLength];

						numRead = 0;
						while(numRead != numberLength){
							numberPayload[numRead] = wifiIn.readShort();
							numRead++;
						}

						numRead = 0;
						while(numRead != textLength){
							textPayload[numRead] = (char) wifiIn.read();
							numRead++;
						}

						this.parsePayloadShort(cmd, numberPayload, textPayload, objectName);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}

		private void parsePayloadShort(int cmd, short[] numberPayload, char[] textPayload, short objectID){
			plotWidth = WifiScreen.plotWindow.getWidth();
			plotHeight = WifiScreen.plotWindow.getHeight();
			boolean editEntry = (objectID != 0) && (objectID <= WifiScreen.commandBuffer.size());
			
			switch(cmd){
			case BACKGROUND:			
				//System.out.println("Background.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.clear();
					Color tempColor = WifiScreen.plotWindow.getStroke();
					WifiScreen.commandBuffer.add(new float[]{STROKE, tempColor.r, tempColor.g, tempColor.b});
					tempColor = WifiScreen.plotWindow.getFill();
					WifiScreen.commandBuffer.add(new float[]{FILL, tempColor.r, tempColor.g, tempColor.b});					
					int tempSize = WifiScreen.plotWindow.getTextSize();
					WifiScreen.commandBuffer.add(new float[]{SETTEXTSIZE, tempSize});

					WifiScreen.commandBuffer.add( new float[] {BACKGROUND, numberPayload[0]/255f, numberPayload[1]/255f, numberPayload[2]/255f} );
				}
				break;
			case STROKE:				
				//System.out.println("Stroke.")
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {STROKE, numberPayload[0]/255f , numberPayload[1]/255f, numberPayload[2]/255f} );
				}
				break;
			case NOSTROKE:
				//System.out.println("No stroke.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {NOSTROKE} );
				}
				break;
			case FILL:
				//System.out.println("Fill.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {FILL, numberPayload[0]/255f , numberPayload[1]/255f, numberPayload[2]/255f} );
				}							
				break;
			case NOFILL:
				//System.out.println("No fill.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {NOFILL} );
				}
				break;
			case TEXT:
				synchronized(WifiScreen.commandBuffer){
					String tempText = "";
					for(int i=0; i<textPayload.length; i++){
						tempText += textPayload[i];
					}
					
					if(editEntry){
						WifiScreen.commandBuffer.get(objectID-1)[1] = numberPayload[0];
						WifiScreen.commandBuffer.get(objectID-1)[2] = numberPayload[1];
								
						WifiScreen.textBuffer.set((int)WifiScreen.commandBuffer.get(objectID-1)[3], tempText);
					}else{
						WifiScreen.commandBuffer.add( new float[] {TEXT, numberPayload[0], numberPayload[1], WifiScreen.textBuffer.size() });
						WifiScreen.textBuffer.add(tempText);						
					}
				}
				break;
			case SETTEXTSIZE:
				//System.out.println("Set text size.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {SETTEXTSIZE, numberPayload[0]} );
				}
				break;
			case POINT:
				//System.out.println("Point.");
				synchronized(WifiScreen.commandBuffer){
					if(editEntry){
						WifiScreen.commandBuffer.get(objectID-1)[1] = numberPayload[0];
						WifiScreen.commandBuffer.get(objectID-1)[2] = numberPayload[1];
					}else{
						WifiScreen.commandBuffer.add( new float[] {POINT, numberPayload[0], numberPayload[1] });
					}
				}								
				break;
			case LINE:
				//System.out.println("Line.");
				synchronized(WifiScreen.commandBuffer){
					if(editEntry){
						WifiScreen.commandBuffer.get(objectID-1)[1] = numberPayload[0];
						WifiScreen.commandBuffer.get(objectID-1)[2] = numberPayload[1];
						WifiScreen.commandBuffer.get(objectID-1)[3] = numberPayload[2];
						WifiScreen.commandBuffer.get(objectID-1)[4] = numberPayload[3];	
					}else{
						WifiScreen.commandBuffer.add( new float[] {LINE, numberPayload[0] , numberPayload[1], numberPayload[2], numberPayload[3] });
					}
				}	
				break;
			case RECT:
				//System.out.println("Rect:");
				synchronized(WifiScreen.commandBuffer){
					if((objectID != 0) && (objectID <= WifiScreen.commandBuffer.size()) ){
						WifiScreen.commandBuffer.get(objectID-1)[1] = numberPayload[0];
						WifiScreen.commandBuffer.get(objectID-1)[2] = numberPayload[1];
						WifiScreen.commandBuffer.get(objectID-1)[3] = numberPayload[2];
						WifiScreen.commandBuffer.get(objectID-1)[4] = numberPayload[3];
					}else{
						WifiScreen.commandBuffer.add( new float[] {RECT, numberPayload[0] , numberPayload[1], numberPayload[2], numberPayload[3] });
					}
				}								
				break;
			case WIDTH:	
				try {
					wifiOut.write(WIDTH);
					wifiOut.writeInt((int) WifiScreen.plotWindow.getWidth());
					wifiOut.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case HEIGHT:
				try {
					wifiOut.write(HEIGHT);					
					wifiOut.writeInt((int) WifiScreen.plotWindow.getHeight());
					wifiOut.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case BUFFERSIZE:
				synchronized(WifiScreen.commandBuffer){
					try {
						wifiOut.write(BUFFERSIZE);					
						wifiOut.writeInt((int) WifiScreen.commandBuffer.size());
						wifiOut.flush();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				break;				
			case CIRCLE:	
				//System.out.println("Circle.");
				synchronized(WifiScreen.commandBuffer){
					if(editEntry){
						WifiScreen.commandBuffer.get(objectID-1)[1] = numberPayload[0];
						WifiScreen.commandBuffer.get(objectID-1)[2] = numberPayload[1];
						WifiScreen.commandBuffer.get(objectID-1)[3] = numberPayload[2];						
					}else{
						WifiScreen.commandBuffer.add( new float[] {CIRCLE, numberPayload[0] , numberPayload[1], numberPayload[2] });
					}
				}	
				break;
			case IMAGE:
				synchronized(WifiScreen.commandBuffer){
					if(editEntry){
						WifiScreen.commandBuffer.get(objectID-1)[1] = numberPayload[0];
						WifiScreen.commandBuffer.get(objectID-1)[2] = numberPayload[1];
						WifiScreen.commandBuffer.get(objectID-1)[3] = numberPayload[2];
					}else{
						WifiScreen.commandBuffer.add( new float[] {IMAGE, numberPayload[0], numberPayload[1], numberPayload[2] });
					}
				}
				break;
			case NEWIMAGE:
				//System.out.println("Create new image");

				if(numberPayload[1] == 0){ //1 = overwrite files, 0 = don't
					imageID = imageCounter;
					imageOverwrite = false;
				}else{
					imageID = numberPayload[2]; //index of file to overwrite
					imageOverwrite = true;
				}

				switch(numberPayload[0]){ //file type
				case 0:
					imageName = "image_" + Integer.toString(imageID) + ".png";					
					break;
				case 1:
					imageName = "image_" + Integer.toString(imageID) + ".jpg";
					break;
				case 2:
					imageName = "image_" + Integer.toString(imageID) + ".bmp";
					break;
				default:
					System.out.println("Error - unknown file number " + numberPayload[0]);
					break;
				}
				imageWriter = new BufferedOutputStream(myMedium.getFileOutputStream(imageName));
				break;
			case GETIMAGE:
				System.out.println("Rx'd " + textPayload.length + " bytes.");
				try {
					if(textPayload.length > 0){
						for(int i=0; i<textPayload.length; i++){
							imageWriter.write(textPayload[i]);
						}
					}else{
						System.out.println("Image transfer complete");
						imageWriter.close();
						imageCounter++;			
						imageNameList.add(imageName);

						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								synchronized(WifiScreen.commandBuffer){
									if(imageOverwrite == false){
										WifiScreen.imageBuffer.add(new Texture(myMedium.getFileHandle(imageName)));
									}else{
										WifiScreen.imageBuffer.set(imageID, new Texture(myMedium.getFileHandle(imageName)));
									}
								}
							}
						});

					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				System.out.println("Unknown cmd: " + cmd);
				break;
			}//end switch							
			if( cmd != GETIMAGE ){
				Gdx.graphics.requestRendering();
			}
		}

	}//end dataProcessor

}
