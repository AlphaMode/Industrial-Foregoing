package com.buuz135.industrial;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;

public class IndustrialForegoingASM implements Runnable {
    @Override
    public void run() {
        ClassTinkerers.enumBuilder(mapC("class_1814"), "L" + mapC("class_124") + ";")
                .addEnum("pity", new Object[] { ChatFormatting.GREEN })
                .addEnum("simple", new Object[] { ChatFormatting.AQUA })
                .addEnum("advanced", new Object[] { ChatFormatting.LIGHT_PURPLE })
                .addEnum("supreme", new Object[] { ChatFormatting.GOLD })
                .build();
    }

    /**
     * Remap a class name from intermediary to the runtime name
     * @param intermediaryName the intermediary name for the class alone, such as 'class_123'
     * @return the fully qualified remapped name, such as 'net.minecraft.thing.Thing',
     *         or the input with 'net.minecraft.' prepended if not found.
     */
    public static String mapC(String intermediaryName) {
        return FabricLoader.getInstance().getMappingResolver()
                .mapClassName("intermediary", "net.minecraft." + intermediaryName);
    }
}
