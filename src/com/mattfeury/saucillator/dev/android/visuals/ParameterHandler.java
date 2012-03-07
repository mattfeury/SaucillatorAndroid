package com.mattfeury.saucillator.dev.android.visuals;

/**
 * Essentially just a way to pass functions as arguments.
 * Passed to a DrawableParameter and called on the parameter when it is changed.
 */
public abstract class ParameterHandler {

  // X and Y are percentages (0.0 - 1.0)
  // FIXME x currently needs to be "unscaled" by the controller width - 1.0
  public abstract void updateParameter(float x, float y);
}
