package com.mattfeury.saucillator.dev.android.visuals;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.services.ViewService;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SauceView extends View {

    FractalGen fractGen;
    float fX = 0, fY = 0; //fractal x and y coords
    private boolean visuals = false;

    public static final int SELECTOR_COLOR = 0xff5CB67D;
    public static final int TAB_COLOR = 0XFF43875A;
    public static final int PAD_COLOR = 0XFF1B3F24;
    public static final int ALERT_COLOR = 0XC8C81414;

    private boolean showGrid = false;
    private Paint gridPaint = new Paint();

    // Add drawables to this collection to get drawn whenever the view is drawn.
    private LinkedList<Drawable> drawables = new LinkedList<Drawable>();

    public SauceView(Context context) {
      super(context);

      init();
    }
    public SauceView(Context context, AttributeSet attrs) {
      super(context, attrs);

      init();
    }
    public SauceView(Context context, AttributeSet attrs, int defStyle) {
    	 super(context, attrs, defStyle);

       init();
    }
    private void init() {
      ViewService.setup(this);

      gridPaint.setStyle(Paint.Style.FILL_AND_STROKE);
      gridPaint.setARGB(25, 255, 255, 255);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
      super.onLayout(changed, left, top, right, bottom);

      for (Drawable drawable : drawables)
        drawable.layoutChanged(right - left, bottom - top);
    }

    public void setVisuals(boolean show) {
      visuals = show;
      invalidate();
    }
    public boolean getVisuals() {
      return visuals;
    }

    public boolean toggleGrid() {
      this.showGrid = ! this.showGrid;
      return this.showGrid;
    }
    public boolean isGridShowing() {
      return showGrid;
    }

    public boolean isInPad(float x, float y) {
      return x > (getMeasuredWidth() * LayoutDefinitions.controllerWidth) &&
              (getMeasuredHeight() * LayoutDefinitions.padHeight) > y; 
    }
    /**
     * Takes a point x,y on the screen and turns it into
     * a percentage from 0.0 to 1.0 of each's respective
     * location in the pad.
     */
    public float[] scaleToPad(float x, float y) {
      int controllerWidth = (int) (getWidth() * LayoutDefinitions.controllerWidth),
          padHeight = (int) (getHeight() * LayoutDefinitions.padHeight);
      final float padX = Math.max((x - controllerWidth) / (getWidth() - controllerWidth), 0),
                  padY = (padHeight - y) / (float)padHeight;

      return new float[]{padX, padY};
    }
 
    @Override
    public void onDraw(Canvas canvas) {    	
      if (fractGen == null)
        fractGen = new FractalGen(canvas);

      canvas.drawColor(PAD_COLOR);

      if (visuals) {
        LinkedList<FingeredOscillator> fingers = getFingers();

        if (fingers.size() > 0) {
          fX = 0;
          fY = 0;

          FingeredOscillator finger = fingers.getFirst();
          fX += finger.x;
          fY += finger.y;
          fractGen.paint.setColor(Color.HSVToColor(new float[]{360 - (finger.x / canvas.getWidth()* 360), 1f - finger.y / canvas.getHeight(), 1f - finger.y / canvas.getHeight()}));

          fX /= fingers.size();
          fY /= fingers.size();
        }

        fractGen.drawFractal(new ComplexNum(fractGen.toInput(fX, true), fractGen.toInput(fY, false)), new ComplexNum(0,0), -1);
      }

      if (showGrid) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int rowDelta = height / SauceEngine.TRACKPAD_GRID_SIZE;
        for (int i = 0; i < SauceEngine.TRACKPAD_GRID_SIZE; i++) {
          int y = rowDelta * i;
          canvas.drawLine(0, y, width, y, gridPaint);
        }
      }


      for (Drawable drawable : drawables)
        drawable.draw(canvas);
    }

    public void addDrawable(Drawable drawable) {
      this.drawables.add(drawable);

      drawable.layoutChanged(getWidth(), getHeight());
    }
    public void removeDrawable(Drawable drawable) {
      this.drawables.remove(drawable);
    }
    public void clearFingers() {
      LinkedList<FingeredOscillator> fingers = getFingers();
      drawables.removeAll(fingers);

      invalidate();
    }

    public LinkedList<FingeredOscillator> getFingers() {
      LinkedList<FingeredOscillator> fingers = new LinkedList<FingeredOscillator>();
      for (Drawable drawable : drawables) {
        if (drawable instanceof FingeredOscillator)
          fingers.add((FingeredOscillator) drawable);
      }

      return fingers;
    }
}
