package frog.awfulranger.froggypics.shared.item;

import frog.awfulranger.froggypics.client.screen.UploadScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;



public class EmptyPicItem extends Item {
	
	public EmptyPicItem( Settings settings ) {
		
		super( settings );
		
	}
	
	@Override
	@Environment( EnvType.CLIENT )
	public TypedActionResult< ItemStack > use( World world, PlayerEntity player, Hand hand ) {
		
		if ( world.isClient() == true ) { MinecraftClient.getInstance().setScreen( new UploadScreen() ); }
		
		return TypedActionResult.success( player.getStackInHand( hand ) );
		
	}
	
}
