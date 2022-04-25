package emu.grasscutter.game.tower.info;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

@Entity
public class FloorInfo {
	@Id
	int floor_id;
	
	public FloorInfo() {
		floor_id = 0;
	}
	
	public FloorInfo(int new_floor_id){
		this.floor_id = new_floor_id;
	}
	
	public int get_floor_id() {
		return floor_id;
	}
}
