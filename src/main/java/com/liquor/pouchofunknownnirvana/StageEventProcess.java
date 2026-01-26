package com.liquor.pouchofunknownnirvana;

import com.alessandro.astages.api.event.player.StageAddedPlayerEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import java.util.UUID;

import static com.liquor.pouchofunknownnirvana.PouchOfUnknownNirvana.*;

public class StageEventProcess {
    @SubscribeEvent
    public static void onStageAdd(StageAddedPlayerEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUUID();
        MinecraftServer server = player.getServer();
        PouchOfUnknownNirvana.LOGGER.debug("RunRunRunRunRunRun");
        String unlockStage = event.stage;

        CompoundTag pouchContents = pouchContentsAll.get(uuid);
        CompoundTag canTakeOutList = (CompoundTag) pouchContents.get(unlockStage);
        pouchContents.remove(unlockStage);
        if (canTakeOutList != null) {
            pouchContents.put("canTakeOut", canTakeOutList);
        }
        canTakeOutListAll.put(uuid, canTakeOutList);
        DataOperater.fileWriter(server, player, pouchContents);
        pouchContents.remove("canTakeOut");
    }

    public static void depositToPouch(Player player, ItemStack itemStack, String stage) {
        UUID uuid = player.getUUID();
        String itemName = itemStack.getItem().toString();
        LOGGER.debug(itemName);

        CompoundTag pouchContents = pouchContentsAll.get(uuid);

        int stackSize = itemStack.getCount();
        HolderLookup.Provider provider = player.level().registryAccess();
        CompoundTag itemNbt = (CompoundTag) itemStack.save(provider);
        LOGGER.debug(itemNbt.toString());
        if (!itemNbt.contains("count")) {
            itemNbt.putInt("count", 1);
        }
        LOGGER.debug(itemNbt.toString());
        CompoundTag itemNbtWithoutCount = itemNbt.copy();
        itemNbtWithoutCount.remove("count");
        LOGGER.debug(itemNbtWithoutCount.toString());
        // CompoundTag itemNbtWithoutCount = (CompoundTag) DataComponentMap.CODEC.encodeStart(NbtOps.INSTANCE, itemStack.getComponents()).getOrThrow();

        if (pouchContents.contains(stage)) {
            CompoundTag tagT1 = (CompoundTag) pouchContents.get(stage);
            if (tagT1.contains(itemName)) {
                CompoundTag tagT2 = (CompoundTag) tagT1.get(itemName);
                if (tagT2.contains(itemNbtWithoutCount.toString())) {
                    CompoundTag tagT3 = (CompoundTag) tagT2.get(itemNbtWithoutCount.toString());
                    LOGGER.debug(tagT3.toString());
                    stackSize += tagT3.getInt("count");
                    LOGGER.debug(String.valueOf(stackSize));
                    tagT3.remove("count");
                    LOGGER.debug(tagT3.toString());
                    IntTag tempIntTag = IntTag.valueOf(stackSize);
                    tagT3.put("count", tempIntTag);
                    LOGGER.debug(tagT3.toString());
                    tagT2.remove(itemNbtWithoutCount.toString());
                    tagT2.put(itemNbtWithoutCount.toString(), tagT3);
                    tagT1.remove(itemName);
                    tagT1.put(itemName, tagT2);
                    pouchContents.remove(stage);
                    pouchContents.put(stage, tagT1);
                } else {
                    LOGGER.debug(itemNbt.toString());
                    tagT2.put(itemNbtWithoutCount.toString(), itemNbt);
                    LOGGER.debug(tagT2.toString());
                    tagT1.remove(itemName);
                    tagT1.put(itemName, tagT2);
                    LOGGER.debug(tagT1.toString());
                    pouchContents.remove(stage);
                    pouchContents.put(stage, tagT1);
                }

            } else {
                CompoundTag tempTag2 = new CompoundTag();
                tempTag2.put(itemNbtWithoutCount.toString(), itemNbt);
                tagT1.put(itemName, tempTag2);
                pouchContents.remove(stage);
                pouchContents.put(stage, tagT1);
            }
        } else {
            CompoundTag tempTag2 = new CompoundTag();
            LOGGER.debug(itemNbt.toString());
            tempTag2.put(itemNbtWithoutCount.toString(), itemNbt);
            CompoundTag tempTag1 = new CompoundTag();
            tempTag1.put(itemName, tempTag2);
            pouchContents.put(stage, tempTag1);
        }
        pouchContentsAll.put(uuid, pouchContents);
        LOGGER.debug(pouchContentsAll.toString());
    }
}
