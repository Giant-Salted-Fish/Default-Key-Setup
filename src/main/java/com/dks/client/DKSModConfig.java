package com.dks.client;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

@Config( modid = DKSMod.MODID )
public final class DKSModConfig
{
	@Comment( {
		"DKS mod relies on the existence of the options.txt file to detect whether it is a fresh",
		"startup of the game. But mods like Default Options will generate this file at the very",
		"beginning of the game startup, which makes this approach infeasible. So this option was",
		"introduced to force the key binding reset on next startup."
	} )
	public static boolean force_key_reset = false;
	
	@Comment( {
		"Current default setup for key bindings. It will be applied if option.txt does not exist.",
		"DO NOT TRY TO MODIFY THIS MANUALLY! USE THE CONFIG GUI IN GAME INSTEAD!"
	} )
	public static String[] default_key_setup = { };
	
	@Comment( {
		"Whether the saved setup is created using Key Binding Patch.",
		"DO NOT TRY TO MODIFY THIS MANUALLY! USE THE CONFIG GUI IN GAME INSTEAD!"
	} )
	public static boolean is_kbp_setup = false;
	
	
	private DKSModConfig() {
	}
}
