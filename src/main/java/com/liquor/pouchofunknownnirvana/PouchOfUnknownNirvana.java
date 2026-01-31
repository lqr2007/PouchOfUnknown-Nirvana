package com.liquor.pouchofunknownnirvana;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

@Mod(PouchOfUnknownNirvana.MODID)
public class PouchOfUnknownNirvana {
    public static final String MODID = "pouchofunknownnirvana";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Map<UUID, CompoundTag> pouchContentsAll = new HashMap<>();
    public static Map<UUID, CompoundTag> canTakeOutListAll = new HashMap<>();

    public PouchOfUnknownNirvana(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(StageEventProcess.class);
        PouchOfUnknownItem.register(modEventBus);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUUID();
        MinecraftServer server = player.getServer();
        CompoundTag pouchContents = DataOperater.fileReader(server, player);
        CompoundTag canTakeOutList = pouchContents.getCompound("canTakeOut");
        pouchContents.remove("canTakeOut");
        pouchContentsAll.put(uuid, pouchContents);
        canTakeOutListAll.put(uuid, canTakeOutList);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        MinecraftServer server = player.getServer();
        UUID uuid = player.getUUID();
        CompoundTag pouchContents = pouchContentsAll.get(uuid);
        CompoundTag canTakeOutList = canTakeOutListAll.get(uuid);
        if(canTakeOutList != null) {
            pouchContents.put("canTakeOut", canTakeOutList);
        }
        DataOperater.fileWriter(server, player, pouchContents);
        pouchContentsAll.remove(uuid);
        canTakeOutListAll.remove(uuid);
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getSide().isClient()) {
            return; // 客户端直接退出，只让服务端执行下面的代码
        }
        Player player = event.getEntity();
        if (!event.getItemStack().getItem().toString().equals("pouchofunknownnirvana:pouch")) {
            return;
        }
        UUID uuid = player.getUUID();
        MinecraftServer server = player.getServer();
        HolderLookup.Provider provider = server.registryAccess();

        CompoundTag pouchContents = pouchContentsAll.get(uuid);
        CompoundTag canTakeOutList = canTakeOutListAll.get(uuid);
        LOGGER.debug(canTakeOutList.toString());
        Inventory inventory = player.getInventory();
        if (canTakeOutList.isEmpty()) {
            Component takeOutMessage = Component.literal("没有可以取出的物品！");
            player.sendSystemMessage(takeOutMessage);
            return;
        }
        int takeOutSum = 0;
        for(int i = 0; i < 36; i++) {
            ItemStack slotContent = inventory.getItem(i);
            if (slotContent.isEmpty()) {
                List<String> itemNames = new ArrayList<>(canTakeOutList.getAllKeys());
                for (String itemName : itemNames) {
                    CompoundTag tempTag = canTakeOutList.getCompound(itemName);
                    ResourceLocation itemLocation = ResourceLocation.parse(itemName);
                    Item item = BuiltInRegistries.ITEM.get(itemLocation);
                    ItemStack itemStackTemp = new ItemStack(item, 1);
                    boolean itemStillHasAmount = false;
                    List<String> nbtStrings = new ArrayList<>(tempTag.getAllKeys());
                    for (String nbtString : nbtStrings) {
                        itemStillHasAmount = true;
                        CompoundTag tempTag2 = tempTag.getCompound(nbtString);
                        LOGGER.debug(tempTag2.toString());
                        if (tempTag2.getInt("count") <= itemStackTemp.getMaxStackSize()) {
                            // 反编译nbt
                            ItemStack itemStack = ItemStack.parseOptional(provider, tempTag2);
                            inventory.setItem(i, itemStack);
                            tempTag.remove(nbtString);
                            canTakeOutList.remove(itemName);
                            if (!tempTag.getAllKeys().isEmpty()) {
                                canTakeOutList.put(itemName, tempTag);
                            }
                            break;
                        } else {
                            CompoundTag tempTag2_Copy = tempTag2.copy();
                            LOGGER.debug(tempTag2_Copy.toString());
                            tempTag2_Copy.remove("count");
                            tempTag2_Copy.putInt("count", itemStackTemp.getMaxStackSize());
                            // 反编译nbt
                            ItemStack itemStack = ItemStack.parseOptional(provider, tempTag2_Copy);
                            inventory.setItem(i, itemStack);

                            IntTag tempIntTag = IntTag.valueOf(tempTag2.getInt("count") - itemStackTemp.getMaxStackSize());
                            tempTag2.remove("count");
                            tempTag2.put("count", tempIntTag);
                            tempTag.remove(nbtString);
                            tempTag.put(nbtString, tempTag2);
                            canTakeOutList.remove(itemName);
                            canTakeOutList.put(itemName, tempTag);
                            break;
                        }
                    }
                    if (!itemStillHasAmount) {
                        canTakeOutList.remove(itemName);
                    }
                    break;
                }
                takeOutSum++;
                if(canTakeOutList.isEmpty()) {
                    break;
                }
            }
        }
        pouchContents.put("canTakeOut", canTakeOutList);
        DataOperater.fileWriter(server, player, pouchContents);
        pouchContents.remove("canTakeOut");

        Component takeOutMessage = Component.translatable("pouchofunknownnirvana.text.output", takeOutSum);
        player.sendSystemMessage(takeOutMessage);
    }
}
