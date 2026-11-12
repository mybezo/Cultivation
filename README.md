<p align="center">
<img width="500" src="https://user-images.githubusercontent.com/20646323/231161480-7b6bd303-cec9-4555-aa40-5c502aaa031b.png"><br><br>
</p>

Cultivation is a Slimefun4 addon that adds a plethora of plants, bushes and trees to Minecraft as well as an amazing food selection to be made via a unique, fully featured, kitchen!

This fork is based on [Sefiraat/Cultivation](https://github.com/Sefiraat/Cultivation) (via the SlimefunGuguProject translation) and changes how plants are shown: plants, bushes, crop sticks and the Garden Cloche are **real blocks instead of display entities**. It also adds the Cultivation Expansion machines and is fully in English.

## Download

[![Java CI](https://github.com/mybezo/Cultivation/actions/workflows/maven.yml/badge.svg)](https://github.com/mybezo/Cultivation/actions/workflows/maven.yml)

Every build on GitHub Actions uploads the plugin jar:

1. Open the [Actions tab](https://github.com/mybezo/Cultivation/actions/workflows/maven.yml) and pick the latest successful run.
2. Download the **Cultivation** artifact from the bottom of the page (you need to be logged in to GitHub).
3. Unzip it and put `Cultivation v1.0.0.jar` in your server's `plugins/` folder.

A build can also be started by hand with **Run workflow** on the same page.

### Requirements

- Minecraft 1.19.4 or newer
- [Slimefun4](https://github.com/SlimefunGuguProject/Slimefun4) (SlimefunGuguProject build)
- [GuizhanLibPlugin](https://50l.cc/gzlib)

You can find a fuller guide to Cultivation in the original [Documentation Pages](https://sefiraat.dev).

## Cultivation Plants

Plants are Cultivation's resource generating flora. Each plant type will grow vanilla Minecraft items periodically.

| Stage | Looks like |
|---|---|
| Seed | A seed head in the plant's colour |
| Growing | Azalea |
| Ready to harvest | Flowering Azalea |

Right-click a flowering plant to harvest it; it then goes back to a plain azalea. Bone meal does nothing on plants and bushes (from players or dispensers), so they never grow into azalea trees. If the block under a plant is broken, the plant pops off and drops its seed.

Plants require crop sticks to be able to grow and, if double-crop-sticked, the plants will attempt to breed with nearby plants:

- Place two mature, double-crop-sticked plants in a line with exactly one air block between them.
- Breeding will either result in a brand new plant type or a copy of the parent with possibly stronger stats. Try your best to get a plant to 10/10/10 for the most drops, the fastest growth and the best breeding.
- Two plants of the same kind always spread a copy when they don't breed a new child.
- When the same parents can give more than one child (e.g. Coal + Earth gives Raw Iron or Raw Copper), each child has an equal chance.
- Black particles mean the two plants cannot breed.

Primal plants (Darkness, Light, Earth, Water, Fire, Air, Power) don't produce items; they are only used for breeding.

There are a total of 90 plants to discover via breeding or, if you're struggling to find a combination, Wandering Traders will sell you documents that will help you on your journey.

## Garden Cloche

You can automate the production of Cultivation Plants within the Garden Cloche, a green terracotta block. While a little slower than manually harvested plants, it will produce constantly and allows Slimefun Cargo and Networks to pull out its produce.

## Cultivation Expansion

Extra machines and items, found in the existing Machines, Crafting Materials and Tools categories.

| Item | What it does | Power |
|---|---|---|
| Genetic Recombinator | Mutates a seed to raise its Level, Speed and Strength. With Genetic Nectar it keeps mutating until the seed is maxed or the nectar runs out. | 500 J/t |
| Propagation | Breeds two plants using 1 Genetic Nectar. A matching pair has a 25% chance to give the child, otherwise a copy of a parent. The child's stats are the average of its parents. Takes 180 s (Strength 1) down to 30 s (Strength 10). Does not unlock the Plant Codex. | 750 J/t |
| Android Kitchen: Finishing | Automatically crafts Finishing Counter recipes when an Android Chef is inserted. Keep at least 2 of each ingredient; the last one stays as the recipe template. | 200 J/t |
| Record of Botanist | Right-click to unlock every entry in the Plant Codex. Consumed on use. | - |

Crafting materials: Genetic Nectar, Silicone, Spatula, Chef Hat and Android Chef.

## Cultivation Bushes

Bushes are somewhat simpler than plants, they don't breed nor do they level and their sole purpose is the production of foodstuffs. Like plants, they are an azalea while growing and a flowering azalea once ready to harvest.
Each bush produces a different food item ("Produce") that can be used within our kitchen to make food by-products and eventually crafted into fully cooked meals.

Bushes are bought from Farmer villagers.

## Cultivation Trees

<p align="center"><img width="1108" alt="image" src="https://user-images.githubusercontent.com/20646323/231168164-fa57deee-e4fb-4463-9439-56b729cd4229.png"></p>

Trees will grow into beautiful, custom built, trees bearing superbly ripe fruits.
Fruits can be plucked off of the tree and used in cooking and smoothie making.
Breaking the tree's leaves has a 20% chance to drop another sapling to be able to replant the tree.

Trees are bought from Fletching Villagers.

## The Kitchen!

<img width="1391" alt="image" src="https://user-images.githubusercontent.com/20646323/231169938-c94116a1-2064-4cb1-85e8-2ecd14b6d61e.png">

The Kitchen is where you can cook all your wonderful produce. The kitchen counters still use display entities for their looks.

Nearly all Cultivation Products (and most Vanilla foods) can be:
- Chopped
- Sliced
- Boiled
- Blended
- Ground
- Fried
- Mashed

Then you can combine your by-products into a meal via:
- Oven Cooking/Baking
- Finishing

Each food item has a custom effect when eaten. Most effects last for 10 whole minutes, but do not allow you to eat again during said 10 mins.

Some foods have custom one-off effects that break this mold.

## Building

```
mvn package
```

The jar is written to `target/`. Pull requests to `main` are built automatically by the Java CI workflow.

## Thanks!

A big thanks goes to Sefiraat and J3fftw for creating Cultivation, and to the SlimefunGuguProject team for their translation.

A big thanks goes to the SlimefunAddonCommunity guinea pig testers and a separate thanks for MCTantrum (B, Decoy and Oddish) who went over and above in their testing.

Another big shoutout to GentlemanCheesy who creates some of the skulls used in the addon, taken from Netheopoiesis.

And lastly, a big thanks goes to https://minecraft-heads.com who kindly gave their permission to utilise heads they've procured within our addon.

[![](https://minecraft-heads.com/images/banners/minecraft-heads_fullbanner_468x60.png)](https://minecraft-heads.com/)
