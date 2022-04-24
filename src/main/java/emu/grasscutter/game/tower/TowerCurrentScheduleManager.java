package emu.grasscutter.game.tower;

import java.util.ArrayList;
import java.util.Arrays;

import dev.morphia.query.experimental.filters.Filters;
import emu.grasscutter.database.DatabaseManager;
import emu.grasscutter.game.tower.info.FloorInfo;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;


public class TowerCurrentScheduleManager {
	//set default TowerScheduleInfo
	public static TowerScheduleInfo current_tower_schedule;
	static int FAKE_TOWER_SCHEDULE_ID = 99970000;
	
	public static void init() {
		//use fake tower schedule as init value
		set_current_tower_schedule(FAKE_TOWER_SCHEDULE_ID);
		ensure_basic_floor();
	}
	
	public static TowerScheduleInfo get_current_tower_schedule() {
		if(current_tower_schedule == null) {
			init();
		}
		return current_tower_schedule;
	}
	
	public static void set_current_tower_schedule(int tower_schedule_id) {
		current_tower_schedule = getTowerScheduleInfoById(tower_schedule_id);
		if(current_tower_schedule == null) {
			current_tower_schedule = generate_fake_tower_schedule();
		}
	}
	
	private static TowerScheduleInfo generate_fake_tower_schedule() {
		// return an empty tower info which is inside the DB as a fake one

		int FAKE_START_TIMESTAMP = 1000000000;
		int FAKE_END_TIME_STAMP = 2147483647;
		
		ArrayList<FloorInfo> floor_info_list = new ArrayList<FloorInfo>();
		
		for(int i=99934001; i<=99934004; i++) {
			FloorInfo new_floor_info = new FloorInfo(i);
			floor_info_list.add(new_floor_info);
			saveTowerFloorInfo(new_floor_info);
		}
		
		TowerScheduleInfo new_schedule = new TowerScheduleInfo(FAKE_TOWER_SCHEDULE_ID, FAKE_START_TIMESTAMP, FAKE_END_TIME_STAMP, floor_info_list);
		saveTowerScheduleInfo(new_schedule);
		
		return new_schedule;
	}
	
	
	private static void ensure_basic_floor() {
		// floor 1-8 is fixed, should be auto loaded
		//TODO load true data
		ArrayList<Integer> basic_floor_id_list = new ArrayList<Integer>(Arrays.asList(1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008));
		for(Integer single_floor_id: basic_floor_id_list) {
			FloorInfo current_floor_info = getFloorInfoById(single_floor_id);
			if(current_floor_info == null) {
				current_floor_info = new FloorInfo(single_floor_id);
				saveTowerFloorInfo(current_floor_info);
			}
		}
	}
	
	
	
	// DB function
	
	public static TowerScheduleInfo getTowerScheduleInfoById(int tower_schedule_id) {
		return DatabaseManager.getDatastore().find(TowerScheduleInfo.class).filter(Filters.eq("tower_schedule_id", tower_schedule_id)).first();
	}
	
	public static FloorInfo getFloorInfoById(int floor_id) {
		return DatabaseManager.getDatastore().find(FloorInfo.class).filter(Filters.eq("floor_id", floor_id)).first();
	}
	
	public static void saveTowerScheduleInfo(TowerScheduleInfo tower_schedule) {
		DatabaseManager.getDatastore().save(tower_schedule);
	}
	
	public static void saveTowerFloorInfo(FloorInfo floor_info) {
		DatabaseManager.getDatastore().save(floor_info);
	}
}
