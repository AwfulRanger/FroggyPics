package frog.awfulranger.froggypics.shared.item;

import frog.awfulranger.froggypics.client.FroggyPicsClient;
import frog.awfulranger.froggypics.client.render.PicFrameRenderer;
import frog.awfulranger.froggypics.shared.entity.PicEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.codec.binary.Hex;
import java.util.List;



public class PicItem extends Item {
	
	public static final Identifier TEXTURE_SIDE = new Identifier( "minecraft", "textures/block/birch_planks.png" );
	
	public PicItem( Settings settings ) { super( settings ); }
	
	@Environment( EnvType.CLIENT )
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
	
	@Environment( EnvType.CLIENT )
	public static void renderPic( ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay ) {
		
		if ( stack == null ) { return; }
		
		NbtCompound nbt = stack.getNbt();
		if ( nbt == null ) { return; }
		
		byte[] pic = nbt.getByteArray( "pic" );
		if ( pic == null ) { return; }
		
		matrices.push();
		
		matrices.translate( 0.5d, 0.5d, 0.5d );
		
		if ( mode != ModelTransformation.Mode.FIXED ) {
			
			if ( mode == ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND ) {
				
				matrices.multiply( Vec3f.POSITIVE_X.getDegreesQuaternion( -20 ) );
				matrices.multiply( Vec3f.POSITIVE_Y.getDegreesQuaternion( -90 ) );
				matrices.translate( 0.3d, 0.0d, 0.1d );
				
			} else if ( mode == ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND ) {
				
				matrices.multiply( Vec3f.POSITIVE_X.getDegreesQuaternion( -20 ) );
				matrices.multiply( Vec3f.POSITIVE_Y.getDegreesQuaternion( 90 ) );
				matrices.translate( -0.3d, 0.0d, 0.1d );
				
			}
			
			MatrixStack.Entry entry = matrices.peek();
			
			if ( mode != ModelTransformation.Mode.GUI ) {
				
				matrices.translate( 0.0d, 0.2d, 0.0d );
				matrices.scale( 0.5f, 0.5f, 0.5f );
				
			} else { entry.getNormal().multiply( Vec3f.POSITIVE_X.getDegreesQuaternion( -90 ) ); }
			
			entry.getModel().multiply( Vec3f.POSITIVE_Y.getDegreesQuaternion( 180 ) );
			
		}
		
		PicFrameRenderer.renderPic( matrices, vertexConsumers, light, pic, 0.5f, 0.5f, 0.5f, 0.5f );
		
		matrices.pop();
		
	}
	
	@Override
	@Environment( EnvType.CLIENT )
	public void appendTooltip( ItemStack stack, World world, List< Text > tooltip, TooltipContext context ) {
		
		NbtCompound nbt = stack.getNbt();
		if ( nbt == null ) { return; }
		
		byte[] pic = nbt.getByteArray( "pic" );
		if ( pic == null ) { return; }
		
		String resolution;
		
		NativeImageBackedTexture texture = FroggyPicsClient.storageClient.getTexture( pic );
		if ( texture != null ) {
			
			NativeImage image = texture.getImage();
			resolution = image.getWidth() + "x" + image.getHeight() + " ";
			
		} else { resolution = ""; }
		
		tooltip.add( new LiteralText( resolution + "(" + Hex.encodeHexString( pic ).substring( 0, 8 ) + ")" ).formatted( Formatting.DARK_GRAY ) );
	
	}
	
	@Override
	public ActionResult useOnBlock( ItemUsageContext context ) {
		
		ItemStack stack = context.getStack();
		
		NbtCompound nbt = stack.getNbt();
		if ( nbt == null ) { return ActionResult.FAIL; }
		
		byte[] pic = nbt.getByteArray( "pic" );
		if ( pic == null ) { return ActionResult.FAIL; }
		
		BlockPos pos = context.getBlockPos();
		Direction dir = context.getSide();
		BlockPos offset = pos.offset( dir );
		PlayerEntity player = context.getPlayer();
		
		if ( dir.getAxis().isHorizontal() != true ) { return ActionResult.FAIL; }
		if ( player != null && player.canPlaceOn( offset, dir, stack ) != true ) { return ActionResult.FAIL; }
		
		World world = context.getWorld();
		
		PicEntity frame = new PicEntity( world, offset, dir, pic, nbt );
		
		if ( frame.canStayAttached() == true ) {
			
			boolean client = world.isClient();
			
			if ( client != true ) {
				
				frame.onPlace();
				world.emitGameEvent( player, GameEvent.ENTITY_PLACE, pos );
				world.spawnEntity( frame );
				
			}
			
			stack.decrement( 1 );
			
			return ActionResult.success( client );
			
		} else { return ActionResult.CONSUME; }
		
	}
	
}
