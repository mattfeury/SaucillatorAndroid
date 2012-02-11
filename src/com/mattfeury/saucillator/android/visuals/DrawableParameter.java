package com.mattfeury.saucillator.android.visuals;

import java.text.DecimalFormat;

import com.mattfeury.saucillator.android.utilities.Utilities;

import android.graphics.*;

/**
 * A parameter that is drawn on the screen and can be dragged to be changed.
 */
public class DrawableParameter {

  private float x = 0f, y = 0f;
  private int lastX = 0, lastY = 0,
              maxX = 1, maxY = 1;
  protected Paint paint = new Paint(),
                  textPaint = new Paint();
  private int radius = 20,
              textSize = 14;

  private boolean enabled = true;
  protected boolean showCoords = true;

  private static final DecimalFormat df = new DecimalFormat("#.00");
  private int textOffset = radius * 2;
 
  // The radius of the circle that encompasses this drawable circle
  // that is the touchable area for this parameters
  public int touchRadius = radius * 2;

  protected ParameterHandler handler;

  public DrawableParameter(ParameterHandler handler) {
    this.handler = handler;
    init();
  }
  public DrawableParameter(ParameterHandler handler, float x, float y) {
    this(handler, x, y, 1, 1);
  }
  public DrawableParameter(ParameterHandler handler, float x, float y, int maxX, int maxY) {
    this(handler);

    this.x = Utilities.scale(x, SauceView.controllerWidth, 1);
    this.y = y;
    this.maxX = maxX;
    this.maxY = maxY;
  }
  private void init() {
    paint.setARGB(150, 200, 0, 0);
    textPaint.setARGB(150, 200, 200, 200);
    textPaint.setTextSize(textSize);
  }

  public void draw(Canvas canvas) {
    if (enabled) {
      lastX = (int) (this.x * canvas.getWidth());
      lastY = canvas.getHeight() - (int) (this.y * canvas.getHeight());
      canvas.drawCircle(lastX, lastY, radius, paint);

      if (showCoords) {
        int textWidth = 100,
            textHeight = textSize;
        int textX = (lastX - textWidth - textOffset) > 0 ?
                      lastX - textWidth - textOffset : lastX + textOffset;

        int textY = (lastY - textHeight - textOffset) > 0 ?
                      lastY - textHeight - textOffset : lastY + textOffset;

        float xVal = Float.valueOf(df.format(x)) * maxX;
        float yVal = Float.valueOf(df.format(y)) * maxY;
        canvas.drawText("(X: " + xVal + ", Y: " + yVal + ")", textX, textY, textPaint);
      }
    }
  }
  public void toggle() {
    enabled = ! enabled;
  }

  public void set(float x, float y) {
    //FIXME is there a better way to remove the controller width?
    this.x = Utilities.scale(x, SauceView.controllerWidth, 1);
    this.y = y;

    handler.updateParameter(this.x, this.y);
  }

  public boolean contains(int x, int y) {
    return Math.abs(x - lastX) <= touchRadius &&
            Math.abs(y - lastY) <= touchRadius;
  }
}
