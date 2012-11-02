package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.OscillatorUpdater;
import com.mattfeury.saucillator.dev.android.templates.*;

public class FxTab extends Tab {

  public FxTab(final AudioEngine engine) {
    super("Effects", engine);

    panel.addChild(
      new KnobButton("LFO Rate") {
        @Override
        public void onChange(final float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            @Override
            public void update(ComplexOsc osc) {
              osc.setModRate(progress);
            }
          });
         }
      },
      new KnobButton("LFO Depth") {
        @Override
        public void onChange(final float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            @Override
            public void update(ComplexOsc osc) {
              osc.setModDepth(progress);
            }
          });
        }
      },
      new KnobButton("Delay Rate") {
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
      },
      new KnobButton("Delay Decay") {
        @Override
        public void onChange(final float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            @Override
            public void update(ComplexOsc osc) {
              osc.setDelayDecay(progress);
            }
          });
        }
      },
      new KnobButton("Attack") {
        @Override
        public void onChange(final float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            @Override
            public void update(ComplexOsc osc) {
              // Don't allow 1.0 attack or release because it would be 100% and never actually go anywhere
              osc.setAttack(Math.min(progress, .99f));
            }
          });
        }

        @Override
        public boolean shouldClearFloat() {
          return true;
        }
      },
      new KnobButton("Release") {
        @Override
        public void onChange(final float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            @Override
            public void update(ComplexOsc osc) {
              // Don't allow 1.0 attack or release because it would be 100% and never actually go anywhere
              osc.setRelease(Math.min(progress, .99f));
            }
          });
        }
      },
      new KnobButton("Glide") {
        @Override
        public void onChange(final float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            @Override
            public void update(ComplexOsc osc) {
              osc.setLag(Math.min(progress, .99f));
            }
          });
        }
        public boolean shouldClearFloat() {
          return true;
        }
      }
    );
  }
}
