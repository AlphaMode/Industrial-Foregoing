package com.buuz135.industrial.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobAccessor {
    @Invoker
    void callDropCustomDeathLoot(DamageSource damageSource, int i, boolean bl);
}
