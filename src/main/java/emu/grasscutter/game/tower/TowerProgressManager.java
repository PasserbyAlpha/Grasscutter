package emu.grasscutter.game.tower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import emu.grasscutter.database.DatabaseManager;
import emu.grasscutter.game.tower.info.TowerBuffInfo;

public class TowerProgressManager {

	public static Map<Integer, TowerBuffInfo> buff_map=null;
	
	public static void add_buff_to_db(int buff_id, boolean available_all_level) {
		TowerBuffInfo new_buff_info = new TowerBuffInfo(buff_id, available_all_level);
		DatabaseManager.getDatastore().save(new_buff_info);
	}
	
	public static void init_buff_map() {
		if(buff_map == null) {
			
			if(DatabaseManager.getDatastore().find(TowerBuffInfo.class).count() == 0) {
				// no buff in db, generate
				add_buff_to_db(24, true);
				add_buff_to_db(29, true);
				add_buff_to_db(18, true);
				add_buff_to_db(4, true);
				add_buff_to_db(21, true);
				add_buff_to_db(31, true);
				add_buff_to_db(28, true);
				add_buff_to_db(19, true);
				add_buff_to_db(2, true);
				add_buff_to_db(35, true);
				add_buff_to_db(6, true);
				add_buff_to_db(3, true);
				add_buff_to_db(17, true);
				add_buff_to_db(34, true);
			}
			
			buff_map = new HashMap<Integer, TowerBuffInfo>();
			Iterator<TowerBuffInfo> buff_iter = DatabaseManager.getDatastore().find(TowerBuffInfo.class).iterator();
			while(buff_iter.hasNext()) {
				TowerBuffInfo single_buff_info = buff_iter.next();
				buff_map.put(single_buff_info.buff_id, single_buff_info);
			}
		}
	}
	
	public static void update_buff_list_new_level(List<Integer> original_buff_list) {
		init_buff_map();
		int curr_idx = 0;
		while(curr_idx < original_buff_list.size()) {
			TowerBuffInfo buff_info = buff_map.get(original_buff_list.get(curr_idx));
			if(!buff_info.available_all_level) {
				// remove buff
				original_buff_list.remove(curr_idx);
			}else {
				curr_idx += 1;
			}
		}
	}
	
	public static List<Integer> get_available_buff_list(List<Integer> original_buff_list, int number_of_buff){
		init_buff_map();

		List<Integer> all_buff_list = new ArrayList<Integer>(buff_map.keySet());
		Collections.shuffle(all_buff_list);
		
		return all_buff_list.subList(0, number_of_buff);
	}
	
	public static List<Integer> get_available_buff_list(List<Integer> original_buff_list){
		
		return get_available_buff_list(original_buff_list, 3);
	}
}
