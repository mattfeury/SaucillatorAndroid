package com.mattfeury.saucillator.dev.android.sound;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/*
 * A Digital to Analog converter
 * This spins up a main AudioTrack for playing back digital samples.
 * You can "chuck" any UGen to this to route its sound to the DAC.
 * 
 * Many thanks to code by Adam Smith (EtherealDialpad) for helping me get started here.
 */
public class Dac extends UGen {
  private final float[] localBuffer;
  private boolean isClean, playing;
  private final AudioTrack track;
  private final short [] target = new short[UGen.CHUNK_SIZE];
  private final short [] silentTarget = new short[UGen.CHUNK_SIZE];
  int minSize, added;
  boolean started = false;
  boolean recording = false;
	
	public Dac() {
		playing = false;
		localBuffer = new float[CHUNK_SIZE];
		
		minSize = AudioTrack.getMinBufferSize(
				UGen.SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
        		AudioFormat.ENCODING_PCM_16BIT);
		
		track = new AudioTrack(
        		AudioManager.STREAM_MUSIC,
        		UGen.SAMPLE_RATE,
        		AudioFormat.CHANNEL_CONFIGURATION_MONO,
        		AudioFormat.ENCODING_PCM_16BIT,
        		Math.max(UGen.CHUNK_SIZE*4, minSize),
        		AudioTrack.MODE_STREAM);
	}

  public void record() {
    if ( ! recording) {
      WavWriter.clear();
      recording = true;
    }
  }
  public void stopRecording() {
    if (recording) {
      recording = false;
      WavWriter.writeWav();
    }
  }
  public boolean toggleRecording() {
    if (recording) stopRecording();
    else record();
    return recording;
  }
	
	public boolean render(final float[] _buffer) {
		if(!isClean) {
			zeroBuffer(localBuffer);

			isClean = true;
		}
		isClean = !renderKids(localBuffer);
		return !isClean; // we did some work if the buffer isn't clean
	}
	
	public void open() {
		playing = true;
	}
	
	public boolean isPlaying() {
		return playing;
	}

  public boolean isRecording() {
    return recording;
  }

	public void toggle() {
		playing = !playing;
	}
	
	public void setPan(float l, float r)
	{
		track.setStereoVolume(l, r);
	}
	
	public void tick() {

		render(localBuffer);

		if(isClean || !playing) {
      // sleeping is messy, so lets just queue this silent buffer
      track.write(silentTarget, 0, silentTarget.length);
      if (recording) {
        for(int i = 0; i < CHUNK_SIZE; i++) {
          WavWriter.pushShort((short)0);
        }
      }
    } else {
      Limiter.limit(localBuffer);
      for(int i = 0; i < CHUNK_SIZE; i++) {
        float sample = localBuffer[i];
        target[i] = (short)(Short.MAX_VALUE * (sample + 1.0) / 2.0);

        if (recording) {
          try {
            //Write dat shit into dat wav buffa.
            WavWriter.pushFloat(sample);
          } catch (Exception e) {
            // Something bad happened. Try to write the wav and bail.
            // This is often an OutOfMemory error. Will it still write it?
            WavWriter.writeWav();
            recording = false;
          }
        }
			}

      track.write(target, 0, target.length);

      added += target.length;

      if(! started && added > minSize) {
        track.play();
        started = true;
      }
		}
	}
	
	public void close() {
    track.stop();
    track.release();
	}
}
