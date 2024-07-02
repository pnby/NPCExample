package org.pink.npc.events.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.pink.npc.NPCData;
import org.pink.npc.NPCExample;

import java.util.List;

import static org.pink.npc.NPCExample.playerNPCs;


public class PacketInteractEntity extends PacketListenerAbstract {
    /**
     * Вызывается когда клиент отправляет INTERACT_ENTITY пакет
     */
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            Player player = (Player) SpigotReflectionUtil.getCraftPlayer((Player) event.getPlayer()); // Получаем Bukkit Player`а
            int clickedEntityId = packet.getEntityId(); // Получаем EntityID сущности на которую кликнул игрок

            for (List<NPCData> npcList : playerNPCs.values()) {
                for (NPCData npcData : npcList) {
                    if (npcData.entityId() == clickedEntityId) { // Проверяем, кликнул ли игрок на нужную сущность
                        Bukkit.getScheduler().runTask(
                                NPCExample.instance,
                                () -> player.performCommand(npcData.command())); // С помощью планировщика Bukkit запускаем задачу в основном потоке через тик
                        return;
                    }
                }
            }
        }
    }
}