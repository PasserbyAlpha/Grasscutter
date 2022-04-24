package emu.grasscutter.game.tower.record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.morphia.annotations.Entity;
import emu.grasscutter.Grasscutter;

@Entity
public class TowerFloorRecord{
	public int floor_id;
	public Map<Integer, Integer> passed_level_map;
	public int floor_star_reward_progress;
	public List<TowerLevelRecord> passed_level_record_list;
	
	TowerFloorRecord(){
		this.floor_id = 0;
		this.passed_level_map = null;
		this.floor_star_reward_progress = 0;
		this.passed_level_record_list = null;
	}
	
	TowerFloorRecord(int new_floor_id){
		this.floor_id = new_floor_id;
		this.passed_level_map = new HashMap<Integer, Integer>();
		this.floor_star_reward_progress = 0;
		this.passed_level_record_list = new ArrayList<TowerLevelRecord>();
	}
	
	void metNewConditionInLevel(int level_id, ArrayList<Integer> met_cond_list) {
		
		// find target level record, or add it if not exist
		TowerLevelRecord current_level_record = null;
		for(TowerLevelRecord single_level_record: this.passed_level_record_list) {
			if(single_level_record.level_id == level_id) {
				current_level_record = single_level_record;
				break;
			}
		}
		
		if(current_level_record == null) {
			current_level_record = new TowerLevelRecord(level_id);
			this.passed_level_record_list.add(current_level_record);
		}
		
		// update level info
		int new_level_cond_num = current_level_record.metNewCondition(met_cond_list);
		
		this.passed_level_map.put(level_id, new_level_cond_num);
		
		
	}
}