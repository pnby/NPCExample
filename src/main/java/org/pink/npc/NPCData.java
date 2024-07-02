package org.pink.npc;

import com.github.retrooper.packetevents.protocol.npc.NPC;
import org.bukkit.World;

public record NPCData(NPC npc, int entityId, String command, World world) {}
