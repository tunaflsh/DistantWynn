package me.tunaflsh.distantwynn.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			if (MixinConfigPlugin.hasSodium()) {
				var page = (OptionPage) ConfigManager.CONFIG.getModOptions().stream()
						.filter(modOptions -> modOptions.configId().equals("distantwynn"))
						.findFirst().get()
						.pages().get(0);
				return (VideoSettingsScreen) VideoSettingsScreen.createScreen(parent, page);
			}
			return null;
		};
	}
}
