package com.mattfeury.saucillator.dev.android.services;

import android.content.Context;
import android.widget.Toast;

public class ToastService {
  private static Context context;
  private static boolean canToast = false;

  public static void setup(Context appContext) {
    context = appContext;

    if (context != null)
      canToast = true;
  }
  public static void makeToast(String msg) {
    makeToast(msg, false);
  }

  public static void makeToast(String msg, boolean longLength) {
    if (canToast)
      Toast.makeText(context, msg, longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
  }
}
