package nz.carso.the_toolkits.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.settings.KeyBindingMap;
import nz.carso.the_toolkits.Constants;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

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

    public static void doSearch(Player player, String text) {
        if (theRuntime == null) {
            return;
        }
        IIngredientFilter filter = theRuntime.getIngredientFilter();
        filter.setFilterText(text);
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new InventoryScreen(player));

        //KeyMapping.click(Minecraft.getInstance().options.keyInventory.getKey());

    }
}
