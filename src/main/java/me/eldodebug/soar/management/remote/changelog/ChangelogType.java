package me.eldodebug.soar.management.remote.changelog;

import java.awt.Color;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.font.Icon;

public enum ChangelogType {
	ADDED(0, Icon.PLUS, new Color(0, 142, 65)),
	FIXED(1, Icon.REFRESH, new Color(207, 112, 3)),
	REMOVED(2, Icon.MINUS, new Color(209, 34, 34)),
	ERROR(999, Icon.PROHIBITED, new Color(143, 0, 0));
	
	private int id;
	private String string;
	private Color color;
	
	private ChangelogType(int id, String string, Color color) {
		this.id = id;
		this.string = string;
		this.color = color;
	}
	
	public int getId() {
		return id;
	}

	public String getText() {
		return string;
	}

	public Color getColor() {
		return color;
	}
	
	public static ChangelogType getTypeById(int id) {
		
		for(ChangelogType type : ChangelogType.values()) {
			if(type.getId() == id) {
				return type;
			}
		}
		
		return ChangelogType.ERROR;
	}
}
