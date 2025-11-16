package com.liquor.pouchofunknownnirvana.Mixin;

import com.alessandro.astages.core.ARestrictionManager;
import com.alessandro.astages.core.server.restriction.item.ABaseItemRestriction;
import com.alessandro.astages.event.CommonEventSettings;
import com.alessandro.astages.event.item.ServerEventHandler;
import com.liquor.pouchofunknownnirvana.PouchOfUnknownNirvana;
import com.liquor.pouchofunknownnirvana.StageEventProcess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerEventHandler.class)
public abstract class ServerEventHandlerMixin {
    @Unique
    private static boolean astagesFixer_GCTN_1_21_1$itemPickupExecuted = false;

    /**
     * @author liquor
     * @reason Don't need the function.
     */
    @SubscribeEvent
    @Overwrite
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!astagesFixer_GCTN_1_21_1$itemPickupExecuted) {
            PouchOfUnknownNirvana.LOGGER.debug("为什么我能捡东西 :)");
            astagesFixer_GCTN_1_21_1$itemPickupExecuted = true;
        }
        return;
    }

    /**
     * @author liquor
     * @reason Don't need the function.
     */
    @SubscribeEvent
    @Overwrite
    public static void onPlayerTickContainer(PlayerTickEvent.Pre event) {
        if (!CommonEventSettings.requireContainerCheck()) {
            return;
        }
        if (!event.getEntity().level().isClientSide && !(event.getEntity() instanceof FakePlayer)) {
            CommonEventSettings.resetContainerChanged();
        }
    }

    /**
     * @author liquor
     * @reason Send the items to pouch
     */
    @SubscribeEvent
    @Overwrite
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        ResourceLocation pouchLoacation = ResourceLocation.parse("pouchofunknownnirvana:pouch");
        Item pouchItem = BuiltInRegistries.ITEM.get(pouchLoacation);
        ItemStack pouchStack = new ItemStack(pouchItem, 1);
        Component destroyMessage = Component.literal("由于你没有未知之袋，未知物品已销毁!");

        if (!event.getEntity().level().isClientSide && !(event.getEntity() instanceof FakePlayer)) {
            Player player = event.getEntity();
            boolean isAnotherInventoryOpened = CommonEventSettings.hasPlayerAnotherContainerOpened(player);
            if (isAnotherInventoryOpened) {
                return;
            }

            Inventory inventory = player.getInventory();
            int armorStart = inventory.items.size();
            int armorEnd = armorStart + inventory.armor.size();
            if (CommonEventSettings.getSlotChanged() == null) {
                for(int i = 0; i < inventory.getContainerSize(); ++i) {
                    ItemStack slotContent = inventory.getItem(i);
                    if (!slotContent.isEmpty()) {
                        ABaseItemRestriction<?, ?> restriction;
                        if (i >= armorStart && i <= armorEnd) {
                            restriction = ARestrictionManager.ITEM_INSTANCE.getEquipmentRestriction(event.getEntity(), slotContent);
                        } else {
                            restriction = ARestrictionManager.ITEM_INSTANCE.getInventoryRestriction(event.getEntity(), slotContent);
                        }

                        if (restriction != null) {
                            if (inventory.contains(pouchStack)) {
                                Component getMessage = Component.literal("你将未知物品*" + slotContent.getCount() + "放入了未知之袋");
                                player.sendSystemMessage(getMessage);
                                StageEventProcess.depositToPouch(player, slotContent, restriction.getStage());
                            } else {
                                player.sendSystemMessage(destroyMessage);
                            }
                            inventory.setItem(i, ItemStack.EMPTY);
                        }
                    }
                }
            } else {
                ItemStack slotContent = inventory.getItem(CommonEventSettings.getSlotChanged());
                if (!slotContent.isEmpty()) {
                    ABaseItemRestriction<?, ?> restriction;
                    if (CommonEventSettings.getSlotChanged() >= armorStart && CommonEventSettings.getSlotChanged() <= armorEnd) {
                        restriction = ARestrictionManager.ITEM_INSTANCE.getEquipmentRestriction(event.getEntity(), slotContent);
                    } else {
                        restriction = ARestrictionManager.ITEM_INSTANCE.getInventoryRestriction(event.getEntity(), slotContent);
                    }

                    if (restriction != null) {
                        if (inventory.contains(pouchStack)) {
                            Component getMessage = Component.literal("你将未知物品*" + slotContent.getCount() + "放入了未知之袋");
                            player.sendSystemMessage(getMessage);
                            StageEventProcess.depositToPouch(player, slotContent, restriction.getStage());
                        } else {
                            player.sendSystemMessage(destroyMessage);
                        }
                        inventory.setItem(CommonEventSettings.getSlotChanged(), ItemStack.EMPTY);
                    }
                }
            }
            CommonEventSettings.resetSlotChanged();
        }

        // if (CommonEventSettings.requireSlotCheck()) {}
    }
}