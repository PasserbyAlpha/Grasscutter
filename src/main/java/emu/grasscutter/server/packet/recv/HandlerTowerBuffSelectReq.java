package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.tower.TowerCurrentScheduleManager;
import emu.grasscutter.game.tower.TowerRecordManager;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;
import emu.grasscutter.game.tower.record.TowerScheduleRecord;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerBuffSelectReqOuterClass.TowerBuffSelectReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketServerBuffChangeNotify;
import emu.grasscutter.server.packet.send.PacketTowerBuffSelectRsp;

@Opcodes(PacketOpcodes.TowerBuffSelectReq)
public class HandlerTowerBuffSelectReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		
		TowerBuffSelectReq req = TowerBuffSelectReq.parseFrom(payload);
		int tower_buff_id = req.getTowerBuffId();

		//load current player record
		TowerScheduleInfo current_schedule_info = TowerCurrentScheduleManager.get_current_tower_schedule();
		//load current schedule record
		TowerScheduleRecord current_schedule_record = TowerRecordManager.getTowerScheduleRecord(session.getPlayer(), current_schedule_info);


		//calculat idx
		int buff_idx = current_schedule_record.cur_level_record.buff_id_list.size() + 1;
		
		for(EntityAvatar team_avatar :session.getPlayer().getTeamManager().getActiveTeam()){
			session.send(
				new PacketServerBuffChangeNotify(
					team_avatar.getAvatar().getGuid(),
					tower_buff_id,
					buff_idx 
					));
		}

		session.send(new PacketTowerBuffSelectRsp(tower_buff_id));

	}

}
