package me.eldodebug.soar.utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.compat.Minecraft;
import me.eldodebug.soar.compat.ResourceLocation;

public class IOUtils {

	
	public static void copyStringToClipboard(String s) {
		StringSelection stringSelection = new StringSelection(s);
		getToolkit().getSystemClipboard().setContents(stringSelection, null);
	}
	
	public static String getStringFromClipboard() {
		try {
			return getToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
		} catch(Exception e) {
			return null;
		}
	}
	
	private static Toolkit getToolkit() {
		return Toolkit.getDefaultToolkit();
	}
	
    public static ByteBuffer resourceToByteBuffer(ResourceLocation location) {
    	
		try {
			byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(IOUtils.class.getResourceAsStream(location.classpath()));
			
	        ByteBuffer data = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder()).put(bytes);
	        ((Buffer) data).flip();
	        
	        return data;
		} catch (Exception e) {
			GlideLogger.error("Failed to load resource", e);
		}
        
		return null;
    }
    
    public static ByteBuffer resourceToByteBuffer(File file) {
    	
		try {
			byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(new FileInputStream(file));
			
	        ByteBuffer data = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder()).put(bytes);
	        ((Buffer) data).flip();
	        
	        return data;
		} catch (Exception e) {
			GlideLogger.error("Failed to load resource", e);
		}
        
		return null;
    }
}
