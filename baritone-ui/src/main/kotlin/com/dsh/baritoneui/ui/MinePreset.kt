package com.dsh.baritoneui.ui

/**
 * Mining presets for the control panel.
 *
 * Baritone's `#mine` takes a comma-separated list of block ids, so a preset can
 * name several variants of the same ore (stone + deepslate forms) and Baritone
 * will mine whichever it finds.
 *
 * Ids are written WITH their namespace when the block does not live in
 * `minecraft:` (for example `thermal:tin_ore`); [com.dsh.baritoneui.BaritoneBridge.mine]
 * only adds the `minecraft:` prefix when the id has no namespace of its own.
 *
 * Blocks that do not exist in a given pack are simply never matched by
 * Baritone, so listing ids from mods that may be absent is harmless.
 */
enum class MinePreset(
    val labelKey: String,
    val blocks: String
) {
    // ---------------------------------------------------------------- vanilla ores
    DIAMOND("baritoneui.preset.diamond", "diamond_ore,deepslate_diamond_ore"),
    IRON("baritoneui.preset.iron", "iron_ore,deepslate_iron_ore"),
    GOLD("baritoneui.preset.gold", "gold_ore,deepslate_gold_ore,nether_gold_ore"),
    COPPER("baritoneui.preset.copper", "copper_ore,deepslate_copper_ore"),
    COAL("baritoneui.preset.coal", "coal_ore,deepslate_coal_ore"),
    REDSTONE("baritoneui.preset.redstone", "redstone_ore,deepslate_redstone_ore"),
    LAPIS("baritoneui.preset.lapis", "lapis_ore,deepslate_lapis_ore"),
    EMERALD("baritoneui.preset.emerald", "emerald_ore,deepslate_emerald_ore"),
    QUARTZ("baritoneui.preset.quartz", "nether_quartz_ore"),
    ANCIENT_DEBRIS("baritoneui.preset.ancient_debris", "ancient_debris"),
    ALL_ORES("baritoneui.preset.all_ores",
        "diamond_ore,deepslate_diamond_ore,iron_ore,deepslate_iron_ore," +
        "gold_ore,deepslate_gold_ore,nether_gold_ore,copper_ore,deepslate_copper_ore," +
        "coal_ore,deepslate_coal_ore,redstone_ore,deepslate_redstone_ore," +
        "lapis_ore,deepslate_lapis_ore,emerald_ore,deepslate_emerald_ore," +
        "nether_quartz_ore,ancient_debris"),

    // ---------------------------------------------------------------- Mekanism
    OSMIUM("baritoneui.preset.osmium", "mekanism:osmium_ore,mekanism:deepslate_osmium_ore"),
    TIN("baritoneui.preset.tin",
        "mekanism:tin_ore,mekanism:deepslate_tin_ore,thermal:tin_ore,thermal:deepslate_tin_ore"),
    LEAD("baritoneui.preset.lead",
        "mekanism:lead_ore,mekanism:deepslate_lead_ore,thermal:lead_ore,thermal:deepslate_lead_ore"),
    URANIUM("baritoneui.preset.uranium", "mekanism:uranium_ore,mekanism:deepslate_uranium_ore"),
    FLUORITE("baritoneui.preset.fluorite", "mekanism:fluorite_ore,mekanism:deepslate_fluorite_ore"),
    NAQUADAH("baritoneui.preset.naquadah", "mekanism_extras:naquadah_ore,mekanism_extras:end_naquadah_ore"),

    // ---------------------------------------------------------------- Thermal
    SILVER("baritoneui.preset.silver", "thermal:silver_ore,thermal:deepslate_silver_ore"),
    NICKEL("baritoneui.preset.nickel", "thermal:nickel_ore,thermal:deepslate_nickel_ore"),
    APATITE("baritoneui.preset.apatite", "thermal:apatite_ore,thermal:deepslate_apatite_ore"),
    CINNABAR("baritoneui.preset.cinnabar", "thermal:cinnabar_ore,thermal:deepslate_cinnabar_ore"),
    NITER("baritoneui.preset.niter", "thermal:niter_ore,thermal:deepslate_niter_ore"),
    SULFUR("baritoneui.preset.sulfur", "thermal:sulfur_ore,thermal:deepslate_sulfur_ore"),
    RUBY("baritoneui.preset.ruby", "thermal:ruby_ore,thermal:deepslate_ruby_ore"),
    SAPPHIRE("baritoneui.preset.sapphire", "thermal:sapphire_ore,thermal:deepslate_sapphire_ore"),

    // ---------------------------------------------------------------- other tech mods
    ZINC("baritoneui.preset.zinc", "create:zinc_ore,create:deepslate_zinc_ore"),
    THORIUM("baritoneui.preset.thorium", "create_new_age:thorium_ore"),
    CERTUS_QUARTZ("baritoneui.preset.certus", "ae2:quartz_cluster"),
    DRACONIUM("baritoneui.preset.draconium",
        "draconicevolution:overworld_draconium_ore,draconicevolution:deepslate_draconium_ore," +
        "draconicevolution:nether_draconium_ore,draconicevolution:end_draconium_ore"),
    INFERIUM("baritoneui.preset.inferium",
        "mysticalagriculture:inferium_ore,mysticalagriculture:deepslate_inferium_ore"),
    PROSPERITY("baritoneui.preset.prosperity",
        "mysticalagriculture:prosperity_ore,mysticalagriculture:deepslate_prosperity_ore"),
    SOULIUM("baritoneui.preset.soulium", "mysticalagriculture:soulium_ore"),

    // ---------------------------------------------------------------- Tinkers
    COBALT("baritoneui.preset.cobalt", "tconstruct:cobalt_ore"),
    SLIME_CRYSTAL("baritoneui.preset.slime_crystal",
        "tconstruct:earth_slime_crystal_cluster,tconstruct:sky_slime_crystal_cluster," +
        "tconstruct:ichor_slime_crystal_cluster,tconstruct:ender_slime_crystal_cluster"),

    // ---------------------------------------------------------------- Aether family
    SKYJADE("baritoneui.preset.skyjade", "deep_aether:skyjade_ore"),
    AGATE("baritoneui.preset.agate",
        "deep_aether:agate_ore,deep_aether:highstone_agate_ore"),
    ADIBIUM("baritoneui.preset.adibium",
        "deep_aether:adibium_ore,deep_aether:highstone_adibium_ore"),
    ORATIE("baritoneui.preset.oratie",
        "deep_aether:oratie_ore,deep_aether:highstone_oratie_ore"),
    STRATUS_DEBRIS("baritoneui.preset.stratus_debris", "deep_aether:stratus_debris"),

    // ---------------------------------------------------------------- common materials
    STONE("baritoneui.preset.stone", "stone"),
    DEEPSLATE("baritoneui.preset.deepslate", "deepslate"),
    COBBLESTONE("baritoneui.preset.cobblestone", "cobblestone"),
    NETHERRACK("baritoneui.preset.netherrack", "netherrack"),
    END_STONE("baritoneui.preset.end_stone", "end_stone"),
    OBSIDIAN("baritoneui.preset.obsidian", "obsidian,crying_obsidian"),
    GRAVEL("baritoneui.preset.gravel", "gravel"),
    SAND("baritoneui.preset.sand", "sand,red_sand"),
    DIRT("baritoneui.preset.dirt", "dirt,coarse_dirt,rooted_dirt"),
    CLAY("baritoneui.preset.clay", "clay"),
    GLOWSTONE("baritoneui.preset.glowstone", "glowstone"),
    MAGMA("baritoneui.preset.magma", "magma_block"),
    SOUL_SAND("baritoneui.preset.soul_sand", "soul_sand,soul_soil"),
    MOSSY("baritoneui.preset.mossy", "moss_block"),
    SNOW_ICE("baritoneui.preset.snow_ice", "snow_block,ice,packed_ice,blue_ice"),
    SPONGE("baritoneui.preset.sponge", "sponge,wet_sponge"),
    PRISMARINE("baritoneui.preset.prismarine", "prismarine,prismarine_bricks,dark_prismarine,sea_lantern"),
    AMETHYST("baritoneui.preset.amethyst", "amethyst_block,budding_amethyst"),

    // ---------------------------------------------------------------- wood
    OAK("baritoneui.preset.oak", "oak_log"),
    SPRUCE("baritoneui.preset.spruce", "spruce_log"),
    BIRCH("baritoneui.preset.birch", "birch_log"),
    JUNGLE("baritoneui.preset.jungle", "jungle_log"),
    ACACIA("baritoneui.preset.acacia", "acacia_log"),
    DARK_OAK("baritoneui.preset.dark_oak", "dark_oak_log"),
    MANGROVE("baritoneui.preset.mangrove", "mangrove_log"),
    CHERRY("baritoneui.preset.cherry", "cherry_log"),
    CRIMSON("baritoneui.preset.crimson", "crimson_stem"),
    WARPED("baritoneui.preset.warped", "warped_stem"),
    BAMBOO("baritoneui.preset.bamboo", "bamboo_block"),
    ANY_LOG("baritoneui.preset.any_log",
        "oak_log,spruce_log,birch_log,jungle_log,acacia_log,dark_oak_log," +
        "mangrove_log,cherry_log,crimson_stem,warped_stem,bamboo_block");

    companion object {
        val ORES = listOf(
            DIAMOND, IRON, GOLD, COPPER, COAL, REDSTONE,
            LAPIS, EMERALD, QUARTZ, ANCIENT_DEBRIS, ALL_ORES
        )

        /** Ores added by the tech / magic mods in this pack. */
        val MODDED_ORES = listOf(
            // Mekanism
            OSMIUM, TIN, LEAD, URANIUM, FLUORITE, NAQUADAH,
            // Thermal
            SILVER, NICKEL, APATITE, CINNABAR, NITER, SULFUR, RUBY, SAPPHIRE,
            // other tech
            ZINC, THORIUM, CERTUS_QUARTZ, DRACONIUM,
            INFERIUM, PROSPERITY, SOULIUM,
            // Tinkers
            COBALT, SLIME_CRYSTAL,
            // Aether family
            SKYJADE, AGATE, ADIBIUM, ORATIE, STRATUS_DEBRIS
        )

        val MATERIALS = listOf(
            STONE, DEEPSLATE, COBBLESTONE, NETHERRACK,
            END_STONE, OBSIDIAN, GRAVEL, SAND, DIRT, CLAY,
            GLOWSTONE, MAGMA, SOUL_SAND, MOSSY,
            SNOW_ICE, SPONGE, PRISMARINE, AMETHYST
        )

        val WOOD = listOf(
            OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK,
            MANGROVE, CHERRY, CRIMSON, WARPED, BAMBOO, ANY_LOG
        )
    }
}
