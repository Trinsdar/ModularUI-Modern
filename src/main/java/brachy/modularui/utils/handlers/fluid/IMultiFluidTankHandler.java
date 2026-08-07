package brachy.modularui.utils.handlers.fluid;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IMultiFluidTankHandler extends IFluidHandler {

    IFluidTank getFluidTank(int index);
}
