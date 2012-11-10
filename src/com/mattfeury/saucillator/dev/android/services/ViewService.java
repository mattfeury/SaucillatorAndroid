package com.mattfeury.saucillator.dev.android.services;

import android.view.View;

public class ViewService {
  private static View view;
  private static boolean canService = false;

  public static void setup(View view) {
    ViewService.view = view;

    if (view != null)
      canService = true;
  }
  public static void refresh() {
    if (canService)
      view.invalidate();
  }
}
