package com.dks.client;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.impl.IKeyMappingImpl;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Mod( "default_key_setup" )
@EventBusSubscriber
public final class DKSMod
{
	private static void __saveDefaultKeySetup()
	{
		final var mc = Minecraft.getInstance();
		DKSModConfig.DEFAULT_KEY_SETUP.set(
			Arrays.stream( mc.options.keyMappings )
			.map( km -> String.format( "%s=%s", km.getName(), km.saveString() ) )
			.toList()
		);
		DKSModConfig.DEFAULT_KEY_SETUP.save();
	}
	
	private static void __applyDefaultKeySetup()
	{
		final var has_kbp_mod = ModList.get().isLoaded( "key_binding_patch" );
		final BiConsumer< KeyMapping, String[] > setup_rest;
		if ( has_kbp_mod )
		{
			setup_rest = ( km, split ) -> {
				final ImmutableSet< Key > cmb_keys;
				final KeyModifier modifier;
				if ( split.length == 1 )
				{
					cmb_keys = ImmutableSet.of();
					modifier = KeyModifier.NONE;
				}
				else
				{
					modifier = KeyModifier.valueFromString( split[ 1 ] );
					if ( split.length > 2 )
					{
						cmb_keys = (
							Arrays.stream( split[ 2 ].split( "\\+" ) )
							.map( InputConstants::getKey )
							.collect( ImmutableSet.toImmutableSet() )
						);
					}
					else {
						cmb_keys = IKeyMappingImpl.toCmbKeySet( modifier );
					}
				}
				
				KeyMappingAccess._setKeyModifierDefault( km, modifier );
				KeyMappingAccess._setDefaultCmbKeys( km, cmb_keys );
			};
		}
		else
		{
			// KBP mod is not installed, so can not use IKeyMappingImpl#toModifier(...).
			setup_rest = ( km, split ) -> {
				final String value = split.length > 1 ? split[ 1 ] : "";
				final var modifier = KeyModifier.valueFromString( value );
				KeyMappingAccess._setKeyModifierDefault( km, modifier );
			};
		}
		
		final var data = (
			DKSModConfig.DEFAULT_KEY_SETUP.get().stream()
			.map( s -> {
				final var idx = s.indexOf( '=' );
				final var name = s.substring( 0, idx );
				final var value = s.substring( idx + 1 );
				return Pair.of( name, value );
			} )
			.collect( Collectors.toMap( Pair::getFirst, Pair::getSecond ) )
		);
		
		final var mc = Minecraft.getInstance();
		for ( var km : mc.options.keyMappings )
		{
			final var value = data.get( km.getName() );
			if ( value != null )
			{
				final var split = value.split( ":" );
				final var key = InputConstants.getKey( split[ 0 ] );
				KeyMappingAccess._setDefaultKey( km, key );
				setup_rest.accept( km, split );
			}
		}
	}
	
	@SubscribeEvent
	static void onConfigReload( ModConfigEvent.Reloading evt ) {
		__applyDefaultKeySetup();
	}
	
	public DKSMod()
	{
		// Make sure the mod being absent on the other network side does not
		// cause the client to display the server as incompatible.
		// Make sure the mod being absent on the other network side does not
		// cause the client to display the server as incompatible.
		final var load_ctx = ModLoadingContext.get();
		load_ctx.registerExtensionPoint(
			DisplayTest.class,
			() -> new DisplayTest(
				() -> "This is a client only mod.",
				( remote_version_string, network_bool ) -> network_bool
			)
		);
		
		// Setup mod config settings.
		load_ctx.registerConfig( Type.CLIENT, DKSModConfig.CONFIG_SPEC );
		load_ctx.registerExtensionPoint(
			ConfigScreenFactory.class,
			() -> new ConfigScreenFactory( ( mc, screen ) -> new ConfirmScreen(
				result -> {
					if ( result )
					{
						__saveDefaultKeySetup();
						__applyDefaultKeySetup();
					}
					mc.setScreen( screen );
				},
				Component.translatable( "dks.gui.warning_title" ),
				Component.translatable( "dks.gui.warning_msg" )
			) )
		);
		
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void onOpenGui( ScreenEvent.Opening evt )
			{
				final boolean should_reset_km;
				if ( DKSModConfig.FORCE_KEY_RESET.get() )
				{
					DKSModConfig.FORCE_KEY_RESET.set( false );
					DKSModConfig.FORCE_KEY_RESET.save();
					should_reset_km = true;
				}
				else
				{
					final var mc = Minecraft.getInstance();
					final File file = ObfuscationReflectionHelper.getPrivateValue( Options.class, mc.options, "f_92110_");
					should_reset_km = !Objects.requireNonNull( file ).exists();
				}
				
				__applyDefaultKeySetup();
				
				if ( should_reset_km )
				{
					final var mc = Minecraft.getInstance();
					Arrays.stream( mc.options.keyMappings ).forEachOrdered( IForgeKeyMapping::setToDefault );
					KeyMapping.resetMapping();
				}
				
				MinecraftForge.EVENT_BUS.unregister( this );
			}
		} );
	}
}
