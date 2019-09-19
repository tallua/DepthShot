package com.tallua.depthshot;

import java.io.File;

import io.netty.util.internal.UnstableApi;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

// on develop
@UnstableApi
public class DepthShotConfigHandler
{
    Configuration config;

    DepthShotConfigHandler(String path)
    {
        File filepath = new File(path);

        config = new Configuration(filepath);
        try
        {
            config.load();
            


        }
        catch(Exception e)
        {
            DepthShotCore.logError("Error loading config file");
        }
    }

    public String getString(String key, String def)
    {
        Property prop = config.get("DepthShot", key, def);
        if(config.hasChanged())
            config.save();

        return prop.getString();
    }

    



    public String getSavePath()
    {
        return getString("save_path", "C:/Data/temp");
    }
}





