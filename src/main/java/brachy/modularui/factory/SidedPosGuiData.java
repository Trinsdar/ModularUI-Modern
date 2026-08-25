package brachy.modularui.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
@Getter
public class SidedPosGuiData extends PosGuiData {

    @NonNull
    private final Direction side;

    public SidedPosGuiData(@NonNull Player player, BlockPos pos, @NonNull Direction side) {
        super(player, pos);
        this.side = side;
    }
}
