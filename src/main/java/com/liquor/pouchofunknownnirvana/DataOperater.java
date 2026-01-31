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

    private static final String DATA_FOLDER = "pouch";
    private static final String DATA_FILE_NAME = "contents.dat";

    public static CompoundTag fileReader(MinecraftServer server, Player player) {
        Path worldSaveRootPath = server.getWorldPath(LevelResource.ROOT);
        UUID uuid = player.getUUID();

        Path uuidFolderPath = worldSaveRootPath
                .resolve(DATA_FOLDER)
                .resolve(uuid.toString());

        Path dataFilePath = uuidFolderPath.resolve(DATA_FILE_NAME);

        try {
            Files.createDirectories(uuidFolderPath);

            if (!Files.exists(dataFilePath)) {
                return new CompoundTag();
            }

            try (InputStream inputStream = Files.newInputStream(dataFilePath)) {
                return NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
            }

        } catch (IOException e) {
            PouchOfUnknownNirvana.LOGGER.error("Failed to read data file: {}", dataFilePath, e);
            return new CompoundTag();
        }
    }

    public static void fileWriter(MinecraftServer server, Player player, CompoundTag compoundTag) {
        Path worldSaveRootPath = server.getWorldPath(LevelResource.ROOT);
        UUID uuid = player.getUUID();

        Path uuidFolderPath = worldSaveRootPath
                .resolve(DATA_FOLDER)
                .resolve(uuid.toString());

        Path dataFilePath = uuidFolderPath.resolve(DATA_FILE_NAME);

        try {
            Files.createDirectories(uuidFolderPath);

            try (OutputStream outputStream = Files.newOutputStream(dataFilePath)) {
                NbtIo.writeCompressed(compoundTag, outputStream);
            }

            PouchOfUnknownNirvana.LOGGER.debug("{} was saved at: {}", compoundTag, dataFilePath);

        } catch (IOException e) {
            PouchOfUnknownNirvana.LOGGER.error("Failed to create data file!", e);
        }
    }
}