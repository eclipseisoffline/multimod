package xyz.eclipseisoffline.multimod

plugins {
    id("xyz.eclipseisoffline.multimod.modding-base-conventions")
}

val modConfiguration = project.extensions.getByType(ModConfigurationExtension::class)

dependencies {
    //implementation("net.minecraft:neoform_joined:${modConfiguration.minecraft.get().get().version + "-${modConfiguration.neoFormTimestamp.get()}"}")
}
