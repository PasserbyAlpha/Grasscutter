package emu.grasscutter.game.tower.info;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

@Entity
public class TowerBuffInfo {
	
	@Id
	public int buff_id;
	public boolean available_all_level;
	
	TowerBuffInfo(){
		buff_id = 0;
		available_all_level = false;
	}
	
	public TowerBuffInfo(int new_buff_id, boolean new_available_all_level) {
		buff_id = new_buff_id;
		available_all_level = new_available_all_level;
	}
}
