package com.me.wifiscreen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.me.wifiscreen.Medium;


public class HtmlMedium implements Medium {
	private DataProcessor dataProcessor;
	private Thread dataProcessorThread;


	//private ServerSocket server;
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

	@Override
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


		dataProcessor = new DataProcessor();
		dataProcessorThread = new Thread(dataProcessor);
		dataProcessorThread.start();

		return 0;
	}

	private static class DataProcessor implements Runnable{
		public boolean running = true;
		float plotWidth;
		float plotHeight;

		int cmd;
		short length;
		int numRead = 0;
		int[] payload = null;

		public DataProcessor() {
		}

		@Override
		public void run() {
			while(running){
				//if input, get it, process it, signal w/ runnable
				try {
					if( wifiIn.available() > 0 ){
						//System.out.println("About to read");
						cmd = wifiIn.read(); //cmd
						length = wifiIn.readShort(); //length
						payload = new int[length];

						//System.out.println("Cmd = " + cmd + ", length = " + length);
						numRead = 0;
						while(numRead != length){
							payload[numRead] = wifiIn.read();
							numRead++;
						}
						//System.out.println("Numread = " + numRead);
						//System.out.println("About to parse");
						this.parsePayloadShort(cmd, length, payload);
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

		private void parsePayloadShort(int cmd, short length, int[] payload){
			plotWidth = WifiScreen.plotWindow.getWidth();
			plotHeight = WifiScreen.plotWindow.getHeight();

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

					WifiScreen.commandBuffer.add( new float[] {BACKGROUND, payload[0]/255f, payload[1]/255f, payload[2]/255f} );
				}
				break;
			case STROKE:				
				//System.out.println("Stroke.")
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {STROKE, payload[0]/255f , payload[1]/255f, payload[2]/255f} );
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
					WifiScreen.commandBuffer.add( new float[] {FILL, payload[0]/255f , payload[1]/255f, payload[2]/255f} );
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
					WifiScreen.commandBuffer.add( new float[] {TEXT, ((payload[0]&0xFF)<<8)+payload[1], ((payload[2]&0xFF)<<8)+payload[3], WifiScreen.textBuffer.size() });
					String tempText = "";
					for(int i=4; i<payload.length; i++){
						tempText += (char)payload[i];
					}
					WifiScreen.textBuffer.add(tempText);
				}
				break;
			case SETTEXTSIZE:
				//System.out.println("Set text size.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {SETTEXTSIZE, payload[0]} );
				}
				break;
			case POINT:
				//System.out.println("Point.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {POINT, ((payload[0]&0xFF)<<8)+payload[1], ((payload[2]&0xFF)<<8)+payload[3] });
				}								
				break;
			case LINE:
				//System.out.println("Line.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {LINE, ((payload[0]&0xFF)<<8)+payload[1] , ((payload[2]&0xFF)<<8)+payload[3], ((payload[4]&0xFF)<<8)+payload[5], ((payload[6]&0xFF)<<8)+payload[7] });
				}	
				break;
			case RECT:
				//System.out.println("Rect:");
				synchronized(WifiScreen.commandBuffer){					
					WifiScreen.commandBuffer.add( new float[] {RECT, ((payload[0]&0xFF)<<8)+payload[1] , ((payload[2]&0xFF)<<8)+payload[3], ((payload[4]&0xFF)<<8)+payload[5], ((payload[6]&0xFF)<<8)+payload[7] });
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
			case CIRCLE:	
				//System.out.println("Circle.");
				synchronized(WifiScreen.commandBuffer){
					WifiScreen.commandBuffer.add( new float[] {CIRCLE, ((payload[0]&0xFF)<<8)+payload[1] , ((payload[2]&0xFF)<<8)+payload[3], ((payload[4]&0xFF)<<8)+payload[5] });
				}	
				break;
			case IMAGE:
				synchronized(WifiScreen.commandBuffer){					
					WifiScreen.commandBuffer.add( new float[] {IMAGE, ((payload[0]&0xFF)<<8)+payload[1], ((payload[2]&0xFF)<<8)+payload[3], payload[4] });
				}
				break;
			case NEWIMAGE:
				//System.out.println("Create new image");

				if(payload[1] == 0){ //1 = overwrite files, 0 = don't
					imageID = imageCounter;
					imageOverwrite = false;
				}else{
					imageID = payload[2]; //index of file to overwrite
					imageOverwrite = true;
				}

				switch(payload[0]){ //file type
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
					System.out.println("Error - unknown file number " + payload[0]);
					break;
				}

				try {
					imageWriter = new BufferedOutputStream(new FileOutputStream(imageName));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case GETIMAGE:
				//System.out.println("Rx'd " + payload.length + " bytes.");
				try {
					if(payload.length > 0){
						for(int i=0; i<payload.length; i++){
							imageWriter.write(payload[i]);
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
										WifiScreen.imageBuffer.add(new Texture(Gdx.files.local(imageName)));
									}else{
										WifiScreen.imageBuffer.set(imageID, new Texture(Gdx.files.local(imageName)));
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

	/*
	System.out.println("Waiting for connection...");
	try {
		server.accept();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	//System.out.println("Got connection.");

	@Override
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
				connected = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Deleting images");
			Boolean tempBool = false;
			for(String tempName : imageNameList){
				tempBool = Gdx.files.local(tempName).delete();
				System.out.println("Deleted file " + tempName + " - " + tempBool);
			}
		}
	}
	@Override
	public String[] listPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int send(int cmd) {
		//wifiOut.println("Hello world");
		return 0;
	}

	@Override
	public void status() {
		boolean temp = false;
		temp = client.isConnected();
		System.out.println("Status = " + temp);

	}

	@Override
	public boolean isConnected() {		
		return connected;
	}

	@Override
	public void signal() {
		try {
			wifiOut.write(ACK);
			wifiOut.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
