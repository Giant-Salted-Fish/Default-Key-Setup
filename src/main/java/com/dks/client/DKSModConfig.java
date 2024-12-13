package com.dks.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

@OnlyIn( Dist.CLIENT )
final class DKSModConfig
{
	static final ForgeConfigSpec CONFIG_SPEC;
	static final BooleanValue FORCE_KEY_RESET;
	static final ConfigValue< List< ? extends String > > DEFAULT_KEY_SETUP;
	
	static
	{
		final var builder = new Builder();
		FORCE_KEY_RESET = (
			builder.comment(
				"DKS mod relies on the existence of the options.txt file to detect whether it is a fresh",
				"startup of the game. But mods like Default Options will generate this file at the very",
				"beginning of the game startup, which makes this approach infeasible. So this option was",
				"introduced to force the key binding reset on next startup."
			)
			.define( "force_key_reset", false )
		);
		
		DEFAULT_KEY_SETUP = (
			builder.comment(
				"Current default setup for key bindings. It will be applied if option.txt does not exist.",
				"DO NOT TRY TO MODIFY THIS MANUALLY! USE THE CONFIG GUI IN GAME INSTEAD!"
			)
			.defineList( "default_key_setup", List.of(), String.class::isInstance )
		);
		
		CONFIG_SPEC = builder.build();
	}
	
	private DKSModConfig() {
	}
}
