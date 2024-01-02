package com.mattfeury.saucillator.android.sound;

import java.io.*;

import com.mattfeury.saucillator.android.services.ActivityService;
import com.mattfeury.saucillator.android.templates.Handler;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Writes a wave file
 */
public class WavWriter {
  static int numWavFiles = 0;
  //FIXME make this stuff instances. might prevent memory leaks
  static ByteArrayOutputStream data = new ByteArrayOutputStream();
  private static String lastFile = null;
  public static String filePrefix = "saucillator-recording";

  public static String getLastFile(){
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

    ActivityService.withActivity(new Handler<Activity>() {
       @Override
       public void handle(Activity activity) {
         ContentValues values = new ContentValues();
         String fileName = filePrefix + "-" + System.currentTimeMillis() + ".wav";

         ContentResolver resolver = activity.getApplicationContext().getContentResolver();

         // Find all audio files on the primary external storage device.
         Uri audioCollection;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           audioCollection = MediaStore.Audio.Media
                   .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
         } else {
           audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
         }

         // Publish a new song.
         ContentValues newSongDetails = new ContentValues();
         newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);

         // Keep a handle to the new song's URI in case you need to modify it later
         Uri fileUri = resolver.insert(audioCollection, newSongDetails);
         OutputStream outFile = null;
         try {
           outFile = resolver.openOutputStream(fileUri);
           outFile.write("RIFF".getBytes());
           outFile.write(intToByteArray((int)(numSamples * numChannels * bitDepth / 8 + 36)), 0, 4);
           outFile.write("WAVE".getBytes());
           outFile.write("fmt ".getBytes());
           outFile.write(intToByteArray((int)16), 0, 4); //16 for PCM
           outFile.write(shortToByteArray((short)1), 0, 2); //1 for PCM
           outFile.write(shortToByteArray((short)numChannels), 0, 2); //Mono
           outFile.write(intToByteArray((int)sampleRate), 0, 4);
           outFile.write(intToByteArray((int)sampleRate * numChannels * bitDepth / 8), 0, 4);
           outFile.write(shortToByteArray((short)(numChannels * bitDepth / 8)), 0, 2);
           outFile.write(shortToByteArray((short)bitDepth), 0, 2);

           // write the data
           outFile.write("data".getBytes());
           outFile.write(intToByteArray((int)numSamples * numChannels * bitDepth / 8), 0, 4);
           outFile.write(buffer);

           // save
           outFile.flush();
           outFile.close();

           clear();

           lastFile = fileName;
           numWavFiles++;
         } catch (Exception e) {
           throw new RuntimeException(e);
         }
       }
    });
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
