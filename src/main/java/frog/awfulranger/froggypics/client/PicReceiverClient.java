package frog.awfulranger.froggypics.client;

import frog.awfulranger.froggypics.client.screen.MessageScreen;
import frog.awfulranger.froggypics.shared.FroggyPics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import java.io.IOException;



@Environment( EnvType.CLIENT )
public class PicReceiverClient {
	
	public PicReceiverClient() {}
	
	public void register() {
		
		ClientPlayNetworking.registerGlobalReceiver( FroggyPics.NET_REQUEST_UPLOAD_PIC, ( client, handler, buf, responseSender ) -> {
			
			byte code = buf.readByte();
			client.execute( () -> {
				
				switch ( code ) {
					
					case ( FroggyPics.RequestCode.OK ): {
						
						if ( FroggyPicsClient.senderClient != null ) {
							
							try { FroggyPicsClient.senderClient.sendData(); }
							catch ( IOException e ) {
								
								client.setScreen( new MessageScreen( null, Text.translatable( "gui." + FroggyPics.MOD_ID + ".invalidimage" ), Text.literal( e.getLocalizedMessage() ) ) );
								
								return;
								
							}
							
						}
						
						break;
						
					}
					case ( FroggyPics.RequestCode.EXISTS ): {
						
						FroggyPicsClient.senderClient = null;
						
						break;
						
					}
					case ( FroggyPics.RequestCode.TOO_LARGE ): {
						
						FroggyPicsClient.senderClient = null;
						
						client.setScreen( new MessageScreen( null, Text.translatable( "gui." + FroggyPics.MOD_ID + ".invalidimage" ), Text.translatable( "gui.froggypics.imagetoolarge" ) ) );
						
						break;
						
					}
					
				}
				
			} );
			
		} );
		
		ClientPlayNetworking.registerGlobalReceiver( FroggyPics.NET_DOWNLOAD_PIC, ( client, handler, buf, responseSender ) -> {
			
			byte[] hash = new byte[ 32 ];
			buf.readBytes( hash );
			byte[] pic = buf.readByteArray( FroggyPics.getMaxPicSize() );
			client.execute( () -> {
				
				FroggyPicsClient.storageClient.writePic( hash, pic );
				
			} );
			
		} );
		
	}
	
}
