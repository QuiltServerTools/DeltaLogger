package com.github.fabricservertools.deltalogger.mixins;

import com.github.fabricservertools.deltalogger.NbtUuid;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.screen.ScreenHandler;

import java.util.UUID;

/**
 * Associate ScreenHandlers with the containers they represent when they are opened
 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements NbtUuid {
    private UUID uuid;

    @Override
    public void setNbtUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getNbtUuid() {
        return uuid;
    }
}
