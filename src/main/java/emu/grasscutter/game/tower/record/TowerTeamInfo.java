package emu.grasscutter.game.tower.record;

import java.util.List;

import dev.morphia.annotations.Entity;

@Entity
public class TowerTeamInfo{
	public int team_id;
	public List<Long> avatar_guid_list;
	
	TowerTeamInfo(){
		team_id = 0;
		avatar_guid_list = null;
	}
	
	public TowerTeamInfo(int new_team_id, List<Long> new_avatar_guid_list){
		team_id = new_team_id;
		avatar_guid_list = new_avatar_guid_list;
	}
}