package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerBuffSelectRspOuterClass.TowerBuffSelectRsp;

public class PacketTowerBuffSelectRsp extends GenshinPacket {

	public PacketTowerBuffSelectRsp(int server_buff_id) {
		super(PacketOpcodes.TowerBuffSelectRsp);

		TowerBuffSelectRsp.Builder builder = TowerBuffSelectRsp.newBuilder();
		builder.setTowerBuffId(server_buff_id);

		TowerBuffSelectRsp proto = builder.build();
		
		this.setData(proto);
	}
}
