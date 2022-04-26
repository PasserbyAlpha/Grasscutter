package emu.grasscutter.server.packet.recv;

import java.util.ArrayList;
import java.util.List;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.avatar.GenshinAvatar;
import emu.grasscutter.game.props.EnterReason;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.game.tower.TowerCurrentScheduleManager;
import emu.grasscutter.game.tower.TowerProgressManager;
import emu.grasscutter.game.tower.TowerRecordManager;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;
import emu.grasscutter.game.tower.record.TowerCurLevelRecordModel;
import emu.grasscutter.game.tower.record.TowerScheduleRecord;
import emu.grasscutter.game.tower.record.TowerTeamInfo;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.EnterTypeOuterClass.EnterType;
import emu.grasscutter.net.proto.TowerEnterLevelReqOuterClass.TowerEnterLevelReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketAvatarFightPropUpdateNotify;
import emu.grasscutter.server.packet.send.PacketPlayerEnterSceneNotify;
import emu.grasscutter.server.packet.send.PacketTowerCurLevelRecordChangeNotify;
import emu.grasscutter.server.packet.send.PacketTowerEnterLevelRsp;
import emu.grasscutter.server.packet.send.PacketTowerTeamSelectRsp;
import emu.grasscutter.utils.Position;

@Opcodes(PacketOpcodes.TowerEnterLevelReq)
public class HandlerTowerEnterLevelReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		
		TowerEnterLevelReq req = TowerEnterLevelReq.parseFrom(payload);
		
		//begin processing tower level
		TowerScheduleInfo current_schedule_info = TowerCurrentScheduleManager.get_current_tower_schedule();
		TowerCurLevelRecordModel cur_level_record = TowerRecordManager.getCurLevelRecord(session.getPlayer(), current_schedule_info);
		
		//if level==1 heal all
		if(cur_level_record.level_idx == 1) {
			for(TowerTeamInfo team_info: cur_level_record.team_list) {
				for(long guid: team_info.avatar_guid_list) {
					GenshinAvatar single_avatar = session.getPlayer().getAvatars().getAvatarByGuid(guid);
					//heal
					single_avatar.setCurrentHp(single_avatar.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP));
					//energy 100%
					for(int i=0;i<=6;i++) {
						single_avatar.setFightProperty(FightProperty.getPropById(1000+i), single_avatar.getFightProperty(FightProperty.getPropById(70+i)));
						single_avatar.save();
					}
					cur_level_record.storeAvatarStatus(single_avatar);
				}
			}
		}
		
		//WARNING 
		// in original server-client, new avatar will be generated for tower
		// however, based on the avatar system for current project, copy avatar has not been implemented and may lead to extra time in development
		// As a result, the original avatar will be used in tower for now.

		
		// update buff status
		TowerProgressManager.update_buff_list_new_level(cur_level_record.buff_id_list);
		TowerRecordManager.saveCurLevelRecord(cur_level_record);

		
		// send cur level record
		//
		session.send(new PacketTowerCurLevelRecordChangeNotify(cur_level_record));
		session.send(new PacketTowerTeamSelectRsp());
		
		// load team status
		for(TowerTeamInfo team_info: cur_level_record.team_list) {
			
			int team_id = team_info.team_id;
			List<Long> team_guid_list = new ArrayList<Long>();
			
			for(long guid: team_info.avatar_guid_list) {
				GenshinAvatar single_avatar = session.getPlayer().getAvatars().getAvatarByGuid(guid);
				cur_level_record.applyAvatarStatus(single_avatar);
				
				// send package to update HP info
				session.send(new PacketAvatarFightPropUpdateNotify(single_avatar, FightProperty.FIGHT_PROP_CUR_HP));
				// send package update energy
				for(int i=1000;i <=1006; i++) {
					session.send(new PacketAvatarFightPropUpdateNotify(single_avatar, FightProperty.getPropById(i)));
				}
			}
		}

		session.send(new PacketTowerEnterLevelRsp(cur_level_record));
		
		//TODO enter scene
		
		//WARNING the dungeon id should be read somewhere else, I just put my assumption here
		// 3 X Y 0 N for scene id, 3XYN for dungeon id
		// X level X=3 => level=1
		// Y floor Y=1 => floor 1
		// N team num, N=0 for Y<=3, N=1 for Y>=4
		// should just works with floor <=8
		
		//generate dungeon id
		int dungeon_scene_id = 30000 + (cur_level_record.level_idx + 3) * 1000 + (cur_level_record.floor_id % 10) * 100;
		int dungeon_id = 3000 + (cur_level_record.level_idx + 3) * 100 + (cur_level_record.floor_id % 10) * 10;
		if(cur_level_record.floor_id >= 1004) {
			dungeon_scene_id += 1;
			dungeon_id += 1;
		}
		Grasscutter.getLogger().info(Integer.toString(dungeon_scene_id));
		Grasscutter.getLogger().info(Integer.toString(dungeon_id));
		
		session.getPlayer().getWorld().transferPlayerToDungeonRegister(session.getPlayer(), dungeon_scene_id, new Position((float)0, (float)-5, (float)42), dungeon_id);
	}

}
