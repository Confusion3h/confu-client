package me.eldodebug.confu.module.impl;

import java.util.Calendar;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;

/** 1.8.9 CalendarMod. */
public final class CalendarModule extends NvgHudModule {

	public CalendarModule() {
		super("calendar", Category.HUD, "tt:CALENDAR", "tt:CALENDAR_DESCRIPTION", 0.80F, 0.05F);
	}

	@Override
	protected void drawNanoVG() {
		Calendar calendar = Calendar.getInstance();
		AccentColor currentColor = Glide.getInstance().getColorManager().getCurrentColor();
		String[] dayOfWeek = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
		float offsetX = 0;
		float offsetY = 0;
		int index = 1;
		int weekIndex = 0;
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		Calendar firstDayCalendar = (Calendar) calendar.clone();
		firstDayCalendar.set(year, month, 1);
		int height = 97;
		this.drawBackground(100, height);
		this.drawText(monthName(month) + " " + year, 6, 6, 11, getHudFont(2));
		for (String s : dayOfWeek) {
			this.drawText(s, 6 + offsetX, 22, 6.5F, getHudFont(2));
			offsetX += 13.4F;
		}
		offsetX = 0;
		index = firstDayCalendar.get(Calendar.DAY_OF_WEEK);
		offsetX = (index - 1) * 13.4F;
		for (int i = 1; i <= maxDay; i++) {
			if (i == day) {
				this.drawRoundedRect(4.5F + offsetX, 30.5F + offsetY, 10F, 10F, 5F);
			}
			this.drawCenteredText(String.valueOf(i), 10 + offsetX, 33 + offsetY, 6, getHudFont(1),
					i == day ? currentColor.getInterpolateColor() : this.getFontColor());
			offsetX += 13.4F;
			if (index % 7 == 0 && i != maxDay) {
				offsetY += 13.4F;
				offsetX = 0;
				weekIndex++;
			}
			index++;
		}
		height = weekIndex < 5 ? 97 : 110;
		this.setWidth(100);
		this.setHeight(height);
	}

	private static String monthName(int month) {
		return switch (month) {
			case 0 -> "January";
			case 1 -> "February";
			case 2 -> "March";
			case 3 -> "April";
			case 4 -> "May";
			case 5 -> "June";
			case 6 -> "July";
			case 7 -> "August";
			case 8 -> "September";
			case 9 -> "October";
			case 10 -> "November";
			case 11 -> "December";
			default -> "null";
		};
	}
}
