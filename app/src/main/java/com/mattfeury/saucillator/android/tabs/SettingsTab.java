package com.mattfeury.saucillator.android.tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.view.View;

import com.mattfeury.saucillator.android.R;
import com.mattfeury.saucillator.android.services.ActivityService;
import com.mattfeury.saucillator.android.services.VibratorService;
import com.mattfeury.saucillator.android.services.ViewService;
import com.mattfeury.saucillator.android.sound.AudioEngine;
import com.mattfeury.saucillator.android.templates.Button;
import com.mattfeury.saucillator.android.templates.Handler;
import com.mattfeury.saucillator.android.templates.RectButton;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SettingsTab extends Tab {

  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15, TEXT_SIZE = 18;

  private MediaPlayer secretSauce;

  public SettingsTab(final AudioEngine engine) {
    super("Settings", engine);

    ActivityService.withActivity(new Handler<Activity>() {
      public void handle(Activity activity) {
        secretSauce = MediaPlayer.create(activity, R.raw.sauceboss);
      }
    });

    SettingsTab self = this;
    final RectButton feedbackButton = new RectButton("Send Feedback") {
      @Override
      public void handle(Object o) {
        super.handle(o);
        self.sendFeedbackEmail();
      }
    };
    feedbackButton.setBorder(BORDER_SIZE);
    feedbackButton.setMargin(MARGIN_SIZE);
    feedbackButton.setTextSize(TEXT_SIZE);
    feedbackButton.setClear(false);

    final RectButton migrateButton = new RectButton("Import Old Synths") {
      @Override
      public void handle(Object o) {
        super.handle(o);
        self.migrateV1Instruments();
      }
    };
    migrateButton.setBorder(BORDER_SIZE);
    migrateButton.setMargin(MARGIN_SIZE);
    migrateButton.setTextSize(TEXT_SIZE);
    migrateButton.setClear(true);

    final RectButton secretButton = new RectButton("Secret Sauce") {
      @Override
      public void handle(Object o) {
        super.handle(o);

        VibratorService.vibrate();
        secretSauce.start();
      }
    };
    secretButton.setBorder(BORDER_SIZE);
    secretButton.setMargin(MARGIN_SIZE);
    secretButton.setTextSize(TEXT_SIZE);
    secretButton.setClear(true);

    panel.addChild(
        feedbackButton,
        migrateButton,
        secretButton
    );
  }

  private void migrateV1Instruments() {
    ActivityService.withActivity(new Handler<Activity>() {
      public void handle(Activity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle("Migration");
        alertDialog.setMessage(activity.getResources().getString(R.string.instrument_migration_about));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Migrate",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

              Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
              activity.startActivityForResult(intent, ActivityService.V1_INSTRUMENT_MIGRATION_REQUEST_CODE);
            } else {
              ActivityService.makeToast("This version of android does not support migration");
            }
          }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Nevermind",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                  }
                }
        );
        alertDialog.show();

      }
    });

  }

  private void sendFeedbackEmail() {
    ActivityService.withActivity(new Handler<Activity>() {
      public void handle(Activity activity) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{activity.getResources().getString(R.string.feedback_email_address)});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, new String[]{activity.getResources().getString(R.string.feedback_email_subject)});
        emailIntent.setType("plain/text");
        activity.startActivity(Intent.createChooser(emailIntent, "Send email..."));
      }
    });

  }

}
