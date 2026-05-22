package com.materialcows;

import com.materialcows.data.CowDefinition;
import com.materialcows.entity.MaterialCowEntity;
import com.materialcows.fluid.ModFluids;
import com.materialcows.network.NetworkRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.materialcows.block.BreedingCageBlock;
import com.materialcows.block.CowCageBlock;
import com.materialcows.block.LiquidCoolerBlock;
import com.materialcows.block.entity.BreedingCageBlockEntity;
import com.materialcows.block.entity.CowCageBlockEntity;
import com.materialcows.block.entity.LiquidCoolerBlockEntity;
import com.materialcows.menu.BreedingCageMenu;
import com.materialcows.menu.CowCageMenu;
import com.materialcows.menu.LiquidCoolerMenu;
import com.materialcows.item.CowStickItem;
import com.materialcows.item.CapturedCowItem;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Materialcows.MODID)
public class Materialcows {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "materialcows";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Registries
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);

    // Blocks
    public static final DeferredBlock<Block> BREEDING_CAGE = BLOCKS.register("breeding_cage",
            () -> new BreedingCageBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredItem<BlockItem> BREEDING_CAGE_ITEM = ITEMS.registerSimpleBlockItem("breeding_cage", BREEDING_CAGE);

    public static final DeferredBlock<Block> COW_CAGE = BLOCKS.register("cow_cage",
            () -> new CowCageBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredItem<BlockItem> COW_CAGE_ITEM = ITEMS.registerSimpleBlockItem("cow_cage", COW_CAGE);

    public static final DeferredBlock<Block> LIQUID_COOLER = BLOCKS.register("liquid_cooler",
            () -> new LiquidCoolerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredItem<BlockItem> LIQUID_COOLER_ITEM = ITEMS.registerSimpleBlockItem("liquid_cooler", LIQUID_COOLER);

    public static final DeferredBlock<Block> MILK_GENERATOR = BLOCKS.register("milk_generator",
            () -> new com.materialcows.block.MilkGeneratorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredItem<BlockItem> MILK_GENERATOR_ITEM = ITEMS.registerSimpleBlockItem("milk_generator", MILK_GENERATOR);

    public static final DeferredBlock<Block> ADVANCED_COOLER = BLOCKS.register("advanced_cooler",
            () -> new com.materialcows.block.AdvancedCoolerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredItem<BlockItem> ADVANCED_COOLER_ITEM = ITEMS.registerSimpleBlockItem("advanced_cooler", ADVANCED_COOLER);

    // Items
    public static final DeferredItem<Item> BASE_UPGRADE = ITEMS.register("base_upgrade", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> SPEED_UPGRADE = ITEMS.register("speed_upgrade", () -> new com.materialcows.item.UpgradeItem(new Item.Properties().stacksTo(64), "tooltip.materialcows.speed_upgrade"));
    public static final DeferredItem<Item> EFFICIENCY_UPGRADE = ITEMS.register("efficiency_upgrade", () -> new com.materialcows.item.UpgradeItem(new Item.Properties().stacksTo(64), "tooltip.materialcows.efficiency_upgrade"));
    public static final DeferredItem<Item> ENERGY_UPGRADE = ITEMS.register("energy_upgrade", () -> new com.materialcows.item.UpgradeItem(new Item.Properties().stacksTo(64), "tooltip.materialcows.energy_upgrade"));
    public static final DeferredItem<Item> COW_STICK = ITEMS.register("cow_stick",
            () -> new CowStickItem(new Item.Properties().durability(64)));
    public static final DeferredItem<CapturedCowItem> CAPTURED_COW = ITEMS.register("captured_cow",
            () -> new CapturedCowItem(new Item.Properties().stacksTo(16)));

    // Entity Types & Spawn Eggs
    public static final DeferredHolder<EntityType<?>, EntityType<MaterialCowEntity>> MATERIAL_COW = ENTITY_TYPES.register("material_cow",
            () -> EntityType.Builder.of(MaterialCowEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.4F)
                    .build("material_cow")
    );

    public static final DeferredItem<Item> MATERIAL_COW_SPAWN_EGG = ITEMS.register("material_cow_spawn_egg",
            () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(MATERIAL_COW, 0xC1C1C1, 0x3F3F3F, new Item.Properties()) {
                @Override
                public Component getName(ItemStack stack) {
                    net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.ENTITY_DATA);
                    if (customData != null) {
                        CompoundTag tag = customData.copyTag();
                        if (tag.contains("CowDefinitionId")) {
                            String idStr = tag.getString("CowDefinitionId");
                            net.minecraft.resources.ResourceLocation defId = net.minecraft.resources.ResourceLocation.tryParse(idStr);
                            if (defId != null) {
                                CowDefinition def = null;
                                try {
                                    def = com.materialcows.data.MaterialCowDataLoader.getDefinition(defId);
                                } catch (Exception e) {}
                                if (def == null) {
                                    try {
                                        def = com.materialcows.network.ClientCowData.getDefinition(defId);
                                    } catch (Exception e) {}
                                }
                                if (def != null) {
                                    return Component.translatable("item.materialcows.spawn_egg_format", def.name());
                                }
                            }
                        }
                    }
                    return super.getName(stack);
                }
            }
    );

    // Block Entities & Menus
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreedingCageBlockEntity>> BREEDING_CAGE_BE = BLOCK_ENTITY_TYPES.register("breeding_cage",
            () -> BlockEntityType.Builder.of(BreedingCageBlockEntity::new, BREEDING_CAGE.get()).build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CowCageBlockEntity>> COW_CAGE_BE = BLOCK_ENTITY_TYPES.register("cow_cage",
            () -> BlockEntityType.Builder.of(CowCageBlockEntity::new, COW_CAGE.get()).build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LiquidCoolerBlockEntity>> LIQUID_COOLER_BE = BLOCK_ENTITY_TYPES.register("liquid_cooler",
            () -> BlockEntityType.Builder.of(LiquidCoolerBlockEntity::new, LIQUID_COOLER.get()).build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.materialcows.block.entity.MilkGeneratorBlockEntity>> MILK_GENERATOR_BE = BLOCK_ENTITY_TYPES.register("milk_generator",
            () -> BlockEntityType.Builder.of(com.materialcows.block.entity.MilkGeneratorBlockEntity::new, MILK_GENERATOR.get()).build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.materialcows.block.entity.AdvancedCoolerBlockEntity>> ADVANCED_COOLER_BE = BLOCK_ENTITY_TYPES.register("advanced_cooler",
            () -> BlockEntityType.Builder.of(com.materialcows.block.entity.AdvancedCoolerBlockEntity::new, ADVANCED_COOLER.get()).build(null)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<BreedingCageMenu>> BREEDING_CAGE_MENU = MENU_TYPES.register("breeding_cage",
            () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(BreedingCageMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<CowCageMenu>> COW_CAGE_MENU = MENU_TYPES.register("cow_cage",
            () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(CowCageMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<LiquidCoolerMenu>> LIQUID_COOLER_MENU = MENU_TYPES.register("liquid_cooler",
            () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(LiquidCoolerMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<com.materialcows.menu.MilkGeneratorMenu>> MILK_GENERATOR_MENU = MENU_TYPES.register("milk_generator",
            () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.materialcows.menu.MilkGeneratorMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<com.materialcows.menu.AdvancedCoolerMenu>> ADVANCED_COOLER_MENU = MENU_TYPES.register("advanced_cooler",
            () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.materialcows.menu.AdvancedCoolerMenu::new)
    );

    // Creative Tabs
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS_BLOCKS_TAB = CREATIVE_MODE_TABS.register("items_blocks_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.materialcows.items_blocks"))
                    .icon(() -> COW_STICK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(BREEDING_CAGE_ITEM.get());
                        output.accept(COW_CAGE_ITEM.get());
                        output.accept(LIQUID_COOLER_ITEM.get());
                        output.accept(MILK_GENERATOR_ITEM.get());
                        output.accept(ADVANCED_COOLER_ITEM.get());
                        output.accept(SPEED_UPGRADE.get());
                        output.accept(EFFICIENCY_UPGRADE.get());
                        output.accept(ENERGY_UPGRADE.get());
                        output.accept(BASE_UPGRADE.get());
                        output.accept(COW_STICK.get());
                    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LIQUIDS_TAB = CREATIVE_MODE_TABS.register("liquids_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.materialcows.liquids"))
                    .icon(() -> ModFluids.LIQUID_OBSIDIAN.bucket().get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        java.util.Set<Item> addedBuckets = new java.util.LinkedHashSet<>();
                        for (CowDefinition def : getActiveDefinitions().values()) {
                            net.minecraft.world.level.material.Fluid fluid = BuiltInRegistries.FLUID.get(def.fluidId());
                            if (fluid != null && fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                                Item bucket = fluid.getBucket();
                                if (bucket != Items.AIR) {
                                    addedBuckets.add(bucket);
                                }
                            }
                        }
                        for (Item bucket : addedBuckets) {
                            output.accept(bucket);
                        }
                    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPAWN_EGGS_TAB = CREATIVE_MODE_TABS.register("spawn_eggs_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.materialcows.spawn_eggs"))
                    .icon(() -> MATERIAL_COW_SPAWN_EGG.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        for (CowDefinition def : getActiveDefinitions().values()) {
                            ItemStack eggStack = new ItemStack(MATERIAL_COW_SPAWN_EGG.get());
                            
                            CompoundTag entityData = new CompoundTag();
                            entityData.putString("id", "materialcows:material_cow");
                            entityData.putString("CowDefinitionId", def.id().toString());
                            
                            eggStack.set(net.minecraft.core.component.DataComponents.ENTITY_DATA,
                                    net.minecraft.world.item.component.CustomData.of(entityData));
                            
                            output.accept(eggStack);
                        }
                    }).build());

    public static java.util.Map<net.minecraft.resources.ResourceLocation, CowDefinition> getActiveDefinitions() {
        var clientDefs = com.materialcows.network.ClientCowData.getDefinitions();
        if (!clientDefs.isEmpty()) {
            return clientDefs;
        }
        return com.materialcows.data.MaterialCowDataLoader.getDefinitions();
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Materialcows(IEventBus modEventBus, ModContainer modContainer) {
        // Register the common setup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register configuration
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, com.materialcows.Config.SPEC);

        // Register static event subscribers
        modEventBus.register(com.materialcows.event.ModBusEvents.class);

        // Register fluid registries
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);

        // Register network payloads
        modEventBus.addListener(NetworkRegistry::registerPayloads);

        // Register the Deferred Registers to the mod event bus
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Materialcows) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("MaterialCows Common Setup");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ADVANCED_COOLER_BE.get(), com.materialcows.client.renderer.CoolerBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(LIQUID_COOLER_BE.get(), com.materialcows.client.renderer.CoolerBlockEntityRenderer::new);
        }
    }
}
