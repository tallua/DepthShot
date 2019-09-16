package com.tallua.depthshot.client;

import com.tallua.depthshot.DepthShot;
import com.tallua.depthshot.DepthShotCore;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;

import extendedshaders.api.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.*;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class FrameCaptureHandler
{
    private enum CaptureState
    {
        Idle,
        CaptureScreen,
        CaptureDepth
    };
    private CaptureState captureState = CaptureState.Idle;
    

    private ResourceLocation vertUniformLoc = 
        new ResourceLocation(DepthShot.MODID, "shaders/depthmap.vuni");
    private ResourceLocation vertCodeLoc = 
        new ResourceLocation(DepthShot.MODID, "shaders/depthmap.vcode");
    private ResourceLocation fragUniformLoc = 
        new ResourceLocation(DepthShot.MODID, "shaders/depthmap.funi");
    private ResourceLocation fragCodeLoc = 
        new ResourceLocation(DepthShot.MODID, "shaders/depthmap.fcode");
    private ShaderSingle depthShader;
    private PostProcessor tmpPostProcessor;

    public FrameCaptureHandler()
    {
        depthShader = new ShaderSingle(
            vertUniformLoc,
            vertCodeLoc,
            fragUniformLoc,
            fragCodeLoc,
            2
        );
    }


    public void doCapture()
    {
        captureState = CaptureState.CaptureScreen;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        if (captureState == CaptureState.CaptureScreen)
        {
            DepthShotCore.logger.info("ds : Start capturing screen...");

            int width = DepthShotCore.mc.displayWidth;
            int height = DepthShotCore.mc.displayHeight;

            capture(width, height, DepthShotCore.config.getSavePath() + "/tmp.png");
            if(depthShader != null)
            {
                ShaderRegistry.addShader(depthShader);
                extendedshaders.core.Main.skipSky(3000);
                captureState = CaptureState.CaptureDepth;
            }
            else
            {
                captureState = CaptureState.Idle;
            }

        }
        else if(captureState == CaptureState.CaptureDepth)
        {
            DepthShotCore.logger.info("ds : Start capturing depth...");

            int width = DepthShotCore.mc.displayWidth;
            int height = DepthShotCore.mc.displayHeight;

            capture(width, height, DepthShotCore.config.getSavePath() + "/tmp_depth.png");

            ShaderRegistry.removeShader(depthShader);
            captureState = CaptureState.Idle;
        }
    }

    @SubscribeEvent
    public void onRender(ShaderEvent.RenderSky event)
    {
        if(captureState == CaptureState.CaptureDepth)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRender(ShaderEvent.Start event)
    {
        if(captureState == CaptureState.CaptureDepth)
        {
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
    }

    void capture(int width, int height, String filepath)
    {
        // game
        File screen_file = new File(filepath);
        if(!screen_file.isFile())
        {
            try{
                screen_file.getParentFile().mkdirs();
                screen_file.createNewFile();
            } 
            catch(Exception e)
            {
                DepthShotCore.logger.error("failed on create file");
                screen_file = null;
                e.printStackTrace(); 
            } 
        }
        else
        {
            try
            {
                screen_file.delete();
                screen_file.createNewFile();
            }
            catch(Exception e)
            {
                DepthShotCore.logger.error("failed on create file");
                screen_file = null;
                e.printStackTrace(); 
            } 
        }

        int backup = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        
        // read buffer
        ByteBuffer screen_buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadBuffer(GL11.GL_BACK);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, 
            GL11.GL_UNSIGNED_BYTE, screen_buffer);
        
        BufferedImage screen_image = new BufferedImage(width, height, 
            BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < width; x++) 
        {
            for(int y = 0; y < height; y++)
            {
                int i = (x + (width * y)) * 4;
                int r = screen_buffer.get(i) & 0xFF;
                int g = screen_buffer.get(i + 1) & 0xFF;
                int b = screen_buffer.get(i + 2) & 0xFF;
                screen_image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        if(screen_file != null)
        {
            try {
                ImageIO.write(screen_image, "PNG", screen_file);
                DepthShotCore.logger.info("ds : capture success");
            } 
            catch (Exception e) 
            { 
                DepthShotCore.logger.info("ds : capture failed");
                e.printStackTrace(); 
            }
        }

        GL11.glReadBuffer(backup);
    }
}