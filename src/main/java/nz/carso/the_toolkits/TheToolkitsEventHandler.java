package nz.carso.the_toolkits;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import nz.carso.the_toolkits.commands.AttributesCommand;
import nz.carso.the_toolkits.commands.JEISearchItemCommand;
import nz.carso.the_toolkits.commands.NBTCommand;
import nz.carso.the_toolkits.commands.TestCommand;
import nz.carso.the_toolkits.compat.jei.TheToolkitsJEI;
import nz.carso.the_toolkits.messages.MessageLinkItem;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SLASH;


public class TheToolkitsEventHandler {

    static KeyBinding linkKey;
    public static void init() {
        // move register KeyMapping logic to RegisterKeyMappingsEvent
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientOnlyModEventHandler {
        @SubscribeEvent
        public static void registryKeyBinding(FMLClientSetupEvent evt) {
            linkKey = new KeyBinding("key." + Constants.MOD_ID + ".link_key",
                    KeyConflictContext.GUI, KeyModifier.SHIFT, InputMappings.Type.KEYSYM, GLFW_KEY_SLASH,
                    "key.categories." + Constants.MOD_ID);
            ClientRegistry.registerKeyBinding(linkKey);
        }
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientOnlyForgeEventHandler {
        @SubscribeEvent
        public static void keyPress(InputEvent.KeyInputEvent event)
        {
            if (linkKey == null) {
                return;
            }
            if (linkKey.isActiveAndMatches(InputMappings.getKey(event.getKey(), event.getScanCode()))) {
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
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ContainerScreen<?>) {
                ContainerScreen<?> gui = (ContainerScreen<?>) screen;
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
            CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
            LiteralArgumentBuilder<CommandSource> builder = Commands.literal("the-toolkits")
                    .then(JEISearchItemCommand.register())
                    .then(NBTCommand.register())
                    .then(AttributesCommand.register())
                    .then(TestCommand.register());
            dispatcher.register(builder);

        }
    }
}
