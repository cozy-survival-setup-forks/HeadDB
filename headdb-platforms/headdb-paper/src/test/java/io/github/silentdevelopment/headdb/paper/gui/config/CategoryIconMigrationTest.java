package io.github.silentdevelopment.headdb.paper.gui.config;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryIconMigrationTest {

    private static Map<String, Object> icon(String name, String lore) {
        Map<String, Object> icon = new LinkedHashMap<>();
        icon.put("name", name);
        icon.put("lore", new ArrayList<>(List.of(lore)));
        return icon;
    }

    @Test
    void theOldDefaultsAreReplacedOnEveryCategoryIcon() {
        Map<String, Object> icons = new LinkedHashMap<>();
        icons.put("category", icon("<gold>Category", "<gray>Default category button icon settings."));
        icons.put("category.animals", icon("<gold>Category", "<gray>Default category button icon settings."));
        icons.put("category.food", icon("<aqua>Food", "<gray>Tasty."));
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("icons", icons);

        assertTrue(GuiConfigLoader.migrateCategoryIcons(root));

        assertEquals("<gold>%name%", ((Map<?, ?>) icons.get("category")).get("name"));
        assertEquals("<gold>%name%", ((Map<?, ?>) icons.get("category.animals")).get("name"));
        assertEquals(List.of(), ((Map<?, ?>) icons.get("category.animals")).get("lore"));
        assertEquals("<aqua>Food", ((Map<?, ?>) icons.get("category.food")).get("name"));
        assertEquals(List.of("<gray>Tasty."), ((Map<?, ?>) icons.get("category.food")).get("lore"));
        assertFalse(GuiConfigLoader.migrateCategoryIcons(root), "a second pass changes nothing");
    }
}
