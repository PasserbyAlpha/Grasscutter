package emu.grasscutter.server.packet.send;

import java.util.List;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.game.tower.TowerCurrentScheduleManager;
import emu.grasscutter.game.tower.TowerRecordManager;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;
import emu.grasscutter.game.tower.record.TowerScheduleRecord;
import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerBriefDataNotifyOuterClass.TowerBriefDataNotify;

public class PacketTowerBriefDataNotify extends GenshinPacket {
	
	public PacketTowerBriefDataNotify(GenshinPlayer player) {
		super(PacketOpcodes.TowerBriefDataNotify);

		TowerBriefDataNotify.Builder builder = TowerBriefDataNotify.newBuilder();
		
		TowerScheduleInfo current_schedule_info = TowerCurrentScheduleManager.get_current_tower_schedule();
		TowerScheduleRecord current_schedule_record = TowerRecordManager.getTowerScheduleRecord(player, current_schedule_info);
		
		builder.setTowerScheduleId(current_schedule_info.get_tower_schedule_id());
		builder.setNextScheduleChangeTime(current_schedule_info.next_schedule_change_time);
		
		
		builder.setIsFinishedEntranceFloor(true);
		builder.setScheduleStartTime(current_schedule_info.schedule_start_time);
		
		
		List<Integer> max_level_info = PacketTowerAllDataRsp.getMaxFloorLevel(current_schedule_record.tower_floor_record_list);
		if(max_level_info.get(0) > 0) {
			builder.setLastFloorIndex(max_level_info.get(0));
			builder.setLastLevelIndex(max_level_info.get(1));
			builder.setTotalStarNum(max_level_info.get(2));
		}
		
		TowerBriefDataNotify proto = builder.build();
		
		this.setData(proto);
	}
}
