package mypals.ml.GUI;

import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class BlueprintGroup {
    public enum BlueprintState {
        LOADING,
        LOADED,
        TIMEOUT
    }
    public BlueprintState state = BlueprintState.LOADING;
    public String name;
    public SchematicPlacement placement;
    public BlueprintGroup (String name, SchematicPlacement placement) {
        this.name = name;
        this.placement = placement;
    }
    public void refreshList() {
        placement.getMaterialList().reCreateMaterialList();
    }
    public String getName() {
        return name;
    }
    public List<MaterialListEntry> getItems() {
        return placement.getMaterialList().getMaterialsAll();
    }

    public BlueprintState getState() {
        return state;
    }

    public void setState(BlueprintState state) {
        this.state = state;
    }
}
