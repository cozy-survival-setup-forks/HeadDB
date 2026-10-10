package io.github.silentdevelopment.headdb.paper.gui.common;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyTextTest {

    @Test
    void ampersandCodesBecomeTags() {
        assertEquals("<gold><bold>Animals<reset>", GuiItems.legacyToMini("&6&lAnimals&r"));
        assertEquals("<#ff8800>Animals", GuiItems.legacyToMini("&#ff8800Animals"));
    }

    @Test
    void anAmpersandInsideAWordIsLeftAlone() {
        assertEquals("R&B", GuiItems.legacyToMini("R&B"));
        assertEquals("Tom & Jerry", GuiItems.legacyToMini("Tom & Jerry"));
        assertEquals("&zzz", GuiItems.legacyToMini("&zzz"));
    }

    @Test
    void miniMessageStillWorks() {
        assertEquals("Animals", PlainTextComponentSerializer.plainText().serialize(GuiItems.mini("<gold>Animals")));
        assertEquals("Animals", PlainTextComponentSerializer.plainText().serialize(GuiItems.mini("&6Animals")));
    }

    @Test
    void titlesAreRedUnlessTheyChooseAColour() {
        assertEquals(net.kyori.adventure.text.format.NamedTextColor.RED, GuiTitles.title("Browse", false).color());
        assertEquals(net.kyori.adventure.text.format.NamedTextColor.GREEN, GuiTitles.title("<green>Browse", false).color());
    }

    @Test
    void titleDetailIsShownAsTyped() {
        String shown = PlainTextComponentSerializer.plainText().serialize(GuiTitles.title("Search", "<red>x", false));
        assertEquals("Search: <red>x", shown);
    }
}
