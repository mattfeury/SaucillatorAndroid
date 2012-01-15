package com.mattfeury.saucillator.android;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.*;

import android.content.res.AssetManager;
import android.util.Log;

public class InstrumentManager {

  private static final String assetPath = "instruments";
  private static final String extension = ".json";
  private static final String dataFolder = SauceEngine.DATA_FOLDER + "instruments/";
  
  public static String getFilename(String file) {
    int extensionIndex = file.lastIndexOf(extension);
    return file.substring(0, extensionIndex);
  }

  public static ArrayList<String> getAllInstrumentNames(AssetManager man) {
    ArrayList<String> instruments = new ArrayList<String>();

    // Get built-in asset instruments
    String[] assets;
    try {
      assets = man.list(assetPath);
      for (String asset : assets) {
        instruments.add(getFilename(asset));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Get user created
    File file =  new File(dataFolder);
    if (file.exists() && file.isDirectory()) {
      String[] files = file.list();
      for (String fileName : files) {
        instruments.add(getFilename(fileName));
      }
    }
    
    return instruments;
  }  

  public static ComplexOsc getInstrument(AssetManager man, String name) {
    return getInstrument(man, name, false);
  }

  public static ComplexOsc getInstrument(AssetManager man, String name, boolean external) {
    try {
      JSONObject json;
      if (external) {
        json = getJsonForCustomInstrument(name);
      } else {
        json = getJsonForInternalInstrument(man, name);
      }
      return decomposeJsonInstrument(json);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e("INSTR SAUCE", "bad instrument " + e.toString());
    }
    
    return null;
  }

  private static JSONObject getJsonForCustomInstrument(String name) throws Exception {
    FileInputStream stream = new FileInputStream(new File(dataFolder + name + extension));

    String jsonString = "";
    try {
      FileChannel fc = stream.getChannel();
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      /* Instead of using default, pass in a decoder. */
      jsonString = Charset.defaultCharset().decode(bb).toString();
    }
    finally {
      stream.close();
    }

    JSONObject json = new JSONObject(jsonString);
    return json;
  }

  private static JSONObject getJsonForInternalInstrument(AssetManager man, String name) throws Exception {
    InputStream is = man.open(assetPath + "/" + name + extension);
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

    String name = json.optString("name", "Unknown");
    instrument.setName(name);
    
    JSONArray timbres = json.getJSONArray("timbre");
    int numHarmonics = timbres.length();

    // TODO FIXME consider, rather than creating multiple sines, squares, etc,
    // just summing their tables together and creating one instrument.
    //
    // this may remove baggage and calculations.
    float totalAmp = 0;
    for (int i = 0; i < numHarmonics; i++) {
      JSONObject timbre = timbres.getJSONObject(i);
      String timbreId = timbre.optString("id", "sine");
      int harmonic = timbre.optInt("harmonic", 1);
      int phase = timbre.optInt("phase", 0);
      float amplitude = (float)timbre.optDouble("amplitude", 1.0);

      totalAmp += amplitude;

      Oscillator osc = getOscillatorForTimbre(timbreId, phase);
      osc.setHarmonic(harmonic);
      osc.setAmplitude(amplitude);

      instrument.fill(osc);
    }

    // scale amplitude values so that they sum to MAX_AMPLITUDE.
    float factor = ComplexOsc.MAX_AMPLITUDE / totalAmp;
    for (int i = 0; i < numHarmonics; i++) {
      Oscillator osc = instrument.getComponent(i);
      osc.factorAmplitude(factor);
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

  private static Oscillator getOscillatorForTimbre(String id, int phase) {
    if ("sine".equals(id))
      return new Sine(phase);
    else if ("saw".equals(id))
      return new Saw(phase);
    else if ("square".equals(id))
      return new Square(phase);
    else if ("noise".equals(id))
      return new Noise(phase);
    else //TODO lookup osc from files
      return new Sine(phase);
  }

}

