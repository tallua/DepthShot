package com.tallua.depthshot.client;

import com.tallua.depthshot.DepthShotCore;

import net.minecraft.client.shader.Framebuffer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.*;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
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
        DepthShotCore.logger.info("ds : Start capturing at :");

        // get framebuffer
        Framebuffer frame = DepthShotCore.mc.getFramebuffer();
        int frameBufferId = frame.framebufferObject;

        int width = Display.getDisplayMode().getWidth();
        int height = Display.getDisplayMode().getHeight();
        captureFront(width, height);

        // get depthbuffer
        int depthBufferId = frame.depthBuffer;
        captureDepth(width, height);

        // save to file
        // [mandatory] need path, seed, player loc, player angle
        // [optional] need realtime, gametime, biome, dimension

    }

    void captureFront(int width, int height)
    {
        DepthShotCore.logger.info("ds : Start capturing front");
        GL11.glReadBuffer(GL11.GL_BACK);
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        
        File file = new File(DepthShotCore.path + "/test.png");
        try{
            if(!file.createNewFile())
            {
                file.delete();
                file.createNewFile();
            }
        } catch(Exception e) { }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < width; x++) 
        {
            for(int y = 0; y < height; y++)
            {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, "PNG", file);
            DepthShotCore.logger.info("ds : Capture success");
        } 
        catch (IOException e) 
        { 
            DepthShotCore.logger.info("ds : Capture failed");
            e.printStackTrace(); 
        }
    }

    void captureDepth(int width, int height)
    {
        StringBuilder sb = new StringBuilder();
        DepthShotCore.logger.info("ds : Start capturing depth");

        GL11.glReadBuffer(GL11.GL_BACK);
        IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, buffer);
        
        File file = new File(DepthShotCore.path + "/test_depth.png");
        try{
            if(!file.createNewFile())
            {
                file.delete();
                file.createNewFile();
            }
        } catch(Exception e) 
        { 
            DepthShotCore.logger.info("ds : Capture failed : cannot create file");
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < width; x++) 
        {
            for(int y = 0; y < height; y++)
            {
                int index = (x + (width * y));
                int val = buffer.get(index);
                val = val * 0xFF;
                int val_int = (int)val;
                

                int r = val_int & 0xFF;
                int g = val_int & 0xFF;
                int b = val_int & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);

                if(x == 0)
                {
                    sb.append(val);
                    sb.append(" ");
                }
            }
        }

        DepthShotCore.logger.info(sb.toString());

        try {
            ImageIO.write(image, "PNG", file);
            DepthShotCore.logger.info("ds : Capture success");
        } 
        catch (IOException e) 
        { 
            DepthShotCore.logger.info("ds : Capture failed : ioexception");
            e.printStackTrace(); 
        }
    }

}









