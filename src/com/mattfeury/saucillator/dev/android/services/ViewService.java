package com.mattfeury.saucillator.dev.android.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.tabs.TimbreTab.TimbreTable;
import com.mattfeury.saucillator.dev.android.templates.*;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;

import android.view.View;

public class ViewService {
  private static SauceView view;
  private static boolean canService = false;
  private static HashMap<String, Button> buttonsByName = new HashMap<String, Button>();

  public static void setup(SauceView view) {
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

  public static void setVisuals(boolean showVisuals) {
    view.setVisuals(showVisuals);
  }
  public static boolean getVisualsToggle() {
    return view.getVisuals();
  }

  // This is kinda lame having this here, but it's better than the alternatives
  // IT should maybe be in the Button object itself: e.g. an updateHandler.
  public static void updateOscillatorSettings(ComplexOsc osc) {
    Map<String, Float> properiesByName = new HashMap<String, Float>();
    properiesByName.put("LFO Rate", osc.getModRate() / (float)AudioEngine.MOD_RATE_MAX);
    properiesByName.put("LFO Depth", osc.getModDepth() / (float)AudioEngine.MOD_DEPTH_MAX);
    properiesByName.put("Delay Rate", osc.getDelayRate() / (float)AudioEngine.DELAY_RATE_MAX);
    properiesByName.put("Delay Decay", osc.getDelayDecay());
    properiesByName.put("Attack", osc.getAttack());
    properiesByName.put("Release", osc.getRelease());
    properiesByName.put("Glide", osc.getLag());

    for (Entry<String, Float> entry : properiesByName.entrySet()) {
      Button button = buttonsByName.get(entry.getKey());
      if (button != null && button instanceof IntervalButton) {
        ((IntervalButton)button).changeProgress(entry.getValue());
      }
    }

    // Timbre
    Button timbreTable = buttonsByName.get("Timbre-Table");
    if (timbreTable != null) {
      ((TimbreTable)timbreTable).fill(osc);
    }

    refresh();
  }
  public static void toggleShowGrid() {
    view.toggleGrid();
    refresh();
  }
  public static boolean isGridShowing() {
    if (canService) {
      return view.isGridShowing();
    } else {
      return false;
    }
  }
}
