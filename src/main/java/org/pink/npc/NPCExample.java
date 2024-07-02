package org.pink.npc;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.pink.npc.commands.NPCCommand;
import org.pink.npc.events.bukkit.PlayerEventListener;
import org.pink.npc.events.packet.PacketInteractEntity;

import java.util.*;

public class NPCExample extends JavaPlugin {
    public static final HashMap<UUID, List<NPCData>> playerNPCs = new HashMap<>();


    @Override
    public void onLoad() {
        instance = this;
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(true)
                .bStats(true);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketInteractEntity());
        Objects.requireNonNull(getCommand("npc")).setExecutor(new NPCCommand());
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
    }

    public static NPCExample instance;
}