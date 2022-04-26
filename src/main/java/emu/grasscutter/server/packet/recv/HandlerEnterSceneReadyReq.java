package emu.grasscutter.server.packet.recv;

import com.google.protobuf.InvalidProtocolBufferException;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.proto.EnterSceneReadyReqOuterClass.EnterSceneReadyReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketEnterScenePeerNotify;
import emu.grasscutter.server.packet.send.PacketEnterSceneReadyRsp;

@Opcodes(PacketOpcodes.EnterSceneReadyReq)
public class HandlerEnterSceneReadyReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws InvalidProtocolBufferException {

		EnterSceneReadyReq req = EnterSceneReadyReq.parseFrom(payload);
		
		if(session.getPlayer().getWorld().transferPlayerContinue(session.getPlayer(), req.getEnterSceneToken())){
			//success
		}

		session.send(new PacketEnterScenePeerNotify(session.getPlayer()));
		session.send(new PacketEnterSceneReadyRsp(session.getPlayer()));
	}

}
