package com.tallua.depthshot.client;

import java.util.Random;

import com.tallua.depthshot.DepthShotCore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class CaptureSpotGenerators {
    public static class XMoveSpotGenerator implements ICaptureSpotGenerator {
        double initX = 0;
        double initY = 0;
        double initZ = 0;

        int count = 0;

        @Override
        public void reset() {
            initX = Minecraft.getMinecraft().player.posX;
            initY = Minecraft.getMinecraft().player.posY;
            initZ = Minecraft.getMinecraft().player.posZ;

            count = 0;
        }

        @Override
        public void next() {
            Vec3d result = new Vec3d(initX + (16 * count++), initY, initZ);
            IBlockState block = DepthShotCore.mc.world.getBlockState(new BlockPos(result));
            DepthShotCore.logInfo("Block at " + result + " : " + (block.isBlockNormalCube()));
            if (block.isBlockNormalCube())
                next();
        }

        @Override
        public Vec3d getPos() {
            return new Vec3d(initX, initY, initZ);
        }

        @Override
        public Vec2f getRot() {
            return DepthShotCore.mc.player.getPitchYaw();
        }

    }


    public static class SnailSpotGenerator implements ICaptureSpotGenerator
    {
        Random rand = new Random();

        int lastX = 0;
        int lastY = 0;
        int lastZ = 0;
        int lastPitch = 0;
        int lastYaw = 0;

        int dir = 0;
        int dir_count = 0;
        int dir_count_max = 0;

        @Override
        public void reset()
        {
            lastX = (int)DepthShotCore.mc.player.posX;
            lastY = (int)DepthShotCore.mc.player.posY;
            lastZ = (int)DepthShotCore.mc.player.posZ;
            lastPitch = (int)DepthShotCore.mc.player.getPitchYaw().x;
            lastYaw = (int)DepthShotCore.mc.player.getPitchYaw().y;
            
            dir = 0;
            dir_count = 0;
            dir_count_max = 1;
        }

        @Override
        public void next() 
        {
            dir_count++;
            if(dir_count_max <= dir_count)
            {
                dir++;
                if(dir > 3)
                    dir = 0;
                dir_count = 0;
                if(dir == 0 || dir == 2)
                {
                    dir_count_max++;
                }
            }

            Vec3i result = new Vec3i(0, 0, 0);
            switch(dir)
            {
            case 0:
                result = new Vec3i(lastX + 16, lastY, lastZ);
                break;
            case 1:
                result = new Vec3i(lastX, lastY, lastZ + 16);
                break;
            case 2:
                result = new Vec3i(lastX - 16, lastY, lastZ);
                break;
            case 3:
                result = new Vec3i(lastX, lastY, lastZ - 16);
                break;

            default:
                return;
            }

            lastX = result.getX();
            lastY = result.getY();
            lastZ = result.getZ();
            lastPitch = rand.nextInt(115) - 35;
            lastYaw = rand.nextInt(360);

            IBlockState block = DepthShotCore.mc.world.getBlockState(new BlockPos(result));
            DepthShotCore.logInfo("Block at " + result + " : " + (block.isBlockNormalCube()));
            if(block.isBlockNormalCube())
                next();
        }

        
        @Override
        public Vec3d getPos() {
            return new Vec3d(lastX, lastY, lastZ);
        }

        @Override
        public Vec2f getRot() {

            return new Vec2f(lastPitch, lastYaw);
        }
        
    }
}