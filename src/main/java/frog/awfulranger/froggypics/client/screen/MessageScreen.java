package frog.awfulranger.froggypics.client.screen;

import frog.awfulranger.froggypics.shared.FroggyPics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;



@Environment( EnvType.CLIENT )
public class MessageScreen extends BaseScreen {
	
	protected static final Identifier BG_TEXTURE = new Identifier( FroggyPics.MOD_ID, "textures/gui/message.png" );
	protected static final int BG_W = 176;
	protected static final int BG_H = 80;
	protected static final int TEXT_COLOR = 0xFFFFFFFF;
	
	protected Screen parent;
	protected Text message;
	
	public MessageScreen( Screen parent, Text title, Text message ) {
		
		super( title, BG_TEXTURE, BG_W, BG_H );
		
		this.parent = parent;
		this.message = message;
		
	}
	
	@Override
	protected void init() {
		
		int x = ( width - BG_W ) / 2;
		int y = ( height - BG_H ) / 2;
		
		Label titleLabel = new Label( textRenderer, title, x + 5, y + 5, TEXT_COLOR );
		addDrawable( titleLabel );
		
		Label messageLabel = new Label( textRenderer, message, x + 5, y + 25, TEXT_COLOR );
		addDrawable( messageLabel );
		
		ButtonWidget ok = new ButtonWidget( ( x + BG_W ) - 45, y + 55, 40, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".ok" ), ( ButtonWidget widget ) -> {
			
			client.setScreen( parent );
			
		} );
		addDrawableChild( ok );
		
	}
	
	@Override
	public void close() {
		
		super.close();
		
		client.setScreen( parent );
		
	}
	
}
