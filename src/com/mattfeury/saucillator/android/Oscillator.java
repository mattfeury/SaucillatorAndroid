package com.mattfeury.saucillator.android;

/**
 * Represents an actual oscillator.
 */
public abstract class Oscillator extends UGen {

  protected float frequency = 440f;
  protected float amplitude = 1.0f,
                  internalAmp = 0f; //used to calculate. changes
  protected int oscPhase = 1;

  protected float BASE_FREQ = 440f;
  protected int harmonic = 1;
  protected float attack = 0f,
                  release = 0f;
  protected Lagger attackLagger = new Lagger(0f, 1f),
                   releaseLagger = new Lagger(1f, 0f);
  protected boolean attacking = false, releasing = false;

  public void setHarmonic(int h) {
    this.harmonic = h;
  }

  @Override
  public void togglePlayback() {
    if ((releasing && isPlaying) || ! isPlaying)
      startAttack();
    else
      startRelease();
  }
  public boolean isReleasing() {
    return releasing;
  }
  public boolean isAttacking() {
    return attacking;
  }

  public void startAttack() {
    resetLaggers();

    this.start();
    releasing = false;
    attacking = true;
    android.util.Log.d("ATTACKING", "release canceled");    
  }
  public void startRelease() {
    resetLaggers();

    attacking = false;
    releasing = true;
    android.util.Log.d("RELEASING", "attack cancelled");
  }
  public void resetLaggers() {
    //TODO set rates
    attackLagger = new Lagger(internalAmp, 1f);
    releaseLagger = new Lagger(1f, 0f);

    //attackLagger.setRate(0.2f);
    //releaseLagger.setRate(0.2f);
  }
  public void updateEnvelope() {
    float previousAmp = internalAmp;
    if (attacking) {
      internalAmp = attackLagger.update();
      android.util.Log.d("AMPERE-A", "ATTACK!!! prev: " + previousAmp + " / now: " + internalAmp);
      
    } else if (releasing) {
      internalAmp = releaseLagger.update();
      android.util.Log.d("AMPERE-R", "RELEASE!!! prev: " + previousAmp + " / now: " + internalAmp);      
    }

    if (internalAmp == previousAmp && (attacking || releasing)) {
      android.util.Log.d("CANCELING", "ATTACK");    
      attacking = false;

      // FIXME maybe require that the amp is near zero 
      // (it won't be exactly zero because we round. generally 2.0e-5)
      if (releasing) {
        android.util.Log.d("CANCELING", "RELEASE");    
        releasing = false;
        this.stop();
      }
    }
  }


  // Callback called post rendering
  public void rendered() {
    updateEnvelope();
  }
  
  public abstract void setFreq(float freq);
  public abstract void setModRate(int rate);
  public abstract void setModDepth(int depth);
  public abstract void setLag(float rate);

  public abstract void setAmplitude(float amp);
  public abstract void factorAmplitude(float factor);

  public void setBaseFreq(float freq) {
    BASE_FREQ = freq;
  }

  public synchronized void setFreqByOffset(int[] scale, int offset) {
    float freq = Theory.getFrequencyForScaleNote(scale, BASE_FREQ, offset);
    setFreq(freq);
  }
}
