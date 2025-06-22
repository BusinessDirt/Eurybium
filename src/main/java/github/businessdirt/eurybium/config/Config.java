package github.businessdirt.eurybium.config;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import github.businessdirt.eurybium.Eurybium;
import github.businessdirt.eurybium.utils.Reference;
import github.businessdirt.eurybium.utils.Scheduler;

import java.io.File;

public class Config extends Vigilant {

    private static Config instance = null;

    @Property(
            type = PropertyType.CHECKBOX,
            name = "Checkbox",
            description = "This is a checkbox property. It stores a boolean value.",
            category = "Property Overview"
    )
    boolean demoCheckbox = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Switch",
            description = "This is a switch property. It stores a boolean value.",
            category = "Property Overview"
    )
    boolean demoSwitch = false;

    @Property(
            type = PropertyType.TEXT,
            name = "Text",
            description = "This is a text property. It stores a single line of continuous text.",
            category = "Property Overview"
    )
    String demoText = "";

    @Property(
            type = PropertyType.PARAGRAPH,
            name = "Paragraph",
            description = "This is a paragraph property. It stores a multi-line piece of text, and expands as the user writes more text",
            category = "Property Overview"
    )
    String demoParagraph = "";

    private Config() {
        super(new File("./config/" + Reference.MOD_ID + "/config.toml"));
        initialize();
    }

    public static void init() {
        if (Config.instance == null)
            Config.instance = new Config();
    }

    public static Config get() {
        if (Config.instance == null)
            Eurybium.LOGGER.error("Config has not been initialized, call init() first.");
        return Config.instance;
    }

    public void display() {
        Scheduler.queueOpenScreen(this.gui());
    }
}
