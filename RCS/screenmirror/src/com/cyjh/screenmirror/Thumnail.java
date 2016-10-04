package com.cyjh.screenmirror;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.util.Log;

import com.cyjh.screenmirror.utils.Utils;

public class Thumnail {
	public static void main(String args[]) {
		Thumnail thum = new Thumnail();
		Log.i("MirrorDisplay", "thumnail process");
		thum.loop();
		//thum.test();
	}
	
	public static void testMain(String[] args) {
		Thumnail thum = new Thumnail();
		Log.i("MirrorDisplay","test");
		thum.test();
	}
	
	public void loop() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(!mQuitting) {
			try {
				String cmd = br.readLine();
				if(cmd !=null && cmd.equals("capture")) {
					Bitmap bm = Utils.screenshot(256, 144);
					if(bm == null) {
						System.out.print(0);
						Log.i("MirrorDisplay", "capture Failed");
					} else {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
						byte data[] = baos.toByteArray();
						System.out.write(Utils.int2Bytes(data.length));
						System.out.write(baos.toByteArray());
					}
				} else if(cmd !=null && cmd.equals("exit")) {
					mQuitting = true;
				} else {
					Thread.sleep(500);
				}
			}catch(IOException e) {
				return;
			}catch(InterruptedException e) {		
			}
				
		}
	}
	
	public void test() {
		Bitmap bm = Utils.screenshot(256, 144);
		if(bm == null) {
			System.out.print(0);
			Log.i("MirrorDisplay", "capture Failed");
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
			byte data[] = baos.toByteArray();
			File file = new File("/sdcard/a.jpeg");
			OutputStream os = null;
			try {
			os = new FileOutputStream(file);
			os.write(data);
			
			}catch(FileNotFoundException e) {
			}catch(IOException e) {
			}finally {
				if(os!=null) {
					try {
						os.close();
					}catch(IOException e) {	
					}
				}
			}
			
		}
		
	}
	boolean mQuitting = false;
}
