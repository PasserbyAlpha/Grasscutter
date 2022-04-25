package emu.grasscutter.game.tower.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import emu.grasscutter.game.avatar.GenshinAvatar;
import emu.grasscutter.game.props.FightProperty;

@Entity
public class TowerCurLevelRecordModel {

	@Id
	public long joint_id;
	
	public int owner_id;
	public int schedule_id;
	
	public int floor_id;
	public int level_idx;
	public List<TowerTeamInfo> team_list;
	public List<Integer> buff_id_list;
	
	public Map<Long, Map<Integer, Float>> level_team_status;
	
	long generate_joint_id(int owner_id, int schedule_id) {
		return (long)owner_id << 32 + schedule_id;
	}
	
	TowerCurLevelRecordModel(){
		joint_id = 0;
		owner_id = 0;
		floor_id = 0;
		level_idx = 0;
		team_list = null;
		level_team_status = new HashMap<Long, Map<Integer, Float>>();
		buff_id_list = new ArrayList<Integer>();
	}
	
	
	public TowerCurLevelRecordModel(int new_owner_id, int new_schedule_id, int new_floor_id, List<TowerTeamInfo> new_team_list){
		owner_id = new_owner_id;
		schedule_id = new_schedule_id;
		joint_id = generate_joint_id(owner_id, schedule_id);
		floor_id = new_floor_id;
		level_idx = 0;
		team_list = new_team_list;
		level_team_status = new HashMap<Long, Map<Integer, Float>>();
		buff_id_list = new ArrayList<Integer>();
	}
	
	public void storeAvatarStatus(GenshinAvatar avatar) {
		long guid = avatar.getGuid();
		Map<Integer, Float> avatar_status_map = new HashMap<Integer, Float>();
		
		for(int i: Arrays.asList(1000, 1001, 1002, 1003, 1004, 1005, 1006, 1010)) {
			avatar_status_map.put(i, avatar.getFightProperty(FightProperty.getPropById(i)));
		}
		level_team_status.put(guid, avatar_status_map);
	}
	
	public void applyAvatarStatus(GenshinAvatar avatar) {
		long guid = avatar.getGuid();
		Map<Integer, Float> avatar_status_map = level_team_status.get(guid);
		if(avatar_status_map != null) {
			for(int i: Arrays.asList(1000, 1001, 1002, 1003, 1004, 1005, 1006, 1010)) {
				avatar.setFightProperty(FightProperty.getPropById(i), avatar_status_map.get(i));
			}
		}
	}
	
	public void add_buff(int new_buff_id) {
		buff_id_list.add(new_buff_id);
	}
}
