package emu.grasscutter.game.tower.record;

import java.util.ArrayList;
import java.util.List;


import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Reference;
import emu.grasscutter.game.GenshinPlayer;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;

@Entity
@Indexes({
	@Index(fields= {@Field("owner_id"), @Field("tower_schedule_id")}, options = @IndexOptions(unique = true)),
	@Index(fields= {@Field("owner_id")}),
})
public class TowerScheduleRecord {

	//floor(1-12, corresponding to floor id)
	//level(x-1~x-3, level id is unique through all floors)
	
	
	@Id
	String inner_id;
	
	
	public int owner_id;
	@Reference
	public TowerScheduleInfo tower_schedule;
	
	public List<TowerFloorRecord> tower_floor_record_list;
	public TowerCurLevelRecord cur_level_record;
	
	
	public TowerScheduleRecord(GenshinPlayer player, TowerScheduleInfo new_tower_schedule){
		this.owner_id = player.getUid();
		this.tower_schedule = new_tower_schedule;
		this.inner_id = Integer.toString(player.getUid()) + "-" + Integer.toString(new_tower_schedule.get_tower_schedule_id());
		this.tower_floor_record_list = new ArrayList<TowerFloorRecord>();
	}
	
	
	public void clear_level_with_condition_list(int floor_id, int level_id, ArrayList<Integer> met_cond_list) {
		// once level is cleared, merge the info into the record
		
		// find target floor record, or add it if not exist
		TowerFloorRecord current_floor_record = null;
		
		for(TowerFloorRecord single_floor_record: this.tower_floor_record_list) {
			if(single_floor_record.floor_id == floor_id) {
				current_floor_record = single_floor_record;
				break;
			}
		}
		
		if(current_floor_record == null) {
			current_floor_record = new TowerFloorRecord(floor_id);
			this.tower_floor_record_list.add(current_floor_record);
		}
		
		//update floor info
		current_floor_record.metNewConditionInLevel(level_id, met_cond_list);
		
	}

}
