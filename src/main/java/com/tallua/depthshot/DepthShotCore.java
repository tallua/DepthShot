package com.tallua.depthshot;

import java.io.File;

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
            logInfo("Add capturekey : " + Keyboard.getKeyName(captureKey.getKeyCode()));

            ClientRegistry.registerKeyBinding(captureKey);
            MinecraftForge.EVENT_BUS.register(captureKeyHandler);
        }
    }


///
/// Helper functions. 
/// If class is too big, create new file that handles helper functions.
///

    public static void logInfo(String message)
    {
        if(logger != null)
            logger.info("[DS] " + message);
    }

    public static void logError(String message)
    {
        if(logger != null)
            logger.error("[DS] " + message);
    }

    public static File CreateNewFile(String filepath)
    {
        File file = new File(filepath);
        if(!file.isFile())
        {
            try{
                file.getParentFile().mkdirs();
                file.createNewFile();
            } 
            catch(Exception e)
            {
                logError("Failed on creating file : " + filepath);
                file = null;
                e.printStackTrace(); 
            } 
        }
        else
        {
            try
            {
                file.delete();
                file.createNewFile();
            }
            catch(Exception e)
            {
                logError("Failed on recreating file : " + filepath);
                file = null;
                e.printStackTrace(); 
            } 
        }
        
        return file;
    }
}