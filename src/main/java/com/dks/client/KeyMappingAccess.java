package com.dks.client;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

@OnlyIn( Dist.CLIENT )
final class KeyMappingAccess
{
	private static final Field KeyMapping$defaultKey = ObfuscationReflectionHelper.findField( KeyMapping.class, "f_90814_" );
	static void _setDefaultKey( KeyMapping km, Key key )
	{
		try {
			KeyMapping$defaultKey.set( km, key );
		}
		catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}
	
	private static final Field KeyMapping$keyModifierDefault = ObfuscationReflectionHelper.findField( KeyMapping.class, "keyModifierDefault" );
	static void _setKeyModifierDefault( KeyMapping km, KeyModifier modifier )
	{
		try {
			KeyMapping$keyModifierDefault.set( km, modifier );
		}
		catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}
	
	private static final Field KeyBinding$default_cmb_keys;
	static
	{
		if ( ModList.get().isLoaded( "key_binding_patch" ) ) {
			KeyBinding$default_cmb_keys = ObfuscationReflectionHelper.findField( KeyMapping.class, "default_cmb_keys" );
		}
		else {
			KeyBinding$default_cmb_keys = null;
		}
	}
	static void _setDefaultCmbKeys( KeyMapping kb, ImmutableSet< Key > cmb_keys )
	{
		try {
			KeyBinding$default_cmb_keys.set( kb, cmb_keys );
		}
		catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}
	
	private KeyMappingAccess() {
	}
}
