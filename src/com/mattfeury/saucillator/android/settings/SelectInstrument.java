package com.mattfeury.saucillator.android.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class SelectInstrument extends PreferenceActivity {

  EditText fileTextBox;
  Spinner noteSpinner;
  Spinner octaveSpinner;
  ToggleButton visualsToggle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setPreferenceScreen(createPreferenceHierarchy());
  }

  private PreferenceScreen createPreferenceHierarchy() {
    // Root
    PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

    // Inline preferences
    /*PreferenceCategory inlinePrefCat = new PreferenceCategory(this);
    inlinePrefCat.setTitle("Select Instrument");
    root.addPreference(inlinePrefCat);

    // Dialog based preferences
    PreferenceCategory dialogBasedPrefCat = new PreferenceCategory(this);
    dialogBasedPrefCat.setTitle("dialoog");
    root.addPreference(dialogBasedPrefCat);*/

    // List preference
    CharSequence[] entries = { "One", "Two", "Three" };
    CharSequence[] entryValues = { "1", "2", "3" };

    ListPreference listPref = new ListPreference(this);
    listPref.setEntries(entries);
    listPref.setEntryValues(entryValues);
    listPref.setDialogTitle("de tee");
    listPref.setKey("list_preference");
    listPref.setTitle("titlar");
    listPref.setSummary("sumry");
    root.addPreference(listPref);

    return root;
  }
}
