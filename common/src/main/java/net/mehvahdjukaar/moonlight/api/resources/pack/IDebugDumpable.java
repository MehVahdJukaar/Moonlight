package net.mehvahdjukaar.moonlight.api.resources.pack;

import java.nio.file.Path;

public interface IDebugDumpable {

    /**
     * Implement if the resource can emit a debug dump.
     */
    void dumpToDisk(Path dir);

}
