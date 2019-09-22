package com.tallua.depthshot.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;

class XMoveSpotGenerator implements ICaptureSpotGenerator
{
    public Vec3d next()
    {
        double posX = Minecraft.getMinecraft().player.posX;
        double posY = Minecraft.getMinecraft().player.posY;
        double posZ = Minecraft.getMinecraft().player.posZ;

        return new Vec3d(posX + 16, posY, posZ);
    }

}