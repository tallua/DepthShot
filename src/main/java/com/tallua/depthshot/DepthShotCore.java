package com.tallua.depthshot;

import java.io.File;

import com.tallua.depthshot.client.FrameCaptureComHandler;
import com.tallua.depthshot.client.FrameCaptureHandler;
import com.tallua.depthshot.client.FrameCaptureKeyHandler;

import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class DepthShotCore {

    // global references
    public static Minecraft mc = null;
    public static Logger logger = null;

    // status
    public static boolean isClient = true;
    public static boolean isOp = true;
    public static String seedString = "ASDF";

    // handlers
    public static KeyBinding captureKey;
    public static DepthShotConfigHandler config;
    public static FrameCaptureHandler captureHandler;
    public static FrameCaptureKeyHandler captureKeyHandler;
    public static FrameCaptureComHandler captureComHandler;
    
    public static void init(Minecraft mc, Logger logger)
    {
        DepthShotCore.mc = mc;
        DepthShotCore.logger = logger;

        config = new DepthShotConfigHandler("config/" + DepthShot.MODID + ".cfg");
        captureHandler = new FrameCaptureHandler();
        captureKeyHandler = new FrameCaptureKeyHandler();
        captureComHandler = new FrameCaptureComHandler();
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
            logInfo("Capture Event Handler");
            MinecraftForge.EVENT_BUS.register(captureHandler);
        }

        if (captureKeyHandler != null && captureKey != null)
        {
            logInfo("Capture Key Event Handler");

            logInfo("Add capturekey : " + Keyboard.getKeyName(captureKey.getKeyCode()));

            ClientRegistry.registerKeyBinding(captureKey);
            MinecraftForge.EVENT_BUS.register(captureKeyHandler);
        }

        if(captureComHandler != null)
        {
            logInfo("Capture Command Event Handler");

            ClientCommandHandler.instance.registerCommand(captureComHandler);
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

    public static String GenCurrentFilePrefix()
    {
        String prefix = "p";

        prefix += (int)(mc.player.posX) + "_";
        prefix += (int)(mc.player.posY) + "_";
        prefix += (int)(mc.player.posZ) + "_";


        return prefix;
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