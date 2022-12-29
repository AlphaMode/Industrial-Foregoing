package com.buuz135.industrial.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public record SimpleRecipeType<T extends Recipe<?>>(ResourceLocation name) implements RecipeType<T> {
    @Override
    public String toString() {
        return name.toString();
    }
}
