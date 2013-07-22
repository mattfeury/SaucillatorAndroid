package com.mattfeury.saucillator.dev.android.tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.widget.EditText;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.WavWriter;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;
import com.mattfeury.saucillator.dev.android.services.ActivityService;
import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.services.ViewService;

public class RecorderTab extends Tab {

  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15;
  private static final String fileFieldPrefix = "Filename: ";
  public RecorderTab(final AudioEngine engine) {
    super("Recorder", "Recorder", engine);

    final String recordName =  "Record";
    RectButton recordButton = new RectButton(recordName) {
      public void handle(Object o) {
        super.handle(o);

        VibratorService.vibrate();
        boolean isRecording = toggleRecording();
        if (isRecording) {
          this.focused = true;
          this.name = "Stop Recording";
        } else {
          this.focused = false;
          this.name = recordName;
        }

        Button recorderTab = ViewService.getButton("Recorder");
        ((TabSelector)recorderTab).toggleAlert();
      }
    };
    recordButton.setBorder(BORDER_SIZE);
    recordButton.setMargin(MARGIN_SIZE);

    RectButton filenameButton = new RectButton(fileFieldPrefix + WavWriter.filePrefix) {
      @Override
      public void handle(Object name) {
        super.handle(name);

        VibratorService.vibrate();
        showFilenameDialog(this);
      }
    };
    filenameButton.setBackgroundColor(SauceView.TAB_COLOR);
    //filenameButton.setBorder(0);
    filenameButton.setTextSize(28);
    filenameButton.setClear(true);

    panel.addChild(
      recordButton,
      filenameButton
    );
  }

  private boolean toggleRecording() {
    return engine.toggleRecording();
    // TODO move the toasty and intent stuff from AudioEngine to here
  }

  private void showFilenameDialog(final Button filenameInput) {
    ActivityService.withActivity(new Handler<Activity>() {
      public void handle(Activity activity) {

        AlertDialog.Builder saveBuilder = new AlertDialog.Builder(activity);
        final EditText input = new EditText(activity);
        input.setText(WavWriter.filePrefix);

        saveBuilder
          .setTitle("Set Prefix for Recorded Files")
          .setView(input)
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              Editable value = input.getText();
              WavWriter.filePrefix = value.toString();
              filenameInput.setName(fileFieldPrefix + value.toString());
              ViewService.refresh();
            }
          }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              // Do nothing.
            }
          }).show();
      }
    });
  }
}
