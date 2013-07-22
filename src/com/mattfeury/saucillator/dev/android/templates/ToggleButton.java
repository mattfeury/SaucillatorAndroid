package com.mattfeury.saucillator.dev.android.templates;

import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

public class ToggleButton extends RectButton {
  public ToggleButton(String name) {
    super(name, 0, 0, 0, 0);

    this.bg.setColor(Color.WHITE);
    this.bg.setStyle(Paint.Style.STROKE);
    this.bg.setStrokeWidth(1);

    this.focusedBg.setColor(SauceView.PAD_COLOR);
    this.focusedBg.setStyle(Paint.Style.FILL);

    this.text.setTextAlign(Align.CENTER);

    addHandler(new Handler() {
      @Override
      public void handle(Object data) {
        VibratorService.vibrate();
        focused = ! focused;
      }
    });
  }

  @Override
  public void draw(Canvas canvas) {
    int boxSize = (int) ((right - left) / 12);
    int middleY = (int)(top + (bottom - top) / 2);
    int middleX = (int)(left + (right - left) / 2);

    Rect rect = new Rect(middleX - boxSize * 3 - margin, middleY - boxSize / 2, middleX - boxSize * 2 - margin, middleY + boxSize / 2);
    canvas.drawRect(rect, bg);

    if (focused) {
      canvas.drawRect(rect, focusedBg);
    }

    canvas.drawText(name, middleX + boxSize, middleY + text.getTextSize() / 2, text);
  }
}
