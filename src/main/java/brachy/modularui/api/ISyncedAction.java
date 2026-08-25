package brachy.modularui.api;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface ISyncedAction {

    @ApiStatus.OverrideOnly
    void invoke(@NonNull FriendlyByteBuf packet);
}
