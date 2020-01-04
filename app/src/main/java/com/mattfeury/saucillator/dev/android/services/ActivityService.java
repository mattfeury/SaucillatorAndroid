package com.mattfeury.saucillator.dev.android.services;

import com.mattfeury.saucillator.dev.android.templates.Handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

public class ActivityService {
  private static Activity activity;
  private static boolean canService = false;

  public static void setup(Activity appActivity) {
    activity = appActivity;

    if (activity != null)
      canService = true;
  }

  public static void makeToast(String msg) {
    makeToast(msg, false);
  }
  public static void makeToast(String msg, boolean longLength) {
    if (canService)
      Toast.makeText(activity, msg, longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
  }

  public static AlertDialog.Builder getAlertBuilder() {
    return new AlertDialog.Builder(activity);
  }
  public static void withActivity(Handler<Activity> handler) {
    // This null is kinda lame. This is not a button handler at all FIXME
    handler.handle(activity);
  }
}
