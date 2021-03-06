package li.cil.scannable.common.scanning;

import li.cil.scannable.api.API;
import li.cil.scannable.api.scanning.ScanFilterBlock;
import li.cil.scannable.api.scanning.ScanResultProvider;
import li.cil.scannable.api.scanning.ScannerModuleBlock;
import li.cil.scannable.client.scanning.filter.ScanFilterBlockCache;
import li.cil.scannable.client.scanning.filter.ScanFilterFluidTag;
import li.cil.scannable.common.config.Constants;
import li.cil.scannable.common.config.Settings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = API.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum ScannerModuleFluid implements ScannerModuleBlock {
    INSTANCE;

    @OnlyIn(Dist.CLIENT)
    ScanFilterBlock filter;

    @Override
    public int getEnergyCost(final PlayerEntity player, final ItemStack module) {
        return Settings.energyCostModuleFluid;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ScanResultProvider getResultProvider() {
        return GameRegistry.findRegistry(ScanResultProvider.class).getValue(API.SCAN_RESULT_PROVIDER_BLOCKS);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float adjustLocalRange(final float range) {
        return range * Constants.MODULE_BLOCK_RADIUS_MULTIPLIER;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Optional<ScanFilterBlock> getFilter(final ItemStack module) {
        validateFilter();
        return Optional.of(filter);
    }

    @OnlyIn(Dist.CLIENT)
    private void validateFilter() {
        if (filter != null) {
            return;
        }

        final List<ScanFilterBlock> filters = new ArrayList<>();
        for (final Tag<Fluid> tag : FluidTags.getCollection().getTagMap().values()) {
            if (!Settings.ignoredFluidTags.contains(tag)) {
                filters.add(new ScanFilterFluidTag(tag));
            }
        }
        filter = new ScanFilterBlockCache(filters);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        // Reset on any config change so we also rebuild the filter when resource reload
        // kicks in which can result in ids changing and thus our cache being invalid.
        ScannerModuleFluid.INSTANCE.filter = null;
    }
}
