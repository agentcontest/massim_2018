package massim;

import massim.protocol.WorldData;
import massim.protocol.DynamicWorldData;
import massim.protocol.StaticWorldData;
import massim.util.IOUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class ReplayWriter {

    private static final int GROUP_SIZE = 5;

    private String replayPath;

    private int lastGroup = -1;
    private JSONObject cache = new JSONObject();

    public ReplayWriter(String replayPath) {
        this.replayPath = replayPath;
    }

    public void updateState(String simId, String startTime, WorldData world) {
        if (world instanceof DynamicWorldData) {
            DynamicWorldData dynamicWorld = (DynamicWorldData) world;

            int step = dynamicWorld.step;
            int group = step / GROUP_SIZE;
            String stepStr = String.valueOf(step);

            if (lastGroup != group || cache.has(stepStr)) cache = new JSONObject();
            cache.put(stepStr, new JSONObject(dynamicWorld));
            write(startTime, simId, String.valueOf(group * GROUP_SIZE), cache);

            lastGroup = group;
        } else {
            write(startTime, simId, "static", new JSONObject(world));
        }
    }

    private void write(String startTime, String simId, String name, JSONObject json) {
        String prefix = startTime + "-" + simId;
        File file = Paths.get(this.replayPath, prefix, name + ".json").toFile();
        File dir = file.getParentFile();
        if (!dir.exists()) dir.mkdirs();

        try {
            FileWriter writer = new FileWriter(file);
            json.write(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
