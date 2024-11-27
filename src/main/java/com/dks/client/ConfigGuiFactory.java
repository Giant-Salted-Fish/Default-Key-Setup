package com.dks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiMessageDialog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;
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
		final String title = "dks.gui.config_title";
		final TextComponentTranslation message = new TextComponentTranslation( "dks.gui.warning_msg" );
		final String btn_label = "gui.cancel";
		return new GuiMessageDialog( parent, title, message, btn_label ) {
			@Override
			public void initGui()
			{
				super.initGui();
				
				final GuiButton confirm_btn = this.buttonList.get( 0 );
				confirm_btn.x = this.width / 2 - 155;
				confirm_btn.width = 150;
				
				final GuiButton cancel_btn = new GuiButton( 1, this.width / 2 + 5, confirm_btn.y, 150, 20, I18n.format( "dks.gui.confirm" ) );
				this.buttonList.add( cancel_btn );
			}
			
			@Override
			protected void actionPerformed( @Nonnull GuiButton button ) throws IOException
			{
				if ( button.id == 1 ) {
					DKSMod._saveDefaultKeySetup();
				}
				super.actionPerformed( this.buttonList.get( 0 ) );
			}
		};
	}
	
	@Override
	public Set< RuntimeOptionCategoryElement > runtimeGuiCategories() {
		return Collections.emptySet();
	}
}
