package com.mattfeury.saucillator.dev.android.tabs;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.widget.EditText;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.ActivityHandler;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.ClickHandler;
import com.mattfeury.saucillator.dev.android.utilities.Box;
import com.mattfeury.saucillator.dev.android.services.InstrumentService;
import com.mattfeury.saucillator.dev.android.services.ActivityService;
import com.mattfeury.saucillator.dev.android.services.VibratorService;

public class InstrumentManagerTab extends Tab {

  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15;

  private AlertDialog.Builder chooserBuilder;

  public InstrumentManagerTab(final AudioEngine engine) {
    super("Synths", "Synth Manager", engine);
    
    // Setup some dialogs and cache em
    ActivityService.withActivity(new ActivityHandler() {
      public void handle(Button button, Activity activity) {
        chooserBuilder = new AlertDialog.Builder(activity);
        chooserBuilder.setTitle("Load a Synth");
      }
    });

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
          
          showSaveAsDialog();
        }
      }),
      makeButton("Revert", new ClickHandler() {
        public void handle(Button button, Object o) {
          ComplexOsc current = AudioEngine.currentOscillator;

          if (current != null)
            loadInstrument(current.getName());
          else
            ActivityService.makeToast("Unable to revert. This synth may not be saved.", true);

          VibratorService.vibrate();
        }
      })/*,
      makeButton("Share", new ClickHandler() {
        public void handle(Button button, Object o) {
          VibratorService.vibrate();
        }
      })*/
    );
  }

  private void loadInstrument(String instrumentName) {
    ComplexOsc newOsc = InstrumentService.getInstrument(instrumentName);

    if (newOsc == null) {
      ActivityService.makeToast("Instrument Unable to Load: " + instrumentName);
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
        loadInstrument(allInstruments[item]);
      }
    });
    chooserBuilder.show();
  }
  
  private void saveInstrumentAs(ComplexOsc osc, String name) {
    ComplexOsc copy = InstrumentService.copyInstrument(osc);
    copy.setName(name);

    Box<ComplexOsc> savedBox = InstrumentService.saveInstrument(copy);
    String latestName = copy.getName(),
            message = "";
    if (savedBox.isDefined()) {
      message = "Instrument saved to SD card: " + latestName;
      AudioEngine.currentOscillator = savedBox.openOr(AudioEngine.currentOscillator);
    } else if (savedBox.isFailure()) {
      message = savedBox.getFailure();
    } else {
      message = "Instrument could not be saved to SD card";
    }
    ActivityService.makeToast(message, true);
  }
  private void showSaveAsDialog() {
    ActivityService.withActivity(new ActivityHandler() {
      public void handle(Button button, Activity activity) {
        ComplexOsc osc = AudioEngine.currentOscillator;

        AlertDialog.Builder saveBuilder = new AlertDialog.Builder(activity);
        final EditText saveAsInput = new EditText(activity);
        saveAsInput.setText(osc != null ? osc.getName() : "");

        saveBuilder
          .setTitle("Save Synth As...")
          .setView(saveAsInput)
          .setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              Editable value = saveAsInput.getText(); 
              saveInstrumentAs(AudioEngine.currentOscillator, value.toString());
            }
          }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              // Do nothing.
            }
          }).show();
      }
    });
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
