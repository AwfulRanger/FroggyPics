package frog.awfulranger.froggypics.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import frog.awfulranger.froggypics.shared.FroggyPics;
import frog.awfulranger.froggypics.shared.entity.PicEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;



@Environment( EnvType.CLIENT )
public class PictureScreen extends BaseScreen {
	
	protected static final Identifier BG_TEXTURE = new Identifier( FroggyPics.MOD_ID, "textures/gui/picture.png" );
	protected static final Identifier BLOCKED_FG_TEXTURE = new Identifier( FroggyPics.MOD_ID, "textures/gui/blockedfg.png" );
	protected static final Identifier BLOCKED_BG_TEXTURE = new Identifier( FroggyPics.MOD_ID, "textures/gui/blockedbg.png" );
	protected static final int BG_W = 224;
	protected static final int BG_H = 224;
	protected static final int TEXT_COLOR = 0xFFFFFFFF;
	protected static final int OUTLINE_COLOR = 0xFFFF00FF;
	protected static final int GRID_SIZE = 16;
	
	protected PicEntity entity;
	protected Identifier picIdentifier;
	protected NativeImage picImage;
	protected int sizeTop = 0;
	protected int sizeBottom = 0;
	protected int sizeLeft = 0;
	protected int sizeRight = 0;
	
	public PictureScreen( PicEntity entity ) {
		
		super( Text.translatable( "gui." + FroggyPics.MOD_ID + "pic" ), BG_TEXTURE, BG_W, BG_H );
		
		this.entity = entity;
		picIdentifier = entity.getPicIdentifier();
		picImage = entity.getPicImage();
		sizeTop = entity.getSizeTop();
		sizeBottom = entity.getSizeBottom();
		sizeLeft = entity.getSizeLeft();
		sizeRight = entity.getSizeRight();
		
	}
	
	@Override
	protected void init() {
		
		int grid = ( GRID_SIZE * 9 ) / 2;
		int cX = width / 2;
		int cY = height / 2;
		
		Label uuidLabel = new Label( textRenderer, Text.translatable( "gui." + FroggyPics.MOD_ID + ".pic" ), ( cX - ( bgW / 2 ) ) + 5, ( cY - ( bgH / 2 ) ) + 5, TEXT_COLOR );
		addDrawable( uuidLabel );
		
		int maxSize = FroggyPics.getMaxPicEntitySize();
		
		ButtonWidget top = new ButtonWidget( cX - 10, ( cY - grid ) - 25, 20, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".uparrow" ), ( ButtonWidget widget ) -> {
			
			if ( sizeTop >= maxSize || entity == null ) { return; }
			
			BlockPos pos = entity.getBlockPos();
			Direction facing = entity.getFacing();
			Direction dir = facing.rotateYCounterclockwise();
			
			for ( int offset = -sizeLeft; offset <= sizeRight; offset++ ) {
				
				BlockPos fgPos = pos.offset( dir, offset ).up( sizeTop + 1 );
				BlockPos bgPos = fgPos.offset( facing, -1 );
				if ( entity.world.getBlockState( fgPos ).isFullCube( entity.world, fgPos ) == true || entity.world.getBlockState( bgPos ).isFullCube( entity.world, bgPos ) != true ) { return; }
				
			}
			
			sizeTop++;
			
		} );
		addDrawableChild( top );
		
		ButtonWidget bottom = new ButtonWidget( cX - 10, cY + grid + 5, 20, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".downarrow" ), ( ButtonWidget widget ) -> {
			
			if ( sizeBottom >= maxSize || entity == null ) { return; }
			
			BlockPos pos = entity.getBlockPos();
			Direction facing = entity.getFacing();
			Direction dir = facing.rotateYCounterclockwise();
			
			for ( int offset = -sizeLeft; offset <= sizeRight; offset++ ) {
				
				BlockPos fgPos = pos.offset( dir, offset ).down( sizeBottom + 1 );
				BlockPos bgPos = fgPos.offset( facing, -1 );
				if ( entity.world.getBlockState( fgPos ).isFullCube( entity.world, fgPos ) == true || entity.world.getBlockState( bgPos ).isFullCube( entity.world, bgPos ) != true ) { return; }
				
			}
			
			sizeBottom++;
			
		} );
		addDrawableChild( bottom );
		
		ButtonWidget left = new ButtonWidget( ( cX - grid ) - 25, cY - 10, 20, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".leftarrow" ), ( ButtonWidget widget ) -> {
			
			if ( sizeLeft >= maxSize || entity == null ) { return; }
			
			BlockPos pos = entity.getBlockPos();
			Direction facing = entity.getFacing();
			Direction dir = facing.rotateYClockwise();
			
			for ( int offset = -sizeBottom; offset <= sizeTop; offset++ ) {
				
				BlockPos fgPos = pos.offset( dir, sizeLeft + 1 ).up( offset );
				BlockPos bgPos = fgPos.offset( facing, -1 );
				if ( entity.world.getBlockState( fgPos ).isFullCube( entity.world, fgPos ) == true || entity.world.getBlockState( bgPos ).isFullCube( entity.world, bgPos ) != true ) { return; }
				
			}
			
			sizeLeft++;
			
		} );
		addDrawableChild( left );
		
		ButtonWidget right = new ButtonWidget( cX + grid + 5, cY - 10, 20, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".rightarrow" ), ( ButtonWidget widget ) -> {
			
			if ( sizeRight >= maxSize || entity == null ) { return; }
			
			BlockPos pos = entity.getBlockPos();
			Direction facing = entity.getFacing();
			Direction dir = facing.rotateYCounterclockwise();
			
			for ( int offset = -sizeBottom; offset <= sizeTop; offset++ ) {
				
				BlockPos fgPos = pos.offset( dir, sizeRight + 1 ).up( offset );
				BlockPos bgPos = fgPos.offset( facing, -1 );
				if ( entity.world.getBlockState( fgPos ).isFullCube( entity.world, fgPos ) == true || entity.world.getBlockState( bgPos ).isFullCube( entity.world, bgPos ) != true ) { return; }
				
			}
			
			sizeRight++;
			
		} );
		addDrawableChild( right );
		
		ButtonWidget reset = new ButtonWidget( ( cX - ( bgW / 2 ) ) + 5, ( cY + ( bgH / 2 ) ) - 25, 40, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".reset" ), ( ButtonWidget widget ) -> {
			
			sizeTop = 0;
			sizeBottom = 0;
			sizeLeft = 0;
			sizeRight = 0;
			
		} );
		addDrawableChild( reset );
		
		ButtonWidget apply = new ButtonWidget( ( cX + ( bgW / 2 ) ) - 45, ( cY + ( bgH / 2 ) ) - 25, 40, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".apply" ), ( ButtonWidget widget ) -> {
			
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt( entity.getId() );
			buf.writeInt( sizeTop );
			buf.writeInt( sizeBottom );
			buf.writeInt( sizeLeft );
			buf.writeInt( sizeRight );
			
			ClientPlayNetworking.send( FroggyPics.NET_UPDATE_PIC_SIZE, buf );
			
			MinecraftClient.getInstance().setScreen( null );
			
		} );
		addDrawableChild( apply );
		
	}
	
	@Override
	public void render( MatrixStack matrices, int mouseX, int mouseY, float delta ) {
		
		super.render( matrices, mouseX, mouseY, delta );
		
		if ( picImage == null || picIdentifier == null || entity == null || entity.world == null ) { return; }
		
		RenderSystem.setShader( GameRenderer::getPositionTexShader );
		RenderSystem.setShaderColor( 1.0f, 1.0f, 1.0f, 1.0f );
		
		BlockPos pos = entity.getBlockPos();
		Direction facing = entity.getFacing();
		Direction dir = facing.rotateYCounterclockwise();
		
		int maxSize = FroggyPics.getMaxPicEntitySize();
		
		for ( int xOffset = -maxSize; xOffset <= maxSize; xOffset++ ) {
			
			for ( int yOffset = -maxSize; yOffset <= maxSize; yOffset++ ) {
				
				if ( xOffset == 0 && yOffset == 0 ) { continue; }
				
				int x = ( ( width - GRID_SIZE ) / 2 ) + ( GRID_SIZE * xOffset );
				int y = ( ( height - GRID_SIZE ) / 2 ) + ( GRID_SIZE * yOffset );
				
				BlockPos fgPos = pos.offset( dir, xOffset ).down( yOffset );
				BlockPos bgPos = fgPos.offset( facing, -1 );
				if ( entity.world.getBlockState( fgPos ).isFullCube( entity.world, fgPos ) == true ) {
					
					RenderSystem.setShaderTexture( 0, BLOCKED_FG_TEXTURE );
					drawTexture( matrices, x, y, 0, 0, GRID_SIZE, GRID_SIZE, GRID_SIZE, GRID_SIZE );
					
				} else if ( entity.world.getBlockState( bgPos ).isFullCube( entity.world, bgPos ) != true ) {
					
					RenderSystem.setShaderTexture( 0, BLOCKED_BG_TEXTURE );
					drawTexture( matrices, x, y, 0, 0, GRID_SIZE, GRID_SIZE, GRID_SIZE, GRID_SIZE );
					
				}
				
			}
			
		}
		
		int iW = picImage.getWidth();
		int iH = picImage.getHeight();
		if ( iW == 0 || iH == 0 ) { return; }
		
		int w = GRID_SIZE * ( sizeLeft + sizeRight + 1 );
		int h = GRID_SIZE * ( sizeTop + sizeBottom + 1 );
		
		float iRatio = ( float ) iW / iH;
		float gRatio = ( float ) w / h;
		
		if ( iRatio > gRatio ) { h = ( int ) ( w / iRatio ); }
		else { w = ( int ) ( h * iRatio ); }
		
		int x = ( ( width - w ) - ( ( -sizeRight + sizeLeft ) * GRID_SIZE ) ) / 2;
		int y = ( ( height - h ) - ( ( -sizeBottom + sizeTop ) * GRID_SIZE ) ) / 2;
		
		RenderSystem.setShaderTexture( 0, picIdentifier );
		drawTexture( matrices, x, y, 0, 0, w, h, w, h );
		
		int oX = ( ( width - GRID_SIZE ) / 2 ) - ( sizeLeft * GRID_SIZE );
		int oY = ( ( height - GRID_SIZE ) / 2 ) - ( sizeTop * GRID_SIZE );
		int oW = ( ( sizeLeft + sizeRight + 1 ) * GRID_SIZE ) - 1;
		int oH = ( ( sizeTop + sizeBottom + 1 ) * GRID_SIZE ) - 1;
		
		drawHorizontalLine( matrices, oX, oX + oW, oY, OUTLINE_COLOR );
		drawHorizontalLine( matrices, oX, oX + oW, oY + oH, OUTLINE_COLOR );
		drawVerticalLine( matrices, oX, oY, oY + oH, OUTLINE_COLOR );
		drawVerticalLine( matrices, oX + oW, oY, oY + oH, OUTLINE_COLOR );
		
	}
	
}
