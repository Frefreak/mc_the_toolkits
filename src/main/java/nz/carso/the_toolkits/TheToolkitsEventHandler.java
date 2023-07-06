package nz.carso.the_toolkits;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nz.carso.the_toolkits.commands.*;
import nz.carso.the_toolkits.compat.jei.TheToolkitsJEI;
import nz.carso.the_toolkits.messages.MessageLinkItem;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SLASH;


public class TheToolkitsEventHandler {

    static KeyMapping linkKey;
    public static void init() {
        // move register KeyMapping logic to RegisterKeyMappingsEvent
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientOnlyModEventHandler {
        @SubscribeEvent
        public static void registryKeyBinding(RegisterKeyMappingsEvent evt) {
            linkKey = new KeyMapping("key." + Constants.MOD_ID + ".link_key",
                    KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW_KEY_SLASH,
                    "key.categories." + Constants.MOD_ID);
            evt.register(linkKey);
        }
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientOnlyForgeEventHandler {
        @SubscribeEvent
        public static void keyPress(ScreenEvent.KeyPressed.Post event)
        {
            if (linkKey == null) {
                return;
            }
            if (!linkKey.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
                return;
            }
            // first try JEI ingredient list or bookmark overlay
            if (TheToolkits.isJEIAvailable()) {
                // ingredient list
                ItemStack is = TheToolkitsJEI.getItemStackUnderMouse();
                if (is != null) {
                    TheToolkitsPacketHandler.sendToServer(new MessageLinkItem(is));
                    return;
                }
                // bookmark
                is = TheToolkitsJEI.getItemStackUnderMouseBookmark();
                if (is != null) {
                    TheToolkitsPacketHandler.sendToServer(new MessageLinkItem(is));
                    return;
                }
            }
            // try player inventory
            Screen screen = event.getScreen();
            if (screen instanceof AbstractContainerScreen<?> gui) {
                Slot slot = gui.getSlotUnderMouse();
                if (slot != null) {
                    ItemStack is = slot.getItem();
                    if (!is.isEmpty()) {
                        TheToolkitsPacketHandler.sendToServer(new MessageLinkItem(is));
                    }
                }
            }

        }
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class CommonForgeEventHandler {
        @SubscribeEvent
        public static void registerCommand(RegisterCommandsEvent event)
        {
            CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("the-toolkits")
                    .then(JEISearchItemCommand.register())
                    .then(NBTCommand.register())
                    .then(DumpBlockEntityCommand.register())
                    .then(DumpEntityCommand.register())
                    .then(RecipeCommand.register())
                    .then(AttributesCommand.register());
            dispatcher.register(builder);

        }
    }
}
