package com.tallua.depthshot.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GLContext;



class FrameCapturer
{


    public void captureAt(Vector position, Quaternion angle)
    {
        // set player transform

        capture();
    }

    
    public void capture()
    {
        
        // get framebuffer
        Framebuffer fb = Minecraft.getMinecraft().getFramebuffer();
        int frameBufferId = fb.framebufferObject;

        // get depthbuffer
        int depthBufferId = fb.depthBuffer;

        // save to file
        // [mandatory] need path, seed, player loc, player angle
        // [optional] need realtime, gametime, biome, dimension

    }

}









