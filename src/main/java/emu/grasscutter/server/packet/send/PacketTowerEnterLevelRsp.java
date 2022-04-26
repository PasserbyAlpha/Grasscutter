package emu.grasscutter.server.packet.send;


import emu.grasscutter.game.tower.TowerProgressManager;
import emu.grasscutter.game.tower.record.TowerCurLevelRecordModel;
import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerEnterLevelRspOuterClass.TowerEnterLevelRsp;

public class PacketTowerEnterLevelRsp extends GenshinPacket {
	
	public PacketTowerEnterLevelRsp(TowerCurLevelRecordModel cur_level_record) {
		super(PacketOpcodes.TowerEnterLevelRsp);

		TowerEnterLevelRsp.Builder builder = TowerEnterLevelRsp.newBuilder();
		builder.setFloorId(cur_level_record.floor_id);
		builder.setLevelIndex(cur_level_record.level_idx);

		//update buff generate available one
		builder.addAllTowerBuffIdList(TowerProgressManager.get_available_buff_list(cur_level_record.buff_id_list));
		
		TowerEnterLevelRsp proto = builder.build();
		
		this.setData(proto);
	}
}
