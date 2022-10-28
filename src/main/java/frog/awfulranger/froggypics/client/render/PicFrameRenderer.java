package frog.awfulranger.froggypics.client.render;

import frog.awfulranger.froggypics.client.FroggyPicsClient;
import frog.awfulranger.froggypics.shared.entity.PicEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;



@Environment( EnvType.CLIENT )
public class PicFrameRenderer extends EntityRenderer< PicEntity > {
	
	public static final Identifier TEXTURE_SIDE = new Identifier( "minecraft", "textures/block/birch_planks.png" );
	
	public PicFrameRenderer( EntityRendererFactory.Context context ) { super( context ); }
	
	@Override
	public Identifier getTexture( PicEntity entity ) { return TEXTURE_SIDE; }
	
	private static void vertex( Matrix4f modelMatrix, Matrix3f normalMatrix, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v, int normalX, int normalY, int normalZ, int light ) {
		
		vertexConsumer
				.vertex( modelMatrix, x, y, z )
				.color( 255, 255, 255, 255 )
				.texture( u, v )
				.overlay( OverlayTexture.DEFAULT_UV )
				.light( light )
				.normal( normalMatrix, ( float ) normalX, ( float ) normalY, ( float ) normalZ )
				.next();
		
	}
	
	public static void renderPic( MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, byte[] pic, float sizeTop, float sizeBottom, float sizeLeft, float sizeRight ) {
		
		float w = sizeLeft + sizeRight;
		float h = sizeTop + sizeBottom;
		
		Identifier picIdentifier = FroggyPicsClient.storageClient.getIdentifier( pic );
		if ( picIdentifier == null ) { picIdentifier = TEXTURE_SIDE; }
		
		float iRatio = ( float ) 1;
		NativeImageBackedTexture texture = FroggyPicsClient.storageClient.getTexture( pic );
		if ( texture != null ) {
			
			NativeImage image = texture.getImage();
			if ( image != null ) { iRatio = ( float ) image.getWidth() / image.getHeight(); }
			
		}
		
		float gRatio = ( float ) w / h;
		
		if ( iRatio > gRatio ) { h = w / iRatio; }
		else { w = h * iRatio; }
		
		float right = ( -w - ( ( -sizeLeft + sizeRight ) ) ) / 2;
		float down = ( -h - ( ( -sizeTop + sizeBottom ) ) ) / 2;
		float left = right + w;
		float up = down + h;
		
		float z = 1.0f / 32.0f;
		
		float u1 = right - 0.5f;
		float v1 = down - 0.5f;
		float u2 = u1 + w;
		float v2 = v1 + h;
		
		MatrixStack.Entry entry = matrices.peek();
		Matrix4f mMat = entry.getModel();
		Matrix3f nMat = entry.getNormal();
		
		VertexConsumer front = vertexConsumers.getBuffer( RenderLayer.getEntitySolid( picIdentifier ) );
		// Front
		vertex( mMat, nMat, front, left, down, -z, 0.0f, 1.0f, 0, 0, 1, light );
		vertex( mMat, nMat, front, right, down, -z, 1.0f, 1.0f, 0, 0, 1, light );
		vertex( mMat, nMat, front, right, up, -z, 1.0f, 0.0f, 0, 0, 1, light );
		vertex( mMat, nMat, front, left, up, -z, 0.0f, 0.0f, 0, 0, 1, light );
		
		VertexConsumer side = vertexConsumers.getBuffer( RenderLayer.getEntitySolid( TEXTURE_SIDE ) );
		// Back
		vertex( mMat, nMat, side, right, down, z, u1, v2, 0, 0, -1, light );
		vertex( mMat, nMat, side, left, down, z, u2, v2, 0, 0, -1, light );
		vertex( mMat, nMat, side, left, up, z, u2, v1, 0, 0, -1, light );
		vertex( mMat, nMat, side, right, up, z, u1, v1, 0, 0, -1, light );
		// Top
		vertex( mMat, nMat, side, left, up, -z, u2, 1.0f, 0, 1, 0, light );
		vertex( mMat, nMat, side, right, up, -z, u1, 1.0f, 0, 1, 0, light );
		vertex( mMat, nMat, side, right, up, z, u1, 1.0f - z, 0, 1, 0, light );
		vertex( mMat, nMat, side, left, up, z, u2, 1.0f - z, 0, 1, 0, light );
		// Bottom
		vertex( mMat, nMat, side, right, down, -z, u1, 0.0f, 0, -1, 0, light );
		vertex( mMat, nMat, side, left, down, -z, u2, 0.0f, 0, -1, 0, light );
		vertex( mMat, nMat, side, left, down, z, u2, z, 0, -1, 0, light );
		vertex( mMat, nMat, side, right, down, z, u1, z, 0, -1, 0, light );
		// Left
		vertex( mMat, nMat, side, left, down, -z, 0.0f, v2, -1, 0, 0, light );
		vertex( mMat, nMat, side, left, up, -z, 0.0f, v1, -1, 0, 0, light );
		vertex( mMat, nMat, side, left, up, z, z, v1, -1, 0, 0, light );
		vertex( mMat, nMat, side, left, down, z, z, v2, -1, 0, 0, light );
		// Right
		vertex( mMat, nMat, side, right, up, -z, 1.0f, v1, 1, 0, 0, light );
		vertex( mMat, nMat, side, right, down, -z, 1.0f, v2, 1, 0, 0, light );
		vertex( mMat, nMat, side, right, down, z, 1.0f - z, v2, 1, 0, 0, light );
		vertex( mMat, nMat, side, right, up, z, 1.0f - z, v1, 1, 0, 0, light );
	
	}
	
	@Override
	public void render( PicEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int l ) {
		
		int light = WorldRenderer.getLightmapCoordinates( entity.world, entity.getBlockPos() );
		
		matrices.push();
		
		matrices.multiply( Vec3f.POSITIVE_Y.getDegreesQuaternion( 180 - yaw ) );
		
		float sizeTop = 0.5f + entity.getSizeTop();
		float sizeBottom = 0.5f + entity.getSizeBottom();
		float sizeLeft = 0.5f + entity.getSizeLeft();
		float sizeRight = 0.5f + entity.getSizeRight();
		
		renderPic( matrices, vertexConsumers, light, entity.getPic(), sizeTop, sizeBottom, sizeLeft, sizeRight );
		
		matrices.pop();
		
	}
	
}
