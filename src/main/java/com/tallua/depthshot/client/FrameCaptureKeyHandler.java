package com.tallua.depthshot.client;

import com.tallua.depthshot.DepthShotCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;


public class FrameCaptureKeyHandler
{

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event)
    {
        if (DepthShotCore.capturekey.isPressed())
        {
            DepthShotCore.logger.info("capture key pressed");
        }
    }
    
}