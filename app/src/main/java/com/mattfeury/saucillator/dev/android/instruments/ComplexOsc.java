package com.mattfeury.saucillator.dev.android.instruments;

import java.util.LinkedList;

import android.view.MotionEvent;

import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.Delay;
import com.mattfeury.saucillator.dev.android.sound.Lagger;
import com.mattfeury.saucillator.dev.android.sound.Limiter;
import com.mattfeury.saucillator.dev.android.utilities.Box;
import com.mattfeury.saucillator.dev.android.utilities.Fingerable;
import com.mattfeury.saucillator.dev.android.utilities.Full;

/**
 * A complex oscillator.
 * An oscillator that is made up of BasicOscs and sums them together.
 */
public class ComplexOsc extends Oscillator {

  protected LinkedList<Oscillator> components;

  public static final float MAX_AMPLITUDE = 1.0f;//what is this for, eh?
  
  private boolean isInternal = false;

  // TODO break out FX into a common trait

  // Envelopes
  private float maxInternalAmp = 1.0f, // internalAmp always ranges from 0-maxInternalAmp
                internalAmp = 0f; // used to calculate attack and release to/from maxInternalAmp

  protected float attack = 0.85f,  // these are really just a percentage
                  release = 0.85f; // can we translate them to something more meaningful?
  protected Lagger attackLagger = new Lagger(0f, 1f, attack),
                   releaseLagger = new Lagger(1f, 0f, release);
  protected boolean attacking = false, releasing = false, envelopeEnabled = true;

  // Delay
  private Delay delay = new Delay(0);

  // Lfo + lag are handled by the children
  // but we keep a copy of their state here so we can update new children
  // as they come in. They should always be the same.
  protected int modRate = 0,
                modDepth = 0;
  protected float lag = AudioEngine.DEFAULT_LAG;

  public ComplexOsc() {
    this(1.0f);
  }
  public ComplexOsc(float amp) {
    amplitude = amp;
    components = new LinkedList<Oscillator>();
  }

  public void fill(Oscillator... oscs) {
    for(Oscillator osc : oscs) {
      components.add(osc);
      connectComponent(osc);
    }
  }
  public void fill() {
    for(Oscillator osc : components)
      osc.fill();    
  }
  public LinkedList<Oscillator> getComponents() {
    return components;
  }
  public Oscillator getComponent(int index) {
    return components.get(index);
  }
  public void removeComponent(int index) {
    Oscillator osc = components.remove(index);
    osc.setPlaying(false);
    osc.unchuck(this);
  }
  public void insertComponent(int index, Oscillator osc) {
    components.add(index, osc);
    connectComponent(osc);
  }
  private void connectComponent(Oscillator osc) {
    osc.setPlaying(true); //we manage playback here, so all the children should always be playing
    osc.chuck(this);

    // LFO + lag are handled in the children, so we need to update the new ones
    setModRate(modRate);
    setModDepth(modDepth);
    setLag(lag);
  }

  public void setInternal(boolean internal) {
    this.isInternal = internal;
  }
  public boolean isInternal() {
    return isInternal;
  }

  public void setFreq(float freq) {
    for(Oscillator osc : components)
      osc.setFreq(freq * this.harmonic);
  }

  public ComplexOsc resetEffects() {
    setModRate(0);
    setModDepth(0);
    setLag(0);
    setDelayRate(0);
    setDelayDecay(0);

    envelopeEnabled = false;
    //setAttack(0);
    //setRelease(0);

    // For chaining. Because, why not?
    return this;
  }

  // LFO methods
  public void setModRate(float progress) {
    setModRate((int)(progress * AudioEngine.MOD_RATE_MAX));
  }
  public void setModRate(int rate) {
    this.modRate = rate;

    for(Oscillator osc : components)
      osc.setModRate(rate);
  }
  public void setModDepth(float progress) {
    setModDepth((int)(progress * AudioEngine.MOD_DEPTH_MAX));
  }
  public void setModDepth(int depth) {
    this.modDepth = depth;

    for(Oscillator osc : components)
      osc.setModDepth(depth);
  }
  public int getModRate() {
    return modRate;    
  }
  public int getModDepth() {
    return modDepth;
  }

  // Lag
  public float getLag() {
    return lag;
  }
  public void setLag(float rate) {
    this.lag = rate;

    for(Oscillator osc : components)
      osc.setLag(rate);
  }

  // Delay
  public void setDelayRate(int rate) {
    delay.setRate(rate);
  }
  public void setDelayRate(float percentage) {
    setDelayRate((int)(percentage * AudioEngine.DELAY_RATE_MAX));
  }

  public int getDelayRate() {
    return delay.getRate();
  }
  public float getDelayDecay() {
    return delay.getDecay();
  }
  public void setDelayDecay(float decay) {
    delay.setDecay(decay);
  }

  public void setMaxInternalAmp(float amp) {
    this.maxInternalAmp = amp;
  }
  public float getMaxInternalAmp() {
    return maxInternalAmp;
  }

  /**
   * Envelope stuff
   * Maybe make this an interface or something
   */
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

  public float getAttack() {
    return attack;
  }
  public void setAttack(float a) {
    attack = a;
    resetLaggers();
  }
  public float getRelease() {
    return release;
  }
  public void setRelease(float r) {
    release = r;
    resetLaggers();
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
    attackLagger = new Lagger(internalAmp, maxInternalAmp, attack);
    releaseLagger = new Lagger(internalAmp, 0f, release);
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

  public void rendered() {
    if (envelopeEnabled)
      updateEnvelope();
  }  

  public synchronized boolean render(final float[] buffer) {
    boolean didWork = false;
    if(isPlaying()) {
      final float[] kidsBuffer = new float[CHUNK_SIZE];
      didWork = renderKids(kidsBuffer);
      for(int i = 0; i < CHUNK_SIZE; i++) {
        if (envelopeEnabled)
          buffer[i] += amplitude*internalAmp*kidsBuffer[i];
        else
          buffer[i] += amplitude*kidsBuffer[i];
      }
    }

    delay.render(buffer);

    Limiter.limit(buffer);

    rendered();

    return didWork;
  }
}
