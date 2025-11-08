package com.liquor.pouchofunknownnirvana;

import com.alessandro.astages.event.custom.actions.StageAddedPlayerEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

import static com.liquor.pouchofunknownnirvana.PouchOfUnknownNirvana.*;

public class StageEventProcess {
    @SubscribeEvent
    public static void onStageAdd(StageAddedPlayerEvent event) {
        Player player = event.getEntity();
        LOGGER.debug("RunRunRunRunRunRun");
        String unlockStage = event.stage;
        CompoundTag tempTag = (CompoundTag) pouchContents.get(unlockStage);
        canTakeOutList = tempTag;
        pouchContents.remove(unlockStage);
        pouchContents.put("canTakeOut", canTakeOutList);
        DataOperater.fileWriter(player.getServer(), player, pouchContents);
        pouchContents.remove("canTakeOut");
    }

    public static void depositToPouch(Player player, ItemStack itemStack, String stage) {
        MinecraftServer server = player.getServer();
        String itemName = itemStack.getItem().toString();
        int stackSize = itemStack.getCount();

        if (pouchContents.contains(stage)) {
            CompoundTag tagT1 = (CompoundTag) pouchContents.get(stage);
            if (tagT1.contains(itemName)) {
                CompoundTag tagT2 = (CompoundTag) tagT1.get(itemName);
                stackSize += tagT2.getInt("amount");
                tagT2.remove("amount");
                IntTag tempIntTag = IntTag.valueOf(stackSize);
                CompoundTag tempTag2 = new CompoundTag();
                tagT2.put("amount", tempIntTag);
                tagT1.remove(itemName);
                tagT1.put(itemName, tagT2);
                pouchContents.remove(stage);
                pouchContents.put(stage, tagT1);

            } else {
                IntTag tempIntTag = IntTag.valueOf(stackSize);
                CompoundTag tempTag2 = new CompoundTag();
                tempTag2.put("amount", tempIntTag);
                tagT1.put(itemName, tempTag2);
                pouchContents.remove(stage);
                pouchContents.put(stage, tagT1);
            }
        } else {
            IntTag tempIntTag = IntTag.valueOf(stackSize);
            CompoundTag tempTag2 = new CompoundTag();
            tempTag2.put("amount", tempIntTag);
            CompoundTag tempTag1 = new CompoundTag();
            tempTag1.put(itemName, tempTag2);
            pouchContents.put(stage, tempTag1);
        }

        pouchContents.put("canTakeOut", canTakeOutList);

        DataOperater.fileWriter(server, player, pouchContents);
    }
}
