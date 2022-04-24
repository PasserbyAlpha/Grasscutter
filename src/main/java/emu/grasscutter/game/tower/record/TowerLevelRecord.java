package emu.grasscutter.game.tower.record;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import dev.morphia.annotations.Entity;

@Entity
public class TowerLevelRecord{
	public int level_id;
	public List<Integer> satisfied_cond_list;

	TowerLevelRecord() {
		this.level_id = 0;
		this.satisfied_cond_list = null;
	}
	
	TowerLevelRecord(int new_level_id) {
		this.level_id = new_level_id;
		this.satisfied_cond_list = new ArrayList<Integer>();
	}
	
	int metNewCondition(ArrayList<Integer> cond_idx_list) {
		//apply new conditions and return the total condition number
		
		
		HashSet<Integer> cond_set = new HashSet<Integer>(this.satisfied_cond_list);
		for(int single_cond_idx : cond_idx_list) {
			cond_set.add(single_cond_idx);
		}
		
		ArrayList<Integer> new_cond_list = new ArrayList<Integer>();
		for(int i=1; i<=3; i++) {
			if(cond_set.contains(i)) {
				new_cond_list.add(i);
			}
		}
		this.satisfied_cond_list = new_cond_list;
		return this.satisfied_cond_list.size();
	}
	
}