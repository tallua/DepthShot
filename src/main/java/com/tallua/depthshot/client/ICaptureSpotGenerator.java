package com.tallua.depthshot.client;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

interface ICaptureSpotGenerator
{
    public void reset();
    public void next();
    public Vec3d getPos();
    public Vec2f getRot();
}