package massim;

import massim.protocol.WorldData;
import massim.protocol.DynamicWorldData;
import massim.protocol.StaticWorldData;
import massim.util.IOUtil;

import java.nio.file.Paths;

public class ReplayWriter {

    private String replayPath;

    public ReplayWriter(String replayPath) {
        this.replayPath = replayPath;
    }

    public void updateState(String simId, String startTime, WorldData world) {
        // determine file name
        String file = "";
        if(world instanceof StaticWorldData) file = "static";
        else if(world instanceof DynamicWorldData) file = "step-" + String.valueOf(((DynamicWorldData) world).step);
        // save to file
        IOUtil.writeXMLToFile(
                world.toXML(world.getClass()),
                Paths.get(this.replayPath, startTime + "-" + simId, file + ".xml").toFile(),
                true);
    }
}
