package com.mattfeury.saucillator.dev.android.utilities;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.instruments.Oscillator;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TimbreAdapter extends ArrayAdapter<Oscillator> {

  private LinkedList<Oscillator> oscs;
  private Activity activity;

  public TimbreAdapter(Activity act, Context context, int textViewResourceId, LinkedList<Oscillator> oscs) {
    super(context, textViewResourceId, oscs);
    this.oscs = oscs;
    this.activity = act;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View v = convertView;
    if (v == null) {
      LayoutInflater vi = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(R.layout.harmonic_list_item, null);
    }
    Oscillator o = oscs.get(position);
    if (o != null) {
      TextView tt = (TextView) v.findViewById(R.id.toptext);
      TextView bt = (TextView) v.findViewById(R.id.bottomtext);
      if (tt != null)
        tt.setText("Type: "+o.getName());
      if (bt != null)
        bt.setText("Harmonic: " + o.getHarmonic() + ", Amplitude: " + o.getAmplitude());
    }
    return v;
  }
}

