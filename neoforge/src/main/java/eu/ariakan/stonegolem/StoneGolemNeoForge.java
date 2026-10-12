package eu.ariakan.stonegolem;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class StoneGolemNeoForge {

    public StoneGolemNeoForge(IEventBus modBus) {
        StoneGolemMod.init();
    }
}
