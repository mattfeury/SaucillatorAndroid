package com.mattfeury.saucillator.dev.android.tabs;

import java.util.ArrayList;
import java.util.LinkedList;

import android.graphics.Paint;
import android.graphics.Paint.Align;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.instruments.Oscillator;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.OscillatorUpdater;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.KnobButton;
import com.mattfeury.saucillator.dev.android.templates.PickerButton;
import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.templates.Table;
import com.mattfeury.saucillator.dev.android.utilities.Utilities;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;
import com.mattfeury.saucillator.dev.android.services.InstrumentService;
import com.mattfeury.saucillator.dev.android.services.VibratorService;

public class TimbreTab extends Tab {
  
  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 8, TEXT_SIZE = 18;
  private TimbreTable timbreTable;
  
  private static final float HARMONIC_MIN = 1, HARMONIC_MAX = 5, PHASE_MIN = 0, PHASE_MAX = 360;
  private Paint textPaint = new Paint();

  public TimbreTab(final AudioEngine engine) {
    super("Timbre", engine);

    textPaint.setARGB(255, 255, 120, 120);
    textPaint.setTextSize(24);
    textPaint.setFakeBoldText(true);
    textPaint.setTextAlign(Align.CENTER);

    final RectButton addButton = new RectButton("Add Timbre") {
      @Override
      public void handle(Object o) {
        super.handle(o);

        VibratorService.vibrate();
      }

      @Override
      public void set(int x, int y, int width, int height) {
        super.set(x, y, width, height);

      }
    };

    addButton.setMargin(0);
    addButton.setTextSize(TEXT_SIZE);
    addButton.setTextSizeMultiplier(3);
    addButton.setClear(false);

    addButton.addHandler(new Handler<Object>() {
      public void handle(Object data) {
        engine.updateOscillatorProperty(new OscillatorUpdater() {
          public void update(ComplexOsc osc) {
            Oscillator newTimbre = InstrumentService.getOscillatorForTimbre("Sine");
            osc.fill(newTimbre);
          }
        });

        timbreTable.fill(AudioEngine.currentOscillator);
      }
    });

    timbreTable = new TimbreTable();
    timbreTable.setRowspan(8);
    timbreTable.setBorder(0);
    timbreTable.setClear(true);
    timbreTable.fill(AudioEngine.getCurrentOscillator());

    panel.addChild(
      addButton,
      timbreTable
    );
  }

  public TimbreRow makeTimbreRowFor(Oscillator timbre, int index, String[] disallowedTypes) {
    TimbreRow row = new TimbreRow(timbre, index, disallowedTypes);

    return row;
  }
  
  public class TimbreTable extends Table {
    public TimbreTable() {
      super("Timbre");
    }

    public void fill(ComplexOsc osc) {
      this.removeChildren();

      LinkedList<Oscillator> timbres = osc.getComponents();

      // TODO should this check to see if it's basic or internal? SingingSaw is internal but not basic...
      String[] disallowed = osc.isInternal() ? new String[0] : new String[]{ osc.getName() }; 

      int i = 0;
      for (Oscillator timbre : timbres) {
        TimbreRow row = makeTimbreRowFor(timbre, i++, disallowed);
        if (i == 1) {
          row.setClear(false);
        }

        this.addChild(row);
      }
    }
  }

  private class TimbreRow extends Table {
    public TimbreRow(final Oscillator timbre, final int timbreIndex, String[] disallowedTypes) {
      super("timbre-row-" + timbre.getName());
      this.setClear(true);
      this.setBorder(0);

      ArrayList<String> timbres = InstrumentService.getAllInstrumentNames();
      for (String disallowedType : disallowedTypes) {
        int index = timbres.lastIndexOf(disallowedType);

        if (index > -1)
          timbres.remove(index);
      }

      /**
       * We can't just update properties on this Oscillator object because it likely comes from AudioEngine's currentOscillator
       * which is an oscillator that is never heard, but is an in-memory version of the current oscillator. It is used for creating
       * new oscillators at runtime. Because of this, we use AudioEngine's updateOscillatorProperty which will update the
       * currentOscillator but also update any existing (currently playing) oscillators. 
       */

      String name = timbre.getName();
      PickerButton<String> typePicker = new PickerButton<String>(name, timbres.toArray(new String[timbres.size()]), name);
      typePicker.setColspan(2);
      typePicker.addHandler(new Handler<String>() {
        public void handle(final String type) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              Oscillator timbre = osc.getComponent(timbreIndex);
              Oscillator newTimbre = InstrumentService.getOscillatorForTimbre(type);
              newTimbre.setAmplitude(timbre.getAmplitude());
              newTimbre.setHarmonic(timbre.getHarmonic());
              newTimbre.setPhase(timbre.getPhase());
              
              osc.removeComponent(timbreIndex);
              osc.insertComponent(timbreIndex, newTimbre);
            }
          });
        }
      });

      float harmonic = Utilities.unscale(timbre.getHarmonic(), HARMONIC_MIN, HARMONIC_MAX);
      KnobButton harmonicKnob = new KnobButton("Harmonic", harmonic);
      harmonicKnob.addHandler(new Handler<Float>() {
        public void handle(final Float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              Oscillator timbre = osc.getComponent(timbreIndex);
              timbre.setHarmonic(Utilities.scale(progress, (int)HARMONIC_MIN, (int)HARMONIC_MAX));
            }
          });
        }
      });

      KnobButton amplitudeKnob = new KnobButton("Amplitude", timbre.getAmplitude()); 
      amplitudeKnob.addHandler(new Handler<Float>() {
        public void handle(final Float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              Oscillator timbre = osc.getComponent(timbreIndex);
              timbre.setAmplitude(progress);
            }
          });
        }
      });

      float phase = Utilities.unscale(timbre.getPhase(), PHASE_MIN, PHASE_MAX);
      KnobButton phaseKnob = new KnobButton("Phase", phase);
      phaseKnob.addHandler(new Handler<Float>() {
        public void handle(final Float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              Oscillator timbre = osc.getComponent(timbreIndex);
              timbre.setPhase(Utilities.scale(progress, (int)PHASE_MIN, (int)PHASE_MAX));
            }
          });
        }
      });

      RectButton delete = new RectButton("X");
      delete.setTextPaint(textPaint);
      delete.setBackgroundColor(SauceView.TAB_COLOR);
      delete.addHandler(new Handler<Object>() {
        public void handle(Object o) {
          VibratorService.vibrate();

          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              osc.removeComponent(timbreIndex);
            }
          });

          // Do we have to refill here? We could potentially decrement the row indexes above this one
          timbreTable.fill(AudioEngine.currentOscillator);
        }
      });
      delete.setMargin(30);

      this.addChild(typePicker, harmonicKnob, amplitudeKnob, phaseKnob, delete);
    }
  }
}
