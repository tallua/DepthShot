package com.tallua.depthshot;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;


import org.apache.logging.log4j.Logger;

@Mod(modid = DepthShot.MODID, name = DepthShot.NAME, version = DepthShot.VERSION)
public class DepthShot {
    public static final String MODID = "depthshot";
    public static final String NAME = "Depth Shot";
    public static final String VERSION = "1.0";

    private static Minecraft mc;
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) 
    {
        mc = Minecraft.getMinecraft();
        logger = event.getModLog();

        logger.info("ds : Preinitializaing depthshot");
        DepthShotCore.init(mc, logger);
        
        logger.info("ds : Loading configs");
        DepthShotCore.loadConfigs();

        
        if(DepthShotCore.isClient)
        {
            logger.info("ds : Registering client events");
            DepthShotCore.registerEvents();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("ds : Initializing depthshot");

        
    }

}
