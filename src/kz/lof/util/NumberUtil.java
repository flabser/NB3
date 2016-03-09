package kz.lof.util;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtil {
	public static String formatFloat(float originalCost) {
		return NumberFormat.getInstance(new Locale("ru")).format(originalCost);
	}
}
