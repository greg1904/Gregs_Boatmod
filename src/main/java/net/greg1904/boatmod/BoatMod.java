package net.greg1904.boatmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.greg1904.boatmod.blocks.ShipWheelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BoatMod implements ModInitializer {
    public static final String MOD_ID = "gregs_boatmod" ;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Block SHIP_WHEEL = new ShipWheelBlock(FabricBlockSettings.of(Material.STONE).strength(3.0f));

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("gregs_boatmod", "ship_wheel"), SHIP_WHEEL);
        Registry.register(Registry.ITEM, new Identifier("gregs_boatmod", "ship_wheel"),
                new BlockItem(SHIP_WHEEL, new FabricItemSettings()));
    }
}
