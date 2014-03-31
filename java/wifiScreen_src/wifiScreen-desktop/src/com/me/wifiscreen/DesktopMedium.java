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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;


public class DesktopMedium extends Medium {

	@Override
	public FileOutputStream getFileOutputStream(String image) {

		try {
			return new FileOutputStream(imageName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public FileHandle getFileHandle(String image) {
		return Gdx.files.local(imageName);
	}

	@Override
	public void deleteCachedFiles() {
		System.out.println("Deleting images");
		Boolean tempBool = false;
		for(String tempName : imageNameList){
			tempBool = Gdx.files.local(tempName).delete();
			System.out.println("Deleted file " + tempName + " - " + tempBool);
		}		
	}
	

}
