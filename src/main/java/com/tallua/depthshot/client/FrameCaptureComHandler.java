package com.tallua.depthshot.client;

import java.util.ArrayList;
import java.util.List;

import com.tallua.depthshot.DepthShotCore;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.IClientCommand;

public class FrameCaptureComHandler implements IClientCommand 
{
    @Override
    public String getName() {
        return "depthshot";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        // TODO Auto-generated method stub
        return "depthshot [iterate count]";
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<String>();
        aliases.add("ds");
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        // TODO Auto-generated method stub
        if(!DepthShotCore.isClient)
        {
            return;
        }
        if(!DepthShotCore.isOp)
        {
            return;
        }

        DepthShotCore.logInfo("Executing command : /depthshot");
        for(int i = 0; i < args.length; ++i)
        {
            DepthShotCore.logInfo("Arg " + i + " : " + args[i]);
        }

        if(args.length != 0)
        {
            try {
                int count = Integer.parseInt(args[0]);
                DepthShotCore.captureHandler.doCapture(count);
            } 
            catch (NumberFormatException e)
            {
                DepthShotCore.captureHandler.doCapture(1);
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        // TODO Auto-generated method stub
        if(DepthShotCore.isClient && DepthShotCore.isOp)
            return true;
        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos targetPos) {
        List<String> what_is_this = new ArrayList<String>();
        return what_is_this;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }


}