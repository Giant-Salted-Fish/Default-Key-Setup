package com.dks.client;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.impl.IKeyBindingImpl;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.extensions.IForgeKeybinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Mod( "default_key_setup" )
@EventBusSubscriber
public final class DKSMod
{
	private static void __saveDefaultKeySetup()
	{
		final boolean has_kbp_mod = ModList.get().isLoaded( "key_binding_patch" );
		final BiConsumer< KeyBinding, StringBuilder > append_value;
		if ( has_kbp_mod ) {
			append_value = ( kb, builder ) -> builder.append( kb.saveString() );
		}
		else
		{
			append_value = ( kb, builder ) -> {
				builder.append( kb.saveString() );
				builder.append( ':' );
				builder.append( kb.getKeyModifier() );
			};
		}
		final Minecraft mc = Minecraft.getInstance();
		DKSModConfig.DEFAULT_KEY_SETUP.set(
			Arrays.stream( mc.options.keyMappings )
			.map( kb -> {
				final StringBuilder builder = new StringBuilder( kb.getName() );
				builder.append( '=' );
				append_value.accept( kb, builder );
				return builder.toString();
			} )
			.collect( Collectors.toList() )
		);
		DKSModConfig.DEFAULT_KEY_SETUP.save();
	}
	
	private static void __applyDefaultKeySetup()
	{
		final boolean has_kbp_mod = ModList.get().isLoaded( "key_binding_patch" );
		final Map< String, String > data = (
			DKSModConfig.DEFAULT_KEY_SETUP.get().stream()
			.map( s -> {
				final int idx = s.indexOf( '=' );
				final String name = s.substring( 0, idx );
				final String value = s.substring( idx + 1 );
				return Pair.of( name, value );
			} )
			.collect( Collectors.toMap( Pair::getLeft, Pair::getRight ) )
		);
		
		final Minecraft mc = Minecraft.getInstance();
		for ( KeyBinding kb : mc.options.keyMappings )
		{
			final String value = data.get( kb.getName() );
			if ( value != null )
			{
				final String[] split = value.split( ":" );
				final Input key = InputMappings.getKey( split[ 0 ] );
				final KeyModifier modifier = KeyModifier.valueFromString( split[ 1 ] );
				KeyBindingAccess._setDefaultKey( kb, key );
				KeyBindingAccess._setKeyModifierDefault( kb, modifier );
				if ( has_kbp_mod )
				{
					final ImmutableSet< Input > cmb_keys;
					if ( split.length > 2 )
					{
						cmb_keys = (
							Arrays.stream( split[ 2 ].split( "\\+" ) )
							.map( InputMappings::getKey )
							.collect( ImmutableSet.toImmutableSet() )
						);
					}
					else {
						cmb_keys = IKeyBindingImpl.toCmbKeySet( modifier );
					}
					KeyBindingAccess._setDefaultCmbKeys( kb, cmb_keys );
				}
			}
		}
	}
	
	@SubscribeEvent
	static void onConfigReload( ModConfig.Reloading evt ) {
		__applyDefaultKeySetup();
	}
	
	public DKSMod()
	{
		// Make sure the mod being absent on the other network side does not
		// cause the client to display the server as incompatible.
		final ModLoadingContext load_ctx = ModLoadingContext.get();
		load_ctx.registerExtensionPoint(
			ExtensionPoint.DISPLAYTEST,
			() -> Pair.of(
				() -> "This is a client only mod.",
				( remote_version_string, network_bool ) -> network_bool
			)
		);
		
		// Setup mod config settings.
		load_ctx.registerConfig( Type.CLIENT, DKSModConfig.CONFIG_SPEC );
		load_ctx.registerExtensionPoint(
			ExtensionPoint.CONFIGGUIFACTORY,
			() -> ( mc, screen ) -> new ConfirmScreen(
				result -> {
					if ( result )
					{
						__saveDefaultKeySetup();
						__applyDefaultKeySetup();
					}
					mc.setScreen( screen );
				},
				new TranslationTextComponent( "dks.gui.warning_title" ),
				new TranslationTextComponent( "dks.gui.warning_msg" )
			)
		);
		
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void onOpenGui( GuiOpenEvent evt )
			{
				final boolean should_reset_kb;
				if ( DKSModConfig.FORCE_KEY_RESET.get() )
				{
					DKSModConfig.FORCE_KEY_RESET.set( false );
					DKSModConfig.FORCE_KEY_RESET.save();
					should_reset_kb = true;
				}
				else
				{
					final Minecraft mc = Minecraft.getInstance();
					final File file = ObfuscationReflectionHelper.getPrivateValue( GameSettings.class, mc.options, "field_74354_ai" );
					should_reset_kb = !Objects.requireNonNull( file ).exists();
				}
				
				__applyDefaultKeySetup();
				
				if ( should_reset_kb )
				{
					final Minecraft mc = Minecraft.getInstance();
					Arrays.stream( mc.options.keyMappings ).forEachOrdered( IForgeKeybinding::setToDefault );
					KeyBinding.resetMapping();
				}
				
				MinecraftForge.EVENT_BUS.unregister( this );
			}
		} );
	}
}
