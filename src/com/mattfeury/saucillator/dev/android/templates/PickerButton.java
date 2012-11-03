package com.mattfeury.saucillator.dev.android.templates;

import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.utilities.*;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public class PickerButton<K> extends Button {
  //protected Paint status;

  private K[] choices;
  private int selected;
  
  private RectButton incr, decr;

  public PickerButton(String name, K[] choices) {
    super(name, 0, 0, 0, 0);

    this.choices = choices;
    chooseChoice(0);

    decr = new RectButton("-");
    decr.addHandler(new Handler<Boolean>() {
      public void handle(Boolean data) {
        VibratorService.vibrate();
        chooseChoice(selected - 1);
      }
    });
    incr = new RectButton("+");
    incr.addHandler(new Handler<Boolean>() {
      public void handle(Boolean data) {
        VibratorService.vibrate();
        chooseChoice(selected + 1);
      }
    });

    //incr.setMargin(20);
    //decr.setMargin(20);

    //status = new Paint();
    //status.setARGB(200, 246, 255, 66);
    //setBorder(3);
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
    canvas.drawText(choices[selected].toString(), left + (right - left) / 2f, top + (bottom - top) / 2f, text);
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
