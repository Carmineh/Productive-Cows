package com.productivecows.compat.jade;

import com.productivecows.ProductiveCows;
import com.productivecows.block.AdvancedCoolerBlock;
import com.productivecows.block.LiquidCoolerBlock;
import com.productivecows.block.entity.AdvancedCoolerBlockEntity;
import com.productivecows.block.entity.LiquidCoolerBlockEntity;
import com.productivecows.entity.MaterialCowEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(ProductiveCows.MODID)
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
