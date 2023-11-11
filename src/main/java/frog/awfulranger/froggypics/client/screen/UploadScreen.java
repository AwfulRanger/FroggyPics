package frog.awfulranger.froggypics.client.screen;

import frog.awfulranger.froggypics.client.FroggyPicsClient;
import frog.awfulranger.froggypics.client.PicSenderClient;
import frog.awfulranger.froggypics.shared.FroggyPics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;



@Environment( EnvType.CLIENT )
public class UploadScreen extends BaseScreen {
	
	protected static final Identifier BG_TEXTURE = new Identifier( FroggyPics.MOD_ID, "textures/gui/upload.png" );
	protected static final int BG_W = 176;
	protected static final int BG_H = 65;
	protected static final int TEXT_COLOR = 0xFFFFFFFF;
	
	public static boolean fileSelectOpen = false;
	public static String fileSelected = null;
	public static boolean fileChanged = false;
	
	protected TextFieldWidget pathInput = null;
	
	public UploadScreen() {
		
		super( Text.translatable( "gui." + FroggyPics.MOD_ID + ".upload" ), BG_TEXTURE, BG_W, BG_H );
		
	}
	
	@Override
	protected void init() {
		
		int x = ( width - BG_W ) / 2;
		int y = ( height - BG_H ) / 2;
		
		Label uuidLabel = new Label( textRenderer, Text.translatable( "gui." + FroggyPics.MOD_ID + ".imagepath" ), x + 5, y + 5, TEXT_COLOR );
		addDrawable( uuidLabel );
		
		pathInput = new TextFieldWidget( textRenderer, x + 5, y + 15, BG_W - 10, 20, null );
		pathInput.setMaxLength( 256 );
		addDrawableChild( pathInput );
		
		ButtonWidget openPath = new ButtonWidget( x + 5, y + 40, BG_W - 55, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".openpath" ), ( ButtonWidget widget ) -> {
			
			synchronized ( this ) {
				
				if ( fileSelectOpen == true ) { return; }
				
				fileSelectOpen = true;
				
			}
			
			String title = Text.translatable( "gui." + FroggyPics.MOD_ID + ".upload" ).getString();
			
			new Thread( () -> {
				
				MemoryStack stack = MemoryStack.stackPush();
				
				PointerBuffer filterBuf = stack.mallocPointer( 5 );
				filterBuf.put( stack.UTF8( "*.png" ) );
				filterBuf.put( stack.UTF8( "*.jpg" ) );
				filterBuf.put( stack.UTF8( "*.jpeg" ) );
				filterBuf.put( stack.UTF8( "*.bmp" ) );
				filterBuf.put( stack.UTF8( "*.gif" ) );
				filterBuf.flip();
				
				String dialogFile = TinyFileDialogs.tinyfd_openFileDialog( title, null, filterBuf, null, false );
				
				MemoryStack.stackPop();
				
				synchronized ( this ) {
					
					if ( dialogFile != null ) {
						
						fileSelected = dialogFile;
						fileChanged = true;
						
					}
					
					fileSelectOpen = false;
					
				}
				
			} ).start();
			
		} );
		addDrawableChild( openPath );
		
		ButtonWidget upload = new ButtonWidget( ( x + BG_W ) - 45, y + 40, 40, 20, Text.translatable( "gui." + FroggyPics.MOD_ID + ".upload" ), ( ButtonWidget widget ) -> {
			
			try {
				
				PicSenderClient sender = new PicSenderClient( ImageIO.read( new File( pathInput.getText() ) ) );
				sender.sendRequest();
				FroggyPicsClient.senderClient = sender;
				
			}
			catch ( IOException e ) {
				
				client.setScreen( new MessageScreen( null, Text.translatable( "gui." + FroggyPics.MOD_ID + ".invalidimage" ), Text.literal( e.getLocalizedMessage() ) ) );
				
				return;
			
			}
			
		} );
		addDrawableChild( upload );
		
	}
	
	@Override
	public void tick() {
		
		super.tick();
		
		synchronized ( this ) {
			
			if ( fileChanged != true ) { return; }
			
			pathInput.setText( fileSelected );
			
			fileChanged = false;
			
		}
		
	}
	
	@Override
	public void filesDragged( List< Path > paths ) {
		
		Path path = paths.get( 0 );
		if ( path == null ) { return; }
		
		pathInput.setText( path.toString() );
		
	}
	
}
