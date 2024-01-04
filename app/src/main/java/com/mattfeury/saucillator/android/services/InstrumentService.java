package com.mattfeury.saucillator.android.services;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.*;

import com.mattfeury.saucillator.android.instruments.*;
import com.mattfeury.saucillator.android.templates.Handler;
import com.mattfeury.saucillator.android.utilities.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

public class InstrumentService {

  public static final String assetPath = "instruments",
                             extension = ".json",
                             instrumentFolder = "instruments/";

  private static AssetManager manager = null;
  private static boolean canService = false;

  public static void setup(AssetManager man) {
    manager = man;

    if (manager != null)
      canService = true;
  }

  public static String stripExtension(String file) {
    int extensionIndex = file.lastIndexOf(extension);
    return file.substring(0, extensionIndex);
  }

  private static final String[] preferredOrder = new String[]{"Starslide", "Theremin", "Electric Eel", "Singing Saw", "Sine", "Square", "Saw", "Pulse", "Noise"};

  private static String getInternalInstrumentsPath() {
    final String[] path = {null};
    ActivityService.withActivity(new Handler<Activity>() {
      @Override
      public void handle(Activity activity) {
        File file = activity.getApplicationContext().getExternalFilesDir(InstrumentService.instrumentFolder);
        if (file != null && file.exists() && file.isDirectory()) {
          path[0] = file.getPath();
        }
      }
    });

    return path[0];
  }

  public static ArrayList<String> getAllInstrumentNames() {
    ArrayList<String> instruments = new ArrayList<String>();

    // Get built-in asset instruments
    // Sort them based on our preferredOrder defined above
    try {
      String[] assets = manager.list(assetPath);
      String[] ordered = new String[assets.length];
      List<String> preferredOrdered = Arrays.asList(preferredOrder);
      for (String asset : assets) {
        String stripped = stripExtension(asset);
        int order = preferredOrdered.indexOf(stripped);
        if (order > -1 && order < assets.length)
          ordered[order] = stripped;
      }
      instruments.addAll(Arrays.asList(ordered));
    } catch (Exception e) {
      e.printStackTrace();
      ActivityService.makeToast("Unable to load internal synths. :/");
    }

    // Get user created
    File file = new File(InstrumentService.getInternalInstrumentsPath());
    if (file.exists() && file.isDirectory()) {
      String[] files = file.list();
      if (files != null) {
        for (String fileName : files) {
          instruments.add(stripExtension(fileName));
        }
      }
    }

    return instruments;
  }

  public static String getAssetPath(String filename) {
    return assetPath + "/" + filename + extension;
  }
  public static boolean isInternal(String name) {
    boolean isInternal = true;

    try {
      manager.open(getAssetPath(name));
    } catch(Exception e) {
      isInternal = false;
    }

    return isInternal;
  }

  public static ComplexOsc getInstrument(String name) {
    boolean isInternal = isInternal(name);
    return getInstrument(name, isInternal);
  }

  public static ComplexOsc getInstrument(String name, boolean internal) {
    try {
      JSONObject json;
      if (internal) {
        json = getJsonForInternalInstrument(name);
      } else {
        json = getJsonForCustomInstrument(name);
      }
      return decomposeJsonInstrument(json);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e("INSTR SAUCE", "bad instrument " + e.toString());
    }
    
    return null;
  }

  private static JSONObject getJsonForCustomInstrument(String name) throws Exception {
    FileInputStream stream = new FileInputStream(new File(InstrumentService.getInternalInstrumentsPath() + "/" + name + extension));

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
    json.put("isInternal", false);
    return json;
  }

  private static JSONObject getJsonForInternalInstrument(String name) throws Exception {
    if (! canService)
      return new JSONObject();

    InputStream is = manager.open(getAssetPath(name));
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
    json.put("isInternal", true);
    return json;
  }

  private static ComplexOsc decomposeJsonInstrument(JSONObject json) throws Exception {
    // Lookup and create timbre
    ComplexOsc instrument = new ComplexOsc();

    String name = json.optString("name", "Unknown");
    instrument.setName(name);
    
    boolean isInternal = json.optBoolean("isInternal", false);
    instrument.setInternal(isInternal);

    JSONArray timbres = json.getJSONArray("timbre");
    int numHarmonics = timbres.length(),
        validHarmonics = numHarmonics;

    float totalAmp = 0;
    for (int i = 0; i < numHarmonics; i++) {
      JSONObject timbre = timbres.getJSONObject(i);
      String timbreId = timbre.optString("id", "sine");

      int harmonic = timbre.optInt("harmonic", 1);
      int phase = timbre.optInt("phase", 0);
      float amplitude = (float)timbre.optDouble("amplitude", 1.0);

      totalAmp += amplitude;

      // It may have been deleted.
      try {
        Oscillator osc = getOscillatorForTimbre(timbreId);
        osc.setPhase(phase);
        osc.setHarmonic(harmonic);
        osc.setAmplitude(amplitude);

        instrument.fill(osc);
      } catch (Exception e) {
        validHarmonics--;
      }
    }

    // TODO show warning message that not all timbres were deserializable
    //if (validHarmonics != numHarmonics)

    // scale amplitude values so that they sum to MAX_AMPLITUDE.
    float factor = ComplexOsc.MAX_AMPLITUDE / totalAmp;
    for (int i = 0; i < validHarmonics; i++) {
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

  public static Oscillator getOscillatorForTimbre(String id) {
    if ("Sine".equals(id))
      return new Sine();
    else if ("Saw".equals(id))
      return new Saw();
    else if ("Square".equals(id))
      return new Square();
    else if ("Noise".equals(id))
      return new Noise();
    else if ("Pulse".equals(id))
      return new Pulse();
    else {
      ComplexOsc osc = getInstrument(id);
      return (osc != null) ? osc.resetEffects() : null;
    }
  }

  public static Box<Boolean> isValidInstrumentName(String name) {
    if (isInternal(name))
      return new Failure<Boolean>("The name is already in use internally. Internal synths may not be modified.");
    else if (name.indexOf("*") != -1)
      return new Failure<Boolean>("Invalid character: *");
    else if (name.trim().equals(""))
      return new Failure<Boolean>("Name cannot be blank/spaces.");
    else
      return new Full<Boolean>(true);
  }

  public static Box<ComplexOsc> saveInstrument(ComplexOsc osc) {
    boolean success = true;
    String name = osc.getName();

    // If we're saving an instrument, it's not internal
    osc.setInternal(false);

    Box<Boolean> validName = isValidInstrumentName(name);
    if (validName.isFailure())
      return new Failure<ComplexOsc>("Invalid name. " + validName.getFailure());

    try {
      File file = new File(InstrumentService.getInternalInstrumentsPath() + "/" + name + extension);
      FileWriter writer = new FileWriter(file, false);
      JSONObject json = decomposeInstrumentToJson(osc);
      
      writer.write(json.toString());
      writer.flush();
      writer.close();
    } catch(Exception e) {
      success = false;
      e.printStackTrace();
    }
    if (success)
      return new Full<ComplexOsc>(osc);
    else
      return new Empty<ComplexOsc>();
  }
  public static Box<Boolean> deleteInstrument(String name) {
    boolean success = true;
    
    if (isInternal(name))
      return new Failure<Boolean>("You cannot delete a built-in synth.");

    try {
      File file = new File(InstrumentService.getInternalInstrumentsPath() + "/" + name + extension);
      success = file.delete();
    } catch(Exception e) {
      success = false;
      e.printStackTrace();
    }
    if (success)
      return new Full<Boolean>(true);
    else
      return new Empty<Boolean>();
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
    json.put("isInternal", osc.isInternal());
      
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

  public static ComplexOsc copyInstrument(ComplexOsc osc) {
    try {
      JSONObject json = decomposeInstrumentToJson(osc);
      ComplexOsc copy = decomposeJsonInstrument(json);
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

  public static void migrateV1Folder(Context context, Intent data) {
    Uri uriTree = data.getData();

    DocumentFile documentFile = null;
    if (uriTree != null) {
      documentFile = DocumentFile.fromTreeUri(context, uriTree);
    }
    int numAttempted = 0, numSuccess = 0;
    if (documentFile != null) {
      for (DocumentFile file : documentFile.listFiles()) {
        try {
          String name = file.getName();
          if (name != null && name.endsWith(".json")) {
            numAttempted++;
            Log.d("SAUCE", "Migrating old synth: " + file.getName() + "\n");

            InputStream inputStream = context.getContentResolver().openInputStream(file.getUri());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder fileContents = new StringBuilder();
            String mLine;
            while ((mLine = reader.readLine()) != null) {
              fileContents.append(mLine);
              fileContents.append('\n');
            }

            File newFile = new File(InstrumentService.getInternalInstrumentsPath() + "/" + name);
            FileWriter writer = new FileWriter(newFile, false);

            writer.write(fileContents.toString());
            writer.flush();
            writer.close();

            // Delete the old file to help with retries
            file.delete();

            numSuccess++;
          }
        } catch (Exception e) {
          Log.e("SAUCE", "Error converting v1 instrument: " + e.toString());
        }
      }
    }
    String msg;
    if (numAttempted > 0) {
      msg = "Migrated " + numSuccess + " synths successfully out of " + numAttempted + " total attempts.";
    } else {
      msg = "Did not find any synths to migrate. Are you sure you chose the right folder? Contact support for help if needed.";
    }

    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
    alertDialog.setTitle("Migration");
    alertDialog.setMessage(msg);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            });
    alertDialog.show();
  }
}