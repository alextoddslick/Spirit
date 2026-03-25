package me.codexadrian.spirit.data;

/**
 * Interface for data-driven "recipes" that use the recipe system as a data
 * loader. These don't actually craft anything - they just store data.
 *
 * In 1.21.11, the Recipe API changed significantly. Serializer/type/result
 * methods are now part of the Recipe interface directly with covariant return types.
 * This interface serves as a marker for Spirit's data-driven recipes.
 */
public interface SyncedData {
}
