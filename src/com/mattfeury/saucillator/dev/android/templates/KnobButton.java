package com.mattfeury.saucillator.dev.android.templates;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.*;
import android.util.FloatMath;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class KnobButton extends Button {
  protected Paint status;

  private float progress, progressSin, progressCos, lastR, lastTheta = 0f;

  // TODO should this scale with the button?
  public static final int width = 75,
                          textSize = 14;

  public KnobButton(String name) {
    this(name, 0, 0);
  }
  public KnobButton(String name, int x, int y) {
    super(name, x, y, x + width, y + width);

    changeProgress(0);

    status = new Paint();
    status.setARGB(255, 255, 120, 120);
    status.setStrokeWidth(5);
    
    bg.setARGB(255, 255,255,255);
    bg.setStyle(Paint.Style.STROKE);
    bg.setStrokeWidth(2);
  }

  private void changeProgress(float f) {
    if (f < 0) f = 0;
    if (f > 1) f = 1;

    this.progress = f;

    float progressAngle = (Utilities.scale(f / 1f, 45, 315) + 180) % 360;
    float radianAngle = (float)Math.toRadians(progressAngle);
    progressSin = FloatMath.cos(radianAngle);
    progressCos = FloatMath.sin(radianAngle);

    handle(progress);
  }

  @Override
  public void draw(Canvas canvas) {
    int radius = width / 2,
        rectCenterX = (int) ((right - left) / 2f + left),
        rectCenterY = (int) ((bottom - top) / 2f + top),
        xCenter = rectCenterX,
        yCenter = rectCenterY;

    canvas.drawCircle(xCenter, yCenter, radius, bg);
    canvas.drawText(name, xCenter, yCenter + textSize / 2, text);
    canvas.drawLine(xCenter + radius * progressCos, yCenter - radius * progressSin, xCenter, yCenter, status);
  }
  
  @Override
  public void layoutChanged(int width, int height) {
    //FIXME what is this i don't even
    // See if we need to recalculate knob dimensions, otherwise just remove this
  }

  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    final int index = event.findPointerIndex(id);
    final int y = (int) event.getY(index);
    final int x = (int) event.getX(index);

    int centerX = (int) (left + width / 2f);
    int centerY = (int) (top + width / 2f);
    int dx = x - centerX;
    int dy = y - centerY;
    float r = FloatMath.sqrt(dx * dx + dy * dy);
    float theta = Utilities.roundFloat((float) Math.atan(dy / (float)dx), 2);

    // Change passed on difference of angles. If angles are the same, compare distance.
    if (theta > lastTheta || (theta == lastTheta && r > lastR && x > centerX) || (theta == lastTheta && r < lastR && x < centerX)) {
      // FIXME this magic .015f. it should probably not increment progress, but set it directly to the angle of the finger to the center 
      changeProgress(progress + .015f);
    } else if (theta < lastTheta || (theta == lastTheta && r < lastR && x > centerX) || (theta == lastTheta && r > lastR && x < centerX)) {
      changeProgress(progress - .015f);
    }

    lastTheta = theta;
    lastR = r;

    return new Full<Fingerable>(this);
  }
}
