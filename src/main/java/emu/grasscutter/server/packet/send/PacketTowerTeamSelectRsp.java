package emu.grasscutter.server.packet.send;


import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerTeamSelectRspOuterClass.TowerTeamSelectRsp;

public class PacketTowerTeamSelectRsp extends GenshinPacket {
	
	public PacketTowerTeamSelectRsp() {
		super(PacketOpcodes.TowerTeamSelectRsp);

		TowerTeamSelectRsp.Builder builder = TowerTeamSelectRsp.newBuilder();
		TowerTeamSelectRsp proto = builder.build();
		
		this.setData(proto);
	}
}
