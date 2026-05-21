package com.materialcows.fluid;

import com.materialcows.Materialcows;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModFluids {
    
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Materialcows.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, Materialcows.MODID);

    public static final ResourceLocation WATER_STILL = ResourceLocation.parse("minecraft:block/water_still");
    public static final ResourceLocation WATER_FLOWING = ResourceLocation.parse("minecraft:block/water_flow");
    public static final ResourceLocation LAVA_STILL = ResourceLocation.parse("minecraft:block/lava_still");
    public static final ResourceLocation LAVA_FLOWING = ResourceLocation.parse("minecraft:block/lava_flow");

    public static final List<RegisteredFluid> ALL_FLUIDS = new ArrayList<>();
    public static final Map<String, RegisteredFluid> FLUIDS_MAP = new HashMap<>();

    public static class RegisteredFluid {
        private final String name;
        private final DeferredHolder<FluidType, FluidType> type;
        private final DeferredHolder<Fluid, BaseFlowingFluid.Source> source;
        private final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
        private final DeferredHolder<Block, LiquidBlock> block;
        private final DeferredHolder<Item, BucketItem> bucket;
        private final BaseFlowingFluid.Properties properties;

        @SuppressWarnings("unchecked")
        public RegisteredFluid(String name, int color, boolean isMolten) {
            this.name = name;

            ResourceLocation still = isMolten ? LAVA_STILL : WATER_STILL;
            ResourceLocation flowingTex = isMolten ? LAVA_FLOWING : WATER_FLOWING;

            int density = isMolten ? 3000 : 1000;
            int viscosity = isMolten ? 6000 : 1000;
            int temperature = isMolten ? 1300 : 300;

            this.type = FLUID_TYPES.register(name, () -> new TintedFluidType(
                    FluidType.Properties.create()
                            .density(density)
                            .viscosity(viscosity)
                            .temperature(temperature)
                            .descriptionId("fluid.materialcows." + name),
                    color, still, flowingTex
            ));

            // Use array wrappers to allow safe deferred references in the lambdas
            final DeferredHolder<Fluid, BaseFlowingFluid.Source>[] sourceHolder = new DeferredHolder[1];
            final DeferredHolder<Fluid, BaseFlowingFluid.Flowing>[] flowingHolder = new DeferredHolder[1];

            // Setup deferred properties
            this.properties = new BaseFlowingFluid.Properties(
                    type,
                    () -> sourceHolder[0].get(),
                    () -> flowingHolder[0].get()
            );

            this.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(this.properties));
            this.flowing = FLUIDS.register(name + "_flowing", () -> new BaseFlowingFluid.Flowing(this.properties));

            sourceHolder[0] = this.source;
            flowingHolder[0] = this.flowing;

            this.block = Materialcows.BLOCKS.register(name, () -> new LiquidBlock(
                    this.source.get(),
                    BlockBehaviour.Properties.ofFullCopy(isMolten ? Blocks.LAVA : Blocks.WATER)
                            .noCollission()
                            .strength(100.0F)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
            ));

            this.bucket = Materialcows.ITEMS.register(name + "_bucket", () -> new BucketItem(
                    this.source.get(),
                    new Item.Properties()
                            .craftRemainder(Items.BUCKET)
                            .stacksTo(1)
            ));

            // Link block and bucket in properties
            this.properties.block(this.block).bucket(this.bucket);
        }

        public String name() { return name; }
        public DeferredHolder<FluidType, FluidType> type() { return type; }
        public DeferredHolder<Fluid, BaseFlowingFluid.Source> source() { return source; }
        public DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing() { return flowing; }
        public DeferredHolder<Block, LiquidBlock> block() { return block; }
        public DeferredHolder<Item, BucketItem> bucket() { return bucket; }
    }

    private static RegisteredFluid register(String name, String hexColor, boolean isMolten) {
        int color = Integer.parseInt(hexColor.replace("#", ""), 16);
        RegisteredFluid fluid = new RegisteredFluid(name, color, isMolten);
        ALL_FLUIDS.add(fluid);
        FLUIDS_MAP.put(name, fluid);
        return fluid;
    }

    // Register all 15 custom fluids as per implementation table
    public static final RegisteredFluid LIQUID_CLAY = register("liquid_clay", "#948A7A", false);
    public static final RegisteredFluid MOLTEN_STONE = register("molten_stone", "#737373", true);
    public static final RegisteredFluid LIQUID_COAL = register("liquid_coal", "#2A2A2A", false);
    public static final RegisteredFluid LIQUID_OBSIDIAN = register("liquid_obsidian", "#191226", false);
    public static final RegisteredFluid MOLTEN_COPPER = register("molten_copper", "#E77C56", true);
    public static final RegisteredFluid MOLTEN_IRON = register("molten_iron", "#D8D8D8", true);
    public static final RegisteredFluid LIQUID_REDSTONE = register("liquid_redstone", "#FF0000", false);
    public static final RegisteredFluid MOLTEN_GOLD = register("molten_gold", "#FFE14B", true);
    public static final RegisteredFluid LIQUID_LAPIS = register("liquid_lapis", "#1044A8", false);
    public static final RegisteredFluid LIQUID_GLOWSTONE = register("liquid_glowstone", "#FFBC5E", false);
    public static final RegisteredFluid LIQUID_QUARTZ = register("liquid_quartz", "#EAE5DE", false);
    public static final RegisteredFluid LIQUID_DIAMOND = register("liquid_diamond", "#55FFFF", false);
    public static final RegisteredFluid LIQUID_EMERALD = register("liquid_emerald", "#17DD62", false);
    public static final RegisteredFluid MOLTEN_NETHERITE = register("molten_netherite", "#31292A", true);
    public static final RegisteredFluid LIQUID_ENDER = register("liquid_ender", "#0B4D42", false);
    public static final RegisteredFluid LIQUID_MILK = register("liquid_milk", "#FDFDFD", false);

    // Conditional Fluids
    public static final RegisteredFluid MOLTEN_ZINC = net.neoforged.fml.ModList.get().isLoaded("create") ? register("molten_zinc", "#8FB8A8", true) : null;
    public static final RegisteredFluid MOLTEN_BRASS = net.neoforged.fml.ModList.get().isLoaded("create") ? register("molten_brass", "#E5BD54", true) : null;

    public static final RegisteredFluid MOLTEN_OSMIUM = net.neoforged.fml.ModList.get().isLoaded("mekanism") ? register("molten_osmium", "#9CB7C7", true) : null;
    public static final RegisteredFluid MOLTEN_TIN = net.neoforged.fml.ModList.get().isLoaded("mekanism") || net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_tin", "#AAB9C4", true) : null;
    public static final RegisteredFluid MOLTEN_LEAD = net.neoforged.fml.ModList.get().isLoaded("mekanism") || net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_lead", "#79808E", true) : null;
    public static final RegisteredFluid MOLTEN_URANIUM = net.neoforged.fml.ModList.get().isLoaded("mekanism") ? register("molten_uranium", "#6F8E6B", true) : null;

    public static final RegisteredFluid MOLTEN_SILVER = net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_silver", "#BFCAD1", true) : null;
    public static final RegisteredFluid MOLTEN_NICKEL = net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_nickel", "#C8C697", true) : null;
    public static final RegisteredFluid MOLTEN_BRONZE = net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_bronze", "#D4943E", true) : null;
    public static final RegisteredFluid MOLTEN_ELECTRUM = net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_electrum", "#F3D263", true) : null;
    public static final RegisteredFluid MOLTEN_INVAR = net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_invar", "#9BA09C", true) : null;
    public static final RegisteredFluid MOLTEN_SIGNALUM = net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_signalum", "#E45924", true) : null;
    public static final RegisteredFluid MOLTEN_LUMIUM = net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_lumium", "#F3EEA2", true) : null;
    public static final RegisteredFluid MOLTEN_ENDERIUM = net.neoforged.fml.ModList.get().isLoaded("thermal") ? register("molten_enderium", "#186A6B", true) : null;
}
