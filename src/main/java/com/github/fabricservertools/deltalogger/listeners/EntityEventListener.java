package com.github.fabricservertools.deltalogger.listeners;

import com.github.fabricservertools.deltalogger.DatabaseManager;
import com.github.fabricservertools.deltalogger.dao.EntityDAO;
import com.github.fabricservertools.deltalogger.events.CreeperExplodeCallback;
import com.github.fabricservertools.deltalogger.events.EntityDeathCallback;
import com.github.fabricservertools.deltalogger.events.FireballExplodeCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class EntityEventListener {
	public EntityEventListener() {
		CreeperExplodeCallback.EVENT.register(this::onCreeperExplode);
		FireballExplodeCallback.EVENT.register(this::onFireballExplode);
		EntityDeathCallback.EVENT.register(this::onEntityDeath);
	}

	private ActionResult onEntityDeath(LivingEntity entity, DamageSource source) {
		Entity attacker = source.getAttacker();
		if (entity.getCustomName() == null) return ActionResult.PASS;

			UUID killer_id;
		if (attacker instanceof PlayerEntity) {
			killer_id = ((PlayerEntity) attacker).getUuid();
		} else {
			return ActionResult.PASS;
		}
		DatabaseManager.getSingleton().queueOp(EntityDAO.insertKill(
				entity.getCustomName().asString(),
				source.getName(),
				killer_id,
				java.time.Instant.now(),
				entity.getBlockPos(),
				attacker.getEntityWorld().getRegistryKey().getValue()
		));

		return ActionResult.PASS;
	}

	private ActionResult onFireballExplode(FireballEntity fireballEntity, HitResult hitResult) {
		Entity owner = fireballEntity.getOwner();
		if (!(owner instanceof GhastEntity)) return ActionResult.PASS;

		GhastEntity ghast = ((GhastEntity) owner);
		if (ghast.getTarget() == null || !(ghast.getTarget() instanceof PlayerEntity)) return ActionResult.PASS;

		DatabaseManager.getSingleton().queueOp(EntityDAO.insertMobGrief(
				((PlayerEntity) ghast.getTarget()).getUuid(),
				java.time.Instant.now(),
				Registry.ENTITY_TYPE.getId(EntityType.GHAST),
				fireballEntity.getBlockPos(),
				ghast.getEntityWorld().getRegistryKey().getValue()
		));

		return ActionResult.PASS;
	}

	private ActionResult onCreeperExplode(CreeperEntity creeperEntity) {
		LivingEntity target = creeperEntity.getTarget();
		if (target instanceof PlayerEntity) {
			PlayerEntity playerTarget = (PlayerEntity) target;

			DatabaseManager.getSingleton().queueOp(EntityDAO.insertMobGrief(
					playerTarget.getUuid(),
					java.time.Instant.now(),
					Registry.ENTITY_TYPE.getId(EntityType.CREEPER),
					creeperEntity.getBlockPos(),
					creeperEntity.getEntityWorld().getRegistryKey().getValue()
			));
		}

		return ActionResult.PASS;
	}
}
