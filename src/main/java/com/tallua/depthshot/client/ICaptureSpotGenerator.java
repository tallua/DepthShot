package com.tallua.depthshot.client;

import net.minecraft.util.math.Vec3d;

interface ICaptureSpotGenerator
{
    public Vec3d next();
}