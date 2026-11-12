package dev.sefiraat.cultivation.implementation.utils;

import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import dev.sefiraat.sefilib.entity.display.builders.BlockDisplayBuilder;
import dev.sefiraat.sefilib.entity.display.builders.ItemDisplayBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

public final class DisplayGroupGenerators {

    private DisplayGroupGenerators() {
        throw new IllegalStateException("Utility class");
    }

    public static DisplayGroup generateBaseCounter(@Nonnull Location location) {
        final DisplayGroup displayGroup = new DisplayGroup(location, 1.01f, 1.01f);
        displayGroup.addDisplay(
            "counter_base",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.4, 0))
                .setItemStack(new ItemStack(Material.STRIPPED_DARK_OAK_LOG))
                .setTransformation(Transformations.COUNTER_BODY.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "counter_top",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.8, 0))
                .setItemStack(new ItemStack(Material.WHITE_CONCRETE))
                .setTransformation(Transformations.COUNTER_TOP.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateChoppingCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        displayGroup.addDisplay(
            "chopping_board",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0.1, 0.9, 0.15))
                .setItemStack(new ItemStack(Material.POLISHED_BLACKSTONE))
                .setTransformation(Transformations.CHOPPING_BOARD.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "knife",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0.1, 0.95, 0.15))
                .setItemStack(new ItemStack(Material.IRON_SWORD))
                .setTransformation(Transformations.CHOPPING_BOARD_KNIFE.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateBlenderCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        displayGroup.addDisplay(
            "blender_base",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(-0.3, 0.9, -0.3))
                .setItemStack(new ItemStack(Material.QUARTZ_BLOCK))
                .setTransformation(Transformations.BLENDER_BASE.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "blender_top",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(-0.3, 1.15, -0.3))
                .setItemStack(new ItemStack(Material.GLASS))
                .setTransformation(Transformations.BLENDER_TOP.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateSlicingCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        displayGroup.addDisplay(
            "chopping_board",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0.2, 0.9, 0.3))
                .setItemStack(new ItemStack(Material.LAPIS_BLOCK))
                .setTransformation(Transformations.CHOPPING_BOARD_2.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "knife",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0.2, 0.95, 0.3))
                .setItemStack(new ItemStack(Material.SHEARS))
                .setTransformation(Transformations.CHOPPING_BOARD_KNIFE_2.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateMashingCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        displayGroup.addDisplay(
            "bowl",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0.05, 0.92, 0.1))
                .setItemStack(new ItemStack(Material.COMPOSTER))
                .setTransformation(Transformations.MASHING_BOWL.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "masher",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0.1, 0.85, -0.3))
                .setItemStack(new ItemStack(Material.NETHERITE_HOE))
                .setTransformation(Transformations.MASHER.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateGrindingCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        BlockData blockData = Material.CAULDRON.createBlockData();
        displayGroup.addDisplay(
            "bowl",
            new BlockDisplayBuilder()
                .setGroupParentOffset(new Vector(0.1, 0.82, -0.2))
                .setBlockData(blockData)
                .setTransformation(Transformations.GRINDING_BOWL.getTransformation(false))
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "bone",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(-0.1, 0.85, 0.3))
                .setItemStack(new ItemStack(Material.BONE))
                .setTransformation(Transformations.BONE.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateOvenCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        displayGroup.addDisplay(
            "oven_door",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.34, 0.45))
                .setItemStack(new ItemStack(Material.POLISHED_BLACKSTONE))
                .setTransformation(Transformations.OVEN_DOOR.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "buttons",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.68, 0.45))
                .setItemStack(new ItemStack(Material.ACTIVATOR_RAIL))
                .setTransformation(Transformations.COOKING_BUTTONS.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateFryingCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        displayGroup.addDisplay(
            "hob",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.85, 0))
                .setItemStack(new ItemStack(Material.TINTED_GLASS))
                .setTransformation(Transformations.FRY_HOB.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "hob_core",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.82, 0))
                .setItemStack(new ItemStack(Material.BLACK_CONCRETE))
                .setTransformation(Transformations.FRY_HOB_CORE.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "buttons",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.68, 0.45))
                .setItemStack(new ItemStack(Material.ACTIVATOR_RAIL))
                .setTransformation(Transformations.COOKING_BUTTONS.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateBoilingCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateFryingCounter(location);
        Levelled blockData = (Levelled) Material.WATER_CAULDRON.createBlockData();
        blockData.setLevel(3);
        displayGroup.addDisplay(
            "pot",
            new BlockDisplayBuilder()
                .setGroupParentOffset(new Vector(-0.15, 0.85, -0.15))
                .setBlockData(blockData)
                .setTransformation(Transformations.BOILING_POT.getTransformation(false))
                .build(displayGroup)
        );

        return displayGroup;
    }

    public static DisplayGroup generateFinishingCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        displayGroup.addDisplay(
            "hob",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.85, 0))
                .setItemStack(new ItemStack(Material.POLISHED_DIORITE))
                .setTransformation(Transformations.FRY_HOB.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

    public static DisplayGroup generateGrillingCounter(@Nonnull Location location) {
        DisplayGroup displayGroup = generateBaseCounter(location);
        displayGroup.addDisplay(
            "grill_door",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.50, 0.45))
                .setItemStack(new ItemStack(Material.POLISHED_BLACKSTONE))
                .setTransformation(Transformations.GRILL_DOOR.getTransformation())
                .build(displayGroup)
        );
        displayGroup.addDisplay(
            "buttons",
            new ItemDisplayBuilder()
                .setGroupParentOffset(new Vector(0, 0.68, 0.45))
                .setItemStack(new ItemStack(Material.ACTIVATOR_RAIL))
                .setTransformation(Transformations.COOKING_BUTTONS.getTransformation())
                .build(displayGroup)
        );
        return displayGroup;
    }

}
