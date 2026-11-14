package dev.sefiraat.cultivation.api.utils;

import dev.sefiraat.cultivation.implementation.utils.ServerVersion;
import dev.sefiraat.sefilib.entity.LivingEntityCategory;
import dev.sefiraat.sefilib.entity.LivingEntityDefinition;
import dev.sefiraat.sefilib.entity.LivingEntitySelector;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public final class EntityDefinitions {

    private EntityDefinitions() {
        throw new IllegalStateException("Utility class");
    }

    private static final Set<LivingEntityDefinition> passiveMobs = select(LivingEntityCategory.PASSIVE);
    private static final Set<LivingEntityDefinition> hostileMobs = select(LivingEntityCategory.HOSTILE);
    private static final Set<LivingEntityDefinition> bossMobs = select(LivingEntityCategory.BOSS);
    private static final Set<LivingEntityDefinition> flyingMobs = select(LivingEntityCategory.FLYING);

    @Nonnull
    private static Set<LivingEntityDefinition> select(@Nonnull LivingEntityCategory category) {
        return LivingEntitySelector.start()
            .includeCategories(category)
            .setVersion(ServerVersion.get())
            .process(LivingEntitySelector.MatchType.MATCH_ALL);
    }

    public static Set<LivingEntityDefinition> getPassiveMobs() {
        return Collections.unmodifiableSet(passiveMobs);
    }

    public static Set<LivingEntityDefinition> getHostileMobs() {
        return Collections.unmodifiableSet(hostileMobs);
    }

    public static Set<LivingEntityDefinition> getBossMobs() {
        return Collections.unmodifiableSet(bossMobs);
    }

    public static Set<LivingEntityDefinition> getFlyingMobs() {
        return Collections.unmodifiableSet(flyingMobs);
    }
}
