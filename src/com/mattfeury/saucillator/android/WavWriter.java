package com.mattfeury.saucillator.android;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import android.util.Log;

public class WavWriter {
	
	static int numWavFiles  = 0;
	/*String filename;*/
	static ByteArrayOutputStream data = new ByteArrayOutputStream();
	
	
	static void PushShort(short s){
	
		data.write((byte)s);
		data.write((byte) s >> 8);
	
	}
  static void clear() {
    data = new ByteArrayOutputStream();
  }

  static void writeWav(){
  	try{
  		writeWav(data.toByteArray());
  	}catch(IOException e){
  		
  		Log.e("blow", "blew it : " + e.toString());  	
  	}
  	
  }
	static void writeWav(byte[] buffer) throws IOException{
		
		File file = new File("/sdcard/Recording" + numWavFiles + ".wav");
	  OutputStream out = new BufferedOutputStream(new FileOutputStream(file));	 

		//FileWriter writer = new FileWriter("/sdcard/recording0.wav");
		
		ByteBuffer bytes = ByteBuffer.allocate(buffer.length + 44);
		
		bytes.put((byte)'R'); bytes.put((byte)'I'); bytes.put((byte)'F'); bytes.put((byte)'F'); // "RIFF"
		bytes.putInt(buffer.length * 2 + 36); //Total length of the file - 8. Header is 44 bits, so buf size * 2 - 8 + 44 => buf size * 2 + 36
		bytes.put((byte)'W'); bytes.put((byte)'A'); bytes.put((byte)'V'); bytes.put((byte)'E'); // "WAVE"
		
		bytes.put((byte)'f'); bytes.put((byte)'m'); bytes.put((byte)'t'); bytes.put((byte)0); //'fmt\0'     <---- MAY BE WRONG
		bytes.putInt(16);
		bytes.putShort((short)1);
		bytes.putShort((short)1);
		bytes.putInt(11025);
		bytes.putInt(11025 * 2);
		bytes.putShort((short)2);
		bytes.putShort((short)16);
		
		bytes.put((byte)'d'); bytes.put((byte)'a'); bytes.put((byte)'t'); bytes.put((byte)'a');
		bytes.putInt(buffer.length);
		
		//for(int i = 0; i < buffer.length; i++)
	  bytes.put(buffer);
		
		//CharBuffer wavFileArray = bytes.asCharBuffer();
		//char[] array = wavFileArray.array();
	  byte[] array = bytes.array();
	  out.write(array, 0, array.length);

    out.flush();
		out.close();	
	}

	
}
