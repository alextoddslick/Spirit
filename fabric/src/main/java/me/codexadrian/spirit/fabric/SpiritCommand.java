package me.codexadrian.spirit.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.codexadrian.spirit.Corrupted;
import me.codexadrian.spirit.data.Tier;
import me.codexadrian.spirit.registry.SpiritItems;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Registers the {@code /spirit} command (op-only):
 * <ul>
 *   <li>{@code /spirit kill} – removes every mob spawned by a soul cage / pedestal (the
 *       {@link Corrupted} marker) across all dimensions.</li>
 *   <li>{@code /spirit witherSkele <tier>} – gives the player a wither-skeleton soul crystal set to
 *       the given tier's soul threshold (debug).</li>
 * </ul>
 */
public final class SpiritCommand {

    private static final String WITHER_SKELETON = "minecraft:wither_skeleton";

    private SpiritCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spirit")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("kill")
                        .executes(SpiritCommand::killCageSpawns))
                .then(Commands.literal("witherSkele")
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1))
                                .executes(ctx -> giveWitherSkeleCrystal(ctx,
                                        IntegerArgumentType.getInteger(ctx, "tier"))))));
    }

    private static int killCageSpawns(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int killed = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            // Collect first: killing mutates the live entity map, so killing mid-iteration would throw CME.
            List<Entity> toKill = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Corrupted corrupted && corrupted.isCorrupted()) {
                    toKill.add(entity);
                }
            }
            for (Entity entity : toKill) {
                entity.kill();
            }
            killed += toKill.size();
        }
        final int total = killed;
        source.sendSuccess(() -> Component.literal("Killed " + total + " soul cage spawn(s)."), true);
        return total;
    }

    private static int giveWitherSkeleCrystal(CommandContext<CommandSourceStack> context, int tier)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        List<Tier> tiers = new ArrayList<>(Tier.getTiers(level));
        if (tiers.isEmpty()) {
            source.sendFailure(Component.literal("No soul cage tiers are loaded."));
            return 0;
        }
        tiers.sort(Comparator.comparingInt(Tier::requiredSouls));
        int index = Math.min(tier, tiers.size()) - 1;
        int souls = Math.max(1, tiers.get(index).requiredSouls());

        ItemStack crystal = new ItemStack(SpiritItems.SOUL_CRYSTAL.get());
        SoulUtils.deviateSoulCount(crystal, souls, level, WITHER_SKELETON);
        player.getInventory().placeItemBackInInventory(crystal);

        final int clampedTier = index + 1;
        final int finalSouls = souls;
        source.sendSuccess(() -> Component.literal(
                "Gave a Tier " + clampedTier + " wither skeleton soul crystal (" + finalSouls + " souls)."), false);
        return 1;
    }
}
