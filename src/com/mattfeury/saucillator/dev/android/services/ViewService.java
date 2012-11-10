package com.mattfeury.saucillator.dev.android.services;

import java.util.HashMap;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.*;

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
}
