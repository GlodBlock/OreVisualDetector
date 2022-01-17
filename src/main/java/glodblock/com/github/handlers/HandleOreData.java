package glodblock.com.github.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import glodblock.com.github.orevisualdetector.Main;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.FMLInjectionData;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Created by GlodBlock on 17.1.2022.
 */
public class HandleOreData {

    public static HashMap<String, short[]> mOreDictMap = new HashMap<>();
    public static HashMap<String, short[]> mUnlocalizedMap = new HashMap<>();
    public static HashMap<String, Short> mNameToIDMAp = new HashMap<>();
    public static HashMap<Short, String> mIDToNameMAp = new HashMap<>();
    public static HashMap<Short, String> mIDToDisplayNameMAp = new HashMap<>();
    public static HashMap<String, String> mTranslate = new HashMap<>();

    public static String readJsonFile() {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource("assets/orevisualdetector/data/data.json");
            assert url != null;
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String s;
            StringBuilder sb = new StringBuilder();
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String tryReadJsonFile() {
        try {
            URL url = new File(new File(new File((File) FMLInjectionData.data()[6], "config"), "OreVisualDetector"), "CustomOreColorMap.json").toURI().toURL();
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String s;
            StringBuilder sb = new StringBuilder();
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
            return sb.toString();
        } catch (Exception e) {
            try {
                new File(new File(new File((File) FMLInjectionData.data()[6], "config"), "OreVisualDetector"), "CustomOreColorMap.json").createNewFile();
            } catch (Exception ignore) { }
        }
        return null;
    }

    public static void run() {
        processColorMapData(readJsonFile());
        processColorMapData(tryReadJsonFile());
    }

    public static void processColorMapData(String data) {
        if (data == null) {
            return;
        }
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject)jsonParser.parse(data);
        JsonArray jsonArray = jsonObject.get("OreColorMap").getAsJsonArray();
        short cnt = 0;
        for (JsonElement elm : jsonArray) {
            JsonObject colorData = elm.getAsJsonObject();
            String oreDictName = null;
            String unlocalizedName = null;
            if (colorData.get("OreDict") != null)
                oreDictName = colorData.get("OreDict").getAsString();
            if (colorData.get("Unlocalized") != null)
                unlocalizedName = colorData.get("Unlocalized").getAsString();

            if (mNameToIDMAp.containsKey(oreDictName) || mNameToIDMAp.containsKey(unlocalizedName)) {
                if (oreDictName != null)
                    Main.Logger.warn(String.format("%s is defined twice!", oreDictName));
                if (unlocalizedName != null)
                    Main.Logger.warn(String.format("%s is defined twice!", unlocalizedName));
                continue;
            }

            if (colorData.get("Redirect") != null) {
                String tRedirect = colorData.get("Redirect").getAsString();
                short tID = mNameToIDMAp.get(tRedirect);
                if (tID == 0) {
                    Main.Logger.warn(String.format("Undefined Redirect Target: %s!", colorData.get("Redirect").getAsString()));
                }
                else {
                    if (oreDictName != null) {
                        mNameToIDMAp.put(oreDictName, tID);
                        if (mOreDictMap.containsKey(tRedirect)) {
                            mOreDictMap.put(oreDictName, mOreDictMap.get(tRedirect));
                            generatorSubOre(oreDictName, mOreDictMap.get(tRedirect), tID);
                        }
                        else if (mUnlocalizedMap.containsKey(tRedirect)) {
                            mOreDictMap.put(oreDictName, mUnlocalizedMap.get(tRedirect));
                            generatorSubOre(oreDictName, mUnlocalizedMap.get(tRedirect), tID);
                        }
                    }
                    if (unlocalizedName != null) {
                        mNameToIDMAp.put(unlocalizedName, tID);
                        if (mOreDictMap.containsKey(tRedirect))
                            mUnlocalizedMap.put(unlocalizedName, mOreDictMap.get(tRedirect));
                        else if (mUnlocalizedMap.containsKey(tRedirect))
                            mUnlocalizedMap.put(unlocalizedName, mUnlocalizedMap.get(tRedirect));
                    }
                }
                continue;
            }
            short r = colorData.get("R").getAsShort();
            short g = colorData.get("G").getAsShort();
            short b = colorData.get("B").getAsShort();
            if (oreDictName != null) {
                mOreDictMap.put(oreDictName, new short[]{r, g, b});
                cnt ++;
                mNameToIDMAp.put(oreDictName, cnt);
                mIDToNameMAp.put(cnt, oreDictName);
                generatorSubOre(oreDictName, new short[]{r, g, b}, cnt);
            }
            if (unlocalizedName != null) {
                mUnlocalizedMap.put(unlocalizedName, new short[]{r, g, b});
                cnt ++;
                mNameToIDMAp.put(unlocalizedName, ++cnt);
                mIDToNameMAp.put(cnt, unlocalizedName);
            }
        }
    }

    public static void generatorSubOre(String oreDictName, short[] color, short id) {
        if (!oreDictName.startsWith("oreNether") && oreDictName.startsWith("ore")) {
            String netherOre = oreDictName.replaceFirst("ore", "oreNether");
            mOreDictMap.put(netherOre, color);
            mNameToIDMAp.put(netherOre, id);
        }
        if (!oreDictName.startsWith("oreEnd") && oreDictName.startsWith("ore")) {
            String endOre = oreDictName.replaceFirst("ore", "oreEnd");
            mOreDictMap.put(endOre, color);
            mNameToIDMAp.put(endOre, id);
        }
    }

}
