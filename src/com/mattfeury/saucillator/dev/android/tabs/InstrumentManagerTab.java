package com.mattfeury.saucillator.dev.android.tabs;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.ClickHandler;
import com.mattfeury.saucillator.dev.android.services.InstrumentService;
import com.mattfeury.saucillator.dev.android.services.ActivityService;
import com.mattfeury.saucillator.dev.android.services.VibratorService;

public class InstrumentManagerTab extends Tab {

  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15;

  private AlertDialog.Builder chooserBuilder;

  public InstrumentManagerTab(final AudioEngine engine) {
    super("MGMT", "Synth Manager", engine);
    
    // Setup some dialogs and cache em
    chooserBuilder = ActivityService.getAlertBuilder();
    chooserBuilder.setTitle("Choose a Synth");

    Button firstChild = makeButton("Load", new ClickHandler() {
      public void handle(Button button, Object o) {
        VibratorService.vibrate();
        showInstrumentChooser();
      }
    });
    firstChild.setClear(false);

    panel.addChild(
      firstChild,
      makeButton("Save As", new ClickHandler() {
        public void handle(Button button, Object o) {
          VibratorService.vibrate();
        }
      }),
      makeButton("Revert", new ClickHandler() {
        public void handle(Button button, Object o) {
          VibratorService.vibrate();
        }
      }),
      makeButton("Share", new ClickHandler() {
        public void handle(Button button, Object o) {
          VibratorService.vibrate();
        }
      })
    );
  }

  private void chooseInstrument(String instrumentName) {
    ComplexOsc newOsc = InstrumentService.getInstrument(instrumentName);

    if (newOsc == null) {
      ActivityService.makeToast("Instrument Unable to Load: ");
    } else {
      ActivityService.makeToast("Instrument Loaded: " + instrumentName);
      engine.setOscillator(newOsc);
    }
  }
  private void showInstrumentChooser() {
    ArrayList<String> instruments = InstrumentService.getAllInstrumentNames();
    final String[] allInstruments = instruments.toArray(new String[instruments.size()]);

    chooserBuilder.setItems(allInstruments, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int item) {
        chooseInstrument(allInstruments[item]);
      }
    });
    chooserBuilder.show();
  }

  private Button makeButton(String name, ClickHandler... handlers) {
    ButtonBuilder builder = ButtonBuilder.build(ButtonBuilder.Type.RECT, name);
    
    for (ClickHandler handler : handlers)
      builder.withHandler(handler);

    return
      builder
        .withBorderSize(BORDER_SIZE)
        .withMargin(MARGIN_SIZE)
        .withClear(true)
        .finish();
  }
}
