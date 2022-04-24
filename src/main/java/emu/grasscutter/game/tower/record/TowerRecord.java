package emu.grasscutter.game.tower.record;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

@Entity
public class TowerRecord {
	@Id
	public int owner_id;
	
	public boolean is_finished_entrance_floor;
	
}
