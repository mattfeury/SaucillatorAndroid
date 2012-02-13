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
import android.os.Environment;
import android.util.Log;

public class InstrumentManager {

  private static final File dataDirectory = Environment.getExternalStorageDirectory();
  private static final String assetPath = "instruments",
                              extension = ".json",
                              dataFolder = SauceEngine.DATA_FOLDER + "instruments/",
                              instrumentDirPath = dataDirectory.getAbsolutePath() + "/" + dataFolder;
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
    File file =  new File(instrumentDirPath);
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
    }

    return isInternal;
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
    FileInputStream stream = new FileInputStream(new File(instrumentDirPath + name.toLowerCase() + extension));

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
      try {      
        final float lag = (float)fx.optDouble("lag", 0);
        instrument.setLag(lag);
      } catch (Exception e) {}

      // LFO
      try {
        JSONObject lfo = fx.getJSONObject("lfo");
        final int rate = lfo.optInt("rate", 0);
        final int depth = lfo.optInt("depth", 0);

        instrument.setModRate(rate);
        instrument.setModDepth(depth);
      } catch (Exception e) {}

      // Delay
      try {
        JSONObject delay = fx.getJSONObject("delay");
        final int time = delay.optInt("time", 0);
        final float decay = (float) delay.optDouble("decay", 0);

        instrument.setDelayRate(time);
        instrument.setDelayDecay(decay);
      } catch (Exception e) {}

      // Envelope
      try {
        JSONObject envelope = fx.getJSONObject("envelope");
        final float  attack = (float)envelope.optDouble("attack", 0.5),
                     release = (float)envelope.optDouble("release", 0.5);

        instrument.setAttack(attack);
        instrument.setRelease(release);
      } catch (Exception e) {}

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
    else {
      ComplexOsc osc = getInstrument(man, id);
      return (osc != null) ? osc.resetEffects() : null;
    }
  }
  
  public static boolean saveInstrument(ComplexOsc osc) {
    boolean success = true;
    try {
      File file = new File(instrumentDirPath + osc.getName() + extension);
      FileWriter writer = new FileWriter(file, false);
      JSONObject json = decomposeInstrumentToJson(osc);
      
      writer.write(json.toString());
      writer.flush();
      writer.close();
    } catch(Exception e) {
      success = false;
      e.printStackTrace();
    }
    return success;
  }
  public static boolean deleteInstrument(String name) {
    boolean success = true;
    try {
      File file = new File(instrumentDirPath + name + extension);
      success = file.delete();
    } catch(Exception e) {
      success = false;
      e.printStackTrace();
    }
    return success;
  }

  public static JSONObject decomposeInstrumentToJson(ComplexOsc osc) throws JSONException {
    JSONObject json = new JSONObject();

    String name = osc.getName();
    json.put("name", name);
    
    // Timbre
    JSONArray timbres = new JSONArray();
    for (Oscillator timbre : osc.getComponents()) {
      String timbreName = timbre.getName();
      if (timbreName != name) {
        JSONObject timbreJson = new JSONObject();
        timbreJson.put("id", timbreName);
        
        final int harmonic = timbre.getHarmonic(),
                  phase = timbre.getPhase();
        final float amplitude = timbre.getAmplitude();
        timbreJson.put("harmonic", harmonic);
        timbreJson.put("phase", phase);
        timbreJson.put("amplitude", amplitude);
        timbres.put(timbreJson);
      }
    }
    
    json.put("timbre", timbres);
      
    // FX
    JSONObject fx = new JSONObject();

    // Lag
    double lag = osc.getLag(); 
    fx.put("lag", lag);

    // LFO
    JSONObject lfo = new JSONObject();
    final int rate = osc.getModRate(),
              depth = osc.getModDepth();
    
    lfo.put("rate", rate);
    lfo.put("depth", depth);
    fx.put("lfo", lfo);

    // Delay
    JSONObject delay = new JSONObject();
    final int time = osc.getDelayRate();
    final double decay = osc.getDelayDecay();
    
    delay.put("time", time);
    delay.put("decay", decay);
    fx.put("delay", delay);
    
    // Envelope
    JSONObject envelope = new JSONObject();
    final double attack = osc.getAttack(),
                 release = osc.getRelease();
    
    envelope.put("attack", attack);
    envelope.put("release", release);
    fx.put("envelope", envelope);

    json.put("fx", fx);

    return json;
  }

  public static ComplexOsc copyInstrument(AssetManager man, ComplexOsc osc) {
    try {
      JSONObject json = decomposeInstrumentToJson(osc);
      ComplexOsc copy = decomposeJsonInstrument(man, json);
      return copy;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  public static Oscillator copyInstrumentForTimbre(Oscillator osc) {
    // TODO have this do json like the above. We can't use decomposeInstrument though
    // because this isn't guaranteed to have any FX
    return (Oscillator) Utilities.deepCopy(osc);
  }

}

