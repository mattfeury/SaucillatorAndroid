package com.mattfeury.saucillator.dev.android.templates;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class RectButton extends Button {
  protected Paint focusedBg;

  public RectButton(String name) {
    this(name, 0, 0, 0, 0);
  }
  public RectButton(String name, int x, int y, int width, int height) {
    super(name, x, y, x + width, y + height);

    focusedBg = new Paint();
    focusedBg.setARGB(255, 28, 171, 11);
    focusedBg.setTextSize(14);
    focusedBg.setTextAlign(Align.CENTER);
  }

  @Override
  public void draw(Canvas canvas) {
    if (focused) {
      canvas.drawRect(left, top, right, top + borderSize, focusedBg); //top line
      canvas.drawText(name, (right + left) / 2f, top + (bottom - top)* .5f, focusedBg);
    } else {
      canvas.drawRect(left + borderSize, top, right - borderSize, top + borderSize, bg); //top line
      canvas.drawRect(left, top, left + borderSize, bottom - borderSize, bg); //left line
      canvas.drawRect(left, bottom - borderSize, right, bottom, bg); //bottom line
      canvas.drawRect(right - borderSize, top, right, bottom - borderSize, bg); //right line
      
      canvas.drawText(name, (right + left) / 2f, top + (bottom - top)* .5f, text);
    }
  }

  public void addOnClick(ClickHandler handler) {
    handlers.add(handler);
  }
  public void click() {
    for (ClickHandler handler : handlers)
      handler.onClick();

    VibratorService.vibrate();
  }
  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    final int action = event.getAction();
    final int actionCode = action & MotionEvent.ACTION_MASK;
    final int actionIndex = event.getActionIndex();
    final int actionId = event.getPointerId(actionIndex);

    // TODO add vibrator here
    if (Utilities.idIsDown(id, event))
      click();

    // We don't want this to keep tracking subsequent touch events, so we handle it and return Empty
    return new Empty<Fingerable>();
  }

  public void setFocus(boolean focus) {
    focused = focus;
  }
  public void focus() {
    focused = true;
  }
  public void unfocus() {
    focused = false;
  }
  public boolean toggleFocus() {
    focused = ! focused;
    return focused;
  }

  public void setClear(boolean clear) {
    this.clearFloat = clear;
  }
  @Override
  public boolean shouldClearFloat() {
    return clearFloat;
  }
}
