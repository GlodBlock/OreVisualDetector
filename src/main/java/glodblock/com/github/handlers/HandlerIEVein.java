package glodblock.com.github.handlers;

import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HandlerIEVein {
    public static HashMap<String, short[]> veinMap = new HashMap<>();
    public static HashMap<String, Short> veinToIDMap = new HashMap<>();
    public static HashMap<Short, String> IDToVeinMap = new HashMap<>();
    public static HashMap<Short, ExcavatorHandler.MineralMix> IDToMaterialMap = new HashMap<>();

    public static void init() {
        short cnt = 0;
        for (ExcavatorHandler.MineralMix veinType : ExcavatorHandler.mineralList.keySet()) {
            int veinName = veinType.name.hashCode();
            XSTR r = new XSTR(veinName);
            veinMap.put(veinType.name, new short[]{
                    (short) r.nextInt(256),
                    (short) r.nextInt(256),
                    (short) r.nextInt(256)});
            veinToIDMap.put(veinType.name, cnt);
            IDToVeinMap.put(cnt, veinType.name);
            IDToMaterialMap.put(cnt, veinType);
            cnt ++;
        }
    }

    @SuppressWarnings("unchecked")
    public static Pair<String, Float>[] getMaterialList(ExcavatorHandler.MineralMix vein) {
        List<Pair<String, Float>> list = new LinkedList<>();
        for (int index = 0 ; index < vein.oreOutput.size(); index ++) {
            list.add(new ImmutablePair<>(vein.oreOutput.get(index).getDisplayName(), vein.recalculatedChances[index]));
        }
        return list.toArray(new Pair[0]);
    }

}
