package com.comphenix.protocol.injector;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;

import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.PlayerChunkMap.EntityTracker;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;

import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityUtilitiesTest {

	@BeforeClass
	public static void beforeClass() {
		BukkitInitialization.initializeItemMeta();
	}

	// @Test
	public void testReflection() throws ReflectiveOperationException {
		CraftWorld bukkit = mock(CraftWorld.class);
		WorldServer world = mock(WorldServer.class);
		when(bukkit.getHandle()).thenReturn(world);

		ChunkProviderServer provider = mock(ChunkProviderServer.class);
		when(world.getChunkProvider()).thenReturn(provider);

		// TODO unsetting final doesn't work anymore
		PlayerChunkMap chunkMap = mock(PlayerChunkMap.class);
		Field chunkMapField = FuzzyReflection.fromClass(ChunkProviderServer.class, true)
				.getField(FuzzyFieldContract.newBuilder().typeExact(PlayerChunkMap.class).build());
		chunkMapField.setAccessible(true);
		chunkMapField.set(provider, chunkMap);

		CraftEntity bukkitEntity = mock(CraftEntity.class);
		Entity fakeEntity = mock(Entity.class);
		when(fakeEntity.getBukkitEntity()).thenReturn(bukkitEntity);

		PlayerChunkMap.EntityTracker tracker = mock(PlayerChunkMap.EntityTracker.class);
		FuzzyReflection.fromClass(EntityTracker.class, true)
				.getField(FuzzyFieldContract.newBuilder().typeExact(EntityTrackerEntry.class).build())
				.set(tracker, fakeEntity);

		Int2ObjectMap<PlayerChunkMap.EntityTracker> trackerMap = new Int2ObjectOpenHashMap<>();
		trackerMap.put(1, tracker);

		new StructureModifier<>(PlayerChunkMap.class, true)
				.withTarget(chunkMap)
				.withParamType(Int2ObjectMap.class, null, EntityTracker.class)
				.write(0, trackerMap);

		assertEquals(bukkitEntity, EntityUtilities.getInstance().getEntityFromID(bukkit, 1));
	}
}
