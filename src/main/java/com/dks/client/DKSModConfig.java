package com.dks.client;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

@Config( modid = DKSMod.MODID )
public final class DKSModConfig
{
	@Comment( {
		"Current default setup for key bindings.",
		"It will be applied if option.txt does not exist."
	} )
	public static String[] default_key_setup = { };
	
	@Comment( "Whether the saved setup is created using Key Binding Patch." )
	public static boolean is_kbp_setup = false;
	
	
	private DKSModConfig() {
	}
}
