package emu.grasscutter.server.packet.recv;

import java.util.ArrayList;
import java.util.List;

import emu.grasscutter.game.tower.TowerCurrentScheduleManager;
import emu.grasscutter.game.tower.TowerRecordManager;
import emu.grasscutter.game.tower.info.TowerScheduleInfo;
import emu.grasscutter.game.tower.record.TowerTeamInfo;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.TowerTeamOuterClass.TowerTeam;
import emu.grasscutter.net.proto.TowerTeamSelectReqOuterClass.TowerTeamSelectReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketTowerTeamSelectRsp;

@Opcodes(PacketOpcodes.TowerTeamSelectReq)
public class HandlerTowerTeamSelectReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		
		TowerTeamSelectReq req = TowerTeamSelectReq.parseFrom(payload);
		
		//create a new record (but not process now, should wait for enter req
		TowerScheduleInfo current_schedule_info = TowerCurrentScheduleManager.get_current_tower_schedule();
		
		List<TowerTeamInfo> team_info_list = new ArrayList<TowerTeamInfo>();
		
		for(TowerTeam single_team_info: req.getTowerTeamListList()) {
			TowerTeamInfo team_info = new TowerTeamInfo(single_team_info.getTowerTeamId(), single_team_info.getAvatarGuidListList());
			team_info_list.add(team_info);
		}
		
		TowerRecordManager.initCurLevelRecord(session.getPlayer(), current_schedule_info, req.getFloorId(), team_info_list);
		
		session.send(new PacketTowerTeamSelectRsp());
		
	}

}
