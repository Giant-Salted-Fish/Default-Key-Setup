package com.dks.client;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.KBPMod;
import com.kbp.client.api.IPatchedKeyBinding;
import com.kbp.client.impl.IKeyBindingImpl;
import com.mojang.realmsclient.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.PostConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod(
	modid = DKSMod.MODID,
	version = "1.12.2-1.0.0.0",
	clientSideOnly = true,
	updateJSON = "https://raw.githubusercontent.com/Giant-Salted-Fish/Default-Key-Setup/1.12.2/update.json",
	acceptedMinecraftVersions = "[1.12,1.13)",
	guiFactory = "com.dks.client.ConfigGuiFactory"
)
@EventBusSubscriber
public final class DKSMod
{
	public static final String MODID = "default_key_setup";
	
	
	// Internal implementations that should not be accessed by other mods.
	static void _saveDefaultKeySetup()
	{
		final Minecraft mc = Minecraft.getMinecraft();
		final Function< KeyBinding, String > to_save_str;
		final boolean has_kbp_mod = Loader.isModLoaded( "key_binding_patch" );
		if ( has_kbp_mod )
		{
			to_save_str = kb -> {
				final IPatchedKeyBinding ikb = KBPMod.getPatched( kb );
				final String name = kb.getKeyDescription();
				final String key = Integer.toString( kb.getKeyCode() );
				final String cmb_keys = (
					ikb.getCmbKeys().stream()
					.map( Object::toString )
					.collect( Collectors.joining( "+" ) )
				);
				return String.format( "%s=%s:%s", name, key, cmb_keys );
			};
		}
		else
		{
			to_save_str = kb -> {
				final String name = kb.getKeyDescription();
				final String key = Integer.toString( kb.getKeyCode() );
				final String modifier = kb.getKeyModifier().toString();
				return String.format( "%s=%s:%s", name, key, modifier );
			};
		}
		
		DKSModConfig.default_key_setup = (
			Arrays.stream( mc.gameSettings.keyBindings )
			.map( to_save_str )
			.toArray( String[]::new )
		);
		DKSModConfig.is_kbp_setup = has_kbp_mod;
		
		final boolean is_world_running = mc.world != null;
		final OnConfigChangedEvent evt = new OnConfigChangedEvent( MODID, null, is_world_running, false );
		MinecraftForge.EVENT_BUS.post( evt );
		if ( evt.getResult() != Result.DENY )
		{
			final PostConfigChangedEvent evt1 = new PostConfigChangedEvent( MODID, null, is_world_running, false );
			MinecraftForge.EVENT_BUS.post( evt1 );
		}
	}
	
	private static void __applyDefaultKeySetup()
	{
		final Map< String, String > data = (
			Arrays.stream( DKSModConfig.default_key_setup )
			.map( s -> {
				final int idx = s.indexOf( '=' );
				final String name = s.substring( 0, idx );
				final String value = s.substring( idx + 1 );
				return Pair.of( name, value );
			} )
			.collect( Collectors.toMap( Pair::first, Pair::second ) )
		);
		
		final boolean has_kbp_mod = Loader.isModLoaded( "key_binding_patch" );
		final int case_id = ( has_kbp_mod ? 0b10 : 0b00 ) | ( DKSModConfig.is_kbp_setup ? 0b01 : 0b00 );
		final BiConsumer< KeyBinding, String > setup_rest;
		switch ( case_id )
		{
		case 0b00:
			setup_rest = ( kb, value ) -> {
				final KeyModifier modifier = KeyModifier.valueFromString( value );
				KeyBindingAccess._setKeyModifierDefault( kb, modifier );
			};
			break;
		case 0b01:
			setup_rest = ( kb, value ) -> {
				// KBP mod is not installed, so can not use IKeyBindingImpl#toModifier(...).
				KeyBindingAccess._setKeyModifierDefault( kb, KeyModifier.NONE );
			};
			break;
		case 0b10:
			setup_rest = ( kb, value ) -> {
				final KeyModifier modifier = KeyModifier.valueFromString( value );
				final ImmutableSet< Integer > cmb_keys = IKeyBindingImpl.toCmbKeySet( modifier );
				KeyBindingAccess._setKeyModifierDefault( kb, modifier );
				KeyBindingAccess._setDefaultCmbKeys( kb, cmb_keys );
			};
			break;
		case 0b11:
			setup_rest = ( kb, value ) -> {
				final ImmutableSet< Integer > cmb_keys;
				if ( !value.isEmpty() )
				{
					cmb_keys = (
						Arrays.stream( value.split( "\\+" ) )
						.map( Integer::parseInt )
						.collect( ImmutableSet.toImmutableSet() )
					);
				}
				else {
					cmb_keys = ImmutableSet.of();
				}
				final KeyModifier modifier = IKeyBindingImpl.toModifier( cmb_keys );
				KeyBindingAccess._setKeyModifierDefault( kb, modifier );
				KeyBindingAccess._setDefaultCmbKeys( kb, cmb_keys );
			};
			break;
		default:
			throw new AssertionError( "Invalid case_id: " + case_id );
		}
		
		final Minecraft mc = Minecraft.getMinecraft();
		for ( KeyBinding kb : mc.gameSettings.keyBindings )
		{
			final String value = data.get( kb.getKeyDescription() );
			if ( value != null )
			{
				final int idx = value.indexOf( ':' );
				final int key_code = Integer.parseInt( value.substring( 0, idx ) );
				KeyBindingAccess._setKeyCodeDefault( kb, key_code );
				setup_rest.accept( kb, value.substring( idx + 1 ) );
			}
		}
	}
	
	@SubscribeEvent
	static void onConfigChanged( OnConfigChangedEvent evt )
	{
		if ( evt.getModID().equals( MODID ) )
		{
			ConfigManager.sync( MODID, Config.Type.INSTANCE );
			__applyDefaultKeySetup();
		}
	}
	
	
	public DKSMod()
	{
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void onOpenGui( GuiOpenEvent evt )
			{
				__applyDefaultKeySetup();
				
				final Minecraft mc = Minecraft.getMinecraft();
				final File file = new File( mc.gameDir, "options.txt" );
				if ( !file.exists() )
				{
					Arrays.stream( mc.gameSettings.keyBindings ).forEachOrdered( KeyBinding::setToDefault );
					KeyBinding.resetKeyBindingArrayAndHash();
				}
				
				MinecraftForge.EVENT_BUS.unregister( this );
			}
		} );
	}
}
