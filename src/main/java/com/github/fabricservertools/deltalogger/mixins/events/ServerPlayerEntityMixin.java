package com.github.fabricservertools.deltalogger.mixins.events;

import com.github.fabricservertools.deltalogger.events.PlayerOpenScreenCallback;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(at = @At(value = "RETURN", ordinal = 2), method = "openHandledScreen", cancellable = true)
    public void openHandledScreen(NamedScreenHandlerFactory nameableContainerFactory, CallbackInfoReturnable<OptionalInt> info) {
        ActionResult result = PlayerOpenScreenCallback.EVENT.invoker().openScreen((ServerPlayerEntity) (Object) this, nameableContainerFactory);

        if (result != ActionResult.PASS) {
            info.cancel();
        }
    }
}