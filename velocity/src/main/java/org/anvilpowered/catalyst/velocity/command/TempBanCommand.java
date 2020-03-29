/*
 *     Copyright (C) 2020 STG_Allen
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anvilpowered.catalyst.velocity.command;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.text.TextComponent;
import org.anvilpowered.anvil.api.data.registry.Registry;
import org.anvilpowered.catalyst.api.data.key.CatalystKeys;
import org.anvilpowered.catalyst.api.member.MemberManager;
import org.anvilpowered.catalyst.api.plugin.PluginMessages;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TempBanCommand implements Command {

    @Inject
    private MemberManager<TextComponent> memberManager;

    @Inject
    private PluginMessages<TextComponent> pluginMessages;

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private Registry registry;

    @Override
    public void execute(CommandSource source, @NonNull String[] args) {
        if (!source.hasPermission(registry.getOrDefault(CatalystKeys.TEMP_BAN))) {
            source.sendMessage(pluginMessages.getNoPermission());
            return;
        }

        if (args.length < 2) {
            source.sendMessage(pluginMessages.getNotEnoughArgs());
            source.sendMessage(pluginMessages.tempBanCommandUsage());
            return;
        }
        String username = args[0];
        String duration = args[1];

        if (proxyServer.getPlayer(username).filter(p ->
            p.hasPermission(registry.getOrDefault(CatalystKeys.BAN_EXEMPT))).isPresent()) {
            source.sendMessage(pluginMessages.getBanExempt());
            return;
        }

        if (args.length == 2) {
            memberManager.tempBan(username, duration).thenAcceptAsync(source::sendMessage);
        } else {
            String reason = String.join(" ", args)
                .replace(username + " ", "").replace(duration, "");
            memberManager.tempBan(username, duration, reason).thenAcceptAsync(source::sendMessage);
        }
    }

    @Override
    public List<String> suggest(CommandSource src, String[] args) {
        if (args.length == 1) {
            return proxyServer.matchPlayer(args[0])
                .stream().map(Player::getUsername).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
