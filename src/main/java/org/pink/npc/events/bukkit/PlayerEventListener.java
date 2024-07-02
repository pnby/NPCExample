package org.pink.npc.events.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.pink.npc.NPCData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.pink.npc.NPCExample.playerNPCs;

public class PlayerEventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Map.Entry<UUID, List<NPCData>> entry : playerNPCs.entrySet()) {
            List<NPCData> npcList = entry.getValue();

            for (NPCData npcData : npcList) {
                npcData.npc().spawn(getUser(event.getPlayer()).getChannel()); // Спавним NPC для каждого новго игрока
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        for (Map.Entry<UUID, List<NPCData>> entry : playerNPCs.entrySet()) {
            List<NPCData> npcList = entry.getValue();

            for (NPCData npcData : npcList) {
                if (npcData.world() != event.getPlayer().getWorld()) return;
                // Конвертируем местоположение NPC в местоположение Bukkit
                org.bukkit.Location convertedNPCLocation = SpigotConversionUtil.toBukkitLocation(npcData.world(), npcData.npc().getLocation());
                float calculatedYaw = calculateYaw(event.getPlayer().getLocation(), convertedNPCLocation);
                // Создаем пакет для поворота головы
                WrapperPlayServerEntityHeadLook HeadLookPacket = new WrapperPlayServerEntityHeadLook(npcData.entityId(), calculatedYaw);
                // Создаем пакет для поворота тела
                WrapperPlayServerEntityRelativeMoveAndRotation RotationMovePacket = prepareRelativeMoveAndRotationPacket(npcData, convertedNPCLocation, calculatedYaw);
                // Отправляем пакеты игроку
                PacketEvents.getAPI().getPlayerManager().sendPacket(event.getPlayer(), RotationMovePacket);
                PacketEvents.getAPI().getPlayerManager().sendPacket(event.getPlayer(), HeadLookPacket);
            }
        }
    }

    private static @NotNull WrapperPlayServerEntityRelativeMoveAndRotation prepareRelativeMoveAndRotationPacket(NPCData npcData, Location convertedNPCLocation, float calculatedYaw) {
        double deltaX = (npcData.npc().getLocation().getX() - convertedNPCLocation.getX()) * 32 * 128;
        double deltaY = (npcData.npc().getLocation().getY() - convertedNPCLocation.getY()) * 32 * 128;
        double deltaZ = (npcData.npc().getLocation().getZ() - convertedNPCLocation.getZ()) * 32 * 128;
        return new WrapperPlayServerEntityRelativeMoveAndRotation(
                npcData.entityId(),
                (short) deltaX,
                (short) deltaY,
                (short) deltaZ,
                calculatedYaw, npcData.npc().getLocation().getPitch(), true);
    }

    private static User getUser(Player player) {
        return PacketEvents.getAPI().getPlayerManager().getUser(player);
    }

    /**
     * Вычисляет yaw в градусах от местоположения игрока к местоположению NPC.
     */
    private static float calculateYaw(org.bukkit.Location playerLocation, org.bukkit.Location npcLocation) {
        double deltaX = playerLocation.getX() - npcLocation.getX();
        double deltaZ = playerLocation.getZ() - npcLocation.getZ();
        return (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
    }
}