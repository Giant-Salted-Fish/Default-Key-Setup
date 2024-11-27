package com.dks.client;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

@SideOnly( Side.CLIENT )
final class KeyBindingAccess
{
	private static final Field KeyBinding$keyCodeDefault = ObfuscationReflectionHelper.findField( KeyBinding.class, "field_151472_e" );
	static void _setKeyCodeDefault( KeyBinding kb, int key_code )
	{
		try {
			KeyBinding$keyCodeDefault.set( kb, key_code );
		}
		catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}
	
	private static final Field KeyBinding$keyModifierDefault = ObfuscationReflectionHelper.findField( KeyBinding.class, "keyModifierDefault" );
	static void _setKeyModifierDefault( KeyBinding kb, KeyModifier modifier )
	{
		try {
			KeyBinding$keyModifierDefault.set( kb, modifier );
		}
		catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}
	
	private static final Field KeyBinding$default_cmb_keys;
	static
	{
		if ( Loader.isModLoaded( "key_binding_patch" ) ) {
			KeyBinding$default_cmb_keys = ObfuscationReflectionHelper.findField( KeyBinding.class, "default_cmb_keys" );
		}
		else {
			KeyBinding$default_cmb_keys = null;
		}
	}
	static void _setDefaultCmbKeys( KeyBinding kb, ImmutableSet< Integer > cmb_keys )
	{
		try {
			KeyBinding$default_cmb_keys.set( kb, cmb_keys );
		}
		catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}
	
	private KeyBindingAccess() {
	}
}
