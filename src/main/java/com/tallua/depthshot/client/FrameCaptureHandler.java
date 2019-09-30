package com.tallua.depthshot.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.tallua.depthshot.DepthShot;
import com.tallua.depthshot.DepthShotCore;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import extendedshaders.api.ShaderEvent;
import extendedshaders.api.ShaderRegistry;
import extendedshaders.api.ShaderSingle;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class FrameCaptureHandler
{
    private ICaptureSpotGenerator spotGenerator = new  CaptureSpotGenerators.SnailSpotGenerator();

    private enum CaptureState
    {
        Idle,
        CaptureScreen,
        CaptureDepth,
        MovePos
    };
    private CaptureState captureState = CaptureState.Idle;
    
    private enum CaptureMode
    {
        Idle,
        One,
        Many
    };
    private CaptureMode mode = CaptureMode.Idle;
    
    private int capture_remain = 0;
    private static int cooldown_init = 60;
    private int cooldown_remain = 3;
    private boolean need_move = false;

    private Vec3d last_pos;
    private Vec2f last_pitchyaw;

    private ResourceLocation vertUniformLoc = 
        new ResourceLocation(DepthShot.MODID, "shaders/depthmap.vuni");
    private ResourceLocation vertCodeLoc = 
        new ResourceLocation(DepthShot.MODID, "shaders/depthmap.vcode");
    private ResourceLocation fragUniformLoc = 
        new ResourceLocation(DepthShot.MODID, "shaders/depthmap.funi");
    private ResourceLocation fragCodeLoc = 
        new ResourceLocation(DepthShot.MODID, "shaders/depthmap.fcode");
    private ShaderSingle depthShader;

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

    public void doCapture(int count)
    {
        if(!DepthShotCore.isClient)
        {
            DepthShotCore.logError("Failed to capture : non client");
            return;
        }
        if(!DepthShotCore.isOp)
        {
            DepthShotCore.logError("Failed to capture : non op");
            return;
        }

        if(mode != CaptureMode.Idle)
        {
            DepthShotCore.logError("Other capturing process ongoing");
            return;
        }

        if(captureState != CaptureState.Idle)
        {
            DepthShotCore.logError("Other capturing process ongoing");
            return;
        }
        

        if(count == 1)
        {
            mode = CaptureMode.One;
            capture_remain = 1;
        }
        else if(count > 1)
        {
            mode = CaptureMode.Many;
            capture_remain = count;

            spotGenerator.reset();
            last_pos = spotGenerator.getPos();
            last_pitchyaw = spotGenerator.getRot();
        }
        DepthShotCore.logInfo("Will capture in " + mode.toString() + " : " + capture_remain);
    }

    public void doCapture()
    {
        doCapture(1);
    }


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(captureState == CaptureState.MovePos && need_move)
        {
            EntityPlayerSP player = DepthShotCore.mc.player;
            DepthShotCore.logInfo("Player was at : " + player.posX + " " + player.posY + " " + player.posZ);
            spotGenerator.next();
            last_pos = spotGenerator.getPos();
            last_pitchyaw = spotGenerator.getRot();
            DepthShotCore.logInfo("Moving player to : " + last_pos.x + " " + last_pos.y + " " + last_pos.z);
            DepthShotCore.logInfo("Rotating player to : " + last_pitchyaw.x + " " + last_pitchyaw.y);

            need_move = false;
        }

        if(captureState != CaptureState.Idle)
        {
            last_pos = spotGenerator.getPos();
            last_pitchyaw = spotGenerator.getRot();
            DepthShotCore.mc.player.setLocationAndAngles(last_pos.x, last_pos.y, last_pos.z, last_pitchyaw.y, last_pitchyaw.x);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        // do action on state
        if (captureState == CaptureState.CaptureScreen)
        {
            String folderpath = DepthShotCore.config.getSavePath() + "/screenimg/";
            String filepath = DepthShotCore.hashCurrentInfo(last_pos, last_pitchyaw) + ".png";
            DepthShotCore.logInfo("Start capturing screenshot");

            int width = DepthShotCore.mc.displayWidth;
            int height = DepthShotCore.mc.displayHeight;

            boolean success = captureScreenShot(width, height, folderpath + filepath);
            if(!success)
            {
                DepthShotCore.logInfo("Capturing screenshot failed");
            }
        }
        else if(captureState == CaptureState.CaptureDepth)
        {
            String depthimg_folderpath = DepthShotCore.config.getSavePath() + "/depthimg/";
            String depthmap_folderpath = DepthShotCore.config.getSavePath() + "/depthmap/";

            String depthimg_filepath = DepthShotCore.hashCurrentInfo(last_pos, last_pitchyaw) + ".png";
            String depthmap_filepath = DepthShotCore.hashCurrentInfo(last_pos, last_pitchyaw) + ".dat";
            DepthShotCore.logInfo("Start capturing depthmap");

            int width = DepthShotCore.mc.displayWidth;
            int height = DepthShotCore.mc.displayHeight;

            boolean success = captureDepthmap(width, height, depthimg_folderpath + depthimg_filepath, "PNG");
            success = success & captureDepthmap(width, height, depthmap_folderpath + depthmap_filepath, "DAT");
            
            if(!success)
            {
                DepthShotCore.logInfo("Capturing depthmap failed");
            }
        }

        
        // move to next state
        if(cooldown_remain > 0)
            cooldown_remain--;

        if(captureState == CaptureState.Idle && mode != CaptureMode.Idle && cooldown_remain <= 0)
        {
            captureState = CaptureState.CaptureScreen;
        }
        else if(captureState == CaptureState.CaptureScreen)
        {
            if(depthShader != null)
            {
                ShaderRegistry.addShader(depthShader);
            }
            extendedshaders.core.Main.skipSky(3000);
    
            // move to depthmap capture
            captureState = CaptureState.CaptureDepth;
        }
        else if(captureState == CaptureState.CaptureDepth)
        {
            if(depthShader != null)
            {
                ShaderRegistry.removeShader(depthShader);
            }
    
            // move to idle state
            if(mode == CaptureMode.One)
            {
                captureState = CaptureState.Idle;
                mode = CaptureMode.Idle;
                capture_remain = 0;
            }
            else if(mode == CaptureMode.Many)
            {
                captureState = CaptureState.MovePos;
            }
        }
        else if(captureState == CaptureState.MovePos)
        {
            captureState = CaptureState.Idle;
            capture_remain--;
            if(capture_remain <= 0)
            {
                mode = CaptureMode.Idle;
                capture_remain = 0;
            }

            need_move = true;
            cooldown_remain = cooldown_init;
        }
    }

    @SubscribeEvent
    public void onRender(ShaderEvent.RenderSky event)
    {
        // remove sky rendering on depthmap capture
        if(captureState == CaptureState.CaptureDepth)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRender(ShaderEvent.Start event)
    {
        // remove sky color on depthmap capture
        if(captureState == CaptureState.CaptureDepth)
        {
            GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
    }

    ///
    /// Capturing task
    ///

    boolean captureScreenShot(int width, int height, String filepath)
    {
        // create file
        File screen_file = DepthShotCore.createNewFile(filepath);
        if(screen_file == null)
            return false;

        // read buffer
        int backup = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        ByteBuffer screen_buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadBuffer(GL11.GL_BACK);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, screen_buffer);
        GL11.glReadBuffer(backup);
        
        // save to file
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

        try {
            ImageIO.write(screen_image, "PNG", screen_file);
            return true;
        } 
        catch (Exception e) 
        { 
            e.printStackTrace(); 
            return false;
        }
    }

    
    boolean captureDepthmap(int width, int height, String filepath, String format)
    {
        // create file
        File depth_file = DepthShotCore.createNewFile(filepath);
        if(depth_file == null)
            return false;

        // read buffer
        int backup = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        ByteBuffer depth_buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadBuffer(GL11.GL_BACK);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, depth_buffer);
        GL11.glReadBuffer(backup);
        
        // save to file
        switch(format)
        {
            case "PNG":
            case "png":
            {
                BufferedImage screen_image = new BufferedImage(width, height, 
                    BufferedImage.TYPE_INT_RGB);
                for(int x = 0; x < width; x++) 
                {
                    for(int y = 0; y < height; y++)
                    {
                        int i = (x + (width * y)) * 4;
                        int depth_r = depth_buffer.get(i) & 0xFF;
                        int depth_g = depth_buffer.get(i + 1) & 0xFF;
                        int depth_b = depth_buffer.get(i + 2) & 0xFF;
                    
                        float depth = 0.0f;
                        depth += depth_b / 255.0f;
                        depth = depth / 255.0f;
                        depth += depth_g / 255.0f;
                        depth = depth / 255.0f;
                        depth += depth_r / 255.0f;
                        depth = depth * 255.0f;
                    
                        int r = ((int)depth) & 0xFF;
                        int g = ((int)depth) & 0xFF;
                        int b = ((int)depth) & 0xFF;
                    
                        screen_image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                    }
                }
            

                try {
                    ImageIO.write(screen_image, "PNG", depth_file);
                    return true;
                } 
                catch (Exception e) 
                { 
                    e.printStackTrace(); 
                    return false;
                }
            }
            case "CSV":
            case "csv":
            {
                StringBuilder builder = new StringBuilder();
    
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++) 
                    {
                        int i = (x + (width * y)) * 4;
                        int depth_r = depth_buffer.get(i) & 0xFF;
                        int depth_g = depth_buffer.get(i + 1) & 0xFF;
                        int depth_b = depth_buffer.get(i + 2) & 0xFF;
            
                        float depth = 0.0f;
                        depth += depth_b / 255.0f;
                        depth = depth / 255.0f;
                        depth += depth_g / 255.0f;
                        depth = depth / 255.0f;
                        depth += depth_r / 255.0f;
                        depth = depth * 255.0f;
            
                        if(x != 0)
                            builder.append(", ");
                        builder.append(String.format("%5.2f", depth));
                    }
    
                    if(y != height - 1)
                        builder.append(",\n");
                }
    
    
                try (FileOutputStream fs = new FileOutputStream(depth_file))
                {
                    OutputStreamWriter writer = new OutputStreamWriter(fs);
                    writer.write(builder.toString());
                    return true;
                } catch(Exception e)
                {
                    e.printStackTrace(); 
                    return false;
                }
            }
            case "DAT":
            case "dat":
            {
                try (FileOutputStream fs = new FileOutputStream(depth_file))
                {
                    byte[] depth_arr = new byte[width * height * 4];
                    for(int i = 0; i < width * height * 4; i++)
                    {
                        depth_arr[i] = depth_buffer.get(i);
                    }
                    fs.write(depth_arr);
                    return true;
                } 
                catch(Exception e)
                {
                    e.printStackTrace(); 
                    return false;
                }
            }
        }

        return false;
    }
}