package com.mattfeury.saucillator.dev.android.templates;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.services.ViewService;
import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;
/**
 * Styling operates like the CSS `box-sizing` of `border-box`:
 * The width is constant, and margin, padding, and border are within those walls.
 */
public abstract class Button extends SmartRect implements Fingerable {
  protected Paint bg, text;
  protected int borderSize = 0, margin = 0, padding = 0, textSizeMultiplier = 1;
  protected String name;
  protected boolean focused = false;
  protected static final int textWidth = -8; //what is this magic number

  protected boolean clearFloat;
  
  private LinkedList<Handler> handlers = new LinkedList<Handler>();

  public Button(String name) {
    this(name, 0, 0, 0, 0);
  }
  public Button(String name, int x, int y, int width, int height) {
    super(x, y, x + width, y + height);
    
    this.name = name;

    bg = new Paint();
    text = new Paint();

    // Some magic-y defaults.
    bg.setColor(SauceView.PAD_COLOR);
    bg.setTextAlign(Paint.Align.CENTER);
    text.setARGB(255, 255,255,255);
    text.setTextAlign(Align.CENTER);

    bg.setStrokeWidth(5);

    ViewService.registerButton(name, this);
  }
  public String getName() {
    return name;
  }

  public void addHandler(Handler handler) {
    handlers.add(handler);
  }
  public void handle(Object o) {
    for (Handler handler : handlers)
      handler.handle(o);
  }

  @Override
  public boolean contains(int x, int y) {
    return x >= left + margin && x <= right - margin && y >= top + margin && y <= bottom - margin;
  }
  public abstract void draw(Canvas canvas);
  public abstract Box<Fingerable> handleTouch(int id, MotionEvent event);

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

  public void setTextSize(int size) {
    bg.setTextSize(size * textSizeMultiplier);
    text.setTextSize(size * textSizeMultiplier);
  }
  public void setTextSizeMultiplier(int factor) {
    this.textSizeMultiplier = factor;
    this.calculateTextSize();
  }
  public void setBorder(int stroke) {
    this.borderSize = stroke;
  }
  public void setMargin(int margin) {
    this.margin = margin;
  }
  public void setPadding(int padding) {
    this.padding = padding;
  }
  public void setClear(boolean clear) {
    this.clearFloat = clear;
  }
  public void setName(String name) {
    ViewService.unregisterButton(this.name);

    this.name = name;
    ViewService.registerButton(this.name, this);
  }

  @Override
  public boolean shouldClearFloat() {
    return clearFloat;
  }
}
