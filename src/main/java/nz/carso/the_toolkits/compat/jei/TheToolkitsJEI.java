package nz.carso.the_toolkits.compat.jei;

import mezz.jei.api.runtime.IJeiRuntime;
import nz.carso.the_toolkits.Constants;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;
import nz.carso.the_toolkits.TheToolkits;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class TheToolkitsJEI implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(Constants.MOD_ID, "main");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        TheToolkits.jeiRuntime = jeiRuntime;
    }

}
