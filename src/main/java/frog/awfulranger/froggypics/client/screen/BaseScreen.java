package frog.awfulranger.froggypics.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;



@Environment( EnvType.CLIENT )
public class BaseScreen extends Screen {
	
	protected Identifier bgTexture;
	protected int bgW;
	protected int bgH;
	
	public BaseScreen( Text title, Identifier bgTexture, int bgW, int bgH ) {
		
		super( title );
		
		this.bgTexture = bgTexture;
		this.bgW = bgW;
		this.bgH = bgH;
		
	}
	
	@Override
	public void render( MatrixStack matrices, int mouseX, int mouseY, float delta ) {
		
		renderBackground( matrices );
		super.render( matrices, mouseX, mouseY, delta );
		
	}
	
	@Override
	public void renderBackground( MatrixStack matrices ) {
		
		RenderSystem.setShader( GameRenderer::getPositionTexShader );
		RenderSystem.setShaderColor( 1.0f, 1.0f, 1.0f, 1.0f );
		RenderSystem.setShaderTexture( 0, bgTexture );
		
		drawTexture( matrices, ( width - bgW ) / 2, ( height - bgH ) / 2, 0, 0, bgW, bgH );
		
	}
	
	@Override
	public boolean isPauseScreen() { return false; }
	
	public class Label implements Drawable {
		
		protected TextRenderer textRenderer;
		
		protected Text text;
		
		protected int x;
		
		protected int y;
		
		protected int color;
		
		public Label( TextRenderer textRenderer, Text text, int x, int y, int color ) {
			
			this.textRenderer = textRenderer;
			this.text = text;
			this.x = x;
			this.y = y;
			this.color = color;
			
		}
		
		@Override
		public void render( MatrixStack matrices, int mouseX, int mouseY, float delta ) {
			
			textRenderer.drawWithShadow( matrices, text, x, y, color );
			
		}
		
	}
	
}
