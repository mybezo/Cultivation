package dev.sefiraat.cultivation.implementation.utils;

import io.github.bakedlibs.dough.versions.SemanticVersion;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the server's Minecraft version.
 * dough and PaperLib only understand the old "1.x.y" scheme and fail on the newer "26.x" releases
 * (e.g. a Bukkit version of "26.3.build.249-stable"), so we parse it ourselves.
 */
public final class ServerVersion {

    /**
     * The oldest version this plugin runs on. Used when the version cannot be read at all.
     */
    private static final SemanticVersion MINIMUM = new SemanticVersion(1, 21, 3);
    private static final Pattern LEADING_VERSION = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    private static SemanticVersion cached;

    private ServerVersion() {
        throw new IllegalStateException("Utility class");
    }

    @Nonnull
    public static SemanticVersion get() {
        if (cached == null) {
            cached = parse(Bukkit.getBukkitVersion());
        }
        return cached;
    }

    @Nonnull
    static SemanticVersion parse(@Nonnull String bukkitVersion) {
        Matcher matcher = LEADING_VERSION.matcher(bukkitVersion);
        if (!matcher.find()) {
            return MINIMUM;
        }
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        return new SemanticVersion(major, minor, patch);
    }
}
