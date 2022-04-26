package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ServerBuffChangeNotifyOuterClass.ServerBuffChangeNotify;
import emu.grasscutter.net.proto.ServerBuffOuterClass.ServerBuff;

public class PacketServerBuffChangeNotify extends GenshinPacket {

	public PacketServerBuffChangeNotify(long guid, int server_buff_id, int buff_idx) {
		super(PacketOpcodes.ServerBuffChangeNotify);

		ServerBuffChangeNotify.Builder builder = ServerBuffChangeNotify.newBuilder();
		builder.addAvatarGuidList(guid);
		builder.addServerBuffList(generateServerBuff(server_buff_id, buff_idx));

		ServerBuffChangeNotify proto = builder.build();
		
		this.setData(proto);
	}

	public ServerBuff generateServerBuff(int server_buff_id, int buff_idx){
		ServerBuff.Builder builder = ServerBuff.newBuilder();
		//could be any int
		int SERVER_BUFF_UID_SHIFT_NUM=883450000;
		// fixed (I guess)
		int SERVER_BUFF_ID_SHIFT_NUM=701000;

		builder.setServerBuffUid(server_buff_id + SERVER_BUFF_UID_SHIFT_NUM);
		builder.setServerBuffId(server_buff_id + SERVER_BUFF_ID_SHIFT_NUM);
		builder.setServerBuffType(2);
		builder.setInstancedModifierId(buff_idx);
		
		return builder.build();
	}
}
