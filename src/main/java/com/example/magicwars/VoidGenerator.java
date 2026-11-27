package com.example.magicwars;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

/** مولّد عالم فراغ للملاعب العائمة */
public class VoidGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        return createChunkData(world);   // فراغ كامل
    }
}
