package com.github.fabricservertools.deltalogger.beans;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import static com.github.fabricservertools.deltalogger.command.rollback.RollbackUtils.createIdentifier;

public class KilledEntity implements Bean {
	private int id;
	private String source;
	private String killer;
	private String dimension;
	private String time;
	private int x;
	private int y;
	private int z;
	private final String type;
	private final String nbt;

	public KilledEntity(int id, String source, String killer, String dimension, String time, int x, int y, int z, String type, String nbt) {
		this.id = id;
		this.source = source;
		this.killer = killer;
		this.dimension = dimension;
		this.time = time;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.nbt = nbt;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNbt() {
		return this.nbt;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getKiller() {
		return this.killer;
	}

	public void setKiller(String killer) {
		this.killer = killer;
	}

	public String getDimension() {
		return this.dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public String getTime() {
		return this.time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getX() {
		return this.x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return this.z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public String getEntityType() {
		return this.type;
	}

	@Override
	public void rollback(World world) {
		Entity entity = Registry.ENTITY_TYPE.get(createIdentifier(this.type)).create(world);
		assert entity != null;
		entity.refreshPositionAndAngles(x, y, z, 0, 0);
		if(entity instanceof MobEntity) {
			((MobEntity) entity).initialize((ServerWorldAccess) world , world.getLocalDifficulty(new BlockPos(x, y, z)), SpawnReason.CHUNK_GENERATION, null, null);
		};
		try {
			CompoundTag tag = StringNbtReader.parse(nbt);
			tag.remove("Health");
			tag.putFloat("Health", 10F);
			entity.fromTag(tag);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		world.spawnEntity(entity);
	}
}
