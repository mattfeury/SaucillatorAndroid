package com.mattfeury.saucillator.dev.android.settings;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.instruments.InstrumentManager;
import com.mattfeury.saucillator.dev.android.instruments.Oscillator;
import com.mattfeury.saucillator.dev.android.utilities.TimbreAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;

public class ModifyTimbre extends ListActivity {
  private static final int COPY_ID = 0;
  private static final int DELETE_ID = 1;

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
    registerForContextMenu(lv);
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

        Oscillator osc = InstrumentManager.getOscillatorForTimbre(getAssets(), type);
        osc.setAmplitude(amplitude);
        osc.setHarmonic(harmonic);
        osc.setPhase(phase);

        if (createNew) {
          // Append to children
          ModifyInstrument.modifying.fill(osc);
        } else {
          // Remove old and insert new into old's position.
          ComplexOsc modifying = ModifyInstrument.modifying;
          modifying.removeComponent(position);

          modifying.insertComponent(position, osc);
        }

        adapter.notifyDataSetChanged();
      }
    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    menu.setHeaderTitle(timbres.get(info.position).getName());
    menu.add(Menu.NONE, COPY_ID, COPY_ID, "Duplicate");
    menu.add(Menu.NONE, DELETE_ID, DELETE_ID, "Delete");
  }
  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    
    switch(item.getItemId()) {
      case COPY_ID:
        Oscillator o = timbres.get(info.position);
        Oscillator copy = InstrumentManager.copyInstrumentForTimbre(o);
        timbres.add(copy);
        break;
      case DELETE_ID:
        ComplexOsc osc = ModifyInstrument.modifying;
        osc.removeComponent(info.position);
        break;
      default:
    }
    
    adapter.notifyDataSetChanged();
    return true;
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

