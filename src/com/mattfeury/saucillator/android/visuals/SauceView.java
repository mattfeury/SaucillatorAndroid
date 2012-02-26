package com.mattfeury.saucillator.android.visuals;

import java.util.HashMap;
import java.util.Random;
import java.util.LinkedList;

import com.mattfeury.saucillator.android.SauceEngine;
import com.mattfeury.saucillator.android.sound.UGen;
import com.mattfeury.saucillator.android.utilities.Utilities;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SauceView extends View {

    //for the controller
    public static final float controllerWidth = .15f,
                              padHeight = .85f; //percentage
    public static final float padWidth = 1.0f - controllerWidth;
    public static final int numButtons = 3;
    
    private SauceEngine engine;
    
    private boolean init = false;

    //graphics elements
    private HashMap<Integer, Finger> fingers = new HashMap<Integer, Finger>();
    private RectButton loop, undo, reset;
    private LinkedList<DrawableParameter> params = new LinkedList<DrawableParameter>();
    private LinkedList<RectButton> paramEnablers = new LinkedList<RectButton>();

    FractalGen fractGen;
    float fX = 0, fY = 0; //fractal x and y coords
    Paint backColor, foregroundColor;
    private boolean visuals = false;

    public SauceView(Context context) {
      super(context);
      init(context);
    }
    public SauceView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context);
    }
    public SauceView(Context context, AttributeSet attrs, int defStyle) {
    	 super(context, attrs, defStyle);
    	 init(context);
    }

    public void setEngine(SauceEngine engine){
      this.engine = engine;
    }
    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
      super.onLayout(changed, left, top, right, bottom);
      recalculateParamEnablerSize();
    }

    public void init(Context context) {
      backColor = new Paint();
      foregroundColor = new Paint();
      foregroundColor.setColor(Color.GREEN);
      foregroundColor.setStrokeWidth(3);
      foregroundColor.setStyle(Paint.Style.STROKE);

      loop = new RectButton("Loop", 0, 0, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
      undo = new RectButton("Undo", 0, getHeight() / numButtons, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
      reset = new RectButton("Reset", 0, getHeight() / numButtons, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
    }

    public void recalculateParamEnablerSize() {
      final int numEnablers = paramEnablers.size(),
                width = getWidth(),
                height = getHeight(),
                buttonWidth = (int) (width * padWidth / numEnablers),
                buttonHeight = (int) (height * (1f - padHeight)),
                controllerWidth = (int) (width * SauceView.controllerWidth),
                padHeight = (int) (height * SauceView.padHeight);

      for (int i = 0; i < numEnablers; i++) {
        RectButton enabler = paramEnablers.get(i);
        enabler.set(i * buttonWidth + controllerWidth, padHeight, buttonWidth, buttonHeight);
      }
    }
    // Takes in pixel values
    public void toggleEnablerAt(float x, float y) {
      for (RectButton enabler : paramEnablers) {
        if (enabler.contains(x, y)) {
          boolean isEnabled = enabler.toggleFocus();
          
          DrawableParameter corresponding = null;
          for (DrawableParameter param : params)
            if (param.getFullName().equals(enabler.getName())) {
              corresponding = param;
              break;
            }
          
          if (corresponding != null)
            corresponding.setEnabled(isEnabled);
          
          return;
        }
      }
    }
    public void addParam(DrawableParameter p) {
      if (! params.contains(p)) {
        params.add(p);

        RectButton enabler = new RectButton(p.getFullName());
        boolean shouldFocus = p.isEnabled();
        enabler.setFocus(shouldFocus);
        paramEnablers.add(enabler);
        recalculateParamEnablerSize();
      }
    }
    public void resetParams() {
      params.clear();
      paramEnablers.clear();
    }
    public DrawableParameter optParameter(float x, float y, Object preferred) {
      // A list of all params at this location. If there are multiple, we prefer 'preferred'
      LinkedList<DrawableParameter> contained = new LinkedList<DrawableParameter>();

      for (DrawableParameter p : params)
        if (p.contains((int)x, (int)y) && p.isEnabled())
          contained.add(p);

      int size = contained.size();
      if (size == 0)
        return null;
      else if (size == 1)
        return contained.getFirst();
      else if (contained.contains(preferred))
        return (DrawableParameter) preferred;
      else
        return contained.getLast();
    }

    public void setVisuals(boolean show) {
      visuals = show;
      invalidate();
    }
    public boolean getVisuals() {
      return visuals;
    }

    public boolean isInPad(float x, float y) {
      return x > (getMeasuredWidth() * controllerWidth) &&
              (getMeasuredHeight() * padHeight) > y; 
    }
    /**
     * Takes a point x,y on the screen and turns it into
     * a percentage from 0.0 to 1.0 of each's respective
     * location in the pad.
     */
    public float[] scaleToPad(float x, float y) {
      int controllerWidth = (int) (getWidth() * SauceView.controllerWidth),
          padHeight = (int) (getHeight() * SauceView.padHeight);
      final float padX = (x - controllerWidth) / (getWidth() - controllerWidth),
                  padY = (padHeight - y) / (float)padHeight;

      return new float[]{padX, padY};
    }

    public void focusLooper() {
      loop.focus();
    }
    public void unfocusLooper() {
      loop.unfocus();
    }

    public void updateOrCreateFinger(int id, float x, float y, float size, float pressure) {
      Finger maybe = fingers.get((Integer)id);
      if (maybe != null) {
        maybe.update(x, y, size, pressure);
      } else {
        Finger f = new Finger(id, x, y, size, pressure);
        fingers.put((Integer)id, f);
      }
      invalidate();
    }

    public void clearFingers() {
      fingers.clear();
      invalidate();
    }
    public void removeFinger(int id) {
      fingers.remove((Integer)id);
      invalidate();    
    }
 
    @Override
    public void onDraw(Canvas canvas) {
    	//draw pad
      if (fractGen == null)
        fractGen = new FractalGen(canvas);

      fX = (fingers.values().size() > 0 ? 0 : fX);
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


      canvas.drawColor(backColor.getColor());

      if(visuals)		
        fractGen.drawFractal(new ComplexNum(fractGen.toInput(fX, true), fractGen.toInput(fY, false)), new ComplexNum(0,0), -1);

      // Spectrum view
      if (engine != null) {
        final float[] samples = engine.getRecentSamples();
        Path path = new Path();
        int i = 0;

        for (float sample : samples) { //samples are floats from -1 to 1
          int scaledY = (int) ((1f - (sample + 1) / 2f) * getHeight());
          int scaledX = (int) (i / (float)UGen.CHUNK_SIZE * getWidth());

          if (i == 0)
            path.moveTo(scaledX, scaledY);
          else
            path.lineTo(scaledX, scaledY);

          android.util.Log.d("HUH", "( " + scaledX + ", " + scaledY + ")");

          i++;
        }
        
        //path.close();

        canvas.drawPath(path, foregroundColor);
      }

      for(Finger f : fingers.values())
        f.draw(canvas);

      // Draw controller
      // We must reset the button dimensions in this event because they are not accurate on load time.
      if (! init) {
        loop.set(0, 0, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        undo.set(0, getHeight() / numButtons, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        reset.set(0, getHeight() / numButtons * 2, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        init = true;
      }

      loop.draw(canvas);
      undo.draw(canvas);
      reset.draw(canvas);

      for (DrawableParameter p : params)
        p.draw(canvas);
      
      for (RectButton enabler : paramEnablers)
        enabler.draw(canvas);
    }

    class RectButton extends RectF {
      private Paint bg,text,focusedBg;
      private int borderWidth = 5;
      private String name;
      private boolean focused = false;
      public static final int textWidth = -8; //in pixels, i guess? just an estimate.

      public RectButton(String name) {
        this(name, 0, 0, 0, 0);
      }
      public RectButton(String name, int x, int y, int width, int height) {
        super(x, y, x + width, y + height);
        this.name = name;
        bg = new Paint();
        focusedBg = new Paint();
        text = new Paint();

        bg.setARGB(200, 12, 81, 4);
        focusedBg.setARGB(255, 28, 171, 11);
        text.setARGB(255, 255,255,255);
        focusedBg.setTextSize(14);
        focusedBg.setTextAlign(Align.CENTER);
        text.setTextSize(14);
        text.setTextAlign(Align.CENTER);
        
        bg.setStrokeWidth(5);

      }
      public String getName() {
        return name;
      }
      public void set(int x, int y, int width, int height) {
        super.set(x, y, x + width, y + height);      	
      }
      public boolean contains(int x, int y) {
        return (x > left && x <= right) &&
                (y > top && y <= bottom);
      }
      
      public void draw(Canvas canvas) {
        if (focused) {
          canvas.drawRect(left, top, right, top + borderWidth, focusedBg); //top line
          canvas.drawText(name, (right + left) / 2f, top + (bottom - top)* .5f, focusedBg);
        } else {
          canvas.drawRect(left + borderWidth, top, right - borderWidth, top + borderWidth, bg); //top line
          canvas.drawRect(left, top, left + borderWidth, bottom - borderWidth, bg); //left line
          canvas.drawRect(left, bottom - borderWidth, right, bottom, bg); //bottom line
          canvas.drawRect(right - borderWidth, top, right, bottom - borderWidth, bg); //right line
          
          canvas.drawText(name, (right + left) / 2f, top + (bottom - top)* .5f, text);
        }
      }

      public void setFocus(boolean focus) {
        focused = focus;
        invalidate();
      }
      public void focus() {
      	focused = true;
      	invalidate();
      }
      public void unfocus() {
      	focused = false;
      	invalidate();
      }
      public boolean toggleFocus() {
        focused = ! focused;
        invalidate();
        return focused;
      }
      
    }
}
