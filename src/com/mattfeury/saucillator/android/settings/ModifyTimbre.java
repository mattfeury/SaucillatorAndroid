package com.mattfeury.saucillator.android.settings;

import java.util.LinkedList;

import com.mattfeury.saucillator.android.R;
import com.mattfeury.saucillator.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.android.instruments.Oscillator;
import com.mattfeury.saucillator.android.utilities.TimbreAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.View;

public class ModifyTimbre extends ListActivity {
  LinkedList<Oscillator> timbres;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.modify_timbre);

    ComplexOsc osc = ModifyInstrument.modifying;
    if (osc == null) {
      exit();
      return;
    }

    Button deleteButton = (Button) findViewById(R.id.add_timbre);

    deleteButton.setOnClickListener(
      new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
          Toast.makeText(getApplicationContext(),"I deed it", Toast.LENGTH_SHORT).show();
        }
      }
    );
    

    timbres = osc.getComponents();
    setListAdapter(new TimbreAdapter(this, this, R.layout.harmonic_list_item, timbres));

    ListView lv = getListView();
    lv.setTextFilterEnabled(false);

    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ModifyTimbre.this, TimbrePreferences.class);
        Oscillator timbre = timbres.get(position);

        intent.putExtra("createNew", false);
        intent.putExtra("type", timbre.getName());
        intent.putExtra("harmonic", timbre.getHarmonic());
        intent.putExtra("amplitude", timbre.getAmplitude());
        intent.putExtra("phase", timbre.getPhase());
        startActivityForResult(intent, 0);
      }
    });
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

