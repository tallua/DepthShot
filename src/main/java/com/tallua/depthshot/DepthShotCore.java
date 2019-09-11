package com.tallua.depthshot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;

import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import com.tallua.depthshot.client.*;

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