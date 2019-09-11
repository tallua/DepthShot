package com.tallua.depthshot.client;

import net.minecraft.client.shader.Framebuffer;

import com.tallua.depthshot.DepthShotCore;

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
        Framebuffer frame = DepthShotCore.mc.getFramebuffer();
        int frameBufferId = frame.framebufferObject;
        

        // get depthbuffer
        int depthBufferId = frame.depthBuffer;

        // save to file
        // [mandatory] need path, seed, player loc, player angle
        // [optional] need realtime, gametime, biome, dimension

    }

}









