package timeescape.ui;

import java.io.*;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import timeescape.system.MenuImageAssets;

/** Build-only generator. Always regenerated so changed source/art cannot leave stale data. */
public final class GenerateMenuWeather {
    public static void main(String[] args) throws Exception {
        Path assets=Path.of(args[0]);
        var atmosphere=new LegacyMenuAtmosphere(new MenuImageAssets());
        var rainField=LegacyMenuAtmosphere.class.getDeclaredField("textureRain");
        rainField.setAccessible(true);
        var rain=(LegacyTextureRain)rainField.get(atmosphere);
        var cycleField=LegacyTextureRain.class.getDeclaredField("cycleSeconds");
        cycleField.setAccessible(true);
        Path data=assets.resolve("effects/menu_rain.bin.gz");
        Files.createDirectories(data.getParent());
        try(var out=new DataOutputStream(new BufferedOutputStream(new java.util.zip.GZIPOutputStream(Files.newOutputStream(data))))){
            out.writeInt(0x54455231);out.writeInt(1);out.writeInt(rain.W);out.writeInt(rain.H);
            for(int value:rain.source)out.writeInt(value);
            for(float value:rain.detail)out.writeFloat(value);
            for(float value:rain.weight)out.writeFloat(value);
            out.writeDouble(cycleField.getDouble(rain));out.writeInt(rain.events.size());
            for(double[] event:rain.events)for(double value:event)out.writeDouble(value);
        }
        var glowField=LegacyMenuAtmosphere.class.getDeclaredField("stormGlow");glowField.setAccessible(true);
        if(!ImageIO.write((BufferedImage)glowField.get(atmosphere),"png",assets.resolve("images/menu/menu_storm_glow.png").toFile()))
            throw new IOException("PNG writer unavailable");
        // Exact round-trip verification before packaging.
        var restored=new TextureRain();
        if(!java.util.Arrays.equals(rain.source,restored.source)
            ||!java.util.Arrays.equals(rain.detail,restored.detail)
            ||!java.util.Arrays.equals(rain.weight,restored.weight)
            ||rain.events.size()!=restored.events.size())throw new AssertionError("Rain round-trip mismatch");
        for(int i=0;i<rain.events.size();i++)if(!java.util.Arrays.equals(rain.events.get(i),restored.events.get(i)))
            throw new AssertionError("Event mismatch");
        System.out.println("Generated and verified precomputed rain and storm assets.");
    }
}
