package com.liquor.pouchofunknownnirvana;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class DataOperater {
    private static final String DATA_FOLDER = "pouch/";
    private static final String DATA_FILE_NAME = "contents.dat";

    public static CompoundTag fileReader(MinecraftServer server, Player player) {
        Path worldSaveRootPath = server.getWorldPath(LevelResource.ROOT);
        UUID uuid = player.getUUID();
        final String UUID_FOLDER = uuid.toString() + "/";
        Path dataFolderPath = worldSaveRootPath.resolve(DATA_FOLDER);
        Path uuidFolderPath = dataFolderPath.resolve(UUID_FOLDER);
        Path dataFilePath = uuidFolderPath.resolve(DATA_FILE_NAME);
        try {
            // 检查文件是否已存在，不存在则创建
            if (!Files.exists(dataFilePath)) {
                // 日志提示
                PouchOfUnknownNirvana.LOGGER.info("Created initial data file at: " + dataFilePath);
                return new CompoundTag();
            } else {
                try (InputStream inputStream = Files.newInputStream(dataFilePath)) {
                    return NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
                }
            }
        } catch (IOException e) {
            PouchOfUnknownNirvana.LOGGER.error("Failed to create data file!", e);
        }
        return new CompoundTag();
    }

    public static void fileWriter(MinecraftServer server, Player player, CompoundTag compoundTag) {
        Path worldSaveRootPath = server.getWorldPath(LevelResource.ROOT);
        UUID uuid = player.getUUID();
        String UUID_FOLDER = uuid.toString() + "/";
        Path dataFolderPath = worldSaveRootPath.resolve(DATA_FOLDER);
        Path uuidFolderPath = dataFolderPath.resolve(UUID_FOLDER);
        Path dataFilePath = uuidFolderPath.resolve(DATA_FILE_NAME);
        try {
            // 检查文件是否已存在，不存在则创建
            if (Files.exists(dataFilePath)) {
                // 删除原文件
                Files.delete(dataFilePath);
                PouchOfUnknownNirvana.LOGGER.info("Created initial data file at: " + dataFilePath);
            }
            try (OutputStream outputStream = Files.newOutputStream(dataFilePath)) {
                NbtIo.writeCompressed(compoundTag, outputStream);
                PouchOfUnknownNirvana.LOGGER.debug(compoundTag + "was saved at: " + dataFilePath);
            }
        } catch (IOException e) {
            PouchOfUnknownNirvana.LOGGER.error("Failed to create data file!", e);
        }
    }

}
