package com.dks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Set;

/**
 * Reference: <a href="https://harbinger.covertdragon.team/chapter-26/config-gui.html">
 *     Harbinger: Chapter-26.1 </a>
 */
@SideOnly( Side.CLIENT )
public final class ConfigGuiFactory implements IModGuiFactory
{
	@Override
	public void initialize( Minecraft mc ) {
	}
	
	@Override
	public boolean hasConfigGui() {
		return true;
	}
	
	@Override
	public GuiScreen createConfigGui( GuiScreen parent )
	{
		return new GuiYesNo(
			( result, id ) -> {
				if ( result ) {
					DKSMod._saveDefaultKeySetup();
				}
				parent.mc.displayGuiScreen( parent );
			},
			I18n.format( "dks.gui.warning_title" ),
			I18n.format( "dks.gui.warning_msg" ),
			-1
		);
	}
	
	@Override
	public Set< RuntimeOptionCategoryElement > runtimeGuiCategories() {
		return Collections.emptySet();
	}
}
