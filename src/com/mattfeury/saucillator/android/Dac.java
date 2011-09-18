package com.mattfeury.saucillator.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/*
 * A lot of this code was provided by Adam Smith from EtherealDialpad: https://gist.github.com/376028
 * 
 * I really like his project and recommend you check it out. It seems that we both share a similar vision
 * for transforming android devices into instruments.
 * 
 * It has been touched up and modified slightly 
 * 
 */
public class Dac extends UGen {
	private final float[] localBuffer;
	private boolean isClean, playing;
	private final AudioTrack track;
	private final short [] target = new short[UGen.CHUNK_SIZE];
	private final short [] silentTarget = new short[UGen.CHUNK_SIZE];
	int minSize, added;
	boolean started = false;
	
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
	
	public boolean render(final float[] _buffer) {
		if(!isClean) {
			zeroBuffer(localBuffer);

			isClean = true;
		}
		// localBuffer is always clean right here, does it stay that way?
		isClean = !renderKids(localBuffer);
		return !isClean; // we did some work if the buffer isn't clean
	}
	
	public void open() {
		playing = true;
	}
	
	public boolean isPlaying() {
		return playing;
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
		} else {
			for(int i = 0; i < CHUNK_SIZE; i++) {
				target[i] = (short)(Short.MAX_VALUE * (localBuffer[i] + 1.0) / 2);
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
