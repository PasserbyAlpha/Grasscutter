package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonDataNotifyOuterClass.DungeonDataNotify;

public class PacketDungeonDataNotify extends GenshinPacket {
	
	public PacketDungeonDataNotify() {
		super(PacketOpcodes.DungeonDataNotify);

		DungeonDataNotify.Builder builder = DungeonDataNotify.newBuilder();

		// //TODO test only
		// builder.putDungeonDataMap(4, 1696);
		// builder.putDungeonDataMap(5, 1200);

		DungeonDataNotify proto = builder.build();
		
		this.setData(proto);
	}
}
