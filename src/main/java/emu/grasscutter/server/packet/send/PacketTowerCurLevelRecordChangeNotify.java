package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.tower.record.TowerCurLevelRecordModel;
import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerCurLevelRecordChangeNotifyOuterClass.TowerCurLevelRecordChangeNotify;

public class PacketTowerCurLevelRecordChangeNotify extends GenshinPacket {
	
	public PacketTowerCurLevelRecordChangeNotify(TowerCurLevelRecordModel cur_level_record) {
		super(PacketOpcodes.TowerCurLevelRecordChangeNotify);

		TowerCurLevelRecordChangeNotify.Builder builder = TowerCurLevelRecordChangeNotify.newBuilder();
		
		builder.setCurLevelRecord(PacketTowerAllDataRsp.getTowerCurLevelRecordBuilder(cur_level_record));
		
		TowerCurLevelRecordChangeNotify proto = builder.build();
		
		this.setData(proto);
	}
}
