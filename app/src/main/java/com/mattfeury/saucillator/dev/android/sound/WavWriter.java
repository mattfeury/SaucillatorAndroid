package com.mattfeury.saucillator.dev.android.sound;

import java.io.*;

import com.mattfeury.saucillator.dev.android.services.InstrumentService;

import android.util.Log;

/**
 * Writes a wave file
 */
public class WavWriter {
  static int numWavFiles = 0;
  //FIXME make this stuff instances. might prevent memory leaks
  static ByteArrayOutputStream data = new ByteArrayOutputStream();
  private static File lastFile = null;
  public static String filePrefix = "Recording";

  public static File getLastFile(){
    return lastFile;
  }
  static void pushShort(short s){
    data.write((byte)(s & 0xff));
    data.write((byte)((s >> 8) & 0xff));
  }
  static void pushFloat(float f){ //f should be between -1 and 1.
    short i = (short)(f * Short.MAX_VALUE);
    pushShort(i);
  }
  static void clear() {
    data = new ByteArrayOutputStream();
  }

  static void writeWav(){
    try{
      writeWav(data.toByteArray());
    } catch(IOException e){
      Log.e("blow", "blew it : " + e.toString());  	
    }
  }

  private static int sampleRate = UGen.SAMPLE_RATE;
  private static int numChannels = 1;
  private static int bitDepth = 16;
  static void writeWav(byte[] buffer) throws IOException{
    int numSamples = buffer.length / 2;
    
    File file;
    int i = 0;
    
    if (! InstrumentService.ensureProperDirectoryStructure())
      return;
    
    do{
    	i++;
    	file = new File(InstrumentService.dataPath + filePrefix + i + ".wav");
    }while(file.exists());
    
    DataOutputStream outFile  = new DataOutputStream(new FileOutputStream(file));
    
    // write the header
    outFile.writeBytes("RIFF");
    outFile.write(intToByteArray((int)(numSamples * numChannels * bitDepth / 8 + 36)), 0, 4);
    outFile.writeBytes("WAVE");
    outFile.writeBytes("fmt ");
    outFile.write(intToByteArray((int)16), 0, 4); //16 for PCM
    outFile.write(shortToByteArray((short)1), 0, 2); //1 for PCM
    outFile.write(shortToByteArray((short)numChannels), 0, 2); //Mono
    outFile.write(intToByteArray((int)sampleRate), 0, 4);
    outFile.write(intToByteArray((int)sampleRate * numChannels * bitDepth / 8), 0, 4);
    outFile.write(shortToByteArray((short)(numChannels * bitDepth / 8)), 0, 2);
    outFile.write(shortToByteArray((short)bitDepth), 0, 2);

    // write the data
    outFile.writeBytes("data");
    outFile.write(intToByteArray((int)numSamples * numChannels * bitDepth / 8), 0, 4);
    outFile.write(buffer);

    // save
    outFile.flush();
    outFile.close();
    
    clear();

    lastFile = file;
    numWavFiles++;
	}

  //===========================
  // CONVERT JAVA TYPES TO BYTES
  // based on code by Evan Merz
  //===========================
	// returns a byte array of length 4
	private static byte[] intToByteArray(int i)
	{
		byte[] b = new byte[4];
		b[0] = (byte) (i & 0x00FF);
		b[1] = (byte) ((i >> 8) & 0x000000FF);
		b[2] = (byte) ((i >> 16) & 0x000000FF);
		b[3] = (byte) ((i >> 24) & 0x000000FF);
		return b;
	}

	// convert a short to a byte array
	public static byte[] shortToByteArray(short data)
	{
		return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
	}

}
