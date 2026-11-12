package dev.sefiraat.cultivation;

import com.google.common.base.Preconditions;
import dev.sefiraat.cultivation.api.slimefun.items.bushes.CultivationBush;
import dev.sefiraat.cultivation.api.slimefun.items.plants.CultivationPlant;
import dev.sefiraat.cultivation.api.slimefun.items.produce.ByProduct;
import dev.sefiraat.cultivation.api.slimefun.items.trees.CultivationTree;
import dev.sefiraat.cultivation.api.slimefun.plant.BreedResult;
import dev.sefiraat.cultivation.api.slimefun.plant.BreedResultType;
import dev.sefiraat.cultivation.api.slimefun.plant.BreedingPair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Registry {
    private static Registry instance;

    @Nonnull
    private final List<CultivationPlant> registeredPlants = new ArrayList<>();
    @Nonnull
    private final List<CultivationBush> registeredBushes = new ArrayList<>();
    @Nonnull
    private final List<CultivationTree> registeredTrees = new ArrayList<>();
    @Nonnull
    private final List<ByProduct> registeredByProducts = new ArrayList<>();
    @Nonnull
    private final List<BreedingPair> plantBreedingPairs = new ArrayList<>();

    @Nonnull
    private final Map<UUID, BlockPosition> storedPositionOne = new HashMap<>();

    @Nonnull
    private final Map<UUID, BlockPosition> storedPositionTwo = new HashMap<>();

    public Registry() {
        Preconditions.checkArgument(instance == null, "Cannot create a new instance of the Registry");
        instance = this;
    }

    public void addPlant(@Nonnull CultivationPlant cultivationPlant) {
        this.registeredPlants.add(cultivationPlant);
        this.plantBreedingPairs.addAll(cultivationPlant.getBreedingPairs());

    }

    public void addBush(@Nonnull CultivationBush cultivationBush) {
        this.registeredBushes.add(cultivationBush);
    }

    public void addTree(@Nonnull CultivationTree cultivationTree) {
        this.registeredTrees.add(cultivationTree);
    }

    public void addByProduct(@Nonnull ByProduct byProduct) {
        this.registeredByProducts.add(byProduct);
    }

    /**
     * Some parent combinations can produce more than one child (e.g. Coal + Earth gives Raw Iron or Raw Copper).
     * The matching pairs are tested in a random order each time, so no child is favoured just because it was
     * registered first.
     */
    @Nonnull
    public BreedResult getBreedResult(@Nonnull String seed1, @Nonnull String seed2) {
        List<BreedingPair> matchingPairs = getMatchingBreedingPairs(seed1, seed2);
        Collections.shuffle(matchingPairs);
        for (BreedingPair pair : matchingPairs) {
            BreedResultType result = pair.testBreed(seed1, seed2);
            if (result != BreedResultType.FAIL) {
                return new BreedResult(pair, result);
            }
        }
        if (matchingPairs.isEmpty()) {
            if (seed1.equals(seed2)) {
                return new BreedResult(plantBreedingPairs.get(0), BreedResultType.SPREAD_MUTATE);
            } else {
                return new BreedResult(plantBreedingPairs.get(0), BreedResultType.NO_PAIRS);
            }
        } else {
            return new BreedResult(plantBreedingPairs.get(0), BreedResultType.FAIL);
        }
    }

    @Nonnull
    public List<CultivationPlant> getRegisteredPlants() {
        return Collections.unmodifiableList(registeredPlants);
    }

    @Nonnull
    public List<CultivationBush> getRegisteredBushes() {
        return Collections.unmodifiableList(registeredBushes);
    }

    @Nonnull
    public List<ByProduct> getRegisteredByProducts() {
        return registeredByProducts;
    }

    @Nonnull
    public List<CultivationTree> getRegisteredTrees() {
        return registeredTrees;
    }

    /**
     * @return A new, modifiable list of every breeding pair that the two plants form, in registration order
     */
    @Nonnull
    public List<BreedingPair> getMatchingBreedingPairs(@Nonnull String seed1, @Nonnull String seed2) {
        List<BreedingPair> matchingPairs = new ArrayList<>();
        for (BreedingPair pair : plantBreedingPairs) {
            if (pair.isBreedPossible(seed1, seed2)) {
                matchingPairs.add(pair);
            }
        }
        return matchingPairs;
    }

    @Nonnull
    public List<BreedingPair> getPlantBreedingPairs() {
        return Collections.unmodifiableList(plantBreedingPairs);
    }

    public void addPositionOne(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        storedPositionOne.put(uuid, new BlockPosition(player.getLocation()));
    }

    public void addPositionTwo(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        storedPositionTwo.put(uuid, new BlockPosition(player.getLocation()));
    }

    @Nullable
    public BlockPosition getPositionOne(@Nonnull Player player) {
        return storedPositionOne.get(player.getUniqueId());
    }

    @Nullable
    public BlockPosition getPositionTwo(@Nonnull Player player) {
        return storedPositionTwo.get(player.getUniqueId());
    }

    public static Registry getInstance() {
        return instance;
    }
}
