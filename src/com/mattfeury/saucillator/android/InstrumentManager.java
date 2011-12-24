package com.mattfeury.saucillator.android;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.json.*;

import android.content.res.AssetManager;
import android.util.Log;

public class InstrumentManager {

  public static ComplexOsc getInstrument(AssetManager man, String name) {
    try {
      JSONObject json = getJsonForInstrument(man, name);
      return decomposeJsonInstrument(json);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e("INSTR SAUCE", "bad instrument " + e.toString());
    }
    
    return null;
  }

  private static JSONObject getJsonForInstrument(AssetManager man, String name) throws Exception {
    InputStream is = man.open("instruments/" + name + ".json");
    Writer writer = new StringWriter();
    char[] buffer = new char[1024];
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      int n;
      while ((n = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, n);
      }
    } catch(Exception e) {
    } finally {
      is.close();
    }

    String jsonString = writer.toString();
    JSONObject json = new JSONObject(jsonString);
    return json;
  }

  private static ComplexOsc decomposeJsonInstrument(JSONObject json) throws Exception {
    // Lookup and create timbre
    ComplexOsc instrument = new ComplexOsc();
    
    JSONArray timbres = json.getJSONArray("timbre");
    int numHarmonics = timbres.length();

    for (int i = 0; i < numHarmonics; i++) {
      JSONObject timbre = timbres.getJSONObject(i);
      String timbreId = timbre.optString("id", "sine");
      int harmonic = timbre.optInt("harmonic", 1);
      float amplitude = (float)timbre.optDouble("amplitude", 1.0);

      Oscillator osc = getOscillatorForTimbre(timbreId);
      osc.setHarmonic(harmonic);
      osc.setAmplitude(amplitude);

      instrument.fill(osc);
    }

    // Lookup and modify FX
    try {
      JSONObject fx = json.getJSONObject("fx");

      // Lag
      float lag = (float)fx.optDouble("lag", 0);
      instrument.setLag(lag);

      // LFO
      try {
        JSONObject lfo = fx.getJSONObject("lfo");
        int rate = lfo.optInt("rate", 0);
        int depth = lfo.optInt("depth", 0);

        instrument.setModRate(rate);
        instrument.setModDepth(depth);
      } catch (Exception e) {}

      //TODO delay

    } catch (Exception e) {}

    return instrument;
  }

  private static Oscillator getOscillatorForTimbre(String id) {
    if ("sine".equals(id))
      return new Sine();
    else if ("saw".equals(id))
      return new Saw();
    else if ("square".equals(id))
      return new Square();
    else //TODO lookup osc from files
      return new Sine();
  }

}

