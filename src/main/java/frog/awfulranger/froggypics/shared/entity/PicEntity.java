package frog.awfulranger.froggypics.shared.entity;

import frog.awfulranger.froggypics.client.FroggyPicsClient;
import frog.awfulranger.froggypics.client.screen.PictureScreen;
import frog.awfulranger.froggypics.shared.FroggyPics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;



public class PicEntity extends AbstractDecorationEntity {
	
	public static final Identifier TEXTURE_FRONT = new Identifier( "minecraft", "textures/block/white_wool.png" );
	
	protected byte[] pic = new byte[ 32 ];
	
	@Environment( EnvType.CLIENT )
	protected Identifier picIdentifier = null;
	@Environment( EnvType.CLIENT )
	protected NativeImage picImage = null;
	
	protected NbtCompound stackNbt = null;
	
	protected static final TrackedData< Integer > SIZE_TOP = DataTracker.registerData( PicEntity.class, TrackedDataHandlerRegistry.INTEGER );
	protected static final TrackedData< Integer > SIZE_BOTTOM = DataTracker.registerData( PicEntity.class, TrackedDataHandlerRegistry.INTEGER );
	protected static final TrackedData< Integer > SIZE_LEFT = DataTracker.registerData( PicEntity.class, TrackedDataHandlerRegistry.INTEGER );
	protected static final TrackedData< Integer > SIZE_RIGHT = DataTracker.registerData( PicEntity.class, TrackedDataHandlerRegistry.INTEGER );
	
	public PicEntity( EntityType< ? extends AbstractDecorationEntity > entityType, World world ) {
		
		super( entityType, world );
		
	}
	
	public PicEntity( World world, BlockPos pos, Direction dir, byte[] pic, NbtCompound nbt ) {
		
		super( FroggyPics.PIC_ENTITY, world, pos );
		
		setFacing( dir );
		setPic( pic );
		setStackNbt( nbt );
		
	}
	
	public PicEntity( World world, Direction dir, byte[] pic, NbtCompound nbt ) {
		
		super( FroggyPics.PIC_ENTITY, world );
		
		setFacing( dir );
		setPic( pic );
		setStackNbt( nbt );
		
	}
	
	protected void initDataTracker() {
		
		super.initDataTracker();
		
		dataTracker.startTracking( SIZE_TOP, 0 );
		dataTracker.startTracking( SIZE_BOTTOM, 0 );
		dataTracker.startTracking( SIZE_LEFT, 0 );
		dataTracker.startTracking( SIZE_RIGHT, 0 );
		
	}
	
	@Override
	public void setFacing( Direction facing ) { super.setFacing( facing ); }
	public Direction getFacing() { return facing; }
	
	public void setPic( byte[] pic ) { this.pic = pic; }
	public byte[] getPic() { return pic; }
	
	public void setStackNbt( NbtCompound stackNbt ) { this.stackNbt = stackNbt; }
	public NbtCompound getStackNbt() { return stackNbt; }
	
	@Environment( EnvType.CLIENT )
	public Identifier getPicIdentifier() {
		
		if ( picIdentifier != null ) { return picIdentifier; }
		
		picIdentifier = FroggyPicsClient.storageClient.getIdentifier( getPic() );
		if ( picIdentifier != null ) { return picIdentifier; }
		
		return TEXTURE_FRONT;
		
	}
	
	@Environment( EnvType.CLIENT )
	public NativeImage getPicImage() {
		
		if ( picImage == null ) {
			
			NativeImageBackedTexture tex = FroggyPicsClient.storageClient.getTexture( getPic() );
			if ( tex != null ) { picImage = tex.getImage(); }
			
		}
		
		return picImage;
		
	}
	
	public void setSizeTop( int top ) { dataTracker.set( SIZE_TOP, top ); }
	public int getSizeTop() { return dataTracker.get( SIZE_TOP ); }
	
	public void setSizeBottom( int bottom ) { dataTracker.set( SIZE_BOTTOM, bottom ); }
	public int getSizeBottom() { return dataTracker.get( SIZE_BOTTOM ); }
	
	public void setSizeLeft( int left ) { dataTracker.set( SIZE_LEFT, left ); }
	public int getSizeLeft() { return dataTracker.get( SIZE_LEFT ); }
	
	public void setSizeRight( int right ) { dataTracker.set( SIZE_RIGHT, right ); }
	public int getSizeRight() { return dataTracker.get( SIZE_RIGHT ); }
	
	public boolean canPlayerModify( ServerPlayerEntity player ) {
		
		return player.squaredDistanceTo( ( double ) getPos().getX() + 0.5d, ( double ) getPos().getY() + 0.5d, ( double ) getPos().getZ() + 0.5d ) <= 64.0d;
		
	}
	
	@Override
	public int getWidthPixels() { return ( getSizeLeft() + getSizeRight() + 1 ) * 16; }
	
	@Override
	public int getHeightPixels() { return ( getSizeTop() + getSizeBottom() + 1 ) * 16; }
	
	@Override
	protected void updateAttachmentPosition() {
		
		if ( facing == null ) { return; }
		
		double x = attachmentPos.getX() + 0.5d;
		double y = attachmentPos.getY() + 0.5d;
		double z = attachmentPos.getZ() + 0.5d;
		
		double top = getSizeTop() + 0.5d;
		double bottom = getSizeBottom() + 0.5d;
		double left = getSizeLeft() + 0.5d;
		double right = getSizeRight() + 0.5d;
		
		if ( facing.getAxis() == Direction.Axis.Z ) {
			
			z -= facing.getOffsetZ() * ( 0.5d - ( 1.0d / 32.0d ) );
			
			Direction side = facing.rotateYClockwise();
			
			setPos( x, y, z );
			setBoundingBox( new Box( x + ( side.getOffsetX() * left ), y + top, z - ( 1.0d / 32.0d ), x - ( side.getOffsetX() * right ), y - bottom, z + ( 1.0d / 32.0d ) ) );
			
		} else {
			
			x -= facing.getOffsetX() * ( 0.5d - ( 1.0d / 32.0d ) );
			
			Direction side = facing.rotateYClockwise();
			
			setPos( x, y, z );
			setBoundingBox( new Box( x - ( 1.0d / 32.0d ), y + top, z + ( side.getOffsetZ() * left ), x + ( 1.0d / 32.0d ), y - bottom, z - ( side.getOffsetZ() * right ) ) );
			
		}
		
	}
	
	@Override
	public boolean canStayAttached() {
		
		BlockPos pos = getBlockPos();
		Direction dir = facing.rotateYCounterclockwise();
		
		int sizeLeft = getSizeLeft();
		int sizeRight = getSizeRight();
		int sizeTop = getSizeTop();
		int sizeBottom = getSizeBottom();
		
		for ( int xOffset = -sizeLeft; xOffset <= sizeRight; xOffset++ ) {
			
			for ( int yOffset = -sizeTop; yOffset <= sizeBottom; yOffset++ ) {
				
				BlockPos fgPos = pos.offset( dir, xOffset ).down( yOffset );
				BlockPos bgPos = fgPos.offset( facing, -1 );
				if ( world.getBlockState( fgPos ).isFullCube( world, fgPos ) == true || world.getBlockState( bgPos ).isFullCube( world, bgPos ) != true ) { return false; }
				
			}
			
		}
		
		return true;
		
	}
	
	@Override
	public void onPlace() { this.playSound( SoundEvents.ENTITY_PAINTING_PLACE, 1.0f, 1.0f ); }
	
	@Override
	public void onBreak( Entity entity ) {
		
		playSound( SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f );
		
		ItemStack stack = new ItemStack( FroggyPics.PIC_ITEM );
		stack.setNbt( stackNbt );
		
		dropStack( stack );
		
	}
	
	@Override
	@Environment( EnvType.CLIENT )
	public ActionResult interact( PlayerEntity player, Hand hand ) {
		
		if ( world.isClient() == true ) { MinecraftClient.getInstance().setScreen( new PictureScreen( this ) ); }
		
		return ActionResult.SUCCESS;
		
	}
	
	@Override
	public void writeCustomDataToNbt( NbtCompound nbt ) {
		
		super.writeCustomDataToNbt( nbt );
		
		nbt.putByte( "facing", ( byte ) facing.getId() );
		nbt.putByteArray( "pic", pic );
		nbt.put( "stackNbt", stackNbt );
		nbt.putInt( "sizeTop", getSizeTop() );
		nbt.putInt( "sizeBottom", getSizeBottom() );
		nbt.putInt( "sizeLeft", getSizeLeft() );
		nbt.putInt( "sizeRight", getSizeRight() );
		
	}
	
	@Override
	public void readCustomDataFromNbt( NbtCompound nbt ) {
		
		super.readCustomDataFromNbt( nbt );
		
		setFacing( Direction.byId( nbt.getByte( "facing" ) ) );
		setPic( nbt.getByteArray( "pic" ) );
		stackNbt = nbt.getCompound( "stackNbt" );
		setSizeTop( nbt.getInt( "sizeTop" ) );
		setSizeBottom( nbt.getInt( "sizeBottom" ) );
		setSizeLeft( nbt.getInt( "sizeLeft" ) );
		setSizeRight( nbt.getInt( "sizeRight" ) );
		
	}
	
	@Override
	public Packet< ? > createSpawnPacket() { return new EntitySpawnS2CPacket( this ); }
	
	@Override
	public void onStartedTrackingBy( ServerPlayerEntity player ) {
		
		super.onStartedTrackingBy( player );
		
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt( getId() );
		buf.writeByte( facing.getId() );
		buf.writeByteArray( getPic() );
		buf.writeNbt( getStackNbt() );
		
		ServerPlayNetworking.send( player, FroggyPics.NET_SPAWN_PIC, buf );
		
	}
	
	@Override
	public ItemStack getPickBlockStack() {
		
		ItemStack stack = new ItemStack( FroggyPics.PIC_ITEM );
		stack.setNbt( stackNbt );
		
		return stack;
		
	}
	
}
