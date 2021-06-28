package rmc.libs.event_factory;

import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayerFactory;

/**
 * Developed by RMC Team, 2021
 * @author KR33PY
 */
public abstract class EventFactory {

    public static @Nullable ServerPlayerEntity convert(@Nullable Entity entity) {
        return entity instanceof ServerPlayerEntity ? (ServerPlayerEntity) entity : null;
    }

    public static @Nullable ServerPlayerEntity convertFake(@Nullable World world, @Nullable UUID id) {
        return world instanceof ServerWorld && id != null ? FakePlayerFactory.get((ServerWorld) world, new GameProfile(id, "[RMC-Event-Factory]")) : null;
    }

    public static boolean testBlockBreak(@Nullable ServerPlayerEntity player, @Nonnull World world, @Nonnull BlockPos target) {
        return testBlockBreak(player, world, target, null);
    }

    public static boolean testBlockBreak(@Nullable ServerPlayerEntity player, @Nonnull World world, @Nonnull BlockPos target, @Nullable GameProfile fake) {
        return testEvent(player, world, fake, args -> new BlockBreakEvent(CraftBlock.at((ServerWorld) args[1], (BlockPos) args[2]), ((ServerPlayerEntity) args[0]).getBukkitEntity()), target);
    }

    public static boolean testEntityInteract(@Nullable ServerPlayerEntity player, @Nonnull World world, @Nonnull Entity target) {
        return testEntityInteract(player, world, target, null);
    }

    public static boolean testEntityInteract(@Nullable ServerPlayerEntity player, @Nonnull World world, @Nonnull Entity target, @Nullable GameProfile fake) {
        return testEvent(player, world, fake, args -> new PlayerInteractEntityEvent(((ServerPlayerEntity) args[0]).getBukkitEntity(), ((Entity) args[2]).getBukkitEntity()), target);
    }

    private static boolean testEvent(ServerPlayerEntity player, World world, GameProfile fake, Function<Object[], Event> efunc, Object... eargs) {
        if (!(world instanceof ServerWorld)) {
            throw new IllegalArgumentException("world must be not null and instanceof ServerWorld!");
        }
        for (int i = 0; i < eargs.length; i++) {
            if (eargs[i] == null) {
                throw new IllegalArgumentException("event args must not be null!");
            }
        }
        ServerWorld sWorld = (ServerWorld) world;
        ServerPlayerEntity sPlayer = player;
        if (sPlayer == null) {
            if (fake != null) {
                sPlayer = FakePlayerFactory.get(sWorld, fake);
            }
            else {
                return false;
            }
        }
        Object[] args = new Object[eargs.length + 2];
        args[0] = sPlayer;
        args[1] = sWorld;
        System.arraycopy(eargs, 0, args, 2, eargs.length);
        Event event = efunc.apply(args);
        Bukkit.getPluginManager().callEvent(event);
        return !((Cancellable) event).isCancelled();
    }

}