package glodblock.com.github.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import glodblock.com.github.orevisualdetector.Main;
import net.minecraftforge.fml.relauncher.FMLInjectionData;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Created by GlodBlock on 17.1.2022.
 */
public class HandlerOreData {

    public static HashMap<String, short[]> mOreDictMap = new HashMap<>();
    public static HashMap<String, short[]> mUnlocalizedMap = new HashMap<>();
    public static HashMap<String, Short> mNameToIDMap = new HashMap<>();
    public static HashMap<Short, String> mIDToNameMap = new HashMap<>();
    public static HashMap<Short, String> mIDToDisplayNameMap = new HashMap<>();

    public static String readJsonFile() {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource("assets/orevisualdetector/data/data.json");
            return readStream(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String tryReadJsonFile() {
        try {
            URL url = new File(new File(new File((File) FMLInjectionData.data()[6], "config"), "OreVisualDetector"), "CustomOreColorMap.json").toURI().toURL();
            return readStream(url);
        } catch (Exception e) {
            try {
                File tFile = new File(new File(new File((File) FMLInjectionData.data()[6], "config"), "OreVisualDetector"), "CustomOreColorMap.json");
                if (tFile.createNewFile()) {
                    FileOutputStream tOut = new FileOutputStream(tFile, true);
                    String tBuffer =
                            "{\n" +
                            "  \"OreColorMap\":[\n" +
                            "  ]\n" +
                            "}\n";
                    tOut.write(tBuffer.getBytes(StandardCharsets.UTF_8));
                    tOut.close();
                }
            } catch (Exception ignore) { }
        }
        return null;
    }

    private static String readStream(URL url) throws IOException {
        if (url == null) {
            return "";
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        return sb.toString();
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

            if (mNameToIDMap.containsKey(oreDictName) || mNameToIDMap.containsKey(unlocalizedName)) {
                if (oreDictName != null)
                    Main.Logger.warn(String.format("%s is defined twice!", oreDictName));
                if (unlocalizedName != null)
                    Main.Logger.warn(String.format("%s is defined twice!", unlocalizedName));
                continue;
            }

            if (colorData.get("Redirect") != null) {
                String tRedirect = colorData.get("Redirect").getAsString();
                short tID = mNameToIDMap.get(tRedirect);
                if (tID == 0) {
                    Main.Logger.warn(String.format("Undefined Redirect Target: %s!", colorData.get("Redirect").getAsString()));
                } else {
                    if (oreDictName != null) {
                        mNameToIDMap.put(oreDictName, tID);
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
                        mNameToIDMap.put(unlocalizedName, tID);
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
                mNameToIDMap.put(oreDictName, cnt);
                mIDToNameMap.put(cnt, oreDictName);
                generatorSubOre(oreDictName, new short[]{r, g, b}, cnt);
            }
            if (unlocalizedName != null) {
                mUnlocalizedMap.put(unlocalizedName, new short[]{r, g, b});
                cnt ++;
                mNameToIDMap.put(unlocalizedName, ++cnt);
                mIDToNameMap.put(cnt, unlocalizedName);
            }
        }
    }

    public static void generatorSubOre(String oreDictName, short[] color, short id) {
        if (!oreDictName.startsWith("oreNether") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreNether");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreEnd") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreEnd");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreGravel") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreGravel");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreNetherrack") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreNetherrack");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreEndstone") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreEndstone");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreSand") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreSand");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreBlackgranite") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreBlackgranite");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreRedgranite") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreRedgranite");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreMarble") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreMarble");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
        if (!oreDictName.startsWith("oreBasalt") && oreDictName.startsWith("ore")) {
            String subOre = oreDictName.replaceFirst("ore", "oreBasalt");
            mOreDictMap.put(subOre, color);
            mNameToIDMap.put(subOre, id);
        }
    }

}
