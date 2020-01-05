package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Typeface;

public class TabSelector extends RectButton {
  private Paint alertBg = new Paint();
  private boolean alerted = false;

  public TabSelector(String name) {
    this(name, 0, 0, 0, 0);
  }
  public TabSelector(String name, int x, int y, int width, int height) {
    super(name, x, y, x + width, y + height);

    bg.setColor(SauceView.SELECTOR_COLOR);
    focusedBg.setColor(SauceView.TAB_COLOR);
    alertBg.setColor(SauceView.ALERT_COLOR);

    bg.setStyle(Paint.Style.FILL_AND_STROKE);
    focusedBg.setStyle(Paint.Style.FILL_AND_STROKE);
    alertBg.setStyle(Paint.Style.FILL_AND_STROKE);

    this.textSizeMultiplier = 2;
    this.padding = 20;
    text.setSubpixelText(false);
    text.setAntiAlias(false);
    text.setTextAlign(Align.LEFT);
    text.setTypeface(Typeface.DEFAULT_BOLD);
  }
  
  public void toggleAlert() {
    alerted = ! alerted;
  }
  public void setAlert(boolean alert) {
    this.alerted = alert;
  }

  public void set(int left, int top, int right, int bottom) {
    super.set(left, top, right, bottom);
  }

  public void draw(Canvas canvas) {
    Paint bg = this.bg;
    if (alerted) {
      bg = alertBg;
    } else if (focused) {
      bg = focusedBg;
    }

    super.draw(canvas, bg);
  }
}
