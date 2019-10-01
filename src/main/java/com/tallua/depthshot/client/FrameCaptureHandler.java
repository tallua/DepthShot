package com.tallua.depthshot.client;

import java.nio.ByteBuffer;

import com.tallua.depthshot.DepthShot;
import com.tallua.depthshot.DepthShotCore;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import extendedshaders.api.ShaderEvent;
import extendedshaders.api.ShaderRegistry;
import extendedshaders.api.ShaderSingle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class FrameCaptureHandler
{

    private enum CaptureState
    {
        Idle,
        Cooldown,
        CaptureScreen,
        CaptureDepth,
        MovePos
    };
    private CaptureState captureState = CaptureState.Idle;
    
    private int captureCount = 0;
    private int cooldown = 3;

    private ICaptureSpotGenerator spotGenerator = new CaptureSpotGenerators.SnailSpotGenerator();
    private BufferFileWriter writer = new BufferFileWriter();

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

    public void setSpotGenerator(ICaptureSpotGenerator spotGenerator) 
    {
        this.spotGenerator = spotGenerator;
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

        if(captureState != CaptureState.Idle)
        {
            DepthShotCore.logError("Other capturing process ongoing");
            return;
        }

        captureCount = count;
        spotGenerator.reset();
        captureState = CaptureState.Cooldown;
    }

    public void doCapture()
    {
        doCapture(1);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        // do action on state
        if(captureState == CaptureState.Cooldown)
        {
            cooldown--;
            if(cooldown < 0)
            {
                cooldown = 0;
                captureState = CaptureState.CaptureScreen;
            }
        }
        else if (captureState == CaptureState.CaptureScreen)
        {
            String folderpath = DepthShotCore.config.getSavePath() + "/screenimg/";
            String filename = DepthShotCore.hashCurrentInfo(spotGenerator.getPos(), spotGenerator.getRot());

            DepthShotCore.logInfo("Start capturing screenshot : " + filename);

            int width = DepthShotCore.mc.displayWidth;
            int height = DepthShotCore.mc.displayHeight;

            ByteBuffer buffer = captureScreen(width, height);
            writer.writeScreenshot(width, height, buffer, folderpath, filename);
            
            ShaderRegistry.addShader(depthShader);
            extendedshaders.core.Main.skipSky(3000);
 
            captureState = CaptureState.CaptureDepth;
        }
        else if(captureState == CaptureState.CaptureDepth)
        {
            String depthimg_folderpath = DepthShotCore.config.getSavePath() + "/depthimg/";
            String depthmap_folderpath = DepthShotCore.config.getSavePath() + "/depthmap/";
            String filename = DepthShotCore.hashCurrentInfo(spotGenerator.getPos(), spotGenerator.getRot());

            DepthShotCore.logInfo("Start capturing depthmap : " + filename);

            int width = DepthShotCore.mc.displayWidth;
            int height = DepthShotCore.mc.displayHeight;

            ByteBuffer buffer = captureScreen(width, height);
            writer.writeDepthmap(width, height, buffer, depthimg_folderpath, filename, "PNG");
            writer.writeDepthmap(width, height, buffer, depthmap_folderpath, filename, "DAT");

            ShaderRegistry.removeShader(depthShader);

            
            captureCount--;
            captureState = CaptureState.MovePos;
            if(captureCount == 0)
            {
                captureState = CaptureState.Idle;
            }
        }
        else if(captureState == CaptureState.MovePos)
        {
            Vec3d pos = spotGenerator.getPos();
            Vec2f pitchyaw = spotGenerator.getRot();

            DepthShotCore.logInfo("Player was at : " + pos + ", " + pitchyaw);

            spotGenerator.next();
            pos = spotGenerator.getPos();
            pitchyaw = spotGenerator.getRot();

            DepthShotCore.logInfo("Player moved to : " + pos + ", " + pitchyaw);
            DepthShotCore.mc.player.setLocationAndAngles(pos.x, pos.y, pos.z, pitchyaw.y, pitchyaw.x);

            cooldown = DepthShotCore.config.getCooldown();
            captureState = CaptureState.Cooldown;
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

    private ByteBuffer captureScreen(int width, int height)
    {
        int backup = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        ByteBuffer screen_buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadBuffer(GL11.GL_BACK);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, screen_buffer);
        GL11.glReadBuffer(backup);

        return screen_buffer;
    }
}