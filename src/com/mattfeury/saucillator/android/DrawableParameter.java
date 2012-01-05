package com.mattfeury.saucillator.android;

import android.graphics.*;

/**
 * A parameter that is drawn on the screen and can be dragged to be changed.
 */
public class DrawableParameter {

  //protected Point point = new Point();
  private float x = 0f, y = 0f;
  private int lastX = 0, lastY = 0;
  protected Paint paint = new Paint();
  private int radius = 20;

  private boolean enabled = true;

  // The radius of the circle that encompasses this drawable circle
  // that is the touchable area for this parameters
  public int touchRadius = radius * 2;

  protected ParameterHandler handler;

  public DrawableParameter(ParameterHandler handler) {
    this.handler = handler;
    init();
  }
  protected DrawableParameter(ParameterHandler handler, float x, float y) {
    this(handler);

    this.x = Utilities.scale(x, SauceView.controllerWidth, 1);
    this.y = y;
  }
  private void init() {
    paint.setARGB(150, 200, 0, 0);
  }

  public void draw(Canvas canvas) {
    if (enabled) {
      lastX = (int) (this.x * canvas.getWidth());
      lastY = canvas.getHeight() - (int) (this.y * canvas.getHeight());
      canvas.drawCircle(lastX, lastY, radius, paint);
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
