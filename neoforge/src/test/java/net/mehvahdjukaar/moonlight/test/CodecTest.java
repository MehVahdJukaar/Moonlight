package net.mehvahdjukaar.moonlight.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

@ExtendWith(EphemeralTestServerProvider.class)
public abstract class CodecTest<T> {

    @Nullable
    private TestInfo testInfo;

    @Nullable
    private MinecraftServer server;

    @BeforeEach
    void init(TestInfo testInfo, MinecraftServer server) {
        this.testInfo = testInfo;
        this.server = server;
    }

    protected MinecraftServer server() {
        return Objects.requireNonNull(server);
    }

    protected RegistryOps<JsonElement> jsonOps() {
        return RegistryOps.create(JsonOps.INSTANCE, server().registryAccess());
    }

    abstract Codec<T> codec();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected String encodeJson(T input) {
        var result = codec().encodeStart(jsonOps(), input);
        Assertions.assertFalse(result.isError(), () -> result.error().get().message());
        return GSON.toJson(result.getOrThrow());
    }

    private String loadSnapshot(String name) {
        var testClass = Optional.ofNullable(testInfo)
                .flatMap(TestInfo::getTestClass)
                .orElseThrow();
        var loader = getClass().getClassLoader();
        var path = String.join(File.separator, "", "__snapshots__", testClass.getSimpleName(), name);

        try (var stream = loader.getResourceAsStream(path)) {
            Assertions.assertNotNull(stream, "file not found: " + path);
            return IOUtils.toString(stream);
        } catch (IOException e) {
            throw new RuntimeException("error reading file: " + path);
        }
    }

    protected T decodeSnapshot(String name) {
        var string = loadSnapshot(name + ".json");
        var json = GSON.fromJson(string, JsonElement.class);
        var result = codec().parse(jsonOps(), json);
        Assertions.assertFalse(result.isError(), () -> result.error().get().message());
        return result.getOrThrow();
    }

    protected void assertMatchesSnapshot(T actual, String name) {
        var encoded = encodeJson(actual);
        var expected = loadSnapshot(name + ".json");
        JSONAssert.assertEquals(expected, encoded, false);
    }

}
