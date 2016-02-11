package kz.nextbase.script.reports;

import java.io.File;

import kz.flabs.util.Util;
import kz.lof.env.Environment;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;
import kz.nextbase.script.constants._ExportedFileType;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;

public class _ExportManager{
	private String filePath;
	private String originalName;

	_ExportManager(JasperPrint print, String fileName, _ExportedFileType format) throws _Exception{
		originalName = fileName;
		String folderID = Util.generateRandomAsText();
		File tmp = new File(Environment.tmpDir + File.separator + folderID);
		if (!tmp.exists()){
			tmp.mkdir();				
		}
		String filepath = tmp.getAbsolutePath()  + File.separator + fileName;
		filePath =  new File(filepath).getAbsolutePath();
		try {
			switch(format){
			case PDF:
				JasperExportManager.exportReportToPdfFile(print, filePath);				
				break;
			case XLS:
				JExcelApiExporter xlsExporter = new JExcelApiExporter();
				xlsExporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				xlsExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, filePath);
				xlsExporter.exportReport();
				break;
			case HTML:
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, filePath + File.separator);
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, filePath);
				exporter.exportReport();
				break;
            case DOC:
                JRDocxExporter docExporter = new JRDocxExporter();
                docExporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
                docExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, filePath);
                docExporter.exportReport();
			default:
				JasperExportManager.exportReportToPdfFile(print, filePath);
				break;
			}

		} catch (JRException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " constructor: _ExportManger(" + print + "," + fileName + "," + format + ")");
		}
	}


	public String getFilePath() {
		return filePath;
	}

	public String getOriginalFileName() {	
		return originalName;
	}
}
