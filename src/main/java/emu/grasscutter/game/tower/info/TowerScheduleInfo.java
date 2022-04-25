package emu.grasscutter.game.tower.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;

@Entity
public class TowerScheduleInfo {
	
	@Id
	int tower_schedule_id;
	
	public int next_schedule_change_time;
	public int schedule_start_time;
	public List<FloorInfo> floor_list;
	public Map<Integer, Integer> floor_open_time_map;
	
	
	@Reference
	public TowerScheduleInfo previous_tower_schedule_info;
	
	TowerScheduleInfo(){
		tower_schedule_id = 0;
		next_schedule_change_time = 0;
		schedule_start_time = 0;
		floor_list = new ArrayList<FloorInfo>();
		floor_open_time_map = new HashMap<Integer, Integer>();
	}
	
	public int get_tower_schedule_id() {
		return tower_schedule_id;
	}
	
	public TowerScheduleInfo(int new_tower_schedule_id, int tower_start_timestamp, int tower_end_timestamp, ArrayList<FloorInfo> new_floor_info_list){
		this.schedule_start_time = tower_start_timestamp;
		
		this.tower_schedule_id = new_tower_schedule_id;
		this.next_schedule_change_time = tower_end_timestamp;
		this.floor_open_time_map = new HashMap<Integer, Integer>();
		this.floor_list = new_floor_info_list;
		
		for(FloorInfo single_floor_info: new_floor_info_list) {
			floor_open_time_map.put(single_floor_info.get_floor_id(), tower_start_timestamp);
		}
		
		this.previous_tower_schedule_info = null;
	}

}
