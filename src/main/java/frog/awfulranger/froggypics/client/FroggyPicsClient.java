package frog.awfulranger.froggypics.client;

import frog.awfulranger.froggypics.client.render.PicFrameRenderer;
import frog.awfulranger.froggypics.shared.FroggyPics;
import frog.awfulranger.froggypics.shared.entity.PicEntity;
import frog.awfulranger.froggypics.shared.item.PicItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;



@Environment( EnvType.CLIENT )
public class FroggyPicsClient implements ClientModInitializer {
	
	public static PicSenderClient senderClient = null;
	
	public static final PicStorageClient storageClient = new PicStorageClient();
	public static final PicReceiverClient receiverClient = new PicReceiverClient();
	
	@Override
	public void onInitializeClient() {
		
		EntityRendererRegistry.register( FroggyPics.PIC_ENTITY, PicFrameRenderer::new );
		
		BuiltinItemRendererRegistry.INSTANCE.register( FroggyPics.PIC_ITEM, PicItem::renderPic );
		
		receiverClient.register();
		
		ClientPlayNetworking.registerGlobalReceiver( FroggyPics.NET_SPAWN_PIC, ( client, handler, buf, responseSender ) -> {
			
			int id = buf.readInt();
			byte facing = buf.readByte();
			byte[] pic = buf.readByteArray( 32 );
			NbtCompound stackNbt = buf.readNbt();
			client.execute( () -> {
			
				if ( client.world == null ) { return; }
				
				Entity ent = client.world.getEntityById( id );
				if ( ent == null || ent.getType() != FroggyPics.PIC_ENTITY ) { return; }
				
				PicEntity picEnt = ( PicEntity ) ent;
				picEnt.setFacing( Direction.byId( facing ) );
				picEnt.setPic( pic );
				picEnt.setStackNbt( stackNbt );
			
			} );
			
		} );
		
		ClientPlayConnectionEvents.DISCONNECT.register( ( ClientPlayNetworkHandler handler, MinecraftClient client ) -> {
			
			senderClient = null;
			storageClient.clear();
			
		} );
		
	}
	
}
