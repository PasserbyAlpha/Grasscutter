package emu.grasscutter.game.tower;

import java.util.ArrayList;
import java.util.List;

import dev.morphia.query.experimental.filters.Filters;
import emu.grasscutter.database.DatabaseManager;
import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.game.tower.info.FloorInfo;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;
import emu.grasscutter.game.tower.record.TowerCurLevelRecordModel;
import emu.grasscutter.game.tower.record.TowerScheduleRecord;
import emu.grasscutter.game.tower.record.TowerTeamInfo;

public class TowerRecordManager {
	
	public static TowerCurLevelRecordModel getCurLevelRecord(GenshinPlayer player, TowerScheduleInfo tower_schedule) {
		return getTowerScheduleRecord(player, tower_schedule).cur_level_record;
	}
	
	public static void removeCurLevelRecord(GenshinPlayer player, TowerScheduleInfo tower_schedule) {
		TowerScheduleRecord record = getTowerScheduleRecord(player, tower_schedule);
		TowerCurLevelRecordModel cur_level_record = record.cur_level_record;
		if(cur_level_record != null) {
			record.cur_level_record = null;
			saveTowerScheduleRecord(record);
			DatabaseManager.getDatastore().delete(cur_level_record);
		}
	}
	
	public static TowerCurLevelRecordModel initCurLevelRecord(GenshinPlayer player, TowerScheduleInfo tower_schedule, int floor_id, List<TowerTeamInfo> team_info_list) {
		TowerScheduleRecord record = getTowerScheduleRecord(player, tower_schedule);
		removeCurLevelRecord(player, tower_schedule);
		TowerCurLevelRecordModel cur_level_record = new TowerCurLevelRecordModel(player.getUid(), tower_schedule.get_tower_schedule_id(), floor_id, team_info_list);
		DatabaseManager.getDatastore().save(cur_level_record);
		record.cur_level_record = cur_level_record;
		saveTowerScheduleRecord(record);
		return cur_level_record;
	}
	
	public static void saveCurLevelRecord(TowerCurLevelRecordModel cur_level_record) {
		DatabaseManager.getDatastore().save(cur_level_record);
	}
	
	public static TowerScheduleRecord getTowerScheduleRecord(GenshinPlayer player, TowerScheduleInfo tower_schedule) {
		return getTowerScheduleRecord(player, tower_schedule, true);
	}
	
	
	public static TowerScheduleRecord getTowerScheduleRecord(GenshinPlayer player, TowerScheduleInfo tower_schedule, boolean auto_create) {
		
		TowerScheduleRecord current_user_schedule = getTowerScheduleRecordByOwnerAndSchedule(player.getUid(), tower_schedule);
		if(current_user_schedule == null && auto_create) {
			// create one if not exist
			current_user_schedule = new TowerScheduleRecord(player, tower_schedule);
			saveTowerScheduleRecord(current_user_schedule);
		}
		
		// auto unlock all level
		for(int i=1001; i<=1008; i++) {
			for(int j=1; j<=3; j++) {
				current_user_schedule.clear_level_with_condition_list(
						i,
						j,
						new ArrayList<Integer>());
			}
		}
		
		for(FloorInfo current_floor_info: tower_schedule.floor_list) {
			for(int j=1; j<=3; j++) {
				current_user_schedule.clear_level_with_condition_list(
						current_floor_info.get_floor_id(),
						j,
						new ArrayList<Integer>());
			}
		}
		saveTowerScheduleRecord(current_user_schedule);
		
		return current_user_schedule;
	}
	
	public static TowerScheduleRecord getTowerCurrentScheduleRecord(GenshinPlayer player) {
		return getTowerScheduleRecord(player, TowerCurrentScheduleManager.get_current_tower_schedule());
	}
	
	public static TowerScheduleRecord getTowerScheduleRecordByOwnerAndSchedule(int owner_id, TowerScheduleInfo tower_schedule) {
		return DatabaseManager.getDatastore().find(TowerScheduleRecord.class).filter(Filters.eq("owner_id", owner_id), Filters.eq("tower_schedule", tower_schedule)).first();
	}
	
	public static void saveTowerScheduleRecord(TowerScheduleRecord tower_schedule_record) {
		DatabaseManager.getDatastore().save(tower_schedule_record);
	}

	public static long getTowerRecordNumByPlayer(GenshinPlayer player) {
		return DatabaseManager.getDatastore().find(TowerScheduleRecord.class).filter(Filters.eq("owner_id", player.getUid())).count();
	}
}
