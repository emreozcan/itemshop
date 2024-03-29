package jschars.itemshop.multiplier;

import jschars.itemshop.config.MultiplierConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BuyMultiplier extends Multiplier {

    public static final BuyMultiplier unit = new BuyMultiplier(1, "");

    public BuyMultiplier(double multiplier, String name) {
        super(multiplier, name);
    }

    private static BuyMultiplier named(String multiplier, MultiplierConfig multiplierConfig) {
        YamlConfiguration config = multiplierConfig.getConfig();
        String valuePath = "modifiers."+multiplier+".buy";
        String namePath = "modifiers."+multiplier+".name";
        return new BuyMultiplier(
                config.isDouble(valuePath) ? config.getDouble(valuePath) : 1,
                config.isString(namePath) ? config.getString(namePath) : ""
        );
    }

    public static BuyMultiplier best(Player player, MultiplierConfig multiplierConfig) {
        YamlConfiguration config = multiplierConfig.getConfig();
        BuyMultiplier selectedMultiplier = unit;
        ConfigurationSection modifiers = config.getConfigurationSection("modifiers");
        assert modifiers != null;
        for (String multiplier : modifiers.getKeys(false)) {
            if (player.hasPermission("itemshop.m."+multiplier)) {
                BuyMultiplier tempMultiplier = named(multiplier, multiplierConfig);
                if (tempMultiplier.getMultiplier() < selectedMultiplier.getMultiplier()) {
                    // A lower buy multiplier is better than a higher one, so we select it only if it's lower.
                    selectedMultiplier = tempMultiplier;
                }
            }
        }
        return selectedMultiplier;
    }
}
