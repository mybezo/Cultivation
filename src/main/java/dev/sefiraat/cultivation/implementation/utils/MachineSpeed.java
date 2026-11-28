package dev.sefiraat.cultivation.implementation.utils;

import dev.sefiraat.cultivation.Cultivation;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Machine speed from {@code speed.yml}: every machine has a fixed base time and a speed multiplier (default 1).
 * Real time = base time / speed, rounded to Slimefun ticks and never faster than one tick.
 * <p>
 * Everything is read and calculated once when the plugin starts; machines only read the stored numbers.
 * Item lore is never changed by the speed, so items stay identical.
 */
public final class MachineSpeed {

    public static final String FILE_NAME = "speed.yml";

    public static final String GARDEN_CLOCHE = "CLT_GARDEN_CLOCHE";
    public static final String GENETIC_RECOMBINATOR = "CLT_GENETIC_RECOMBINATOR";
    public static final String PROPAGATION = "CLT_PROPAGATION";

    /**
     * The Garden Cloche has no fixed time (every tick a plant has a chance to be harvested),
     * so its maximum is a fixed limit instead: 1000 makes the slowest plant (Legend, Level 1) harvest every tick.
     */
    private static final double GARDEN_CLOCHE_MAX_SPEED = 1000;

    /**
     * The shortest base time in real seconds of each machine with a fixed time, used for its maximum speed.
     */
    private static final Map<String, Double> SHORTEST_BASE_SECONDS = new LinkedHashMap<>();

    static {
        SHORTEST_BASE_SECONDS.put(GENETIC_RECOMBINATOR, 15.0);
        // Propagation takes 180 s (Strength 1) down to 30 s (Strength 10)
        SHORTEST_BASE_SECONDS.put(PROPAGATION, 30.0);
    }

    private static final Map<String, Double> speeds = new HashMap<>();
    private static double tickSeconds = 0.5;

    private MachineSpeed() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @return The speed multiplier of the machine, 1 when it isn't configured
     */
    public static double getSpeed(@Nonnull String id) {
        return speeds.getOrDefault(id, 1.0);
    }

    /**
     * @return The length of one Slimefun tick in real seconds
     */
    public static double getTickSeconds() {
        return tickSeconds;
    }

    /**
     * Converts a base time of the machine into Slimefun ticks at its speed: rounded to the nearest tick,
     * never less than one tick.
     */
    public static int toTicks(@Nonnull String id, double baseSeconds) {
        long ticks = Math.round(baseSeconds / getSpeed(id) / tickSeconds);
        return (int) Math.max(1, Math.min(ticks, Integer.MAX_VALUE));
    }

    /**
     * Loads {@code speed.yml}, creating it from the jar when it doesn't exist. Machines missing from the server's
     * file are added with speed 1, existing values are kept. Must be called before the machines are created.
     */
    public static void load(@Nonnull Cultivation plugin) {
        int tickRate = Math.max(1, Slimefun.getTickerTask().getTickRate());
        tickSeconds = tickRate / 20.0;

        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        // Only the server's file is read, so the jar's defaults never hide a missing entry
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        boolean changed = false;
        for (String id : getIds()) {
            if (!config.isSet(id)) {
                config.set(id, 1);
                changed = true;
            }
        }
        if (changed) {
            try {
                config.save(file);
            } catch (IOException e) {
                Cultivation.logWarning("Could not save " + FILE_NAME + ": " + e.getMessage());
            }
        }

        speeds.clear();
        for (String id : getIds()) {
            speeds.put(id, readSpeed(config, id));
        }
    }

    @Nonnull
    private static String[] getIds() {
        return new String[]{GARDEN_CLOCHE, GENETIC_RECOMBINATOR, PROPAGATION};
    }

    private static double readSpeed(@Nonnull FileConfiguration config, @Nonnull String id) {
        Object raw = config.get(id);
        Double speed = null;
        if (raw instanceof Number number) {
            speed = number.doubleValue();
        } else if (raw != null) {
            try {
                speed = Double.parseDouble(raw.toString().trim());
            } catch (NumberFormatException ignored) {
                // handled below
            }
        }
        if (speed == null || speed.isNaN() || speed.isInfinite() || speed <= 0) {
            Cultivation.logWarning(FILE_NAME + ": invalid " + id + ": " + raw + " (must be above 0). Using 1.");
            return 1;
        }

        double maxSpeed = getMaxSpeed(id);
        if (speed > maxSpeed) {
            Cultivation.logWarning(FILE_NAME + ": " + id + ": " + format(speed)
                + " is above the maximum " + format(maxSpeed) + ", using the maximum.");
            return maxSpeed;
        }
        return speed;
    }

    /**
     * The speed at which the shortest base time takes exactly one tick.
     */
    private static double getMaxSpeed(@Nonnull String id) {
        if (GARDEN_CLOCHE.equals(id)) {
            return GARDEN_CLOCHE_MAX_SPEED;
        }
        return SHORTEST_BASE_SECONDS.getOrDefault(id, tickSeconds) / tickSeconds;
    }

    /**
     * "2" for whole numbers, "1.5" / "2.25" otherwise.
     */
    @Nonnull
    public static String format(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        String text = String.format(Locale.ROOT, "%.2f", value);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
