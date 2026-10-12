package eu.ariakan.stonegolem;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {

    public static final String MOD_ID = "stonegolem";
    public static final String MOD_NAME = "Stone Golem";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Constants() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
