package frog.awfulranger.froggypics.client;

import frog.awfulranger.froggypics.shared.FroggyPics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Hex;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;



@Environment( EnvType.CLIENT )
public class PicStorageClient {
	
	protected HashMap< String, NativeImageBackedTexture > textures = new HashMap<>();
	protected HashMap< String, Identifier > ids = new HashMap<>();
	protected HashMap< String, Boolean > pending = new HashMap<>();
	
	public PicStorageClient() {}
	
	public void request( byte[] hash ) {
		
		String hex = Hex.encodeHexString( hash );
		
		// Make sure picture isn't already downloading
		if ( pending.getOrDefault( hex, false ) == true ) { return; }
		
		// Request download
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeBytes( hash );
		ClientPlayNetworking.send( FroggyPics.NET_DOWNLOAD_PIC, buf );
		
		pending.put( hex, true );
		
	}
	
	public NativeImageBackedTexture getTexture( byte[] hash ) {
		
		NativeImageBackedTexture texture = textures.get( Hex.encodeHexString( hash ) );
		if ( texture != null ) { return texture; }
		
		request( hash );
		
		return null;
		
	}
	
	public Identifier getIdentifier( byte[] hash ) {
		
		Identifier id = ids.get( Hex.encodeHexString( hash ) );
		if ( id != null ) { return id; }
		
		request( hash );
		
		return null;
		
	}
	
	public void writePic( byte[] hash, byte[] data ) {
		
		BufferedImage bufferedImage = null;
		
		ByteArrayInputStream array = new ByteArrayInputStream( data );
		MemoryCacheImageInputStream in = new MemoryCacheImageInputStream( array );
		ImageReader reader = ImageIO.getImageReadersByFormatName( "jpg" ).next();
		reader.setInput( in );
		boolean valid = true;
		try { bufferedImage = reader.read( 0 ); } catch ( IOException e ) {}
		reader.dispose();
		
		try { in.close(); } catch ( IOException e ) {}
		
		if ( bufferedImage == null ) { return; }
		
		int w = bufferedImage.getWidth();
		int h = bufferedImage.getHeight();
		
		NativeImage image = new NativeImage( w, h, false );
		
		for ( int x = 0; x < w; x++ ) {
			
			for ( int y = 0; y < h; y++ ) {
				
				int color = bufferedImage.getRGB( x, y );
				
				int r = NativeImage.getRed( color );
				int g = NativeImage.getGreen( color );
				int b = NativeImage.getBlue( color );
				
				image.setColor( x, y, NativeImage.packColor( 0xFF, r, g, b ) ); // Flip red and blue
				
			}
			
		}
		
		NativeImageBackedTexture texture = new NativeImageBackedTexture( image );
		
		String hex = Hex.encodeHexString( hash );
		
		Identifier id = new Identifier( FroggyPics.MOD_ID, "textures/pics/" + hex + ".jpg" );
		
		MinecraftClient.getInstance().getTextureManager().registerTexture( id, texture );
		
		textures.put( hex, texture );
		ids.put( hex, id );
		pending.remove( hex );
		
	}
	
	public void clear() {
		
		textures.clear();
		ids.clear();
		pending.clear();
		
	}
	
}
