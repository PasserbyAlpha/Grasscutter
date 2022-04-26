package emu.grasscutter.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import emu.grasscutter.game.entity.GenshinEntity;
import emu.grasscutter.game.props.ClimateType;
import emu.grasscutter.game.props.EnterReason;
import emu.grasscutter.game.props.EntityIdType;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.game.props.LifeState;
import emu.grasscutter.data.GenshinData;
import emu.grasscutter.data.def.SceneData;
import emu.grasscutter.game.GenshinPlayer.SceneLoadState;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.entity.EntityClientGadget;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.net.packet.GenshinPacket;
import emu.grasscutter.net.proto.AttackResultOuterClass.AttackResult;
import emu.grasscutter.net.proto.EnterTypeOuterClass.EnterType;
import emu.grasscutter.net.proto.VisionTypeOuterClass.VisionType;
import emu.grasscutter.server.packet.send.PacketDelTeamEntityNotify;
import emu.grasscutter.server.packet.send.PacketDungeonDataNotify;
import emu.grasscutter.server.packet.send.PacketDungeonWayPointNotify;
import emu.grasscutter.server.packet.send.PacketEntityFightPropUpdateNotify;
import emu.grasscutter.server.packet.send.PacketLifeStateChangeNotify;
import emu.grasscutter.server.packet.send.PacketPlayerEnterSceneNotify;
import emu.grasscutter.server.packet.send.PacketSceneEntityAppearNotify;
import emu.grasscutter.server.packet.send.PacketSceneEntityDisappearNotify;
import emu.grasscutter.server.packet.send.PacketScenePlayerInfoNotify;
import emu.grasscutter.server.packet.send.PacketSyncScenePlayTeamEntityNotify;
import emu.grasscutter.server.packet.send.PacketSyncTeamEntityNotify;
import emu.grasscutter.server.packet.send.PacketWorldPlayerInfoNotify;
import emu.grasscutter.server.packet.send.PacketWorldPlayerRTTNotify;
import emu.grasscutter.utils.Position;
import emu.grasscutter.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class World implements Iterable<GenshinPlayer> {
	private final GenshinPlayer owner;
	private final List<GenshinPlayer> players;
	private final Int2ObjectMap<GenshinScene> scenes;
	
	private int levelEntityId;
	private int nextEntityId = 0;
	private int nextPeerId = 0;
	private int worldLevel;

	private Map<Long, SceneSwitchCache> swtich_scene_cache;

	private boolean isMultiplayer;
	
	public World(GenshinPlayer player) {
		this(player, false);
	}
	
	public World(GenshinPlayer player, boolean isMultiplayer) {
		this.owner = player;
		this.players = Collections.synchronizedList(new ArrayList<>());
		this.scenes = new Int2ObjectOpenHashMap<>();
		
		this.levelEntityId = getNextEntityId(EntityIdType.MPLEVEL);
		this.worldLevel = player.getWorldLevel();
		this.isMultiplayer = isMultiplayer;

		this.swtich_scene_cache = new HashMap<Long, SceneSwitchCache>();
	}
	
	public GenshinPlayer getHost() {
		return owner;
	}

	public int getLevelEntityId() {
		return levelEntityId;
	}

	public int getHostPeerId() {
		if (this.getHost() == null) {
			return 0;
		}
		return this.getHost().getPeerId();
	}
	
	public int getNextPeerId() {
		return ++this.nextPeerId;
	}

	public int getWorldLevel() {
		return worldLevel;
	}

	public void setWorldLevel(int worldLevel) {
		this.worldLevel = worldLevel;
	}

	public List<GenshinPlayer> getPlayers() {
		return players;
	}
	
	public Int2ObjectMap<GenshinScene> getScenes() {
		return this.scenes;
	}
	
	public GenshinScene getSceneById(int sceneId) {
		// Get scene normally
		GenshinScene scene = getScenes().get(sceneId);
		if (scene != null) {
			return scene;
		}
		
		// Create scene from scene data if it doesnt exist
		SceneData sceneData = GenshinData.getSceneDataMap().get(sceneId);
		if (sceneData != null) {
			scene = new GenshinScene(this, sceneData);
			this.registerScene(scene);
			return scene;
		}
		
		return null;
	}
	
	public int getPlayerCount() {
		return getPlayers().size();
	}
	
	public boolean isMultiplayer() {
		return isMultiplayer;
	}
	
	public int getNextEntityId(EntityIdType idType) {
		return (idType.getId() << 24) + ++this.nextEntityId;
	}
	
	public synchronized void addPlayer(GenshinPlayer player) {
		// Check if player already in
		if (getPlayers().contains(player)) {
			return;
		}
		
		// Remove player from prev world
		if (player.getWorld() != null) {
			player.getWorld().removePlayer(player);
		}
		
		// Register
		player.setWorld(this);
		getPlayers().add(player);

		// Set player variables
		player.setPeerId(this.getNextPeerId());
		player.getTeamManager().setEntityId(getNextEntityId(EntityIdType.TEAM));
		
		// Copy main team to mp team
		if (this.isMultiplayer()) {
			player.getTeamManager().getMpTeam().copyFrom(player.getTeamManager().getCurrentSinglePlayerTeamInfo(), player.getTeamManager().getMaxTeamSize());
			player.getTeamManager().setCurrentCharacterIndex(0);
		}
		
		// Add to scene
		GenshinScene scene = this.getSceneById(player.getSceneId());
		scene.addPlayer(player);

		// Info packet for other players
		if (this.getPlayers().size() > 1) {
			this.updatePlayerInfos(player);
		}
	}

	public synchronized void removePlayer(GenshinPlayer player) {
		// Remove team entities
		player.sendPacket(
				new PacketDelTeamEntityNotify(
						player.getSceneId(), 
						getPlayers().stream().map(p -> p.getTeamManager().getEntityId()).collect(Collectors.toList())
				)
		);
		
		// Deregister
		getPlayers().remove(player);
		player.setWorld(null);
		
		// Remove from scene
		GenshinScene scene = this.getSceneById(player.getSceneId());
		scene.removePlayer(player);

		// Info packet for other players
		if (this.getPlayers().size() > 0) {
			this.updatePlayerInfos(player);
		}

		// Disband world if host leaves
		if (getHost() == player) {
			List<GenshinPlayer> kicked = new ArrayList<>(this.getPlayers());
			for (GenshinPlayer victim : kicked) {
				World world = new World(victim);
				world.addPlayer(victim);
				
				victim.sendPacket(new PacketPlayerEnterSceneNotify(victim, EnterType.EnterSelf, EnterReason.TeamKick, victim.getSceneId(), victim.getPos()));
			}
		}
	}
	
	public void registerScene(GenshinScene scene) {
		this.getScenes().put(scene.getId(), scene);
	}
	
	public void deregisterScene(GenshinScene scene) {
		this.getScenes().remove(scene.getId());
	}
	
	public boolean transferPlayerToScene(GenshinPlayer player, int sceneId, Position pos) {
		if (GenshinData.getSceneDataMap().get(sceneId) == null) {
			return false;
		}
		
		Integer oldSceneId = null;

		if (player.getScene() != null) {
			oldSceneId = player.getScene().getId();
			player.getScene().removePlayer(player);
		}
		
		GenshinScene scene = this.getSceneById(sceneId);
		scene.addPlayer(player);
		player.getPos().set(pos);
		
		// Teleport packet
		if (oldSceneId.equals(sceneId)) {
			player.sendPacket(new PacketPlayerEnterSceneNotify(player, EnterType.EnterGoto, EnterReason.TransPoint, sceneId, pos));
		} else {
			player.sendPacket(new PacketPlayerEnterSceneNotify(player, EnterType.EnterJump, EnterReason.TransPoint, sceneId, pos));
		}
		return true;
	}


	public static class SceneSwitchCache{
		public int token;
		public int uid;

		public Integer original_scene;
		public Position original_pos;

		public Integer target_scene;
		public Position target_pos;

		public Integer dungeon_id;

		public EnterType enter_type;
		public EnterReason enter_reason;

		public SceneSwitchCache(
			int new_token,
			int new_uid,
			Integer new_original_scene,
			Position new_original_pos,
			Integer new_target_scene,
			Position new_target_pos,
			Integer new_dungeon_id,
			EnterType new_enter_type,
			EnterReason new_enter_reason
		){
			token=new_token;
			uid=new_uid;
			original_scene=new_original_scene;
			original_pos=new_original_pos;
			target_scene=new_target_scene;
			target_pos = new_target_pos;
			dungeon_id = new_dungeon_id;
			enter_type = new_enter_type;
			enter_reason = new_enter_reason;
		}
	}

	Long gen_scene_cache_id(int uid, int token){
		return (long)uid << 32 + token;
	}

	public boolean transferPlayerToDungeonRegister(GenshinPlayer player, int sceneId, Position pos, int dungeon_id) {

		if (GenshinData.getSceneDataMap().get(sceneId) == null) {
			return false;
		}
		
		//generate token for cache key
		int token = Utils.randomRange(1000, 99999);

		Integer old_scene_id = null;
		Position old_scene_pos = null;

		if (player.getScene() != null){
			old_scene_id = player.getScene().getId();
			old_scene_pos = player.getPos();
		}

		SceneSwitchCache current_cache = new SceneSwitchCache(
			token,
			player.getUid(),
			old_scene_id,
			old_scene_pos,
			sceneId,
			pos,
			dungeon_id,
			EnterType.EnterDungeon,
			EnterReason.DungeonEnter
		);

		this.swtich_scene_cache.put(gen_scene_cache_id(player.getUid(), token), current_cache);

		// in dungeon moving, real transfer should not happen until EnterSceneReadyReq happen
		// if (player.getScene() != null) {
		// 	oldSceneId = player.getScene().getId();
		// 	player.getScene().removePlayer(player);
		// }
		
		// GenshinScene scene = this.getSceneById(sceneId);
		// scene.addPlayer(player);
		// player.getPos().set(pos);
		
		// Teleport packet
		player.sendPacket(
			new PacketPlayerEnterSceneNotify(
				player, 
				EnterType.EnterDungeon, 
				EnterReason.DungeonEnter, 
				sceneId,
				pos,
				dungeon_id,
				token));
		return true;
	}

	public boolean transferPlayerContinue(GenshinPlayer player, int token) {
		
		SceneSwitchCache current_cache = this.swtich_scene_cache.get(gen_scene_cache_id(player.getUid(), token));
		if(current_cache == null){
			return false;
		}

		// continue transfer progress

		// remove old team
		GenshinScene original_scene = this.getSceneById(current_cache.original_scene);
		original_scene.removePlayer(player);
		

		// lock save position if target is in dungeon
		if(current_cache.enter_type == EnterType.EnterDungeon){
			player.lock_save_position(
				current_cache.original_scene, 
				player.getPos(),
				player.getRotation());
		}

		// transfer to new position
		GenshinScene target_scene = this.getSceneById(current_cache.target_scene);
		target_scene.addPlayer(player);
		player.getPos().set(current_cache.target_pos);
		player.getRotation().set(0, 180, 0);

		// update team
		player.getTeamManager().updateTeamEntities(null);

		return true;
	}

	public boolean transferPlayerInit(GenshinPlayer player, int token) {
		
		SceneSwitchCache current_cache = this.swtich_scene_cache.get(gen_scene_cache_id(player.getUid(), token));
		if(current_cache == null){
			return false;
		}

		// continue transfer progress (init scene)
		if(current_cache.dungeon_id != null){
			//dungeon mode
			player.getSession().send(new PacketDungeonWayPointNotify());
			player.getSession().send(new PacketDungeonDataNotify());
		}

		return true;
	}

	public boolean transferPlayerPost(GenshinPlayer player, int token) {
		
		SceneSwitchCache current_cache = this.swtich_scene_cache.get(gen_scene_cache_id(player.getUid(), token));
		if(current_cache == null){
			return false;
		}

		//remove cache
		this.swtich_scene_cache.remove(gen_scene_cache_id(player.getUid(), token));

		return true;
	}
	
	private void updatePlayerInfos(GenshinPlayer paramPlayer) {
		for (GenshinPlayer player : getPlayers()) {
			// Dont send packets if player is loading in and filter out joining player
			if (!player.hasSentAvatarDataNotify() || player.getSceneLoadState().getValue() < SceneLoadState.INIT.getValue() || player == paramPlayer) {
				continue;
			}
			
			// Update team of all players since max players has been changed - Probably not the best way to do it
			if (this.isMultiplayer()) {
				player.getTeamManager().getMpTeam().copyFrom(player.getTeamManager().getMpTeam(), player.getTeamManager().getMaxTeamSize());
				player.getTeamManager().updateTeamEntities(null);
			}

			// World player info packets
			player.getSession().send(new PacketWorldPlayerInfoNotify(this));
			player.getSession().send(new PacketScenePlayerInfoNotify(this));
			player.getSession().send(new PacketWorldPlayerRTTNotify(this));
			
			// Team packets
			player.getSession().send(new PacketSyncTeamEntityNotify(player));
			player.getSession().send(new PacketSyncScenePlayTeamEntityNotify(player));
		}
	}
	
	public void broadcastPacket(GenshinPacket packet) {
    	// Send to all players - might have to check if player has been sent data packets
    	for (GenshinPlayer player : this.getPlayers()) {
    		player.getSession().send(packet);
    	}
	}
	
	public void close() {
		
	}

	@Override
	public Iterator<GenshinPlayer> iterator() {
		return getPlayers().iterator();
	}
}
