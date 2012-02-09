package com.mattfeury.saucillator.android.instruments;

import java.util.LinkedList;

import com.mattfeury.saucillator.android.sound.Delay;
import com.mattfeury.saucillator.android.sound.Lagger;
import com.mattfeury.saucillator.android.sound.Limiter;

/**
 * A complex oscillator.
 * An oscillator that is made up of BasicOscs and sums them together.
 */
public class ComplexOsc extends Oscillator {

  protected LinkedList<Oscillator> components;

  public static final float MAX_AMPLITUDE = 1.0f;//what is this for, eh?

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

  // Lfo is handled by the children

  public ComplexOsc() {
    this(1.0f);
  }
  public ComplexOsc(float amp) {
    amplitude = amp;
    components = new LinkedList<Oscillator>();
  }

  public void fill(Oscillator... oscs) {
    for(Oscillator osc : oscs) {
      osc.setPlaying(true); //we manage playback here, so all the children should always be playing
      components.add(osc);
      osc.chuck(this);
    }
  }
  public LinkedList<Oscillator> getComponents() {
    return components;
  }
  public Oscillator getComponent(int index) {
    return components.get(index);
  }

  public void setFreq(float freq) {
    for(Oscillator osc : components)
      osc.setFreq(freq * this.harmonic);
  }

  public ComplexOsc resetEffects() {
    setModRate(0);
    setModDepth(0);
    setLag(0);

    envelopeEnabled = false;
    //setAttack(0);
    //setRelease(0);

    // For chaining. Because, why not?
    return this;
  }

  // LFO methods
  public void setModRate(int rate) {
    for(Oscillator osc : components)
      osc.setModRate(rate);
  }
  public void setModDepth(int depth) {
    for(Oscillator osc : components)
      osc.setModDepth(depth);
  }

  public int getModRate() {
    for(Oscillator osc : components)
      return osc.getModRate();

    return 0;
  }
  public int getModDepth() {
    for(Oscillator osc : components)
      return osc.getModDepth();

    return 0;
  }

  // Lag
  public float getLag() {
    for(Oscillator osc : components)
      return osc.getLag();
    
    return 0;
  }
  public void setLag(float rate) {
    for(Oscillator osc : components)
      osc.setLag(rate);
  }

  // Delay
  public void setDelayRate(int rate) {
    delay.setRate(rate);
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
      Limiter.limit(buffer);
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

    rendered();

    return didWork;
  }
}
