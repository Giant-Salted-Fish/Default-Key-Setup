package com.dks.client;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

@OnlyIn( Dist.CLIENT )
final class KeyBindingAccess
{
	private static final Field KeyBinding$defaultKey = ObfuscationReflectionHelper.findField( KeyBinding.class, "field_151472_e" );
	static void _setDefaultKey( KeyBinding kb, Input key )
	{
		try {
			KeyBinding$defaultKey.set( kb, key );
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
		if ( ModList.get().isLoaded( "key_binding_patch" ) ) {
			KeyBinding$default_cmb_keys = ObfuscationReflectionHelper.findField( KeyBinding.class, "default_cmb_keys" );
		}
		else {
			KeyBinding$default_cmb_keys = null;
		}
	}
	static void _setDefaultCmbKeys( KeyBinding kb, ImmutableSet< Input > cmb_keys )
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
