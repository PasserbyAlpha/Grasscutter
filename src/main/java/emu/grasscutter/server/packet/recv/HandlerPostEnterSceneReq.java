package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.PostEnterSceneReqOuterClass.PostEnterSceneReq;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketPostEnterSceneRsp;

@Opcodes(PacketOpcodes.PostEnterSceneReq)
public class HandlerPostEnterSceneReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {

		PostEnterSceneReq req = PostEnterSceneReq.parseFrom(payload);

		//handle continue
		if(session.getPlayer().getWorld().transferPlayerPost(session.getPlayer(), req.getEnterSceneToken())){
			//success
		}

		session.send(new PacketPostEnterSceneRsp(session.getPlayer()));
	}

}
