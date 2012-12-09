package com.mattfeury.saucillator.dev.android.services;

import java.util.HashMap;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.tabs.TimbreTab;
import com.mattfeury.saucillator.dev.android.tabs.TimbreTab.TimbreTable;
import com.mattfeury.saucillator.dev.android.templates.*;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;

import android.view.View;

public class ViewService {
  private static View view;
  private static boolean canService = false;
  private static HashMap<String, Button> buttonsByName = new HashMap<String, Button>();

  public static void setup(View view) {
    ViewService.view = view;

    if (view != null)
      canService = true;
  }
  public static void refresh() {
    if (canService)
      view.invalidate();
  }

  public static void registerButton(String name, Button button) {
    buttonsByName.put(name, button);
  }
  public static void unregisterButton(String name) {
    buttonsByName.remove(name);
  }
  public static Button getButton(String name) {
    return buttonsByName.get(name);
  }

  // This is kinda lame having this here, but it's better than the alternatives
  // IT should maybe be in the Button object itself: e.g. an updateHandler.
  public static void updateOscillatorSettings(ComplexOsc osc) {
    // LFO knobs
    Button lfoRate = buttonsByName.get("LFO Rate");
    if (lfoRate != null) {
      float rate = osc.getModRate() / (float)AudioEngine.MOD_RATE_MAX;
      ((KnobButton)lfoRate).changeProgress(rate);
    }
    Button lfoDepth = buttonsByName.get("LFO Depth");
    if (lfoDepth != null) {
      float rate = osc.getModDepth() / (float)AudioEngine.MOD_DEPTH_MAX;
      ((KnobButton)lfoDepth).changeProgress(rate);
    }

    // Delay knobs
    Button delayRate = buttonsByName.get("Delay Rate");
    if (delayRate != null) {
      float rate = osc.getDelayRate() / (float)AudioEngine.DELAY_RATE_MAX;
      ((KnobButton)delayRate).changeProgress(rate);
    }
    Button delayDecay = buttonsByName.get("Delay Decay");
    if (delayDecay != null) {
      ((KnobButton)delayDecay).changeProgress(osc.getDelayDecay());
    }

    // Envelope
    Button attack = buttonsByName.get("Attack");
    if (attack != null) {
      ((KnobButton)attack).changeProgress(osc.getAttack());
    }
    Button release = buttonsByName.get("Release");
    if (release != null) {
      ((KnobButton)release).changeProgress(osc.getRelease());
    }

    Button glide = buttonsByName.get("Glide");
    if (glide != null) {
      ((KnobButton)glide).changeProgress(osc.getLag());
    }

    // Timbre
    Button timbreTable = buttonsByName.get("timbre-table");
    if (timbreTable != null) {
      ((TimbreTable)timbreTable).fill(osc);
    }
  }
}
