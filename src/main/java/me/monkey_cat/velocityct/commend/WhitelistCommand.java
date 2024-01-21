package me.monkey_cat.velocityct.commend;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import me.monkey_cat.velocityct.utils.Context;
import me.monkey_cat.velocityct.utils.MainCategory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class WhitelistCommand extends MainCategory {
    public WhitelistCommand(Context context) {
        super(context);
    }

    public static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static Optional<String> getString(final CommandContext<?> ctx, final String name) {
        try {
            return Optional.ofNullable(StringArgumentType.getString(ctx, name));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public static CompletableFuture<Suggestions> suggestMatching(Iterable<String> suggestions, SuggestionsBuilder suggestionsBuilder) {
        String remaining = suggestionsBuilder.getRemaining().toLowerCase();

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(remaining)) {
                suggestionsBuilder.suggest(suggestion);
            }
        }

        return suggestionsBuilder.buildFuture();
    }

    public void register(CommandManager commandManager) {
        var root = literal("whitelistct")
                .executes(this::showPluginInfo)
                .then(literal("groups")
                        .then(literal("list").executes(this::showGroups).then(argumentGroupName().executes(this::showGroups)))
                        .then(literal("create").then(argumentGroupName()))
                        .then(literal("delete").then(argumentGroupName()))
                        .then(literal("add").then(argumentGroupName().then(argumentGroupName())))
                        .then(literal("remove").then(argumentGroupName()).then(argumentServerName()))
                );
//                .then(
//                        literal("add")
//                ).then(
//                        literal("remove")
//                ).then(
//                        literal("enable")
//                ).then(
//                        literal("disable")
//                )
        commandManager.register(new BrigadierCommand(root.build()));
    }

    private RequiredArgumentBuilder<CommandSource, String> argumentGroupName() {
        return argument("groupName", word()).suggests(this::suggestGroups);
    }

    private CompletableFuture<Suggestions> suggestGroups(CommandContext<CommandSource> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(config.getGroups().keySet(), suggestions);
    }

    private RequiredArgumentBuilder<CommandSource, String> argumentServerName() {
        return argument("serverName", word()).suggests(this::suggestGroups);
    }

    private CompletableFuture<Suggestions> suggestServer(CommandContext<CommandSource> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(server.getAllServers().stream().map(s -> s.getServerInfo().getName()).toList(), suggestions);
    }

    private int showPluginInfo(CommandContext<CommandSource> ctx) {
//        ctx.getSource(Component.text(VelocityWhitelistMeta.NAME));
        final Component component = Component.text("Hello");
        return 0;
    }

    private int showGroups(CommandContext<CommandSource> ctx) {
        CommandSource source = ctx.getSource();
        Optional<String> groupName = getString(ctx, "groupName");
        List<String> serverNames = server.getAllServers().stream().map(s -> s.getServerInfo().getName()).toList();
        if (groupName.isPresent()) {
            Set<String> players = config.getGroups().getOrDefault(groupName.get(), null);
            if (players == null) {
                source.sendMessage(Component.translatable("velocityct.whitelist.groupNotFound").color(TextColor.color(0xff0000)));
                return -1;
            }

            int playerCount = players.size() - 1;
            List<String> playersList = players.stream().sorted().toList();
            Component message = Component.translatable("velocityct.whitelist.groupShowPlayers").color(TextColor.color(0x00AAAA));
            for (int i = 0; i <= playerCount; i++) {
                String serverName = playersList.get(i);
                message = message.append(Component.text(" " + serverName)
                        .color(TextColor.color(0xAAAAAA)));
                if (i < playerCount) {
                    message = message.append(Component.text(","));
                }
            }
            source.sendMessage(message);
        } else {
            source.sendMessage(Component.text(String.join(", ", config.getGroups().keySet())));
        }
        return 0;
    }
}
