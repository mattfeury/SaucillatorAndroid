package com.mattfeury.saucillator.dev.android.visuals;

import java.util.HashMap;
import java.util.LinkedList;

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
    private Paint backColor = new Paint(Color.BLACK);
    private boolean visuals = false;

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

      canvas.drawColor(backColor.getColor());

      if(visuals)	{
        // FIXME we don't got no fingas no mo
        /*fX = (fingers.values().size() > 0 ? 0 : fX);
        fY = (fingers.values().size() > 0 ? 0 : fY);

        for(Finger f : fingers.values()){
          if (f.id == 0) {
            fX += f.x;
            fY += f.y;
          } else {
            //backColor.setColor(Color.HSVToColor(new float[]{(f.x / canvas.getWidth())* 360, f.y / canvas.getHeight(), f.y / canvas.getHeight()}));
            fractGen.paint.setColor(Color.HSVToColor(new float[]{360 - (f.x / canvas.getWidth()* 360), 1f - f.y / canvas.getHeight(), 1f - f.y / canvas.getHeight()}));
          }
        }

        fX /= (fingers.values().size() > 0 ? fingers.values().size() : 1);
        fY /= (fingers.values().size() > 0 ? fingers.values().size() : 1);

        fractGen.drawFractal(new ComplexNum(fractGen.toInput(fX, true), fractGen.toInput(fY, false)), new ComplexNum(0,0), -1);*/
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
      LinkedList<Drawable> fingers = new LinkedList<Drawable>();
      for (Drawable drawable : drawables) {
        if (drawable instanceof FingeredOscillator)
          fingers.add(drawable);
      }

      drawables.removeAll(fingers);

      invalidate();
    }
}
