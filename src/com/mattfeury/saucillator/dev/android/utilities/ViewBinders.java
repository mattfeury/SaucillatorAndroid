package com.mattfeury.saucillator.dev.android.utilities;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;

public class ViewBinders {

  /**
   * Binds a SeekBar to update a TextView with the value, passing in
   * the current value (integer) and what the "min" of the slider is.
   * The max should be set via the layout xml. 
   */
  public static void bindSliderToVariable(Activity a, int sliderId, int textId, int progress) {
    bindSliderToVariable(a, sliderId, textId, progress, 0);
  }
  public static void bindSliderToVariable(Activity a, int sliderId, int textId, int progress, final int startAt) {
    SeekBar slider = (SeekBar) a.findViewById(sliderId);
    slider.setIndeterminate(false);
    slider.setProgress(progress - startAt);
    final int max = slider.getMax();

    final TextView value = (TextView) a.findViewById(textId);
    value.setText(""+progress);
    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int change, boolean fromUser) {
        int scaled = (int) ((max-startAt)/(float)max * change + startAt);
        value.setText(""+scaled);
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });    
  }
  public static void bindSliderToVariable(Activity a, int sliderId, int textId, float percent) {
    bindSliderToVariable(a, sliderId, textId, percent, 1f);
  }
  
  /**
   * Binds a SeekBar to update a TextView with the value, passing in
   * the current value (percentage, float 0.0-1.0) and what the "max" of the slider is.
   * Min is 0.
   */
  public static void bindSliderToVariable(Activity a, int sliderId, int textId, float percent, final float max) {
    SeekBar slider = (SeekBar) a.findViewById(sliderId);
    slider.setIndeterminate(false);
    slider.setProgress((int) ((percent / max) * 100));
    final TextView value = (TextView) a.findViewById(textId);
    value.setText(""+percent);
    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        value.setText(""+(progress * max / 100f));
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });    
  }
  
  /**
   * Enables any number of sliders depending on if a checkbox is checked.
   * It seems that this can only get called on a checkbox once, so pass through as many as needed.
   */
  public static void bindCheckboxToSlider(Activity a, int checkboxId, boolean enabled, int... sliderIds) {
    final SeekBar[] sliders = new SeekBar[sliderIds.length];
    int i = 0;
    for (int id : sliderIds) {
      SeekBar slider = (SeekBar) a.findViewById(id);
      slider.setEnabled(enabled);
      sliders[i] = slider;
      i++;
    }

    CheckBox checkbox = (CheckBox) a.findViewById(checkboxId);
    checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for (SeekBar slider : sliders)
          slider.setEnabled(isChecked);
      }
    });
  }
}
