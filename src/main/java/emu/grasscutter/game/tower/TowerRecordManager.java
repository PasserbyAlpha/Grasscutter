package emu.grasscutter.game.tower;

import java.util.ArrayList;
import java.util.Arrays;

import dev.morphia.query.experimental.filters.Filters;
import emu.grasscutter.database.DatabaseManager;
import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;
import emu.grasscutter.game.tower.record.TowerScheduleRecord;

public class TowerRecordManager {
	
	
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
		
		// test only
		current_user_schedule.clear_level_with_condition_list(
				1001,
				1,
				new ArrayList<Integer>(Arrays.asList(1,2,3)));
		current_user_schedule.clear_level_with_condition_list(
				1001,
				2,
				new ArrayList<Integer>(Arrays.asList(1,3)));
		current_user_schedule.clear_level_with_condition_list(
				1001,
				3,
				new ArrayList<Integer>(Arrays.asList(2)));
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
