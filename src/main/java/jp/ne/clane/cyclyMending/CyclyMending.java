package jp.ne.clane.cyclyMending;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import jp.ne.clane.cyclyMending.commons.ClientUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CyclyMending.MOD_ID)
public class CyclyMending {
	public static final String MOD_ID = "cyclymending";
	public static final String MOD_NAME = "CyclyMending";
	public static final String[] MOD_AUTHORS = {"ALFEECLARE@CLANE SOFTWARE"};
	private static final Logger log = LogManager.getLogger(MOD_ID);
	public static Minecraft mc;

	private static CyclyMending instance;

	private CyclyMendingConfig config;

	private KeyMapping mendingModeKey;
	public static boolean isMendingMode = false;
	
	public static void log(String message) {
		log.info("[{}] {}", log.getName(), message);
	}

	/**
	 * Reload modules
	 */
	public CyclyMending modules() {
		try {
			mc = Minecraft.getInstance();
			if (mc.levelRenderer != null)
					mc.levelRenderer.allChanged();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return this;
	}

	public CyclyMending(IEventBus modEventBus, ModContainer modContainer) {
		instance = this;
		IEventBus bus = modEventBus;
		bus.addListener(this::setup);
		bus.addListener(this::registerKeyBinding);

		NeoForge.EVENT_BUS.register(this);
		log("CyclyMending Started");
	}

	/**
	 * get this mod
	 */
	public static CyclyMending getMod() {
		return instance;
	}

	private void setup(final FMLCommonSetupEvent event) {
		setupCompatibility();
		config = new CyclyMendingConfig();
		try {
			config.loadConfig(config.getConfigFile());
		} catch (IllegalAccessException | IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		try {
			config.saveConfig(config.getConfigFile());
		} catch (IllegalAccessException | IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		log("cyclyMending Init");
	}

	private void setupCompatibility() {
    	LoadingModList modlist = FMLLoader.getLoadingModList();
    	ClientUtils.ipn = modlist.getModFileById("inventoryprofilesnext");
    }
	
	public Minecraft getMC() {
		if (mc == null) {	
			try {
				mc = Minecraft.getInstance();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		return mc;
	}

	@SubscribeEvent
	public void onKeyEvent(InputEvent.Key ev) {
		Minecraft client = Minecraft.getInstance();
		if (client.screen != null)
			return;

		if (mendingModeKey.consumeClick()) {
			isMendingMode = !isMendingMode;
			getMC().player.sendSystemMessage(Component.translatable("jp.ne.clane.cyclyMending.message." + (isMendingMode ? "activated" : "deactivated")));
		}
	}

	private void registerKeyBinding(final RegisterKeyMappingsEvent ev) {
		mendingModeKey = new KeyMapping("jp.ne.clane.cyclyMending.toggle", GLFW.GLFW_KEY_PERIOD, "jp.ne.clane.cyclyMending.title");

		ev.register(mendingModeKey);
	}
}
