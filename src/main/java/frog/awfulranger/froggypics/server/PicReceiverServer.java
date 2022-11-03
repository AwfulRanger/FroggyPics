package frog.awfulranger.froggypics.server;

import frog.awfulranger.froggypics.shared.FroggyPics;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;



public class PicReceiverServer {
	
	protected HashMap< ServerPlayerEntity, byte[] > uploading = new HashMap<>();
	protected HashMap< ServerPlayerEntity, Stack< byte[] > > requesting = new HashMap<>();
	
	public PicReceiverServer() {}
	
	public void register() {
		
		ServerPlayNetworking.registerGlobalReceiver( FroggyPics.NET_REQUEST_UPLOAD_PIC, ( server, player, handler, buf, responseSender ) -> {
			
			final int size = buf.readInt();
			final byte[] hash = new byte[ 32 ];
			buf.readBytes( hash );
			if ( size == 0 ) { return; }
			server.execute( () -> {
				
				PacketByteBuf sendBuf = PacketByteBufs.create();
				
				if ( FroggyPics.storageServer.getFile( hash, server ).isFile() == true ) {
					
					ItemStack stack = getEmptyPic( player );
					if ( stack != null ) { convertPicItem( player, stack, hash ); }
					
					sendBuf.writeByte( FroggyPics.RequestCode.EXISTS );
					ServerPlayNetworking.send( player, FroggyPics.NET_REQUEST_UPLOAD_PIC, sendBuf );
					
					return;
					
				}
				
				if ( size > FroggyPics.getMaxPicSize() ) {
					
					sendBuf.writeByte( FroggyPics.RequestCode.TOO_LARGE );
					ServerPlayNetworking.send( player, FroggyPics.NET_REQUEST_UPLOAD_PIC, sendBuf );
					
					return;
					
				}
				
				uploading.put( player, new byte[ size ] );
				
				sendBuf.writeByte( FroggyPics.RequestCode.OK );
				ServerPlayNetworking.send( player, FroggyPics.NET_REQUEST_UPLOAD_PIC, sendBuf );
				
			} );
			
		} );
		
		ServerPlayNetworking.registerGlobalReceiver( FroggyPics.NET_UPLOAD_PIC, ( server, player, handler, buf, responseSender ) -> {
			
			int dataSize = FroggyPics.getMaxPicData();
			
			final int pos = buf.readInt();
			int bufSize = buf.readInt();
			if ( bufSize > dataSize ) { bufSize = dataSize; }
			final int size = bufSize;
			final byte[] bytes = new byte[ size ];
			buf.readBytes( bytes );
			server.execute( () -> {
				
				if ( uploading.containsKey( player ) != true ) { return; }
				
				byte[] upload = uploading.get( player );
				
				int posEnd = pos + size;
				if ( posEnd > upload.length ) { posEnd = upload.length; }
				
				for ( int i = pos; i < posEnd; i++ ) {
					
					upload[ i ] = bytes[ i - pos ];
					
				}
				
				if ( posEnd >= upload.length ) {
					
					// Verify image
					ByteArrayInputStream array = new ByteArrayInputStream( upload );
					MemoryCacheImageInputStream in = new MemoryCacheImageInputStream( array );
					ImageReader reader = ImageIO.getImageReadersByFormatName( "jpg" ).next();
					reader.setInput( in );
					boolean valid = true;
					try { reader.read( 0 ); } catch ( IOException e ) { valid = false; }
					reader.dispose();
					
					try { in.close(); } catch ( IOException e ) {}
					
					if ( valid != true ) { return; }
					
					byte[] hash = FroggyPics.getImageHash( upload );
					
					ItemStack stack = getEmptyPic( player );
					if ( stack == null ) { return; }
					
					convertPicItem( player, stack, hash );
					
					FroggyPics.storageServer.writePic( hash, upload, server );
					
					uploading.remove( player );
					
				}
				
			} );
			
		} );
		
		ServerPlayNetworking.registerGlobalReceiver( FroggyPics.NET_DOWNLOAD_PIC, ( server, player, handler, buf, responseSender ) -> {
			
			byte[] hash = new byte[ 32 ];
			buf.readBytes( hash );
			server.execute( () -> {
				
				if ( requesting.containsKey( player ) != true ) { requesting.put( player, new Stack< byte[] >() ); }
				
				requesting.get( player ).push( hash );
				
			} );
			
		} );
		
	}
	
	public ItemStack getEmptyPic( ServerPlayerEntity player ) {
	
		ItemStack handStack = player.getMainHandStack();
		if ( handStack.isOf( FroggyPics.EMPTY_PIC_ITEM ) != true ) { return null; }
		
		return handStack;
		
	}
	
	public void convertPicItem( ServerPlayerEntity player, ItemStack stack, byte[] hash ) {
		
		if ( stack != null ) { stack.decrement( 1 ); }
		
		ItemStack pic = new ItemStack( FroggyPics.PIC_ITEM );
		
		pic.getOrCreateNbt().putByteArray( "pic", hash );
		
		if ( player.giveItemStack( pic ) != true ) { player.dropItem( pic, false ); }
		
	}
	
	public void tick( MinecraftServer server ) {
		
		if ( server.getTicks() % 2 != 0 ) { return; }
		
		Iterator< HashMap.Entry< ServerPlayerEntity, Stack< byte[] > > > i = requesting.entrySet().iterator();
		while( i.hasNext() == true ) {
			
			HashMap.Entry< ServerPlayerEntity, Stack< byte[] > > kv = i.next();
			
			ServerPlayerEntity player = kv.getKey();
			if ( player.isDisconnected() == true ) { i.remove(); continue; }
			
			Stack< byte[] > stack = kv.getValue();
			
			byte[] hash = stack.pop();
			if ( stack.empty() == true ) { i.remove(); }
			
			byte[] pic = FroggyPics.storageServer.getPic( hash, server );
			if ( pic != null ) {
				
				PacketByteBuf sendBuf = PacketByteBufs.create();
				sendBuf.writeBytes( hash );
				sendBuf.writeByteArray( pic );
				ServerPlayNetworking.send( player, FroggyPics.NET_DOWNLOAD_PIC, sendBuf );
				
			}
			
		}
		
	}
	
	public void clear() {
		
		uploading.clear();
		requesting.clear();
		
	}

}
