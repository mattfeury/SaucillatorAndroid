package com.mattfeury.saucillator.android;

import java.io.*;
import java.nio.ByteBuffer;

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

  static File writeWav(){
  	try{
  		return writeWav(data.toByteArray());
  	}catch(IOException e){
  		
  		Log.e("blow", "blew it : " + e.toString());
  		return null;
  	
  	}
  	
  }
	static File writeWav(byte[] buffer) throws IOException{
		
		File file = new File("Recording " + numWavFiles + ".wav");
		
		FileWriter writer = new FileWriter(file);
		
		ByteBuffer bytes = ByteBuffer.allocate(buffer.length + 44);
		
		bytes.putChar('R'); bytes.putChar('I'); bytes.putChar('F'); bytes.putChar('F'); // "RIFF"
		bytes.putInt(buffer.length * 2 + 36); //Total length of the file - 8. Header is 44 bits, so buf size * 2 - 8 + 44 => buf size * 2 + 36
		bytes.putChar('W'); bytes.putChar('A'); bytes.putChar('V'); bytes.putChar('E'); // "WAVE"
		
		bytes.putChar('f'); bytes.putChar('m'); bytes.putChar('t'); bytes.put((byte)0); //'fmt\0'     <---- MAY BE WRONG
		bytes.putInt(16);
		bytes.putShort((short)1);
		bytes.putShort((short)1);
		bytes.putInt(11025);
		bytes.putInt(11025 * 2);
		bytes.putShort((short)2);
		bytes.putShort((short)16);
		
		bytes.putChar('d'); bytes.putChar('a'); bytes.putChar('t'); bytes.putChar('a');
		bytes.putInt(buffer.length);
		
		for(int i = 0; i < buffer.length; i++)
			bytes.put(buffer[i]);
		
		char[] wavFileArray = bytes.asCharBuffer().array();
		
		writer.write(wavFileArray);
		writer.close();

		
		return file;
	
	}

	
}
