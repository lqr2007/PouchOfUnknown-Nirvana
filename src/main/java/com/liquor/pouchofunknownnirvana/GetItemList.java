package com.liquor.pouchofunknownnirvana;

import com.alessandro.astages.core.ARestrictionManager;
import com.alessandro.astages.core.server.manager.AItemManager;
import com.alessandro.astages.core.server.restriction.item.AItemRestriction;
import com.alessandro.astages.util.AStagesUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class GetItemList {

    public static List<Item> GetUnlockItemList(Player player) {
        List<Item> unlockItems = new ArrayList<>();

        AItemManager itemManager = ARestrictionManager.ITEM_INSTANCE;
        List<AItemRestriction> itemRestrictions = itemManager.getItemRestrictions();

        for (AItemRestriction restriction : itemRestrictions) {
            String stage = restriction.getStage();

            if (AStagesUtil.hasStage(player, stage)) {
                List<Item> stageItems = restriction.getItems();
                unlockItems.addAll(stageItems);
            }
        }
        return unlockItems;
    }

    public static List<Item> GetUnknownItemList(Player player) {
        List<Item> unknownItems = new ArrayList<>();

        AItemManager itemManager = ARestrictionManager.ITEM_INSTANCE;
        List<AItemRestriction> itemRestrictions = itemManager.getItemRestrictions();

        for (AItemRestriction restriction : itemRestrictions) {
            String stage = restriction.getStage();

            if (!AStagesUtil.hasStage(player, stage)) {
                List<Item> stageItems = restriction.getItems();

                unknownItems.addAll(stageItems);
            }
        }

        return unknownItems;
    }
}
