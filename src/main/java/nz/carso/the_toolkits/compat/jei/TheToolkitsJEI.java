package nz.carso.the_toolkits.compat.jei;

import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import nz.carso.the_toolkits.Constants;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@JeiPlugin
public class TheToolkitsJEI implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static IJeiRuntime theRuntime = null;
    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Constants.MOD_ID, "main");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        theRuntime = jeiRuntime;
    }

    public static ItemStack getItemStackUnderMouse() {
        if (theRuntime == null) {
            return null;
        }
        IIngredientListOverlay listOverlay = theRuntime.getIngredientListOverlay();
        IIngredientType<ItemStack> ingredientType = theRuntime.getIngredientManager().getIngredientType(ItemStack.class);
        return listOverlay.getIngredientUnderMouse(ingredientType);
    }

    public static ItemStack getItemStackUnderMouseBookmark() {
        if (theRuntime == null) {
            return null;
        }
        IBookmarkOverlay bookmarkOverlay = theRuntime.getBookmarkOverlay();
        IIngredientType<ItemStack> ingredientType = theRuntime.getIngredientManager().getIngredientType(ItemStack.class);
        Object ingredient = bookmarkOverlay.getIngredientUnderMouse();
        if (ingredient instanceof ItemStack) {
            return (ItemStack)ingredient;
        }
        return null;
    }

    public static void doSearch(String text) {
        if (theRuntime == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        if (!mc.level.isClientSide) {
            LOGGER.error("somehow doSearch called in non-client side?");
            return;
        }
        IIngredientFilter filter = theRuntime.getIngredientFilter();
        filter.setFilterText(text);
        if (mc.player != null) {
            mc.setScreen(new InventoryScreen(mc.player));
            //mc.player.sendMessage(new TranslationTextComponent("disable opening inventory for now"), Util.NIL_UUID);
        }
    }
}
