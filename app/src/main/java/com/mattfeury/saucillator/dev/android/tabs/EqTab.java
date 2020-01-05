package com.mattfeury.saucillator.dev.android.tabs;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.OscillatorUpdater;
import com.mattfeury.saucillator.dev.android.sound.ParametricEQ;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.KnobButton;
import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.templates.ColumnPanel;
import com.mattfeury.saucillator.dev.android.utilities.Box;
import com.mattfeury.saucillator.dev.android.utilities.Empty;
import com.mattfeury.saucillator.dev.android.utilities.Fingerable;
import com.mattfeury.saucillator.dev.android.utilities.Full;
import com.mattfeury.saucillator.dev.android.utilities.Utilities;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;
import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.services.ViewService;

public class EqTab extends Tab {

  public EqTab(final AudioEngine engine) {
    super("EQ", engine);

    final ParametricEQ eq = engine.getEq();
    final EqPanel eqPanel = new EqPanel(eq);
    eqPanel.setRowspan(3);

    final KnobButton freqButton =
      (KnobButton) ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Freq")
        .withProgress(Utilities.unscale(eq.getFrequency(), ParametricEQ.minFreq, ParametricEQ.maxFreq))
        .withHandler(new Handler<Float>() {
          public void handle(final Float progress) {
            int frequency = (int) Utilities.scale(progress, ParametricEQ.minFreq, ParametricEQ.maxFreq);
            eqPanel.setFrequency(frequency);
            eq.setFrequency(frequency);
          }
        })
        .withClear(true)
        .finish();
    
    final KnobButton gainButton =
      (KnobButton) ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Gain")
        .withProgress(Utilities.unscale(eq.getGain(), ParametricEQ.minGain, ParametricEQ.maxGain))
        .withHandler(new Handler<Float>() {
          public void handle(final Float progress) {
            float gain = Utilities.scale(progress, ParametricEQ.minGain, ParametricEQ.maxGain);
            eqPanel.setGain(gain);
            eq.setGain(gain);
          }
        })
        .finish();

    eqPanel.setFreqHandler(new Handler<Float>() {
      public void handle(Float progress) {
        freqButton.changeProgress(progress);
      }
    });
    eqPanel.setGainHandler(new Handler<Float>() {
      public void handle(Float progress) {
        gainButton.changeProgress(progress);
      }
    });

    panel.addChild(
      eqPanel,
      freqButton,
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Q")
        .withProgress(Utilities.unscale(eq.getQ(), ParametricEQ.minQ, ParametricEQ.maxQ))
        .withHandler(new Handler<Float>() {
          public void handle(final Float progress) {
            float q = Utilities.scale(progress, ParametricEQ.minQ, ParametricEQ.maxQ);
            eqPanel.setQ(q);
            eq.setQ(q);
          }
        })
        .finish(),
      gainButton
    );
  }

  class EqPanel extends Button {
    private Path path;
    private Paint eqPaint;

    private int frequency;
    private float q, gain;
    
    private float midY = (bottom - top) / 2f;

    private static final int drawIncrement = 1;

    private Handler<Float> freqHandler, gainHandler;

    private int leftAnchor, rightAnchor, topAnchor, bottomAnchor;

    public EqPanel(ParametricEQ eq) {
      super("eq-panel");

      path = new Path();
      //path.setFillType(Path.FillType.EVEN_ODD);
      
      eqPaint = new Paint();
      eqPaint.setColor(SauceView.PAD_COLOR);

      setFrequency((int) eq.getFrequency());
      setQ(eq.getQ());
      setGain(eq.getGain());
    }

    public void setFreqHandler(Handler<Float> handler) {
      this.freqHandler = handler;
    }
    public void setGainHandler(Handler<Float> handler) {
      this.gainHandler = handler;
    }

    public void setFrequency(int frequency) {
      this.frequency = frequency;
      recalculatePath();
    }
    public void setQ(float q) {
      this.q = q;
      recalculatePath();
    }
    public void setGain(float gain) {
      this.gain = gain;
      recalculatePath();
    }

    @Override
    public void set(int x, int y, int width, int height) {
      super.set(x, y, width, height);

      midY = top + (bottom - top) / 2f;
      recalculatePath();
    }

    private void recalculatePath() {
      // Quadratic equation in form A*(x - h)^2 + C
      // Orientation is backwards since y values on the canvas go start at 0 and increment as you go down
      float orientation = (this.gain > 0) ? -1 : 1,
            a = (float) (Math.pow(ParametricEQ.minQ + ParametricEQ.maxQ - this.q, -2) * orientation),
            c = (bottom - top) / 2f * (this.gain / ParametricEQ.maxGain),
            h = (right - left) * (this.frequency / 20000f) + left;

      float discriminant = (float) (-4 * a * c); 
      float root1 = (float) (Math.sqrt(discriminant) / (2f * a)) + h; 
      float root2 = (float) (-Math.sqrt(discriminant) / (2f * a)) + h;

      path.reset();
      path.moveTo(left, midY);

      if (discriminant > 0) {
        float minRoot = Math.max(left, (float) Math.ceil(Math.min(root1, root2)));
        float maxRoot = Math.min(right, (float) Math.floor(Math.max(root1, root2)));

        path.lineTo(minRoot, midY);
        for (int x = (int) minRoot; x < maxRoot; x = x + drawIncrement) {
          float y = midY - (a * (float)Math.pow(x - h, 2) + c);
          path.lineTo(x, y);
        }

        path.lineTo(maxRoot, midY);
      }

      path.lineTo(right, midY);
      path.lineTo(right, midY - 1);
      path.lineTo(left, midY - 1);
    }

    @Override
    public void draw(Canvas canvas) {
      canvas.drawPath(path, eqPaint);
    }

    @Override
    public Box<Fingerable> handleTouch(int id, MotionEvent event) {
      int width = (int) (right - left);
      int height = (int) (bottom - top);

      if (Utilities.idIsDown(id, event)) {
        final int index = event.findPointerIndex(id);
        int originalX = (int) event.getX(index);
        int originalY = (int) event.getY(index);

        float gainProgress = 1f - Utilities.unscale(this.gain, ParametricEQ.minGain, ParametricEQ.maxGain);
        float freqProgress = Utilities.unscale(this.frequency, ParametricEQ.minFreq, ParametricEQ.maxFreq);

        leftAnchor = (int) (originalX - freqProgress * width);
        rightAnchor = (int) (originalX + (1 - freqProgress) * width);

        topAnchor = (int) (originalY - gainProgress * height);
        bottomAnchor = (int) (originalY + (1 - gainProgress) * height);

      } else if (Utilities.idIsMove(id, event)) {
        final int index = event.findPointerIndex(id);
        final int x = (int) event.getX(index);
        final int y = (int) event.getY(index);

        if (freqHandler != null) {
          int diff = x - leftAnchor;
          float percent = ((diff / (float)(rightAnchor - leftAnchor)));
          float clampedPercent = Math.max(0f, Math.min(percent, 1f));

          freqHandler.handle(clampedPercent);
        }
        if (gainHandler != null) {
          int diff = bottomAnchor - y;
          float percent = diff / (float)(bottomAnchor - topAnchor);
          float clampedPercent = Math.max(0f, Math.min(percent, 1f));

          gainHandler.handle(clampedPercent);
        }
      }

      return new Full<Fingerable>(this);
    }
  }
}
