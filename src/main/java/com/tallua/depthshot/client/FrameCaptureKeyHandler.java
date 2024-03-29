package com.tallua.depthshot.client;

import com.tallua.depthshot.DepthShotCore;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;


public class FrameCaptureKeyHandler
{

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event)
    {
        if (DepthShotCore.captureKey.isPressed())
        {
            DepthShotCore.logger.info("capture key pressed");
            if(DepthShotCore.captureHandler != null)
            {
                DepthShotCore.captureHandler.doCapture();
            }
        }
    }
    
}