package com.liquor.pouchofunknownnirvana;

import com.alessandro.astages.event.custom.actions.StageAddedPlayerEvent;
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
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import java.nio.file.Path;
import java.util.UUID;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PouchOfUnknownNirvana.MODID)
public class PouchOfUnknownNirvana {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "pouchofunknownnirvana";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static CompoundTag pouchContents;
    public static CompoundTag canTakeOutList;

    ResourceLocation pouchLoacation = ResourceLocation.parse("pouchofunknownnirvana:pouch");
    Item pouchItem = BuiltInRegistries.ITEM.get(pouchLoacation);
    ItemStack pouchStack = new ItemStack(pouchItem, 1);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public PouchOfUnknownNirvana(IEventBus modEventBus, ModContainer modContainer) {
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (PouchOfUnknownNirvana) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(StageEventProcess.class);
        PouchOfUnknownItem.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        // modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUUID();
        LOGGER.debug(uuid.toString());
        MinecraftServer server = player.getServer();
        Path worldSaveRootPath = server.getWorldPath(LevelResource.ROOT);
        LOGGER.debug(worldSaveRootPath.toString());
        pouchContents = DataOperater.fileReader(server, player);
        canTakeOutList = pouchContents.getCompound("canTakeOut");
        pouchContents.remove("canTakeOut");

        LOGGER.debug(pouchContents.toString());
        LOGGER.debug(pouchContents.getAllKeys().toString());
        /*
        for (String t1 : pouchContents.getAllKeys()) {
            LOGGER.debug(t1);
            CompoundTag tagT1 = (CompoundTag) pouchContents.get(t1);
            for(String t2 : tagT1.getAllKeys()) {
                LOGGER.debug(t2);
                CompoundTag tagT2 = (CompoundTag) tagT1.get(t2);
                LOGGER.debug(String.valueOf(tagT2.getInt("amount")));
            }
        }

         */
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getSide().isClient()) {
            return; // 客户端直接退出，只让服务端执行下面的代码
        }

        Player player = event.getEntity();
        MinecraftServer server = player.getServer();
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
                for (String itemName : canTakeOutList.getAllKeys()) {
                    CompoundTag tempTag = canTakeOutList.getCompound(itemName);
                    ResourceLocation itemLoacation = ResourceLocation.parse(itemName);
                    Item item = BuiltInRegistries.ITEM.get(itemLoacation);
                    ItemStack itemStackTemp = new ItemStack(item, 1);
                    if (tempTag.getInt("amount") <= itemStackTemp.getMaxStackSize()) {
                        ItemStack itemStack = new ItemStack(item, tempTag.getInt("amount"));
                        inventory.setItem(i, itemStack);
                        canTakeOutList.remove(itemName);
                        LOGGER.debug(itemName + " is empty.");
                        break;
                    } else {
                        ItemStack itemStack = new ItemStack(item, itemStackTemp.getMaxStackSize());
                        inventory.setItem(i, itemStack);
                        IntTag tempIntTag = IntTag.valueOf(tempTag.getInt("amount") - itemStackTemp.getMaxStackSize());
                        CompoundTag tempInTag = new CompoundTag();
                        tempInTag.put("amount", tempIntTag);
                        LOGGER.debug(itemName+" still has " + (tempTag.getInt("amount") - itemStackTemp.getMaxStackSize()));
                        canTakeOutList.remove(itemName);
                        canTakeOutList.put(itemName, tempInTag);
                        break;
                    }
                }
                takeOutSum++;
            }
        }
        pouchContents.put("canTakeOut", canTakeOutList);
        DataOperater.fileWriter(server, player, pouchContents);
        pouchContents.remove("canTakeOut");

        Component takeOutMessage = Component.literal("已将" + takeOutSum + "组物品取出");
        player.sendSystemMessage(takeOutMessage);
    }
}
