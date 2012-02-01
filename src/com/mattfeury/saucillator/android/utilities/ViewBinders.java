package com.mattfeury.saucillator.android.utilities;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;

public class ViewBinders {
  public static void bindSliderToVariable(Activity a, int sliderId, int textId, int progress) {
    bindSliderToVariable(a, sliderId, textId, progress, 0);
  }
  public static void bindSliderToVariable(Activity a, int sliderId, int textId, int progress, final int startAt) {
    SeekBar slider = (SeekBar) a.findViewById(sliderId);
    slider.setIndeterminate(false);
    slider.setProgress(progress - startAt);
    final TextView value = (TextView) a.findViewById(textId);
    value.setText(""+progress);
    slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int change, boolean fromUser) {
        value.setText(""+(change+startAt));
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
  public static void bindCheckboxToSlider(Activity a, int checkboxId, int... sliderIds) {
    CheckBox checkbox = (CheckBox) a.findViewById(checkboxId);
    boolean enabled = checkbox.isChecked();
    
    final SeekBar[] sliders = new SeekBar[sliderIds.length];
    int i = 0;
    for (int id : sliderIds) {
      SeekBar slider = (SeekBar) a.findViewById(id);
      slider.setEnabled(enabled);
      sliders[i] = slider;
      i++;
    }
    
    checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for (SeekBar slider : sliders)
          slider.setEnabled(isChecked);
      }
    });
  }
}
