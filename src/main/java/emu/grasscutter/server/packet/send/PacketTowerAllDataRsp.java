package emu.grasscutter.server.packet.send;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.game.tower.TowerCurrentScheduleManager;
import emu.grasscutter.game.tower.TowerRecordManager;
import emu.grasscutter.game.tower.info.FloorInfo;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;
import emu.grasscutter.game.tower.record.TowerScheduleRecord;
import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerAllDataRspOuterClass.TowerAllDataRsp;
import emu.grasscutter.net.proto.TowerCurLevelRecordOuterClass.TowerCurLevelRecord;
import emu.grasscutter.net.proto.TowerFloorRecordOuterClass.TowerFloorRecord;
import emu.grasscutter.net.proto.TowerLevelRecordOuterClass.TowerLevelRecord;
import emu.grasscutter.net.proto.TowerMonthlyBriefOuterClass.TowerMonthlyBrief;

public class PacketTowerAllDataRsp extends GenshinPacket {
	
	public PacketTowerAllDataRsp(GenshinPlayer player) {
		super(PacketOpcodes.TowerAllDataRsp);
		
		TowerAllDataRsp.Builder builder = TowerAllDataRsp.newBuilder();
		
		//load current schedule info
		TowerScheduleInfo current_schedule_info = TowerCurrentScheduleManager.get_current_tower_schedule();
		builder.setTowerScheduleId(current_schedule_info.get_tower_schedule_id());
		
		//load current schedule record
		TowerScheduleRecord current_schedule_record = TowerRecordManager.getTowerScheduleRecord(player, current_schedule_info);
		builder.addAllTowerFloorRecordList(genFloorRecord(current_schedule_record.tower_floor_record_list));
		
		//TODO load current level record for continue
		builder.setCurLevelRecord(TowerCurLevelRecord.newBuilder().setIsEmpty(true));
		
		//add schedule info
		builder.setNextScheduleChangeTime(current_schedule_info.next_schedule_change_time);
		builder.putAllFloorOpenTimeMap(current_schedule_info.floor_open_time_map);
		
		//get max floor and level
		List<Integer> max_level_info = getMaxFloorLevel(current_schedule_record.tower_floor_record_list);
		if(max_level_info.get(0) > 0) {
			builder.setMonthlyBrief(TowerMonthlyBrief.newBuilder()
					.setTowerScheduleId(current_schedule_info.get_tower_schedule_id())
					.setBestFloorIndex(max_level_info.get(0))
					.setBestLevelIndex(max_level_info.get(1))
					.setTotalStarCount(max_level_info.get(2))
					);
		}

		//TODO figure out the meaning of the boolean
		builder.setIsFinishedEntranceFloor(true);
		
		//schedule start time
		builder.setScheduleStartTime(current_schedule_info.schedule_start_time);
		
		//last month record
		TowerScheduleInfo prev_schedule_info = current_schedule_info.previous_tower_schedule_info;
		if(prev_schedule_info != null) {
			TowerScheduleRecord prev_schedule_record = TowerRecordManager.getTowerScheduleRecord(player, prev_schedule_info, false);
			if(prev_schedule_record != null) {
				List<Integer> prev_max_level_info = getMaxFloorLevel(prev_schedule_record.tower_floor_record_list);
				if(max_level_info.get(0) > 0) {
					builder.setMonthlyBrief(TowerMonthlyBrief.newBuilder()
							.setTowerScheduleId(prev_schedule_info.get_tower_schedule_id())
							.setBestFloorIndex(prev_max_level_info.get(0))
							.setBestLevelIndex(prev_max_level_info.get(1))
							.setTotalStarCount(prev_max_level_info.get(2))
							);
				}
			}
		}
		
		//Record number
		builder.setValidTowerRecordNum((int)TowerRecordManager.getTowerRecordNumByPlayer(player));
		
		TowerAllDataRsp proto = builder.build();
		Grasscutter.getLogger().info("TowerAllDataRsp: " + proto.toString());

		/* deprecated
		
		TowerAllDataRsp proto = TowerAllDataRsp.newBuilder()
				.setTowerScheduleId(29)
				.addTowerFloorRecordList(TowerFloorRecord.newBuilder().setFloorId(1001))
				.setCurLevelRecord(TowerCurLevelRecord.newBuilder().setIsEmpty(true))
				.setNextScheduleChangeTime(Integer.MAX_VALUE)
				.putFloorOpenTimeMap(1024, 1630486800)
				.putFloorOpenTimeMap(1025, 1630486800)
				.putFloorOpenTimeMap(1026, 1630486800)
				.putFloorOpenTimeMap(1027, 1630486800)
				.setScheduleStartTime(1630486800)
				.build();
		*/
		
		this.setData(proto);
	}
	
	public static List<TowerFloorRecord> genFloorRecord(List<emu.grasscutter.game.tower.record.TowerFloorRecord> floor_record_list){
		
		ArrayList<TowerFloorRecord> built_floor_record_list = new ArrayList<TowerFloorRecord>();
		
		if(floor_record_list != null)
			for(emu.grasscutter.game.tower.record.TowerFloorRecord single_floor_record: floor_record_list) {
	
				TowerFloorRecord.Builder builder = TowerFloorRecord.newBuilder();
				builder.setFloorId(single_floor_record.floor_id);
				builder.putAllPassedLevelMap(single_floor_record.passed_level_map);
				builder.setFloorStarRewardProgress(single_floor_record.floor_star_reward_progress);
				builder.addAllPassedLevelRecordList(genLevelRecord(single_floor_record.passed_level_record_list));
				
				built_floor_record_list.add(builder.build());
			}
		
		return built_floor_record_list;
	}
	
	
	public static List<TowerLevelRecord> genLevelRecord(List<emu.grasscutter.game.tower.record.TowerLevelRecord> level_record_list){
		
		ArrayList<TowerLevelRecord> built_level_record_list = new ArrayList<TowerLevelRecord>();
		
		if(level_record_list != null)
			for(emu.grasscutter.game.tower.record.TowerLevelRecord single_level_record: level_record_list) {
				TowerLevelRecord.Builder builder = TowerLevelRecord.newBuilder();
				builder.setLevelId(single_level_record.level_id);
				builder.addAllSatisfiedCondList(single_level_record.satisfied_cond_list);
				built_level_record_list.add(builder.build());
			}
		
		return built_level_record_list;
	}
	
	public static List<Integer> getMaxFloorLevel(List<emu.grasscutter.game.tower.record.TowerFloorRecord> floor_record_list){
		
		int max_floor = 0;
		int max_level = 0;
		int total_star = 0;
		
		if(floor_record_list != null)
			for(emu.grasscutter.game.tower.record.TowerFloorRecord single_floor_record: floor_record_list) {
				if(single_floor_record.passed_level_map.size() > 0) {
					max_floor += 1;
					max_level = single_floor_record.passed_level_map.size();
					for(Integer single_star_num: single_floor_record.passed_level_map.values()) {
						total_star += single_star_num;
					}
				}
			}
		
		return new ArrayList<Integer>(Arrays.asList(max_floor, max_level, total_star));
		
	}
}
