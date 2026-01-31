package com.liquor.pouchofunknownnirvana.Mixin;

import com.alessandro.astages.api.holder.AHolder;
import com.alessandro.astages.core.ARestrictionManager;
import com.alessandro.astages.core.server.restriction.item.ABaseItemRestriction;
import com.alessandro.astages.event.CommonEventSettings;
import com.alessandro.astages.event.item.ServerEventHandler;
import com.alessandro.astages.store.Attributes;
import com.liquor.pouchofunknownnirvana.StageEventProcess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static com.alessandro.astages.event.item.ServerEventHandler.canBeRunForPlayer;

@Mixin(ServerEventHandler.class)
public abstract class ServerEventHandlerMixin {
    /**
     * @author liquor
     * @reason Change function: Allowed pick up to pouch.
     */
    @SubscribeEvent
    @Overwrite
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        if (canBeRunForPlayer(event.getPlayer())) {
            var restriction = ARestrictionManager.ITEM_INSTANCE.getRestriction(AHolder.player(event.getPlayer()), event.getItemEntity().getItem());
            if (restriction != null && restriction.isDisabled(Attributes.PICKING_UP)) {
                event.setCanPickup(TriState.FALSE);
                event.getItemEntity().remove(Entity.RemovalReason.KILLED);
                Component getMessage = Component.translatable(
                        "pouchofunknownnirvana.text.input",
                        event.getItemEntity().getItem().getCount(),
                        Component.translatable("tooltip.astages.item.hidden_name")
                );
                player.sendSystemMessage(getMessage);
                StageEventProcess.depositToPouch(player, event.getItemEntity().getItem(), restriction.getStage());
            }
        }
    }

    /**
     * @author liquor
     * @reason Change function: Don't drop items from containers.
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
     * @reason Change function: Don't drop items, Send the items to pouch
     */
    @SubscribeEvent
    @Overwrite
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!CommonEventSettings.requireSlotCheck()) { return; }

        ResourceLocation pouchLoacation = ResourceLocation.parse("pouchofunknownnirvana:pouch");
        Item pouchItem = BuiltInRegistries.ITEM.get(pouchLoacation);
        ItemStack pouchStack = new ItemStack(pouchItem, 1);
        Component destroyMessage = Component.translatable("pouchofunknownnirvana.text.remove");

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
                            restriction = ARestrictionManager.ITEM_INSTANCE.getEquipmentRestriction(AHolder.serverAndPlayer(player), slotContent);
                        } else {
                            restriction = ARestrictionManager.ITEM_INSTANCE.getEquipmentRestriction(AHolder.serverAndPlayer(player), slotContent);
                        }

                        if (restriction != null) {
                            if (inventory.contains(pouchStack)) {
                                Component getMessage = Component.translatable("pouchofunknownnirvana.text.enter",
                                        Component.translatable("tooltip.astages.item.hidden_name"),
                                        slotContent.getCount()
                                );
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
                        restriction = ARestrictionManager.ITEM_INSTANCE.getEquipmentRestriction(AHolder.serverAndPlayer(player), slotContent);
                    } else {
                        restriction = ARestrictionManager.ITEM_INSTANCE.getEquipmentRestriction(AHolder.serverAndPlayer(player), slotContent);
                    }

                    if (restriction != null) {
                        if (inventory.contains(pouchStack)) {
                            Component getMessage = Component.translatable("pouchofunknownnirvana.text.enter",
                                    Component.translatable("tooltip.astages.item.hidden_name"),
                                    slotContent.getCount()
                            );
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

    }
}