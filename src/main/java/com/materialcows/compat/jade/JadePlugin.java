package com.materialcows.compat.jade;

import com.materialcows.Materialcows;
import com.materialcows.block.AdvancedCoolerBlock;
import com.materialcows.block.LiquidCoolerBlock;
import com.materialcows.block.entity.AdvancedCoolerBlockEntity;
import com.materialcows.block.entity.LiquidCoolerBlockEntity;
import com.materialcows.entity.MaterialCowEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(Materialcows.MODID)
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CoolerComponentProvider.INSTANCE, LiquidCoolerBlockEntity.class);
        registration.registerBlockDataProvider(CoolerComponentProvider.INSTANCE, AdvancedCoolerBlockEntity.class);
        registration.registerEntityDataProvider(CowComponentProvider.INSTANCE, MaterialCowEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CoolerComponentProvider.INSTANCE, LiquidCoolerBlock.class);
        registration.registerBlockComponent(CoolerComponentProvider.INSTANCE, AdvancedCoolerBlock.class);
        registration.registerEntityComponent(CowComponentProvider.INSTANCE, MaterialCowEntity.class);
    }
}
