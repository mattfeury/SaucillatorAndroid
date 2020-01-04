package com.mattfeury.saucillator.dev.android.visuals;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.util.FloatMath;

import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.SmartRect;
import com.mattfeury.saucillator.dev.android.utilities.*;

public class FingeredOscillator extends SmartRect implements Fingerable {
  
  protected ComplexOsc osc;
  
  // We only need this to scale our x values appropriately. It kinda sucks to pass it around just for that.
  // TODO Consider an alternative.
  protected SauceView view;

  float x, y, size, pressure;
  float PRESSURE = 0.2f; //defaults because some screens don't report these and hence they won't be visible
  float SIZE = 0.1f;
  Paint color = new Paint();
  public final static int BASE_SIZE = 250;

  public FingeredOscillator(final SauceView view, ComplexOsc osc, int x, int y) {
    super(x, y, 0, 0);

    this.view = view;
    this.osc = osc;
  }

  @Override
  public void draw(Canvas canvas) {
    int width = canvas.getWidth();

    //rgb(160,203,69)
    color.setARGB((int)Math.floor((x/(float)width)*255), 160, 203, 69);

    canvas.drawCircle(x, y, size * BASE_SIZE, color);
  }

  private void updateFingerProps(float x, float y, float size, float pressure) {
    this.x = x;
    this.y = y;
    this.size = Math.max(SIZE, size);
    this.pressure = Math.max(PRESSURE, pressure);

    set(x, y, 0, 0); // width and height are calculated internally via our finger size
  }

  @Override
  public void set(int x, int y, int width, int height) {
    int diameter = (int) (size * BASE_SIZE * 2);
    super.set(x, y, diameter, diameter);
  }

  @Override
  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    final int index = event.findPointerIndex(id);
    final int action = event.getAction();
    final int actionCode = action & MotionEvent.ACTION_MASK;
    final int actionIndex = event.getActionIndex();
    final int actionId = event.getPointerId(actionIndex);

    final float y = event.getY(index);
    final float x = event.getX(index);
    final float[] scaledCoords = view.scaleToPad(x,y);
    final float xScaled = scaledCoords[0];
    final float yScaled = scaledCoords[1];

    if (Utilities.idIsDown(id, event) || actionCode == MotionEvent.ACTION_MOVE) {
      updateFingerProps(x, y, event.getSize(index), event.getPressure(index));

      if(! osc.isPlaying())
        osc.togglePlayback();
      else if (osc.isReleasing())
        osc.startAttack();

      osc.setFreqByOffset(AudioEngine.scale, (int)(yScaled * SauceEngine.TRACKPAD_GRID_SIZE));
      osc.setAmplitude(xScaled);

      return new Full<Fingerable>(this);
    } else if (Utilities.idIsUp(id, event)) {
      if(osc.isPlaying() && ! osc.isReleasing())
        osc.togglePlayback();
    }

    return new Empty<Fingerable>();
  }

}
