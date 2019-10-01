package com.tallua.depthshot.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.tallua.depthshot.DepthShotCore;


class BufferFileWriter
{
    public boolean writeScreenshot(int width, int height, ByteBuffer buffer, String filepath, String filename)
    {
        // create file
        File file = createNewFile(filepath + filename + ".png");
        if(file == null)
            return false;

        // build image buffer
        BufferedImage screen_image = new BufferedImage(width, height, 
            BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < width; x++) 
        {
            for(int y = 0; y < height; y++)
            {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                screen_image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        // save to file
        try {
            ImageIO.write(screen_image, "PNG", file);
            return true;
        } 
        catch (Exception e) 
        { 
            e.printStackTrace(); 
            return false;
        }
    }

    public boolean writeDepthmap(int width, int height, ByteBuffer buffer, String filepath, String filename, String format)
    {
        switch(format)
        {
            case "PNG":
            case "png":
                return writeDepthmapImage(width, height, buffer, filepath, filename);
            case "CSV":
            case "csv":
                return writeDepthmapCSV(width, height, buffer, filepath, filename);
            case "DAT":
            case "dat":
                return writeDepthmapData(width, height, buffer, filepath, filename);
        }

        return false;
    }

    public boolean writeDepthmapImage(int width, int height, ByteBuffer buffer, String filepath, String filename)
    {
        // create file
        File file = createNewFile(filepath + filename + ".png");
        if(file == null)
            return false;

        // build image buffer
        BufferedImage screen_image = new BufferedImage(width, height, 
            BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < width; x++) 
        {
            for(int y = 0; y < height; y++)
            {
                int i = (x + (width * y)) * 4;
                int depth_r = buffer.get(i) & 0xFF;
                int depth_g = buffer.get(i + 1) & 0xFF;
                int depth_b = buffer.get(i + 2) & 0xFF;
            
                float depth = 0.0f;
                depth += depth_b / 255.0f;
                depth = depth / 255.0f;
                depth += depth_g / 255.0f;
                depth = depth / 255.0f;
                depth += depth_r / 255.0f;
                depth = depth * 255.0f;
                if(depth > 255)
                    depth = 0;
            
                int r = ((int)depth) & 0xFF;
                int g = ((int)depth) & 0xFF;
                int b = ((int)depth) & 0xFF;
            
                screen_image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        // save to file
        try {
            ImageIO.write(screen_image, "PNG", file);
            return true;
        } 
        catch (Exception e) 
        { 
            e.printStackTrace(); 
            return false;
        }
    }

    public boolean writeDepthmapData(int width, int height, ByteBuffer buffer, String filepath, String filename)
    {
        // create file
        File file = createNewFile(filepath + filename + ".dat");
        if(file == null)
            return false;

        // save to file
        try (FileOutputStream fs = new FileOutputStream(file))
        {
            byte[] bytes = new byte[4];
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++) 
                {
                    // process buffer
                    int i = (x + (width * y)) * 4;
                    int depth_r = buffer.get(i) & 0xFF;
                    int depth_g = buffer.get(i + 1) & 0xFF;
                    int depth_b = buffer.get(i + 2) & 0xFF;
        
                    float depth = 0.0f;
                    depth += depth_b / 255.0f;
                    depth = depth / 255.0f;
                    depth += depth_g / 255.0f;
                    depth = depth / 255.0f;
                    depth += depth_r / 255.0f;
                    depth = depth * 255.0f;
                    if(depth > 255)
                        depth = 0;
        
                    fs.write(ByteBuffer.wrap(bytes).putFloat(depth).array());
                }
            }
            return true;
        } 
        catch(Exception e)
        {
            e.printStackTrace(); 
            return false;
        }
    }

    public boolean writeDepthmapCSV(int width, int height, ByteBuffer buffer, String filepath, String filename)
    {
        // create file
        File file = createNewFile(filepath + filename + ".csv");
        if(file == null)
            return false;
        
        // build csv string
        StringBuilder builder = new StringBuilder();
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++) 
            {
                int i = (x + (width * y)) * 4;
                int depth_r = buffer.get(i) & 0xFF;
                int depth_g = buffer.get(i + 1) & 0xFF;
                int depth_b = buffer.get(i + 2) & 0xFF;
    
                float depth = 0.0f;
                depth += depth_b / 255.0f;
                depth = depth / 255.0f;
                depth += depth_g / 255.0f;
                depth = depth / 255.0f;
                depth += depth_r / 255.0f;
                depth = depth * 255.0f;
                if(depth > 255)
                    depth = 0;
    
                if(x != 0)
                    builder.append(", ");
                builder.append(String.format("%5.2f", depth));
            }

            if(y != height - 1)
                builder.append(",\n");
        }

        // save to file
        try (FileOutputStream fs = new FileOutputStream(file))
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


    private File createNewFile(String filepath)
    {
        File file = new File(filepath);
        if(!file.isFile())
        {
            try{
                file.getParentFile().mkdirs();
                file.createNewFile();
            } 
            catch(Exception e)
            {
                DepthShotCore.logError("Failed on creating file : " + filepath);
                file = null;
                e.printStackTrace(); 
            } 
        }
        else
        {
            try
            {
                file.delete();
                file.createNewFile();
            }
            catch(Exception e)
            {
                DepthShotCore.logError("Failed on recreating file : " + filepath);
                file = null;
                e.printStackTrace(); 
            } 
        }
        
        return file;
    }

}