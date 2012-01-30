package com.mattfeury.saucillator.android.settings;

import java.util.LinkedList;

import com.mattfeury.saucillator.android.R;
import com.mattfeury.saucillator.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.android.instruments.InstrumentManager;
import com.mattfeury.saucillator.android.instruments.Oscillator;
import com.mattfeury.saucillator.android.utilities.TimbreAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.View;

public class ModifyTimbre extends ListActivity {
  private LinkedList<Oscillator> timbres;
  private TimbreAdapter adapter;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.modify_timbre);

    ComplexOsc osc = ModifyInstrument.modifying;
    if (osc == null) {
      exit();
      return;
    }

    Button addButton = (Button) findViewById(R.id.add_timbre);
    addButton.setOnClickListener(
      new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(ModifyTimbre.this, TimbrePreferences.class);
          intent.putExtra("createNew", true);
          intent.putExtra("creating", ModifyInstrument.creating);
          startActivityForResult(intent, 0);
        }
      }
    );

    timbres = osc.getComponents();
    adapter = new TimbreAdapter(this, this, R.layout.harmonic_list_item, timbres); 
    setListAdapter(adapter);

    ListView lv = getListView();
    lv.setTextFilterEnabled(false);

    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ModifyTimbre.this, TimbrePreferences.class);
        Oscillator timbre = timbres.get(position);

        intent.putExtra("createNew", false);
        intent.putExtra("creating", ModifyInstrument.creating);
        intent.putExtra("timbrePosition", position);
        intent.putExtra("type", timbre.getName());
        intent.putExtra("harmonic", timbre.getHarmonic());
        intent.putExtra("amplitude", timbre.getAmplitude());
        intent.putExtra("phase", timbre.getPhase());
        startActivityForResult(intent, 0);
      }
    });
  }
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0 && data != null) {
      Bundle extras = data.getExtras();

      if (extras != null) {
        Boolean createNew = extras.getBoolean("createNew");
        int position = extras.getInt("timbrePosition");

        String type = extras.getString("type");
        int harmonic = extras.getInt("harmonic");
        float amplitude = extras.getFloat("amplitude");
        int phase = extras.getInt("phase");

        Oscillator osc = InstrumentManager.getOscillatorForTimbre(type.toLowerCase(), phase);
        osc.setAmplitude(amplitude);
        osc.setHarmonic(harmonic);

        if (createNew)
          timbres.add(osc);
        else
          timbres.set(position, osc);

        adapter.notifyDataSetChanged();
      }
    }
  }


  public void exit() {
    Intent intent = new Intent(ModifyTimbre.this, ModifyInstrument.class);
    setResult(0, intent);

		Toast.makeText(this, "Changes Saved.", Toast.LENGTH_SHORT).show();
		finish();
  }
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Exit button
    if (keyCode == KeyEvent.KEYCODE_BACK)
      exit();

    return true;
	}
}  

