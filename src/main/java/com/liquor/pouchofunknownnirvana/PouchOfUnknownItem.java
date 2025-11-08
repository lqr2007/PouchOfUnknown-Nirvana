package com.liquor.pouchofunknownnirvana;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PouchOfUnknownItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PouchOfUnknownNirvana.MODID);

    public static final DeferredItem<Item> unknownItem = ITEMS.register("pouch",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
