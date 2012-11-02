package com.mattfeury.saucillator.dev.android.services;

import android.os.Vibrator;

public class VibratorService {
  private static Vibrator vibrator;
  private static boolean canVibrate = false;
  private static final int VIBRATE_SPEED = 100; //in ms

  public static void setup(Vibrator systemVibrator) {
    vibrator = systemVibrator;

    if (vibrator != null)
      canVibrate = true;
  }
  public static void vibrate() {
    if (canVibrate)
      vibrator.vibrate(VIBRATE_SPEED);
  }
}
