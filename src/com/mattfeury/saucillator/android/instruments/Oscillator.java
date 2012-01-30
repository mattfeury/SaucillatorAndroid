package com.mattfeury.saucillator.android.instruments;

import com.mattfeury.saucillator.android.sound.Lagger;
import com.mattfeury.saucillator.android.sound.UGen;

/**
 * Represents an actual oscillator.
 */
public abstract class Oscillator extends UGen {

  protected String name = "Unknown";

  protected float frequency = 440f;
  protected float amplitude = 1.0f,
                  internalAmp = 0f; //used to calculate. changes
  protected int oscPhase = 0;

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
  public int getHarmonic() {
    return harmonic;
  }

  @Override
  public void togglePlayback() {
    if ((releasing && isPlaying()) || ! isPlaying())
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
  }
  public void startRelease() {
    resetLaggers();

    attacking = false;
    releasing = true;
  }
  public void resetLaggers() {
    //TODO set rates
    attackLagger = new Lagger(internalAmp, 1f);
    releaseLagger = new Lagger(internalAmp, 0f);

    //attackLagger.setRate(0.2f);
    //releaseLagger.setRate(0.2f);
  }
  public void updateEnvelope() {
    float previousAmp = internalAmp;
    if (attacking) {
      internalAmp = attackLagger.update();
    } else if (releasing) {
      internalAmp = releaseLagger.update();
    }

    if (internalAmp == previousAmp && (attacking || releasing)) {
      attacking = false;

      if (releasing) {
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

  public abstract int getModRate();
  public abstract int getModDepth();

  public float getAmplitude() {
    return amplitude;
  }
  public abstract void setAmplitude(float amp);
  public abstract void factorAmplitude(float factor);

  public void setBaseFreq(float freq) {
    BASE_FREQ = freq;
  }

  public synchronized void setFreqByOffset(int[] scale, int offset) {
    float freq = Theory.getFrequencyForScaleNote(scale, BASE_FREQ, offset);
    setFreq(freq);
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public int getPhase() {
    return oscPhase;
  }
  public void setPhase(int phase) {
    this.oscPhase = phase;
    // We need to recalc the tables here
    // FIXME
    //fill();
  }
}
