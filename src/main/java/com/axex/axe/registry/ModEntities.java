package com.axex.axe.registry;

import com.axex.axe.AxeMod;
import com.axex.axe.entity.ThrownAxeEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AxeMod.MOD_ID);

    public static final RegistryObject<EntityType<ThrownAxeEntity>> THROWN_AXE = ENTITIES.register("thrown_axe",
            () -> EntityType.Builder.<ThrownAxeEntity>of(ThrownAxeEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("thrown_axe"));

    private ModEntities() {
    }
}
