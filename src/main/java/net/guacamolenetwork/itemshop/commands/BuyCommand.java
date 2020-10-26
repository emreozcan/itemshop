package net.guacamolenetwork.itemshop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.guacamolenetwork.itemshop.Itemshop;
import net.guacamolenetwork.itemshop.classes.ItemValues;
import net.guacamolenetwork.itemshop.classes.Multiplier;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

@CommandPermission("itemshop.buy")
@CommandAlias("buy")
public class BuyCommand extends BaseCommand {

    Itemshop plugin;

    public BuyCommand(Itemshop plugin) {
        this.plugin = plugin;
    }

    private int getAvaliableSlots(Inventory inv){
        int empty = 0;
        for (ItemStack item: inv.getContents()) {
            if(item == null) {
                empty++;
            }
        }
        return empty;
    }

    @Subcommand("hand")
    @CommandPermission("itemshop.buy.hand")
    @Description("Buy items of the type you're currently holding")
    @CommandCompletion("1|32|64")
    public void onHand(Player player, @Default("1") Integer amount) {
        onMaterial(player, player.getInventory().getItemInMainHand().getType(), amount);
    }

    @Default
    @CommandPermission("itemshop.buy.material")
    @Description("Buy items of the type you specified")
    @CommandCompletion("@itemMaterials 1|32|64")
    public void onMaterial(Player player, Material material, @Default("1") Integer amount) {
        PlayerInventory inventory = player.getInventory();
        FileConfiguration config = plugin.getConfig();
        Multiplier buyMultiplier = Multiplier.buyBest(player, config);
        ItemValues itemValues = ItemValues.getFor(material, config);

        if (!itemValues.isBuyable()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis item cannot be bought."));
            return;
        }

        double buyPrice = itemValues.getBuyCost() * buyMultiplier.getMultiplier();
        double total = buyPrice * amount;
        if (Itemshop.getEconomy().has(player, total)) {
            int requiredSlots = (int) (Math.ceil((double) amount)/ ((double) material.getMaxStackSize()));
            int avaliableSlots = getAvaliableSlots(inventory);
            if (avaliableSlots >= requiredSlots) {
                HashMap<Integer, ItemStack> failedItems = inventory.addItem(new ItemStack(material, amount));

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                        "&aYou have been charged &c$%.2f&a for &c%d&a of &c%s&a.",
                        total, amount, material.name())));
                Itemshop.getEconomy().withdrawPlayer(player, total);

                if (!failedItems.isEmpty()) {
                    int totalItemsFailed = 0;
                    for (ItemStack stack : failedItems.values()) {
                        totalItemsFailed += stack.getAmount();
                    }
                    double refundPrice = buyPrice * totalItemsFailed;
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                            "&cError: Failed to give %d items. You will be refunded $%.2f.",
                            totalItemsFailed, refundPrice)));
                    Itemshop.getEconomy().depositPlayer(player, refundPrice);
                }

            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                        "&cYou need to have %d available inventory slots for this but you only have %d.",
                        requiredSlots, avaliableSlots)));
            }
        }
    }

    @Subcommand("multipliers")
    @CommandPermission("itemshop.multipliers")
    @Description("See active multipliers")
    public void onMultipliers(Player player) {
        Multiplier.listMultipliers(player, plugin);
    }

    @HelpCommand
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}