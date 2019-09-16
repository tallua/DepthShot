package com.tallua.depthshot;

import com.tallua.depthshot.client.*;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;
import org.apache.logging.log4j.Logger;

public class DepthShotCore {

    // global references
    public static Minecraft mc = null;
    public static Logger logger = null;

    // status
    public static boolean isClient = true;
    public static String seedString = "ASDF";

    // handlers
    public static KeyBinding captureKey;
    public static DepthShotConfigHandler config;
    public static FrameCaptureHandler captureHandler;
    public static FrameCaptureKeyHandler captureKeyHandler;
    
    public static void init(Minecraft mc, Logger logger)
    {
        DepthShotCore.mc = mc;
        DepthShotCore.logger = logger;

        config = new DepthShotConfigHandler("config/" + DepthShot.MODID + ".cfg");
        captureHandler = new FrameCaptureHandler();
        captureKeyHandler = new FrameCaptureKeyHandler();
    }

    public static void loadConfigs() 
    {
        // set default parameters
        isClient = true;
        captureKey = new KeyBinding("depthshot.capture", Keyboard.KEY_B, "key.categories.misc");
    }

    public static void registerEvents()
    {
        if(captureHandler != null)
        {
            MinecraftForge.EVENT_BUS.register(captureHandler);
        }

        if (captureKeyHandler != null && captureKey != null)
        {
            logger.info("ds : Add caputerkey : " + captureKey.getKeyCode());

            ClientRegistry.registerKeyBinding(captureKey);
            MinecraftForge.EVENT_BUS.register(captureKeyHandler);
        }
    }

    

}