package com.me.wifiscreen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;


public class AndroidMedium extends Medium {
	
	static Context myCtx = null;
	
	public AndroidMedium(Context applicationContext) {
		myCtx = applicationContext;
	}
	
	@Override
	public void deleteCachedFiles(){
		FileHandle dirHandle;

		dirHandle = Gdx.files.absolute("/data/data/com.me.wifiscreen/files");
		System.out.println("Stored files");
		Boolean tempBool = false;

		for (FileHandle entry: dirHandle.list()) {
			if( (entry.extension().equals("bmp")) || (entry.extension().equals("png")) || (entry.extension().equals("jpg")) ){
				tempBool = entry.delete();
				System.out.println("Deleted file " + entry.name() + " - " + tempBool);
			}
		}
	}
	



	@Override
	public FileOutputStream getFileOutputStream(String image) {
		try {
			return myCtx.openFileOutput(imageName, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	

	@Override
	public FileHandle getFileHandle(String image) {
		return new FileHandle(myCtx.getFileStreamPath(imageName));
	}

}
