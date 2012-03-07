package com.mattfeury.saucillator.dev.android.archive;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/*
 * An old implementation of a DAC. This works fine actually, but I prefer Dac.java for now because the idea
 * of adding children to the UGens seems more intuitive.
 * 
 * This is as simple as it gets. Great way to learn how the Android AudioTrack works (along with Sine.java) 
 */
public class AndroidAudioDeviceOLD
{
   AudioTrack track;
   short[] buffer = new short[1024];
   boolean started = false;
   int minSize;
   int added = 0;
   static int fs = 11025;
 
   public AndroidAudioDeviceOLD( )
   {
      minSize = AudioTrack.getMinBufferSize( fs, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
      System.out.println("min " + minSize);
      Log.i("dac","min 2 " + minSize);
      track = new AudioTrack( AudioManager.STREAM_MUSIC, fs, 
                                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
                                        minSize * 4, AudioTrack.MODE_STREAM);
   }	   
 
   public void writeSamples(float[] samples) 
   {	
      fillBuffer( samples );
      track.write( buffer, 0, samples.length );
      added += samples.length;
      
      if(! started && added > minSize) {
          track.play();        
          started = true;
      }
   }
 
   private void fillBuffer( float[] samples )
   {
      if( buffer.length < samples.length )
         buffer = new short[samples.length];
 
      for( int i = 0; i < samples.length; i++ )
         buffer[i] = (short)((samples[i] + 1) / 2 * Short.MAX_VALUE);;
   }		
}
