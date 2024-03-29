package jschars.itemshop.multiplier;

import jschars.itemshop.config.MultiplierConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SellMultiplier extends Multiplier {

    public static final SellMultiplier unit = new SellMultiplier(1, "");

    public SellMultiplier(double multiplier, String name) {
        super(multiplier, name);
    }

    private static SellMultiplier named(String multiplier, MultiplierConfig multiplierConfig) {
        YamlConfiguration config = multiplierConfig.getConfig();
        String valuePath = "modifiers."+multiplier+".sell";
        String namePath = "modifiers."+multiplier+".name";
        return new SellMultiplier(
                config.isDouble(valuePath) ? config.getDouble(valuePath) : 1,
                config.isString(namePath) ? config.getString(namePath) : ""
        );
    }

    public static SellMultiplier best(Player player, MultiplierConfig multiplierConfig) {
        YamlConfiguration config = multiplierConfig.getConfig();
        SellMultiplier selectedMultiplier = unit;
        ConfigurationSection modifiers = config.getConfigurationSection("modifiers");
        assert modifiers != null;
        for (String multiplier : modifiers.getKeys(false)) {
            if (player.hasPermission("itemshop.m."+multiplier)) {
                SellMultiplier tempMultiplier = named(multiplier, multiplierConfig);
                if (tempMultiplier.getMultiplier() > selectedMultiplier.getMultiplier()) {
                    // A higher sell multiplier is better than a lower one, so we select it only if it's higher.
                    selectedMultiplier = tempMultiplier;
                }
            }
        }
        return selectedMultiplier;
    }
}
