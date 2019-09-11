package com.tallua.depthshot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class DepthShotCore {

    // references
    public static Minecraft mc = null;
    public static Logger logger = null;

    // status
    public static boolean isClient = true;
    public static String seedString = "ASDF";
    
    // config
    public static String path;
    public static KeyBinding capturekey;
    
    public static void init(Minecraft mc, Logger logger)
    {
        DepthShotCore.mc = mc;
        DepthShotCore.logger = logger;
    }

    public static void loadConfigs() {
        isClient = true;
        path = "./";
        capturekey = new KeyBinding("depthshot.capture", Keyboard.KEY_B, "key.categories.misc");
    }

    

}