package me.monkey_cat.velocityct.commend;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import me.monkey_cat.velocityct.VelocityWhitelistMeta;
import me.monkey_cat.velocityct.utils.Context;
import me.monkey_cat.velocityct.utils.MainCategory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static me.monkey_cat.velocityct.utils.Utils.getAllPlayersName;
import static me.monkey_cat.velocityct.utils.Utils.getAllServersName;

public class WhitelistCommand extends MainCategory {
    static public final String GROUP_NAME = "GroupName";
    static public final String PLAYER_NAME = "PlayerName";
    static public final String SERVER_NAME = "ServerName";

    public WhitelistCommand(Context context) {
        super(context);
    }

    public static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static Optional<String> getStringOpt(final CommandContext<?> ctx, final String name) {
        try {
            return Optional.ofNullable(getString(ctx, name));
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
                .requires(s -> s.hasPermission(VelocityWhitelistMeta.ID + ".command"))
                .executes(this::showPluginInfo)
                .then(literal("groups")
                        .then(literal("list").executes(this::showGroups)
                                .then(argumentGroupName().executes(this::showGroups).then(literal("players").executes(this::showGroupPlayers))))

                        .then(literal("create").executes(missingArg(GROUP_NAME))
                                .then(argument(GROUP_NAME, word()).executes(this::createGroup)))

                        .then(literal("delete").executes(missingArg(GROUP_NAME))
                                .then(argumentGroupName().executes(this::deleteGroup)))

                        .then(literal("add").executes(missingArg(GROUP_NAME))
                                .then(argumentGroupName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::addGroupWhitelist))))

                        .then(literal("remove").executes(missingArg(GROUP_NAME))
                                .then(argumentGroupName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::removeGroupWhitelist))))
                )
                .then(literal("add").executes(missingArg(SERVER_NAME))
                        .then(argumentServerName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::addServerWhitelist))))
                .then(literal("remove").executes(missingArg(SERVER_NAME))
                        .then(argumentServerName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::removeServerWhitelist))))
                .then(literal("on").executes(this::setEnable))
                .then(literal("off").executes(this::setDisable));
        commandManager.register(new BrigadierCommand(root.build()));
    }

    private RequiredArgumentBuilder<CommandSource, String> argumentGroupName() {
        return argument(GROUP_NAME, word()).suggests(this::suggestGroups);
    }

    private CompletableFuture<Suggestions> suggestGroups(CommandContext<CommandSource> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(config.getGroups().keySet(), suggestions);
    }

    private RequiredArgumentBuilder<CommandSource, String> argumentPlayerName() {
        return argument(PLAYER_NAME, word()).suggests(this::suggestPlayers);
    }

    private CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSource> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(getAllPlayersName(server), suggestions);
    }

    private RequiredArgumentBuilder<CommandSource, String> argumentServerName() {
        return argument(SERVER_NAME, word()).suggests(this::suggestServer);
    }

    private CompletableFuture<Suggestions> suggestServer(CommandContext<CommandSource> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(getAllServersName(server), suggestions);
    }

    private int showPluginInfo(CommandContext<CommandSource> ctx) {
//        ctx.getSource(Component.text(VelocityWhitelistMeta.NAME));
        final Component component = Component.text("Hello");
        return 0;
    }


    private Command<CommandSource> missingArg(String arg) {
        return ctx -> missingArg(arg, ctx);
    }

    private int missingArg(String arg, CommandContext<CommandSource> ctx) {
        ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.missingArgument", NamedTextColor.RED, Component.text(arg)));
        return 0;
    }

    private int setEnable(CommandContext<CommandSource> ctx) {
        config.setWhitelistEnable(true);
        ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.enable"));
        return 0;
    }

    private int setDisable(CommandContext<CommandSource> ctx) {
        config.setWhitelistEnable(true);
        ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.disable"));
        return 0;
    }

    private int createGroup(CommandContext<CommandSource> ctx) {
        CommandSource source = ctx.getSource();
        Optional<String> groupNameOpt = getStringOpt(ctx, GROUP_NAME);
        if (groupNameOpt.isPresent()) {
            String groupName = groupNameOpt.get();
            Map<String, Set<String>> groups = config.getGroups();
            if (groups.containsKey(groupName)) {
                source.sendMessage(Component.translatable("velocityct.whitelist.groupAlreadyExists", NamedTextColor.RED, Component.text(groupName)));
                return -1;
            }
            groups.put(groupName, new HashSet<>());
            config.setGroups(groups);
            source.sendMessage(Component.translatable("velocityct.whitelist.groupCreateCompleted", Component.text(groupName)));
        }
        return 0;
    }

    private int deleteGroup(CommandContext<CommandSource> ctx) {
        CommandSource source = ctx.getSource();
        String groupName = getString(ctx, GROUP_NAME);
        Map<String, Set<String>> groups = config.getGroups();
        if (!groups.containsKey(groupName)) {
            source.sendMessage(Component.translatable("velocityct.whitelist.groupNotFound", NamedTextColor.RED, Component.text(groupName)));
            return -1;
        }
        groups.remove(groupName);
        config.setGroups(groups);
        source.sendMessage(Component.translatable("velocityct.whitelist.groupDeleteCompleted", Component.text(groupName)));
        return 0;
    }

    private int addGroupWhitelist(CommandContext<CommandSource> ctx) {
        String groupName = getString(ctx, GROUP_NAME);
        String playerName = getString(ctx, PLAYER_NAME);

        Map<String, Set<String>> whitelist = config.getWhitelist();
        Set<String> players = whitelist.getOrDefault(groupName, new HashSet<>());
        players.add(playerName);
        whitelist.put(groupName, players);
        config.setWhitelist(whitelist);

        ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.groupAddPlayerCompleted",
                Component.text(groupName), Component.text(playerName)));
        return 0;
    }

    private int removeGroupWhitelist(CommandContext<CommandSource> ctx) {
        String groupName = getString(ctx, GROUP_NAME);
        String playerName = getString(ctx, PLAYER_NAME);

        Map<String, Set<String>> whitelist = config.getWhitelist();
        Set<String> players = whitelist.getOrDefault(groupName, new HashSet<>());
        players.remove(playerName);
        whitelist.put(groupName, players);
        config.setWhitelist(whitelist);

        ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.groupRemovePlayerCompleted",
                Component.text(groupName), Component.text(playerName)));
        return 0;
    }

    private int addServerWhitelist(CommandContext<CommandSource> ctx) {
        String serverName = getString(ctx, SERVER_NAME);
        String playerName = getString(ctx, PLAYER_NAME);

        Map<String, Set<String>> specialWhitelist = config.getSpecialWhitelist();
        Set<String> servers = specialWhitelist.getOrDefault(playerName, new HashSet<>());
        servers.add(serverName);
        specialWhitelist.put(playerName, servers);
        config.setSpecialWhitelist(specialWhitelist);
        ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.serverAddPlayerCompleted",
                Component.text(serverName), Component.text(playerName)));

        return 0;
    }

    private int removeServerWhitelist(CommandContext<CommandSource> ctx) {
        String serverName = getString(ctx, SERVER_NAME);
        String playerName = getString(ctx, PLAYER_NAME);

        Map<String, Set<String>> specialWhitelist = config.getSpecialWhitelist();
        Set<String> servers = specialWhitelist.getOrDefault(playerName, new HashSet<>());
        servers.remove(serverName);

        if (config.hasInWhitelist(serverName, playerName)) {
            servers.add("!" + serverName);
        }
        if (servers.isEmpty()) specialWhitelist.remove(playerName);
        else specialWhitelist.put(playerName, servers);
        config.setSpecialWhitelist(specialWhitelist);

        ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.serverRemovePlayerCompleted",
                Component.text(serverName), Component.text(playerName)));
        return 0;
    }

    private int showGroups(CommandContext<CommandSource> ctx) {
        CommandSource source = ctx.getSource();
        Optional<String> groupNameOpt = getStringOpt(ctx, GROUP_NAME);
        if (groupNameOpt.isPresent()) {
            String groupName = groupNameOpt.get();
            Set<String> groups = config.getGroups().getOrDefault(groupName, null);
            if (groups == null) {
                source.sendMessage(Component.translatable("velocityct.whitelist.groupNotFound", NamedTextColor.RED));
                return -1;
            }

            int serverCount = groups.size() - 1;
            List<String> serverNames = getAllServersName(server);
            List<String> serverList = groups.stream().sorted().toList();
            Component message = Component.translatable("velocityct.whitelist.groupShowServers", NamedTextColor.DARK_AQUA, Component.text(groupName));
            for (int i = 0; i <= serverCount; i++) {
                String serverName = serverList.get(i);
                Component tmp = Component.text(" " + serverName, NamedTextColor.GRAY);
                if (!serverNames.contains(serverName)) {
                    tmp = tmp.color(NamedTextColor.RED).hoverEvent(
                            HoverEvent.showText(Component.translatable("velocityct.serverInvalid", NamedTextColor.RED, Component.text(serverName)))
                    );
                }
                message = message.append(tmp);
                if (i < serverCount) {
                    message = message.append(Component.text(",", NamedTextColor.GRAY));
                }
            }
            source.sendMessage(message);
        } else {
            List<String> groupsList = config.getGroups().keySet().stream().sorted().toList();
            Component message = Component.translatable("velocityct.whitelist.groupShow", NamedTextColor.DARK_AQUA);
            ctx.getSource().sendMessage(message.append(Component.text(" " + String.join(", ", groupsList), NamedTextColor.GRAY)));
        }
        return 0;
    }

    private int showGroupPlayers(CommandContext<CommandSource> ctx) {
        Optional<String> groupNameOpt = getStringOpt(ctx, GROUP_NAME);

        if (groupNameOpt.isPresent()) {
            String groupName = groupNameOpt.get();
            Set<String> players = config.getWhitelist().getOrDefault(groupName, null);

            if (players == null) {
                ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.groupNotFound", NamedTextColor.RED));
                return -1;
            }

            Component message = Component.translatable("velocityct.whitelist.groupShowPlayers", NamedTextColor.DARK_AQUA, Component.text(groupName));
            List<String> playersList = players.stream().sorted().toList();
            ctx.getSource().sendMessage(message.append(Component.text(" " + String.join(", ", playersList), NamedTextColor.GRAY)));
            return 0;
        }

        ctx.getSource().sendMessage(Component.translatable("velocityct.whitelist.groupNotFound", NamedTextColor.RED));
        return -1;
    }
}
