package com.mattfeury.saucillator.dev.android.templates;

import java.util.Arrays;

import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public class PickerButton<K> extends Button {
  private K[] choices;
  private int selected;
  
  private RectButton incr, decr;

  public PickerButton(String name, K[] choices) {
    // Cast to int. The generic could be Integer. What would happen?
    this(name, choices, (int)0);
  }
  public PickerButton(String name, K[] choices, K choice) {
    this(name, choices, (int)(Arrays.asList(choices).indexOf(choice)));
  }

  public PickerButton(String name, K[] choices, int choice) {
    super(name, 0, 0, 0, 0);

    this.choices = choices;
    chooseChoice(choice);

    decr = new RectButton("-");
    decr.setBackgroundColor(SauceView.TAB_COLOR);
    decr.addHandler(new Handler<Boolean>() {
      public void handle(Boolean data) {
        VibratorService.vibrate();
        chooseChoice(selected - 1);
      }
    });
    incr = new RectButton("+");
    incr.setBackgroundColor(SauceView.TAB_COLOR);
    incr.addHandler(new Handler<Boolean>() {
      public void handle(Boolean data) {
        VibratorService.vibrate();
        chooseChoice(selected + 1);
      }
    });

    setTextSize(22);
  }
  
  private void chooseChoice(int i) {
    while (i < 0) { i += choices.length; }
    i %= choices.length;

    this.selected = i;

    handle(choices[selected]);
  }

  @Override
  public void setBorder(int size) {
    super.setBorder(size);

    incr.setBorder(size);
    decr.setBorder(size);
  }

  @Override
  public void setTextSize(int size) {
    super.setTextSize(size);

    incr.setTextSize(size);
    decr.setTextSize(size);
  }

  @Override
  public void set(int x, int y, int width, int height) {
    super.set(x, y, width, height);
    
    int columnWidth = (int)(width / 3f);
    decr.set(x, y, columnWidth, height);
    incr.set(x + columnWidth * 2, y, columnWidth, height);
  }

  @Override
  public void draw(Canvas canvas) {
    incr.draw(canvas);
    decr.draw(canvas);
    canvas.drawText(choices[selected].toString(), left + (right - left) / 2f, top + (bottom - top) / 2f + text.getTextSize() / 2, text);
  }

  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    final int index = event.findPointerIndex(id);
    final int x = (int) event.getX(index);
    final int y = (int) event.getY(index);

    if (incr.contains(x, y)) {
      return incr.handleTouch(id, event);
    } else if (decr.contains(x, y)) {
      return decr.handleTouch(id, event);
    } else {
      return new Empty<Fingerable>();
    }
  }
}
