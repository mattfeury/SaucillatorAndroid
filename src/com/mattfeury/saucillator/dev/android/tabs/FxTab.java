package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.OscillatorUpdater;
import com.mattfeury.saucillator.dev.android.templates.*;

public class FxTab extends Tab {

  public FxTab(final AudioEngine engine) {
    super("Effects", engine);

    panel.addChild(
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "LFO Rate")
        .withHandler(new KnobHandler() {
          public void handle(final Float progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setModRate(progress);
              }
            });
          }
        })
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "LFO Depth")
        .withHandler(new KnobHandler() {
          public void handle(final Float progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setModDepth(progress);
              }
            });
          }
        })
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Delay Rate")
        .withHandler(new KnobHandler() {
          public void handle(final Float progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setDelayRate(progress);
              }
            });
          }
        })
        .withClear(true)
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Delay Decay")
        .withHandler(new KnobHandler() {
          public void handle(final Float progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setDelayDecay(progress);
              }
            });
          }
        })
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Attack")
        .withHandler(new KnobHandler() {
          public void handle(final Float progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                // Don't allow 1.0 attack or release because it would be 100% and never actually go anywhere
                osc.setAttack(Math.min(progress, .99f));
              }
            });
          }
        })
        .withClear(true)
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "")
        .withHandler(new KnobHandler() {
          public void handle(final Float progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                // Don't allow 1.0 attack or release because it would be 100% and never actually go anywhere
                osc.setRelease(Math.min(progress, .99f));
              }
            });
          }
        })
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Glide")
        .withHandler(new KnobHandler() {
          public void handle(final Float progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setLag(Math.min(progress, .99f));
              }
            });
          }
        })
        .withClear(true)
        .finish()
    );
  }
}
