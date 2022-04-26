package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonWayPointNotifyOuterClass.DungeonWayPointNotify;

public class PacketDungeonWayPointNotify extends GenshinPacket {
	
	public PacketDungeonWayPointNotify() {
		super(PacketOpcodes.TowerBriefDataNotify);

		DungeonWayPointNotify.Builder builder = DungeonWayPointNotify.newBuilder();
		DungeonWayPointNotify proto = builder.build();
		
		this.setData(proto);
	}
}
