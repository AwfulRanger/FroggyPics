package frog.awfulranger.froggypics.server;

import frog.awfulranger.froggypics.shared.FroggyPics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.codec.binary.Hex;
import java.io.*;



public class PicStorageServer {
	
	public PicStorageServer() {}
	
	public File getFile( byte[] hash, MinecraftServer server ) {
		
		return new File( server.getSavePath( WorldSavePath.ROOT ) + "/" + FroggyPics.MOD_ID, Hex.encodeHexString( hash ) + ".jpg" );
		
	}
	
	public byte[] getPic( byte[] hash, MinecraftServer server ) {
		
		File file = getFile( hash, server );
		if ( file.isFile() != true ) { return null; }
		
		FileInputStream stream;
		try { stream = new FileInputStream( file ); } catch ( FileNotFoundException e ) { return null; }
		
		byte[] pic = null;
		try { pic = stream.readAllBytes(); } catch ( IOException e ) {}
		
		try { stream.close(); } catch ( IOException e ) {}
		
		return pic;
		
	}
	
	public void writePic( byte[] hash, byte[] pic, MinecraftServer server ) {
		
		File file = getFile( hash, server );
		file.getParentFile().mkdirs();
		
		FileOutputStream stream;
		try { stream = new FileOutputStream( file ); } catch( FileNotFoundException e ) { return; }
		try { stream.write( pic ); } catch ( IOException e ) {}
		try { stream.close(); } catch ( IOException e ) {}
	
	}
	
}
