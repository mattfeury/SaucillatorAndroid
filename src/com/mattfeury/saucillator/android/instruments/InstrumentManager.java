package com.mattfeury.saucillator.android.instruments;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.*;

import com.mattfeury.saucillator.android.SauceEngine;
import com.mattfeury.saucillator.android.utilities.Utilities;

import android.content.res.AssetManager;
import android.util.Log;

public class InstrumentManager {

  private static final String assetPath = "instruments";
  private static final String extension = ".json";
  private static final String dataFolder = SauceEngine.DATA_FOLDER + "instruments/";
  
  public static String stripExtension(String file) {
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
        instruments.add(stripExtension(asset));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Get user created
    File file =  new File(dataFolder);
    if (file.exists() && file.isDirectory()) {
      String[] files = file.list();
      for (String fileName : files) {
        instruments.add(stripExtension(fileName));
      }
    }
    
    return instruments;
  }

  public static String getAssetPath(String filename) {
    return assetPath + "/" + filename.toLowerCase() + extension;
  }
  public static boolean isInternal(AssetManager man, String name) {
    boolean isInternal = true;

    try {
      man.open(getAssetPath(name));
    } catch(Exception e) {
      isInternal = false;
    } finally {
      return isInternal;
    }
  }

  public static ComplexOsc getInstrument(AssetManager man, String name) {
    boolean isInternal = isInternal(man ,name);
    return getInstrument(man, name, isInternal);
  }

  public static ComplexOsc getInstrument(AssetManager man, String name, boolean internal) {
    try {
      JSONObject json;
      if (internal) {
        json = getJsonForInternalInstrument(man, name);
      } else {
        json = getJsonForCustomInstrument(name);
      }
      return decomposeJsonInstrument(man, json);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e("INSTR SAUCE", "bad instrument " + e.toString());
    }
    
    return null;
  }

  private static JSONObject getJsonForCustomInstrument(String name) throws Exception {
    FileInputStream stream = new FileInputStream(new File(dataFolder + name.toLowerCase() + extension));

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
    InputStream is = man.open(getAssetPath(name));
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

  private static ComplexOsc decomposeJsonInstrument(AssetManager man, JSONObject json) throws Exception {
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

      Oscillator osc = getOscillatorForTimbre(man, timbreId);
      osc.setPhase(phase);
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

  public static Oscillator getOscillatorForTimbre(AssetManager man, String id) {
    id = id.toLowerCase();
    if ("sine".equals(id))
      return new Sine();
    else if ("saw".equals(id))
      return new Saw();
    else if ("square".equals(id))
      return new Square();
    else if ("noise".equals(id))
      return new Noise();
    else //TODO lookup osc from files
      return getInstrument(man, id);
  }

  public static Oscillator copyInstrument(Oscillator o) {
    //TODO make this not deepCopy. maybe json or something
    return (Oscillator)Utilities.deepCopy(o);
  }

}

