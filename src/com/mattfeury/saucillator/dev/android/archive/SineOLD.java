package com.mattfeury.saucillator.dev.android.archive;

/*
 * This is old. Don't really use it much right now, although it works.
 * The problem is this way each oscillator would create its own thread and DAC.
 * There is no way that scales.
 * 
 * Kept here for research's sake.
 */
public class SineOLD implements Runnable {

	private boolean playback = true;
	
	private float toneFreq = 100;

	public SineOLD(float toneFreq) {
		this.toneFreq = toneFreq;
	}

	public void run() {
		
		
        AndroidAudioDeviceOLD device = new AndroidAudioDeviceOLD( );
        float samples[] = new float[1024];
        float emptySamples[] = new float[1024];

        float angle = 0;
        while( true )
        {
        	if(playback) {
        		float increment = (float)(2*Math.PI) * toneFreq / AndroidAudioDeviceOLD.fs; // angular increment for each sample

        		for( int i = 0; i < samples.length; i++ )
        		{
        			samples[i] = (float) Math.sin(angle);
        			angle += increment;
        		}
                device.writeSamples( samples );

        	} else {
                device.writeSamples( emptySamples );
        	}

        }        	

		
		
	}
	
	public void stopPlayback() {
		playback = false;
	}

	public void togglePlayback() {
		playback = !playback ;
	}

	public boolean isPlaying() {
		return playback;
	}

	public float getToneFreq() {
		return this.toneFreq;
	}
	
	public void setFrequency(float f) {
		this.toneFreq = f;
	}
	
}
