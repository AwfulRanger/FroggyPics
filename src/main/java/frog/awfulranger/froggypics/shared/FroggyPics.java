package frog.awfulranger.froggypics.shared;

import frog.awfulranger.froggypics.server.PicReceiverServer;
import frog.awfulranger.froggypics.server.PicStorageServer;
import frog.awfulranger.froggypics.shared.entity.PicEntity;
import frog.awfulranger.froggypics.shared.item.EmptyPicItem;
import frog.awfulranger.froggypics.shared.item.PicItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class FroggyPics implements ModInitializer {
	
	public static final String MOD_ID = "froggypics";
	
	public static final Identifier NET_REQUEST_UPLOAD_PIC = new Identifier( MOD_ID, "request_upload_pic" );
	public static final Identifier NET_UPLOAD_PIC = new Identifier( MOD_ID, "upload_pic" );
	public static final Identifier NET_DOWNLOAD_PIC = new Identifier( MOD_ID, "download_pic" );
	
	public static final Identifier NET_SPAWN_PIC = new Identifier( MOD_ID, "spawn_pic" );
	
	public static final Identifier NET_UPDATE_PIC_SIZE = new Identifier( MOD_ID, "update_pic_size" );
	
	public static final EmptyPicItem EMPTY_PIC_ITEM = new EmptyPicItem( new FabricItemSettings().group( ItemGroup.DECORATIONS ) );
	
	public static final EntityType< PicEntity > PIC_ENTITY = FabricEntityTypeBuilder.create( SpawnGroup.MISC, ( EntityType.EntityFactory< PicEntity > ) PicEntity::new ).build();
	public static final PicItem PIC_ITEM = new PicItem( new FabricItemSettings() );
	
	public static int getMaxPicData() { return 32000; }
	public static int getMaxPicSize() { return 0x100000; }
	public static int getMaxPicDim() { return 0x800; }
	public static int getMaxPicEntitySize() { return 4; }
	
	public class RequestCode {
		
		public static final byte OK = 0;
		public static final byte EXISTS = 1;
		public static final byte TOO_LARGE = 2;
		
	}
	
	public static final PicStorageServer storageServer = new PicStorageServer();
	public static final PicReceiverServer receiverServer = new PicReceiverServer();
	
	public static byte[] getImageHash( byte[] image ) {
		
		try {
			
			MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
			return digest.digest( image );
		
		} catch ( NoSuchAlgorithmException e ) {}
		
		return null;
		
	}
	
	@Override
	public void onInitialize() {
		
		Registry.register( Registry.ITEM, new Identifier( MOD_ID, "empty_pic" ), EMPTY_PIC_ITEM );
		
		Registry.register( Registry.ENTITY_TYPE, new Identifier( MOD_ID, "pic_entity" ), PIC_ENTITY );
		Registry.register( Registry.ITEM, new Identifier( MOD_ID, "pic" ), PIC_ITEM );
		
		
		
		// Server
		
		ServerPlayNetworking.registerGlobalReceiver( NET_UPDATE_PIC_SIZE, ( server, player, handler, buf, responseSender ) -> {
			
			int id = buf.readInt();
			int sizeTop = buf.readInt();
			int sizeBottom = buf.readInt();
			int sizeLeft = buf.readInt();
			int sizeRight = buf.readInt();
			server.execute( () -> {
				
				int maxSize = getMaxPicEntitySize();
				if ( sizeTop > maxSize || sizeBottom > maxSize || sizeLeft > maxSize || sizeRight > maxSize ) { return; }
				
				Entity ent = player.getEntityWorld().getEntityById( id );
				if ( ent == null || ent.getType() != PIC_ENTITY ) { return; }
				
				PicEntity picEnt = ( PicEntity ) ent;
				if ( picEnt.canPlayerModify( player ) != true ) { return; }
				
				picEnt.setSizeTop( sizeTop );
				picEnt.setSizeBottom( sizeBottom );
				picEnt.setSizeLeft( sizeLeft );
				picEnt.setSizeRight( sizeRight );
				
			} );
			
		} );
		
		receiverServer.register();
		
		ServerLifecycleEvents.SERVER_STOPPED.register( ( server ) -> {
			
			receiverServer.clear();
			
		} );
		
	}
	
}
