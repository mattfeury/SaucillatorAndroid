package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.instruments.Oscillator;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.OscillatorUpdater;
import com.mattfeury.saucillator.dev.android.visuals.KnobButton;
import com.mattfeury.saucillator.dev.android.visuals.RectButton;

public class FxTab extends Tab {

  public FxTab(final AudioEngine engine) {
    super("Effects", engine);

    panel.addChild(new KnobButton("LFO Rate") {
      @Override
      public void onChange(final float progress) {
        engine.updateOscillatorProperty(new OscillatorUpdater() {
          @Override
          public void update(ComplexOsc osc) {
            osc.setModRate(progress);
          }
        });
       }
    });

    panel.addChild(new KnobButton("LFO Depth") {
      @Override
      public void onChange(final float progress) {
        engine.updateOscillatorProperty(new OscillatorUpdater() {
          @Override
          public void update(ComplexOsc osc) {
            osc.setModDepth(progress);
          }
        });
      }
    });

    panel.addChild(new KnobButton("Delay Rate") {
      @Override
      public void onChange(final float progress) {
        engine.updateOscillatorProperty(new OscillatorUpdater() {
          @Override
          public void update(ComplexOsc osc) {
            osc.setDelayRate(progress);
          }
        });
      }

      @Override
      public boolean shouldClearFloat() {
        return true;
      }
    });

    panel.addChild(new KnobButton("Delay Decay") {
      @Override
      public void onChange(final float progress) {
        engine.updateOscillatorProperty(new OscillatorUpdater() {
          @Override
          public void update(ComplexOsc osc) {
            osc.setDelayDecay(progress);
          }
        });
      }
    });

    //panel.addChild(new KnobButton("Attack"));
    //panel.addChild(new KnobButton("Release"));
  }
}
