package org.pink.npc.commands;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.npc.NPC;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.Location;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pink.npc.NPCData;

import java.util.*;

import static org.pink.npc.NPCExample.playerNPCs;

public class NPCCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0 || !(sender instanceof Player player)) {
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "destroy":
                if (args.length > 1) {
                    try {
                        int entityId = Integer.parseInt(args[1]);
                        handleDestroy(player, entityId);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Укажите валидный entityID.");
                    }
                } else {
                    player.sendMessage("Укажите entityID.");
                }
                break;
            default:
                return true;
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        int entityId = SpigotReflectionUtil.generateEntityId();
        UUID entityUUID = UUID.randomUUID();

        NPC npc = createNpc(player, entityUUID, entityId);
        String Command = (args.length > 1 && !args[1].isEmpty()) ? args[1] : "paper version"; // Устанавливаем дефолтную команду, если игрок не предоставил свою
        playerNPCs.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(new NPCData(npc, entityId, Command, player.getWorld()));

        // Отобразим NPC всем игрокам на сервере
        for (Player p : Bukkit.getOnlinePlayers()) {
            com.github.retrooper.packetevents.protocol.player.User user = PacketEvents.getAPI().getPlayerManager().getUser(p);
            npc.spawn(user.getChannel());
        }

        player.sendMessage("Создан NPC с EntityID: " + entityId + ".");
    }

    /**
     * Данный метод установит все необходимые свойства нашему NPC
     */
    private static @NotNull NPC createNpc(Player player, UUID entityUUID, int entityId) {
        NPC npc = new NPC(
                new UserProfile(entityUUID, player.getName() + "L"), // Фейковый профиль
                entityId,
                GameMode.SURVIVAL, // Игровой режим NPC
                null, // Имя в табе
                NamedTextColor.WHITE, // Цвет ника
                null, // Префикс
                null // Суффикс
        );
        npc.setLocation(new Location(
                player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                player.getLocation().getYaw(), player.getLocation().getPitch()
        ));
        return npc;
    }

    private void handleDestroy(Player player, int entityId) {
        UUID playerUUID = player.getUniqueId();
        List<NPCData> npcs = playerNPCs.get(playerUUID);
        if (npcs == null) {
            return;
        }

        Iterator<NPCData> iterator = npcs.iterator();
        while (iterator.hasNext()) {
            NPCData npcData = iterator.next();
            if (npcData.entityId() == entityId) {
                com.github.retrooper.packetevents.protocol.player.User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                npcData.npc().despawn(user.getChannel());
                iterator.remove();
                player.sendMessage("NPC с EntityID: " + entityId + " уничтожено :}");
                return;
            }
        }

        player.sendMessage("NPC с EntityID " + entityId + " не найдено :{");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("npc")) {
            if (args.length == 1) {
                return Arrays.asList("create", "destroy");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("destroy") && sender instanceof Player player) {
                UUID playerUUID = player.getUniqueId();
                List<NPCData> npcs = playerNPCs.get(playerUUID);
                if (npcs != null) {
                    // Инициализируем массив для хранения всех NPC EntityID игрока
                    List<String> entityIds = new ArrayList<>();
                    for (NPCData npcData : npcs) {
                        entityIds.add(String.valueOf(npcData.entityId()));
                    }
                    return entityIds;
                }
            }
        }
        return new ArrayList<>();
    }
}