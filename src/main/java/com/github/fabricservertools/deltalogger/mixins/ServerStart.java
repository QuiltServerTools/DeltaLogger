package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.ModInit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(MinecraftDedicatedServer.class)
public class ServerStart {
	@Inject(at = @At(value = "HEAD"), method = "setupServer")
	public void setupServer(CallbackInfoReturnable<Boolean> ret) throws IOException {
		ModInit.onServerInit((MinecraftServer) (Object) this);
	}
}
