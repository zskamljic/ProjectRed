package mrtjp.projectred.fabrication.engine.gates;

import codechicken.lib.vec.*;
import mrtjp.projectred.fabrication.editor.ICWorkbenchEditor;
import mrtjp.projectred.fabrication.editor.tools.InteractionZone;
import mrtjp.projectred.fabrication.editor.tools.SimpleInteractionZone;
import mrtjp.projectred.fabrication.engine.ICInterfaceType;
import net.minecraft.network.chat.Component;

import java.util.List;

import static mrtjp.projectred.fabrication.init.FabricationUnlocal.*;

public class RedstoneIOGateTile extends SingleBitIOGateTile {

    private static final Cuboid6[] INPUT_TOGGLE_ZONE_BOUNDS = new Cuboid6[4];
    private static final Cuboid6[] DIR_ZONE_BOUNDS = new Cuboid6[4];

    static {
        for (int r = 0; r < 4; r++) {
            Transformation t = new Scale(1/16D).with(Rotation.quarterRotations[r].at(Vector3.CENTER));
            INPUT_TOGGLE_ZONE_BOUNDS[r] = new Cuboid6(1, 2, 0, 15, 3, 3).apply(t);       // Toggle state of IO register
            DIR_ZONE_BOUNDS[r]          = new Cuboid6(6, 2, 8, 10, 4, 12).apply(t);      // Toggle IO direction
        }
    }

    public RedstoneIOGateTile() {
        super(ICGateTileType.REDSTONE_IO);
    }

    @Override
    public ICInterfaceType getInterfaceType() {
        return ICInterfaceType.REDSTONE;
    }

    //region BaseTile overrides
    @Override
    public void buildInteractionZoneList(List<InteractionZone> zones) {
        super.buildInteractionZoneList(zones);

        // For toggling input to simulation
        zones.add(new SimpleInteractionZone.Builder()
                .bounds(() -> INPUT_TOGGLE_ZONE_BOUNDS[getRotation()])
                .leftClickAction(this::toggleWorldInput)
                .tooltip(toolTip -> {
                    toolTip.add(Component.translatable(isInputIOMode() ? UL_SIM_INPUT : UL_SIM_OUTPUT)
                            .append(Component.literal(": " + ((getState() & 0x44) != 0 ? "0x1" : "0x0")))
                            .withStyle(ICWorkbenchEditor.UNIFORM_GRAY));
                })
                .build());

        // For toggling input/output direction
        zones.add(new SimpleInteractionZone.Builder()
                .bounds(() -> DIR_ZONE_BOUNDS[getRotation()])
                .leftClickAction(this::toggleDirection)
                .tooltip(toolTip -> {
                    toolTip.add(Component.translatable(UL_IO_DIRECTION)
                            .append(Component.literal(": "))
                            .append(Component.translatable((isInputIOMode() ? UL_IO_DIR_INPUT : UL_IO_DIR_OUTPUT)))
                            .withStyle(ICWorkbenchEditor.UNIFORM_GRAY));
                })
                .build());
    }
    //endregion

}
