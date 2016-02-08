package kz.flabs.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.jpa.AppEntity;
import kz.flabs.exception.ConversionError;
import kz.flabs.exception.DataConversionException;
import kz.pchelka.server.Server;

public class Util {
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("kk:mm");
	public static final SimpleDateFormat derbyDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
	// public static final SimpleDateFormat simpleDateFormat = new
	// SimpleDateFormat("dd.MM.yyyy");
	public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat providerDateTimeFormat = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
	public static final Pattern pEntity = Pattern.compile("\\G&(#\\d+|\\w+);");// Pattern.compile("\\G&(#\\d+|\\w+);");
	public static final Pattern pTag = Pattern.compile("<(?:\"[^\"]*\"['\"]*|'[^']*'['\"]*|[^'\">])+>");
	public static final Pattern pAtEnd = Pattern.compile("\\G\\z");
	public static final Pattern pWord = Pattern.compile("\\G(\\w|\\pL)+");
	public static final Pattern pNonHtml = Pattern.compile("\\G([^(\\w|\\p{L})]|\\p{Ps}|\\p{Pe}|\\p{Pi}|\\p{Pf}|\\p{P}|\\p{S})+");
	public static final int dayInMs = 1000 * 60 * 60 * 24;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HashMap cloneMap(HashMap sourceMap) {
		HashMap map = new HashMap();
		for (Object key : sourceMap.keySet()) {
			String k = (String) key;
			map.put(k, sourceMap.get(k));
		}
		return map;

	}

	public static String convertDataTimeToString(Date date) {
		try {
			return dateTimeFormat.format(date);
		} catch (Exception e) {
			// AppEnv.logger.errorLogEntry("Util, Не удалось преобразовать время
			// в текст " + date);
			// AppEnv.logger.errorLogEntry(e);
			return "null";
		}
	}

	public static String convertDataTimeToStringSilently(Date date) {
		try {
			return dateTimeFormat.format(date);
		} catch (Exception e) {
			return "";
		}
	}

	public static String convertDataTimeToProvider(Date date) {
		try {
			return providerDateTimeFormat.format(date);
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry("Util, Не удалось преобразовать время в текст " + date);
			// AppEnv.logger.errorLogEntry(e);
			return "null";
		}
	}

	public static String convertDataTimeToTimeString(Date date) {
		try {
			return timeFormat.format(date);
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry("Util, Не удалось преобразовать время в текст " + date);
			// AppEnv.logger.errorLogEntry(e);
			return "null";
		}
	}

	public static Date convertStringToDateTime(String date) {
		try {
			return dateTimeFormat.parse(date);
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry("Util, Unbale to convert text to date " + date + ", expected format: " + dateTimeFormat.toPattern());
			return null;
		}
	}

	public static Date convertStringToDateTimeSilently(String date) {
		try {
			if (date != null) {
				if (date.length() == 19) {
					return dateTimeFormat.parse(date);
				}
				if (date.length() == 10) {
					return simpleDateFormat.parse(date);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	public static String convertDateTimeToDerbyFormat(Date date) throws DataConversionException {
		try {
			return derbyDateTimeFormat.format(date);
		} catch (Exception e) {
			throw new DataConversionException(ConversionError.CONVERSION_DATE_TO_DATABASE_DATE);
		}
	}

	public static String convertDataToString(Date date) {
		try {
			return dateFormat.format(date);
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry("Util, Не удалось преобразовать дату в текст " + date);
			// AppEnv.logger.errorLogEntry(e);
			return "err date";
		}
	}

	public static String convertDataToString(Calendar date) {
		try {
			return simpleDateFormat.format(date.getTime());
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry("Util, Не удалось преобразовать дату в текст " + date);
			// AppEnv.logger.errorLogEntry(e);
			return "err date";
		}
	}

	public static Date convertStringToDate(String date) {
		try {
			return dateFormat.parse(date);
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry("Util, Не удалось преобразовать текст в дату " + date + ", ожидался формат: " + dateFormat.toPattern());
			// AppEnv.logger.errorLogEntry(e);
			return null;
		}
	}

	public static Date convertStringToSimpleDate(String date) {
		if (date != null && !date.trim().equals("")) {
			try {
				return simpleDateFormat.parse(date);
			} catch (Exception e) {
				AppEnv.logger.errorLogEntry("Util, Cannot convert the date to String (date=" + date + "), exepted : " + simpleDateFormat.toPattern());
				return null;
			}
		} else {
			return null;
		}
	}

	public static long convertStringToLong(String d) {
		d = d.replaceAll("\\s+", "").replace(",", "").replace("/\\D/g", "");
		try {
			return Long.parseLong(d);
		} catch (Exception e) {
			return 0;
		}
	}

	public static int convertStringToInt(String d) {
		d = d.replaceAll("\\s+", "").replaceAll(",", "").replaceAll("/\\D/g", "");
		try {
			return Integer.parseInt(d);
		} catch (Exception e) {
			return 0;
		}
	}

	public static float convertStringToFloat(String d) {
		d = d.replaceAll("\\s+", "").replaceAll(",", ".").replaceAll("/\\D/g", "");
		try {
			return Float.parseFloat(d);
		} catch (Exception e) {
			return 0;
		}
	}

	public static int getRandomNumber(int anyNumber) {
		if (anyNumber > 0) {
			Random random = new Random();
			return Math.abs(random.nextInt()) % anyNumber;
		} else {
			return 0;
		}
	}

	public static int generateRandom() {
		Random random = new Random();
		return Math.abs(random.nextInt());
	}

	public static String generateRandomAsText() {
		return Integer.toString(generateRandom());
	}

	public static Object getRndListElement(List<?> list) {
		Random random = new Random();
		int index = random.nextInt(list.size());
		return list.get(index);
	}

	// TODO It can cause StackOvervlow error
	public static String toStringGettersVal(Object clazz) {
		Class<?> noparams[] = {};
		StringBuilder result = new StringBuilder(10000);
		String newLine = System.getProperty("line.separator");

		result.append(clazz.getClass().getName());
		result.append(" Object {");
		result.append(newLine);

		try {
			for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz.getClass()).getPropertyDescriptors()) {
				Method method = propertyDescriptor.getReadMethod();
				if (method != null && !method.getName().equals("getClass")) {
					System.out.println(result);
					result.append(" ");
					result.append(method.getName());
					result.append(": ");
					try {
						String methodValue = "";
						Object val = method.invoke(clazz, noparams);
						if (val != null) {
							if (val instanceof Date) {
								methodValue = Util.simpleDateFormat.format((Date) val);
							} else if (val.getClass().isInstance(AppEntity.class)) {
								methodValue = val.getClass().getCanonicalName();
							} else {
								methodValue = val.toString();
							}
							result.append(methodValue);
						} else {
							result.append("null");
						}
					} catch (Exception e) {
						AppEnv.logger.errorLogEntry(e);
					}
					result.append(newLine);
				}
			}
		} catch (IntrospectionException e) {
			AppEnv.logger.errorLogEntry(e);
		}

		result.append("}");
		return result.toString();
	}

	public static boolean isGroupName(String userID) {
		if (userID != null && userID.length() != 0) {
			return userID.startsWith("[") && userID.endsWith("]");
		}
		return false;
	}

	public static String getFileName(String fn, String tmpFolder) {
		int folderNum = 1;
		File dir = new File(tmpFolder + File.separator + Integer.toString(folderNum));
		while (dir.exists()) {
			folderNum++;
			dir = new File(tmpFolder + File.separator + Integer.toString(folderNum));
		}
		dir.mkdirs();
		fn = dir + File.separator + fn;
		return fn;
	}

	private static byte[] getDigestFromFile(InputStream is) {
		byte[] result;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] b = new byte[1048576];
			int len = 0;
			while ((len = is.read(b)) > 0) {
				md.update(b, 0, len);
			}
			is.close();
			result = md.digest();
		} catch (NoSuchAlgorithmException e) {
			AppEnv.logger.errorLogEntry("Util, не удалось инициализировать алгоритм шифрования");
			return null;
		} catch (IOException e) {
			AppEnv.logger.errorLogEntry("Util, не удалось произвести чтение файла ");
			return null;
		}
		return result;
	}

	private static byte[] getDigestFromFile(String filePath) {
		byte[] result;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			File file = new File(filePath);
			InputStream is = new FileInputStream(file);
			byte[] b = new byte[1048576];
			int len = 0;
			while ((len = is.read(b)) > 0) {
				md.update(b, 0, len);
			}
			is.close();
			result = md.digest();
		} catch (FileNotFoundException e) {
			AppEnv.logger.errorLogEntry("Util, не удалось получить контрольную сумму файла " + filePath + ": файл не найден");
			return null;
		} catch (NoSuchAlgorithmException e) {
			AppEnv.logger.errorLogEntry("Util, не удалось инициализировать алгоритм шифрования");
			return null;
		} catch (IOException e) {
			AppEnv.logger.errorLogEntry("Util, не удалось произвести чтение файла " + filePath);
			return null;
		}
		return result;
	}

	private static String getHexHash(byte[] data) {
		StringBuffer sb = new StringBuffer();
		for (byte b : data) {
			sb.append(String.format("%02X", b & 0xff));
		}
		return sb.toString();
	}

	public static String getHexHash(String filePath) {
		return getHexHash(getDigestFromFile(filePath));
	}

	public static String getHexHash(InputStream is) {
		return getHexHash(getDigestFromFile(is));
	}

	public static boolean compareFile(String filePath1, String filePath2) {
		return MessageDigest.isEqual(getDigestFromFile(filePath1), getDigestFromFile(filePath2));
	}

	public static boolean checkFileHash(String filePath, String hexHash) {
		return hexHash.equals(getHexHash(filePath));
	}

	public static File getExistFile(String fn, String tmpFolder) {
		int folderNum = 1;
		File file = null;
		// File file = new File(tmpFolder + File.separator +
		// Integer.toString(folderNum) + File.separator + fn);
		do {
			file = new File(tmpFolder + File.separator + Integer.toString(folderNum) + File.separator + fn);
			folderNum++;

		} while (!file.exists() && folderNum < 20);

		return file;
	}

	public static String convertDataTimeToString(Calendar date) {
		try {
			return dateTimeFormat.format(date.getTime());
		} catch (Exception e) {
			if (date != null) {
				AppEnv.logger.errorLogEntry("Util, Не удалось преобразовать время в текст " + date);
			}
			// AppEnv.logger.errorLogEntry(e);
			return "";
		}
	}

	public static void main(String[] args) {
		System.out
		        .println(removeHTMLTags(
		                "<p1><p></p1>I-4979: Берг П. П. -> (Берг П. П.)<p><p> <p>Допереводить непереведенные слова(в файле dict.xml, слова которые с приставкой kaz, файл во вложении)<br></p>")
		                .length());
		System.out
		        .println(removeHTMLTags("I-4979: Берг П. П. -> (Берг П. П.) <p>Допереводить непереведенные слова(в файле dict.xml, слова которые с приставкой kaz, файл во вложении)<br></p>"));
	}

	public static String removeHTMLTags(String text) {
		// Pattern pImgTag = Pattern.compile("\\G(?i)<img\\s+([^>]+)>");
		// Pattern pLink = Pattern.compile("\\G(?i)<A\\s+([^>]+)>");
		// Pattern pLinkX = Pattern.compile("\\G(?i)</A>");
		if (text == null) {
			return "";
		}
		Matcher m = pTag.matcher(text);
		while (!m.usePattern(pAtEnd).find()) {
			if (m.usePattern(pWord).find()) {
			} else if (m.usePattern(pTag).find()) {
				text = text.replaceAll("<br>", " ").replaceAll("<[^>]+>", "");
			} else if (m.usePattern(pEntity).find()) {
				if (m.group().equals("&quot;")) {
					text = text.replaceAll(m.group(), "\"");
				} else if (m.group().equals("&amp;")) {
					text = text.replaceAll(m.group(), "&");
				} else if (m.group().equals("&nbsp;")) {
					text = text.replaceAll(m.group(), " ");
				} else if (m.group().equals("&laquo;")) {
					text = text.replaceAll(m.group(), "«");
				} else if (m.group().equals("&raquo;")) {
					text = text.replaceAll(m.group(), "»");
				} else if (m.group().equals("&lt;")) {
					text = text.replaceAll(m.group(), "<");
				} else if (m.group().equals("&gt;")) {
					text = text.replaceAll(m.group(), ">");
				} else {
					text = text.replaceAll(m.group(), "");
				}
			} else if (m.usePattern(pNonHtml).find()) {
			} else {
				if (m.usePattern(Pattern.compile("\\G(?s).{1,12}")).find()) {
					AppEnv.logger.errorLogEntry("Bad char before '" + m.group() + "'");
				}
				return text.trim();
			}
		}
		return text.trim();
	}

	public static double convertBytesToKilobytes(long a) {
		double k = Math.round(a / 1024.0 * 100000.0) / 100000.0;
		return k;

	}

	public static boolean getRandomBoolean() {
		Random random = new Random();
		return random.nextBoolean();
	}

	public static String generateRandomAsText(String setOfTheLetters) {
		return generateRandomAsText(setOfTheLetters, 16);
	}

	public static String generateRandomAsText(String setOfTheLetters, int len) {
		Random r = new Random();
		String key = "";
		char[] letters = new char[setOfTheLetters.length() + 10];

		for (int i = 0; i < 10; i++) {
			letters[i] = Character.forDigit(i, 10);
		}

		for (int i = 0; i < setOfTheLetters.length(); i++) {
			letters[i + 10] = setOfTheLetters.charAt(i);
		}

		for (int i = 0; i < len; i++) {
			key += letters[Math.abs(r.nextInt()) % letters.length];
		}

		return key;
	}

	public static boolean addrIsCorrect(String email) {
		String validate = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		Pattern pattern = Pattern.compile(validate);
		Matcher matcher = pattern.matcher(email);

		return matcher.matches();
	}

	public static String getTimeDiffInMilSec(long start_time) {
		long time = System.currentTimeMillis() - start_time;
		int sec = (int) time / 1000;// find seconds
		int msec = (int) time % 1000;// find milliseconds
		return Integer.toString(sec) + "." + Integer.toString(msec);
	}

	public static String getTimeDiffInSec(long start_time) {
		long time = System.currentTimeMillis() - start_time;
		int sec = (int) time / 1000;
		return Long.toString(sec);
	}

	public static boolean pwdIsCorrect(String email) {

		return true;

	}

	public static String readFile(String file) {
		BufferedReader reader = null;
		try {
			// File f = new File(file);
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			return stringBuilder.toString();
		} catch (FileNotFoundException e) {
			Server.logger.errorLogEntry(e);
		} catch (IOException e) {
			Server.logger.errorLogEntry(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				Server.logger.errorLogEntry(e);
			}
		}
		return "";
	}

}
