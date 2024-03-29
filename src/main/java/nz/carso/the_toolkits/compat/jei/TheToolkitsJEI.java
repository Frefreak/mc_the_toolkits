package nz.carso.the_toolkits.compat.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.item.ItemStack;
import nz.carso.the_toolkits.Constants;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.slf4j.Logger;

import java.util.Optional;

@JeiPlugin
public class TheToolkitsJEI implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
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

    public static ItemStack getItemStackUnderMouseBookmark() {
        if (theRuntime == null) {
            return null;
        }
        IBookmarkOverlay bookmarkOverlay = theRuntime.getBookmarkOverlay();
        Optional<ITypedIngredient<?>> ingredients = bookmarkOverlay.getIngredientUnderMouse();
        if (ingredients.isEmpty()) {
            return null;
        }
        Optional<ItemStack> itemStack = ingredients.get().getIngredient(VanillaTypes.ITEM_STACK);
        return itemStack.orElse(null);
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
            mc.tell(() -> mc.setScreen(new InventoryScreen(mc.player)));
            //mc.player.sendSystemMessage(Component.literal("disable opening inventory for now"));
        }
    }

}
