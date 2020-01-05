package com.mattfeury.saucillator.dev.android.tabs;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.widget.EditText;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.Label;
import com.mattfeury.saucillator.dev.android.utilities.Box;
import com.mattfeury.saucillator.dev.android.services.InstrumentService;
import com.mattfeury.saucillator.dev.android.services.ActivityService;
import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.services.ViewService;

public class InstrumentManagerTab extends Tab {

  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15;

  private AlertDialog.Builder chooserBuilder;
  private Label currentInstrumentLabel;

  public InstrumentManagerTab(final AudioEngine engine) {
    super("Synths", "Synth Manager", engine);
    
    // Setup some dialogs and cache em
    ActivityService.withActivity(new Handler<Activity>() {
      public void handle(Activity activity) {
        chooserBuilder = new AlertDialog.Builder(activity);
        chooserBuilder.setTitle("Load a Synth");
      }
    });

    currentInstrumentLabel = new Label("Current Instrument: ---");
    setCurrentInstrument(AudioEngine.getCurrentOscillator().getName());
    currentInstrumentLabel.setClear(false);

    panel.addChild(
      currentInstrumentLabel,

      makeButton("Load", true, new Handler<Boolean>() {
        public void handle(Boolean b) {
          VibratorService.vibrate();
          showInstrumentChooser();
        }
      }),
      makeButton("Save As", new Handler<Boolean>() {
        public void handle(Boolean b) {
          VibratorService.vibrate();

          showSaveAsDialog();
        }
      }),
      makeButton("Revert", true, new Handler<Boolean>() {
        public void handle(Boolean b) {
          VibratorService.vibrate();
          ComplexOsc current = AudioEngine.currentOscillator;

          if (current != null)
            loadInstrument(current.getName());
          else
            ActivityService.makeToast("Unable to revert. This synth may not be saved.", true);
        }
      }),
      makeButton("Delete", new Handler<Boolean>() {
        public void handle(Boolean b) {
          VibratorService.vibrate();

          showDeleteDialog();
        }
      })/*,
      makeButton("Share", true, new Handler<Boolean>() {
        public void handle(Boolean b) {
          VibratorService.vibrate();
        }
      })*/
    );
  }

  private void loadInstrument(String instrumentName) {
    ComplexOsc newOsc = InstrumentService.getInstrument(instrumentName);

    if (newOsc == null) {
      ActivityService.makeToast("Unable to Load Instrument: " + instrumentName);
    } else {
      engine.setOscillator(newOsc);
      setCurrentInstrument(instrumentName);
      ViewService.updateOscillatorSettings(newOsc);
      ActivityService.makeToast("Instrument Loaded: " + instrumentName);
    }
  }

  private void setCurrentInstrument(String name) {
    currentInstrumentLabel.setText("Current Instrument: " + name);
    ViewService.refresh();
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
      setCurrentInstrument(AudioEngine.currentOscillator.getName());
    } else if (savedBox.isFailure()) {
      message = savedBox.getFailure();
    } else {
      message = "Instrument could not be saved to SD card";
    }
    ActivityService.makeToast(message, true);
  }
  private void showSaveAsDialog() {
    ActivityService.withActivity(new Handler<Activity>() {
      public void handle(Activity activity) {
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

  private void deleteInstrument(final String name) {
    Box<Boolean> savedBox = InstrumentService.deleteInstrument(name);

    if (savedBox.isDefined()) {
      ActivityService.withActivity(new Handler<Activity>() {
        public void handle(Activity activity) {
          new AlertDialog.Builder(activity)
            .setMessage("Successfully deleted '" + name + "' from disk. This instrument will remain in memory until another instrument is chosen. At that point it will be lost unless saved. Yathzee!")
            .setCancelable(false)
            .setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {}
            })
            .show();
        }
      });

    } else if (savedBox.isFailure()) {
      ActivityService.makeToast(savedBox.getFailure(), true);
    } else {
      ActivityService.makeToast("Unable to delete. This instrument may not be saved.", true);
    }    
  }
  private void showDeleteDialog() {
    ActivityService.withActivity(new Handler<Activity>() {
      public void handle(Activity activity) {
        final ComplexOsc osc = AudioEngine.currentOscillator;
        final String name = osc.getName();

        new AlertDialog.Builder(activity)
          .setMessage("Delete instrument '" + name + "' from SD card? This cannot be undone.")
          .setCancelable(true)
          .setPositiveButton("Yes",
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                  deleteInstrument(name);
                }
          })
          .setNegativeButton("No",
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
          })
          .show();
      }
    });

  }

  private Button makeButton(String name, Handler... handlers) {
    return makeButton(name, false, handlers);
  }
  private Button makeButton(String name, boolean clear, Handler... handlers) {
    ButtonBuilder builder = ButtonBuilder.build(ButtonBuilder.Type.RECT, name);
    
    for (Handler handler : handlers)
      builder.withHandler(handler);

    return
      builder
        .withBorderSize(BORDER_SIZE)
        .withMargin(MARGIN_SIZE)
        .withClear(clear)
        .finish();
  }
}
