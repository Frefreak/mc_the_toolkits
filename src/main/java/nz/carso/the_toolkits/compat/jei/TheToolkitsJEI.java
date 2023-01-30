package nz.carso.the_toolkits.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;
import nz.carso.the_toolkits.Constants;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;
import nz.carso.the_toolkits.TheToolkits;
import nz.carso.the_toolkits.TheToolkitsPacketHandler;
import nz.carso.the_toolkits.messages.MessageLinkItem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@JeiPlugin
public class TheToolkitsJEI implements IModPlugin {
    private static IJeiRuntime theRuntime = null;
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(Constants.MOD_ID, "main");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        theRuntime = jeiRuntime;
    }

    public static ItemStack getItemStackUnderMouse() {
        if (theRuntime == null) {
            return null;
        }
        IIngredientListOverlay listOverlay = theRuntime.getIngredientListOverlay();
        Optional<ITypedIngredient<?>> ingredients = listOverlay.getIngredientUnderMouse();
        if (ingredients.isEmpty()) {
            return null;
        }
        Optional<ItemStack> itemStack = ingredients.get().getIngredient(VanillaTypes.ITEM_STACK);
        return itemStack.orElse(null);
    }

    public static void doSearch(String id) {
        if (theRuntime == null) {
            return;
        }
        IIngredientFilter filter = theRuntime.getIngredientFilter();
        filter.setFilterText("&" + id);
    }
}
