package kz.flabs.runtimeobj;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.h2.holiday.Holiday;
import kz.flabs.dataengine.h2.holiday.HolidayCollection;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.DocumentExceptionType;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.util.Util;
import kz.flabs.webrule.constants.FieldType;
import kz.nextbase.script.constants._PeriodType;

public class RuntimeObjUtil implements Const {
	private static final int v = 60 * 60 * 1000;
	private static final float PRIORITY_FACTOR = 2f;

	int minPriority;
	int maxPriority;
	int minComplication;
	int maxComplication;

	public BaseDocument getGrandParentDocument(IDatabase db, BaseDocument doc) throws DocumentException, DocumentAccessException,
	        ComplexObjectException {
		if (!doc.isNewDoc()) {
			BaseDocument parentDoc = null;
			parentDoc = db.getDocumentByComplexID(doc.parentDocType, doc.parentDocID);
			if (parentDoc != null && parentDoc.parentDocID != 0 && parentDoc.parentDocType != Const.DOCTYPE_UNKNOWN) {
				parentDoc = getGrandParentDocument(db, parentDoc);
			}

			return parentDoc;
		} else {
			throw new DocumentException(DocumentExceptionType.CANNOT_GET_PARENT_DOCUMENT_FROM_NEWDOC);
		}
	}

	public static Calendar getCtrlDate(Calendar fromTime, int priority, int complication) {
		Calendar ctrlDate = Calendar.getInstance(), startDate = Calendar.getInstance(), tempDate = Calendar.getInstance();
		float dayCount;
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss");
		try {
			tempDate.setTime(format.parse("01.01.2000.18.00.00"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		tempDate.set(fromTime.get(Calendar.YEAR), fromTime.get(Calendar.MONTH), fromTime.get(Calendar.DAY_OF_MONTH));

		dayCount = (priority + complication) / PRIORITY_FACTOR; // по приоритету
		// dayCount = (priority + complication) * COMPLICATION_FACTOR; //по
		// сложности

		switch (fromTime.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SATURDAY:
			startDate.setTimeInMillis(tempDate.getTimeInMillis() - 24 * v);
			break;
		case Calendar.SUNDAY:
			startDate.setTimeInMillis(tempDate.getTimeInMillis() - 48 * v);
			break;
		case Calendar.MONDAY:
			if (fromTime.getTimeInMillis() <= tempDate.getTimeInMillis() - 9 * v) {
				startDate.setTimeInMillis(tempDate.getTimeInMillis() - 72 * v);
				break;
			}
		default:
			if (fromTime.getTimeInMillis() >= tempDate.getTimeInMillis()) {
				startDate.setTimeInMillis(tempDate.getTimeInMillis());
				break;
			}
			if (fromTime.getTimeInMillis() <= tempDate.getTimeInMillis() - 9 * v) {
				startDate.setTimeInMillis(tempDate.getTimeInMillis() - 24 * v);
				break;
			}
			startDate = (Calendar) fromTime.clone();
		}

		ctrlDate.setTimeInMillis(startDate.getTimeInMillis());
		for (int i = 0; i < (int) dayCount; i++) {
			if (ctrlDate.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
				ctrlDate.setTimeInMillis(ctrlDate.getTimeInMillis() + 24 * v);
			} else {
				ctrlDate.setTimeInMillis(ctrlDate.getTimeInMillis() + 72 * v);
			}
		}

		tempDate.set(ctrlDate.get(Calendar.YEAR), ctrlDate.get(Calendar.MONTH), ctrlDate.get(Calendar.DAY_OF_MONTH));

		ctrlDate.setTimeInMillis(ctrlDate.getTimeInMillis() + (int) ((dayCount - (int) dayCount) * 8 * 60 * 60 * 1000));

		if (ctrlDate.getTimeInMillis() > tempDate.getTimeInMillis()) {
			if (ctrlDate.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
				ctrlDate.setTimeInMillis(tempDate.getTimeInMillis() + 15 * v);
			} else {
				ctrlDate.setTimeInMillis(tempDate.getTimeInMillis() + 63 * v);
			}
		}
		return ctrlDate;
	}

	public static int getDiffBetweenDays(Calendar currentDate, Calendar ctrlDate, Calendar[] holidays, boolean sixWorkdays) {
		int dayOfWeek, workDayCount, allDayCount, weekCount;
		allDayCount = (int) Math.floor((ctrlDate.getTimeInMillis() - currentDate.getTimeInMillis()) / (v * 24));
		weekCount = (int) Math.ceil(allDayCount / 7);
		workDayCount = sixWorkdays ? weekCount * 6 : weekCount * 5;
		dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);

		for (int i = 0; i < allDayCount - weekCount * 7; i++) {
			if (sixWorkdays ? dayOfWeek != 1 : dayOfWeek != 1 && dayOfWeek != 7) {
				workDayCount++;
			}
			if (dayOfWeek != 7) {
				dayOfWeek++;
			} else {
				dayOfWeek = 1;
			}
		}

		Calendar[] tHolidays = holidays.clone();

		for (int i = currentDate.get(Calendar.YEAR); i <= ctrlDate.get(Calendar.YEAR); i++) {
			for (int j = 0; j < tHolidays.length; j++) {
				tHolidays[j].set(Calendar.YEAR, i + 1900);
				if (currentDate.getTime().compareTo(tHolidays[j].getTime()) * tHolidays[j].getTime().compareTo(ctrlDate.getTime()) > 0) {
					workDayCount--;
				}
			}
		}
		return workDayCount;
	}

	public static String cutHTMLText(String text, int length) {
		String result = Util.removeHTMLTags(text);
		result = cutText(result, length);
		return result;

	}

	public static String cutText(String text, int length) {
		if (text.length() <= length) {
			return text;
		}
		int indOfSpace = 0;
		for (int i = length - 3; i >= 0; i--) {
			if (text.charAt(i) == ' ') {
				indOfSpace = i;
				break;
			}
		}
		return text.substring(0, indOfSpace) + "...";
	}

	public static String getTypeAttribute(FieldType type) {
		String attr = " type=\"" + type.toString() + "\"";
		return attr;
	}

	public static String getTypeAttribute(int type) {
		String attr = "";
		switch (type) {
		case TEXT:
			attr = " type=\"string\"";
			break;
		case DATETIMES:
			attr = " type=\"datetime\"";
			break;
		case NUMBERS:
			attr = " type=\"number\"";
			break;
		case COMPLEX_OBJECT:
			attr = " type=\"complex\"";
			break;
		case AUTHORS:
			attr = " type=\"authors\"";
			break;
		case TEXTLIST:
			attr = " type=\"map\"";
			break;
		case READERS:
			attr = " type=\"readers\"";
			break;
		case FILES:
			attr = " type=\"files\"";
			break;

		}
		return attr;
	}

	public static int countMaxPage(long colCount, int pageSize) {
		float mp = (float) colCount / (float) pageSize;
		float d = Math.round(mp);

		int maxPage = (int) d;
		if (mp > d) {
			maxPage++;
		}
		if (maxPage < 1) {
			maxPage = 1;
		}
		return maxPage;
	}

	@Deprecated
	public static int countMaxPage(int colCount, int pageSize) {
		float mp = (float) colCount / (float) pageSize;
		float d = Math.round(mp);

		int maxPage = (int) d;
		if (mp > d) {
			maxPage++;
		}
		if (maxPage < 1) {
			maxPage = 1;
		}
		return maxPage;
	}

	public static int calcStartEntry(int pageNum, int pageSize) {
		int pageNumMinusOne = pageNum;
		pageNumMinusOne--;
		return pageNumMinusOne * pageSize;
	}

	public static void checkUploadedFiles(String dirPath, List<String> deletedFiles) {
		File dir = new File(dirPath);
		if (dir.exists() && dir.isDirectory()) {
			int folderNum = 1;
			File folder = new File(dirPath + Integer.toString(folderNum));
			while (folder.exists()) {
				File[] listFiles = folder.listFiles();
				for (int i = listFiles.length; --i >= 0;) {
					if (deletedFiles.contains(listFiles[i].getName())) {
						listFiles[i].delete();
					}
				}
				folderNum++;
				folder = new File(dirPath + Integer.toString(folderNum));
			}
		}
	}

	public HashMap<String, BlobFile> getUploadedFiles(Map<String, String[]> fields) {
		HashMap<String, BlobFile> fl = new HashMap<>();
		if (fields.containsKey("filename") && fields.containsKey("fileid")) {
			String[] fileNames = fields.get("filename");
			String[] fileIDS = fields.get("fileid");
			String[] fileHash = fields.get("filehash");
			for (int i = 0; i < fileNames.length; i++) {
				BlobFile bFile = new BlobFile();
				bFile.originalName = fileNames[i];
				bFile.path = "";
				bFile.id = fileIDS[i];
				bFile.checkHash = fileHash[i];
				String commentVal[] = fields.get("comment" + bFile.checkHash);
				if (commentVal != null) {
					bFile.comment = commentVal[0];
				} else {
					bFile.comment = "";
				}
				fl.put(bFile.originalName, bFile);
			}
		}
		return fl;
	}

	public static Calendar[] getPeriodDates(_PeriodType pType, boolean sixWorkDays, HolidayCollection holidays) {
		ArrayList<Calendar> resultList = new ArrayList<>();

		if (pType == _PeriodType.WORKDAYS) {
			Calendar date = Calendar.getInstance();
			date.setFirstDayOfWeek(Calendar.MONDAY);
			date.set(Calendar.DAY_OF_YEAR, date.getActualMaximum(Calendar.DAY_OF_YEAR));
			for (int i = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR); i > 0; i--) {
				boolean isHoliday = false;
				for (Holiday h : holidays.holidays) {
					if (h.getStartDate().get(Calendar.DAY_OF_YEAR) <= date.get(Calendar.DAY_OF_YEAR)
					        && date.get(Calendar.DAY_OF_YEAR) <= h.getEndDate().get(Calendar.DAY_OF_YEAR)) {
						isHoliday = true;
						break;
					}
				}

				if (!isHoliday && date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
				        && (sixWorkDays || date.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)) {
					resultList.add(0, (Calendar) date.clone());
				}
				date.add(Calendar.DATE, -1);
			}
			return resultList.toArray(new Calendar[resultList.size()]);
		}

		Calendar date = Calendar.getInstance(), shiftedEndDate = Calendar.getInstance();
		date.setFirstDayOfWeek(Calendar.MONDAY);
		date.set(Calendar.DAY_OF_YEAR, date.getActualMaximum(Calendar.DAY_OF_YEAR));
		shiftedEndDate.set(Calendar.DAY_OF_YEAR, shiftedEndDate.getActualMaximum(Calendar.DAY_OF_YEAR));
		shiftedEndDate.add(Calendar.DATE, 1);

		for (int i = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR); i > 0; i--) {
			boolean isEnd = false;
			switch (pType) {
			case END_OF_WEEK:
				isEnd = date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				        || date.get(Calendar.DAY_OF_YEAR) == shiftedEndDate.get(Calendar.DAY_OF_YEAR)
				        && date.get(Calendar.YEAR) == shiftedEndDate.get(Calendar.YEAR);
				break;
			case END_OF_MONTH:
				isEnd = date.get(Calendar.DAY_OF_MONTH) == date.getActualMaximum(Calendar.DAY_OF_MONTH)
				        || date.get(Calendar.DAY_OF_YEAR) == shiftedEndDate.get(Calendar.DAY_OF_YEAR)
				        && date.get(Calendar.YEAR) == shiftedEndDate.get(Calendar.YEAR);
				break;
			case END_OF_QUARTER:
				isEnd = date.get(Calendar.MONTH) == (int) Math.floor(date.get(Calendar.MONTH) / 3.0) * 3 + 2
				        && date.get(Calendar.DAY_OF_MONTH) == date.getActualMaximum(Calendar.DAY_OF_MONTH)
				        || date.get(Calendar.DAY_OF_YEAR) == shiftedEndDate.get(Calendar.DAY_OF_YEAR)
				        && date.get(Calendar.YEAR) == shiftedEndDate.get(Calendar.YEAR);
				break;
			case END_OF_YEAR:
				isEnd = date.get(Calendar.DAY_OF_YEAR) == date.getActualMaximum(Calendar.DAY_OF_YEAR)
				        || date.get(Calendar.DAY_OF_YEAR) == shiftedEndDate.get(Calendar.DAY_OF_YEAR)
				        && date.get(Calendar.YEAR) == shiftedEndDate.get(Calendar.YEAR);
				break;
			}

			if (isEnd) {
				boolean isHoliday = false;
				for (Holiday h : holidays.holidays) {
					if (h.getStartDate().get(Calendar.DAY_OF_YEAR) <= date.get(Calendar.DAY_OF_YEAR)
					        && date.get(Calendar.DAY_OF_YEAR) <= h.getEndDate().get(Calendar.DAY_OF_YEAR)) {
						isHoliday = true;
						break;
					}
				}
				if (isHoliday || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || !sixWorkDays
				        && date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
					shiftedEndDate = (Calendar) date.clone();
					shiftedEndDate.add(Calendar.DATE, -1);
				} else {
					resultList.add(0, (Calendar) date.clone());
				}
			}
			date.add(Calendar.DATE, -1);
		}

		return resultList.toArray(new Calendar[resultList.size()]);
	}

	public static void main(String[] args) {

		for (_PeriodType v : _PeriodType.values()) {
			System.out.println(v);
			Calendar[] d = getPeriodDates(v, false, new HolidayCollection(Calendar.getInstance().get(Calendar.YEAR)));
			for (Calendar sd : d) {
				System.out.println(Util.convertDataToString(sd));
			}
			System.out.println("-------------------");
		}

	}

}
