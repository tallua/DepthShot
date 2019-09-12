package com.tallua.depthshot;

import com.tallua.depthshot.client.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import org.lwjgl.opengl.GL11;

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

    // handlers
    public static FrameCaptureHandler captureHandler;
    public static FrameCaptureKeyHandler captureKeyHandler;
    
    public static void init(Minecraft mc, Logger logger)
    {
        DepthShotCore.mc = mc;
        DepthShotCore.logger = logger;

        captureHandler = new FrameCaptureHandler();
        captureKeyHandler = new FrameCaptureKeyHandler();
    }

    public static void loadConfigs() 
    {
        isClient = true;
        path = "D:/Temp/";
        capturekey = new KeyBinding("depthshot.capture", Keyboard.KEY_B, 
            "key.categories.misc");
    }

    public static void registerEvents()
    {
        if(captureHandler != null)
        {
            MinecraftForge.EVENT_BUS.register(captureHandler);
        }

        if (captureKeyHandler != null && DepthShotCore.capturekey != null)
        {
            logger.info("ds : Add caputerkey : " + capturekey.getKeyCode());

            ClientRegistry.registerKeyBinding(capturekey);
            MinecraftForge.EVENT_BUS.register(captureKeyHandler);
        }
    }

    

}