package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.OscillatorUpdater;
import com.mattfeury.saucillator.dev.android.templates.*;

public class FxTab extends Tab {

  public FxTab(final AudioEngine engine) {
    super("Effects", engine);

    panel.addChild(
      buildFxButton(
        "LFO Rate",
        new Handler<Integer>() {
          public void handle(final Integer progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setModRate(progress);
              }
            });
          }
        },
        AudioEngine.MOD_RATE_MIN,
        AudioEngine.MOD_RATE_MAX,
        AudioEngine.getCurrentOscillator().getModRate(),
        false
      ),
      buildFxButton(
        "LFO Depth",
        new Handler<Integer>() {
          public void handle(final Integer progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setModDepth(progress);
              }
            });
          }
        },
        AudioEngine.MOD_DEPTH_MIN,
        AudioEngine.MOD_DEPTH_MAX,
        AudioEngine.getCurrentOscillator().getModDepth(),
        false
      ),

      buildFxButton(
        "Delay Rate",
        new Handler<Integer>() {
          public void handle(final Integer progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setDelayRate(progress);
              }
            });
          }
        },
        AudioEngine.DELAY_RATE_MIN,
        AudioEngine.DELAY_RATE_MAX,
        AudioEngine.getCurrentOscillator().getDelayRate()
      ),
      buildFxButton(
        "Delay Decay",
        new Handler<Integer>() {
          public void handle(final Integer progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setDelayDecay(progress / 100f);
              }
            });
          }
        },
        0,
        100,
        (int) (100 * AudioEngine.getCurrentOscillator().getDelayDecay()),
        false
      ),

      buildFxButton(
        "Attack",
        new Handler<Integer>() {
          public void handle(final Integer progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                // Don't allow 1.0 attack or release because it would be 100% and never actually go anywhere
                osc.setAttack(Math.min(progress / 100f, .99f));
              }
            });
          }
        },
        0,
        99,
        (int) (100 * AudioEngine.getCurrentOscillator().getAttack())
      ),
      buildFxButton(
        "Release",
        new Handler<Integer>() {
          public void handle(final Integer progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                // Don't allow 1.0 attack or release because it would be 100% and never actually go anywhere
                osc.setRelease(Math.min(progress / 100f, .99f));
              }
            });
          }
        },
        0,
        99,
        (int) (100 * AudioEngine.getCurrentOscillator().getRelease()),
        false
      ),

      buildFxButton(
        "Glide",
        new Handler<Integer>() {
          public void handle(final Integer progress) {
            engine.updateOscillatorProperty(new OscillatorUpdater() {
              @Override
              public void update(ComplexOsc osc) {
                osc.setLag(Math.min(progress / 100f, .99f));
              }
            });
          }
        },
        0,
        99,
        (int) (100 * AudioEngine.getCurrentOscillator().getLag())
      )
    );
  }

  private Button buildFxButton(String name, Handler handler, int min, int max, int current) {
    return buildFxButton(name, handler, min, max, current, true);
  }

  private Button buildFxButton(String name, Handler handler, int min, int max, int current, boolean clear) {
    Button button = 
      ButtonBuilder
        .build(ButtonBuilder.Type.SLIDER, name)
        .withHandler(handler)
        .withBounds(min, max, current)
        .withClear(clear)
        .withBorderSize(5)
        .withMargin(25)
        .finish();

    return button;

  }
}
