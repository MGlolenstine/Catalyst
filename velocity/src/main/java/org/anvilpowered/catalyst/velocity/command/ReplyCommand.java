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
import net.kyori.text.format.TextColor;
import org.anvilpowered.anvil.api.data.registry.Registry;
import org.anvilpowered.anvil.api.plugin.PluginInfo;
import org.anvilpowered.catalyst.api.service.PrivateMessageService;
import org.anvilpowered.catalyst.api.data.key.CatalystKeys;
import org.anvilpowered.catalyst.api.plugin.PluginMessages;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.UUID;

public class ReplyCommand implements Command {

    @Inject
    private PluginInfo<TextComponent> pluginInfo;

    @Inject
    private PluginMessages<TextComponent> pluginMessages;

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private Registry registry;

    @Inject
    private PrivateMessageService<TextComponent> privateMessageService;

    @Override
    public void execute(CommandSource source, @NonNull String[] args) {
        if (!source.hasPermission(registry.getOrDefault(CatalystKeys.MESSAGE))) {
            source.sendMessage(pluginMessages.getNoPermission());
            return;
        }

        if (args.length == 0) {
            source.sendMessage(pluginMessages.getNotEnoughArgs());
            return;
        }

        if (source instanceof Player) {
            String message = String.join(" ", args);
            Player sender = (Player) source;
            UUID senderUUID = sender.getUniqueId();

            if (privateMessageService.replyMap().containsKey(senderUUID)) {
                UUID recipientUUID = privateMessageService.replyMap().get(senderUUID);
                Optional<Player> recipient = proxyServer.getPlayer(recipientUUID);

                if (recipient.isPresent()) {
                    privateMessageService.sendMessage(
                        sender.getUsername(), recipient.get().getUsername(), message);
                    privateMessageService.replyMap().put(recipientUUID, senderUUID);
                } else {
                    source.sendMessage(
                        pluginInfo.getPrefix().append(
                            TextComponent.of("Invalid of offline player!").color(TextColor.RED)));
                }
            } else {
                source.sendMessage(
                    pluginInfo.getPrefix().append(
                        TextComponent.of("Nobody to reply to!").color(TextColor.RED)));
            }
        }
    }
}
