package io.github.silentdevelopment.headdb.paper.gui.common;

import io.github.silentdevelopment.grafik.gui.GuiContext;
import io.github.silentdevelopment.grafik.key.GKey;
import io.github.silentdevelopment.grafik.paper.core.element.ItemElement;
import io.github.silentdevelopment.grafik.paper.page.PaperPageBuilder;
import io.github.silentdevelopment.headdb.paper.HeadDBPlugin;
import io.github.silentdevelopment.headdb.paper.gui.config.GuiFillerConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class GuiItems {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private GuiItems() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static <C> void fillBorders(@NotNull HeadDBPlugin plugin, @NotNull PaperPageBuilder<C> page, int rows) {
        fillBorders(plugin, page, rows, Set.of());
    }

    public static <C> void fillBorders(@NotNull HeadDBPlugin plugin, @NotNull PaperPageBuilder<C> page, int rows, @NotNull Set<Integer> reservedSlots) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(reservedSlots, "reservedSlots");

        GuiFillerConfig filler = plugin.guiConfig().filler();

        if (!filler.enabled()) {
            return;
        }

        if (rows < 1) {
            return;
        }

        ItemStack item = fillerItem(filler);

        for (int slot : borderSlots(rows)) {
            if (reservedSlots.contains(slot)) {
                continue;
            }

            page.set(slot, ItemElement.<C>of(GKey.of("border_" + slot), ignored -> item.clone(), ignored -> {}));
        }
    }

    public static <C> void fillEmpty(@NotNull HeadDBPlugin plugin, @NotNull PaperPageBuilder<C> page, int rows, @NotNull Set<Integer> reservedSlots) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(reservedSlots, "reservedSlots");

        GuiFillerConfig filler = plugin.guiConfig().filler();

        if (!filler.enabled()) {
            return;
        }

        if (rows < 1) {
            return;
        }

        ItemStack item = fillerItem(filler);
        int size = rows * 9;

        for (int slot = 0; slot < size; slot++) {
            if (reservedSlots.contains(slot)) {
                continue;
            }

            page.set(slot, ItemElement.<C>of(GKey.of("fill_" + slot), ignored -> item.clone(), ignored -> {}));
        }
    }

    public static <C> @NotNull ItemElement<C> button(@NotNull String key, @NotNull Material material, @NotNull Component name, @NotNull List<Component> lore, @NotNull Consumer<GuiContext<C>> action) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(lore, "lore");
        Objects.requireNonNull(action, "action");

        return ItemElement.<C>of(GKey.of(key), ignored -> item(material, name, lore), action);
    }

    public static @NotNull ItemStack item(@NotNull Material material, @NotNull Component name, @NotNull List<Component> lore) {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(lore, "lore");

        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            meta.lore(cleanLore(lore));
        });

        return item;
    }

    public static @NotNull Component name(@NotNull String value, @NotNull NamedTextColor color) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(color, "color");

        return Component.text(value, color).decoration(TextDecoration.ITALIC, false);
    }

    public static @NotNull Component lore(@NotNull String value, @NotNull NamedTextColor color) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(color, "color");

        return Component.text(value, color).decoration(TextDecoration.ITALIC, false);
    }


    public static @NotNull Component detail(@NotNull String key, @NotNull Object value, @NotNull NamedTextColor valueColor) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(valueColor, "valueColor");

        return Component.text(key + ": ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(value), valueColor))
                .decoration(TextDecoration.ITALIC, false);
    }

    public static @NotNull Component idDetail(@NotNull String key, @NotNull Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        return detail(key, value, NamedTextColor.GOLD);
    }

    public static @NotNull Component metaDetail(@NotNull String key, @NotNull Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        return detail(key, value, NamedTextColor.GOLD);
    }

    public static @NotNull Component mini(@NotNull String value) {
        Objects.requireNonNull(value, "value");
        return MINI_MESSAGE.deserialize(legacyToMini(value)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Lets the old &amp;-codes work next to MiniMessage tags: "&amp;6Animals" and "&amp;#ff8800Animals" are turned into
     * their tag form. A "&amp;" that sits inside a word ("R&amp;B") or is not followed by a code is left alone.
     */
    static @NotNull String legacyToMini(@NotNull String value) {
        if (value.indexOf('&') < 0) {
            return value;
        }

        StringBuilder out = new StringBuilder(value.length() + 16);
        int afterCode = -1;

        for (int index = 0; index < value.length(); index++) {
            char c = value.charAt(index);
            if (c != '&' || index + 1 >= value.length()) {
                out.append(c);
                continue;
            }

            char code = Character.toLowerCase(value.charAt(index + 1));

            // "R&B" is a word, "Animals&r" is a reset: inside a word only a format code at the end of it counts
            boolean insideWord = index > 0 && index != afterCode && Character.isLetterOrDigit(value.charAt(index - 1));

            if (insideWord && !(isFormat(code) && endsWord(value, index + 2))) {
                out.append(c);
                continue;
            }

            if (code == '#' && isHex(value, index + 2, 6)) {
                out.append("<#").append(value, index + 2, index + 8).append('>');
                index += 7;
                afterCode = index + 1;
                continue;
            }

            String tag = legacyTag(code);

            if (tag == null) {
                out.append(c);
                continue;
            }

            out.append(tag);
            index++;
            afterCode = index + 1;
        }

        return out.toString();
    }

    private static boolean isFormat(char code) {
        return code == 'r' || (code >= 'k' && code <= 'o');
    }

    private static boolean endsWord(@NotNull String value, int index) {
        return index >= value.length() || value.charAt(index) == '&' || Character.isWhitespace(value.charAt(index));
    }

    private static boolean isHex(@NotNull String value, int from, int length) {
        if (from + length > value.length()) {
            return false;
        }

        for (int index = from; index < from + length; index++) {
            if (Character.digit(value.charAt(index), 16) < 0) {
                return false;
            }
        }

        return true;
    }

    private static String legacyTag(char code) {
        return switch (code) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> null;
        };
    }


    public static @NotNull Component miniOrWhite(@NotNull String value) {
        Objects.requireNonNull(value, "value");

        if (value.contains("<")) {
            return mini(value);
        }

        return Component.text(value, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);
    }

    public static @NotNull List<Component> miniLore(@NotNull List<String> lines) {
        Objects.requireNonNull(lines, "lines");

        List<Component> result = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            result.add(mini(line));
        }

        return List.copyOf(result);
    }

    private static @NotNull ItemStack fillerItem(@NotNull GuiFillerConfig filler) {
        Material material = GuiMaterials.itemOr(filler.material(), Material.BLACK_STAINED_GLASS_PANE);
        return item(material, mini(filler.name()), miniLore(filler.lore()));
    }

    private static @NotNull List<Integer> borderSlots(int rows) {
        int size = rows * 9;
        List<Integer> slots = new ArrayList<>();

        for (int slot = 0; slot < size; slot++) {
            int row = slot / 9;
            int column = slot % 9;

            if (row != 0 && row != rows - 1 && column != 0 && column != 8) {
                continue;
            }

            slots.add(slot);
        }

        return List.copyOf(slots);
    }

    private static @NotNull List<Component> cleanLore(@NotNull List<Component> lore) {
        Objects.requireNonNull(lore, "lore");

        List<Component> result = new ArrayList<>();

        for (Component line : lore) {
            result.add(line.decoration(TextDecoration.ITALIC, false));
        }

        return List.copyOf(result);
    }
}