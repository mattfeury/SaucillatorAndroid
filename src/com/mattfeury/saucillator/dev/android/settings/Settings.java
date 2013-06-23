package com.mattfeury.saucillator.dev.android.settings;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.Theory;
import com.mattfeury.saucillator.dev.android.instruments.Theory.Scale;
import com.mattfeury.saucillator.dev.android.services.ViewService;
import com.mattfeury.saucillator.dev.android.utilities.ViewBinders;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Settings extends Activity {

  private MediaPlayer secretSauce;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    secretSauce = MediaPlayer.create(this, R.raw.sauceboss);

    ToggleButton toggler = (ToggleButton)findViewById(R.id.visualsToggler);
    toggler.setChecked(ViewService.getVisualsToggle());
  }

  public void sendFeedbackEmail(View view) {
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.feedback_email_address)});
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, new String[]{getResources().getString(R.string.feedback_email_subject)});
    emailIntent.setType("plain/text");
    startActivity(Intent.createChooser(emailIntent, "Send email..."));
  }

  public void secretSauce(View view) {
    secretSauce.start();
  }

  public void saveChanges(View view) {
    ToggleButton toggler = (ToggleButton)findViewById(R.id.visualsToggler);
    boolean showVisuals = toggler.isChecked();
    ViewService.setVisuals(showVisuals);

    Intent intent = new Intent(Settings.this, SauceEngine.class);

    setResult(0, intent);

    finish();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK)
      saveChanges(null);

    return true;
  }
}
