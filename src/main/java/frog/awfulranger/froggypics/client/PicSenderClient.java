package frog.awfulranger.froggypics.client;

import frog.awfulranger.froggypics.shared.FroggyPics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;



@Environment( EnvType.CLIENT )
public class PicSenderClient {
	
	protected BufferedImage image;
	protected byte[] bytes = null;
	
	public PicSenderClient( BufferedImage image ) {
		
		this.image = image;
		
	}
	
	public void sendRequest() throws IOException {
		
		if ( image == null ) { throw new IOException( Text.translatable( "gui." + FroggyPics.MOD_ID + ".imagereadfail" ).getString() ); }
		
		// Clamp dimensions
		int maxDim = FroggyPics.getMaxPicDim();
		int w = image.getWidth();
		int h = image.getHeight();
		if ( w == 0 || h == 0 ) { throw new IOException( Text.translatable( "gui." + FroggyPics.MOD_ID + ".invaliddim" ).getString() ); }
		if ( w > maxDim || h > maxDim ) {
			
			if ( w > h ) { h = ( int ) ( maxDim / ( ( float ) w / h ) ); w = maxDim; }
			else { w = ( int ) ( maxDim / ( ( float ) h / w ) ); h = maxDim; }
			
		}
		
		BufferedImage img = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );
		Graphics2D graphics = img.createGraphics();
		graphics.drawImage( image, 0, 0, w, h, null );
		graphics.dispose();
		
		// Compress image
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		for ( float q = 1.0f; q > 0.0f; q -= 0.05f ) {
			
			ImageWriter writer = ImageIO.getImageWritersByFormatName( "jpg" ).next();
			
			MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream( array );
			
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
			param.setCompressionQuality( q );
			
			writer.setOutput( out );
			IIOImage outImage = new IIOImage( img, null, null );
			try { writer.write( null, outImage, param ); } catch ( IOException e ) {}
			writer.dispose();
			
			try { out.close(); } catch ( IOException e ) {}
			
			try { array.flush(); } catch ( IOException e ) {}
			if ( array.size() <= FroggyPics.getMaxPicSize() ) { break; }
			array.reset();
			
		}
		
		if ( array.size() > FroggyPics.getMaxPicSize() ) { throw new IOException( Text.translatable( "gui." + FroggyPics.MOD_ID + ".imagetoolarge" ).getString() ); }
		
		bytes = array.toByteArray();
		
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt( bytes.length );
		buf.writeBytes( FroggyPics.getImageHash( bytes ) );
		ClientPlayNetworking.send( FroggyPics.NET_REQUEST_UPLOAD_PIC, buf );
		
	}
	
	public void sendData() throws IOException {
		
		if ( bytes == null ) { throw new IOException( Text.translatable( "gui." + FroggyPics.MOD_ID + ".imagereadfail" ).getString() ); }
		
		int dataSize = FroggyPics.getMaxPicData();
		for ( int i = 0; i < bytes.length; i += dataSize ) {
			
			int size = bytes.length - i;
			if ( size > dataSize ) { size = dataSize; }
			
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt( i );
			buf.writeInt( size );
			buf.writeBytes( bytes, i, size );
			ClientPlayNetworking.send( FroggyPics.NET_UPLOAD_PIC, buf );
			
		}
		
	}

}
