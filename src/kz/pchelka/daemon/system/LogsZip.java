package kz.pchelka.daemon.system;

import kz.pchelka.log.LogFiles;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class LogsZip extends AbstractDaemon {
	

	@Override
	public int process(IProcessInitiator processOwner) {
		LogFiles logs = new LogFiles();
		String logPath = logs.logDir.getAbsolutePath();
		zipLogs(logPath,7);
		
		return 0;
	}
	
	private int zipLogs(String dirPath, int cutOffDays){
		
		try{
			File[] files = getFiles(dirPath);			
			arhive(files,dirPath, cutOffDays);
			
		}catch(Exception e){
			e.printStackTrace();
		}

		return 0;
	}
	
	private File[] getFiles(String pathFolder){
		File folder = new File(pathFolder);
		File[] files = null;
		if(folder.isDirectory()){
			files = folder.listFiles();
		}
		
		return files;
	}
	

	private boolean hasZip(String path, String name){
		
		File folder = new File(path);
		File[] files = null;
		if(folder.isDirectory()){
			files = folder.listFiles();
		}
		
		for(File f:files){
			if(f.getName().equals(name)){
				return true;
			}
		}
		
		return false;
	}
	
	


	private void arhive(File[] files, String pathfile, int cutOffDays) throws Exception{
		
		GregorianCalendar today = new GregorianCalendar();
		ArrayList<File> fileList = new ArrayList<File>();
		for(File file:files){
			GregorianCalendar dateLMod = new GregorianCalendar();
			dateLMod.setTimeInMillis(file.lastModified());
			long dst = TimeUnit.DAYS.convert(today.getTimeInMillis()-dateLMod.getTimeInMillis(),TimeUnit.MILLISECONDS);
		
			if(dst >= cutOffDays){
				if(!file.getName().contains(".zip")){
					fileList.add(file);		
				}
			}
		}
		
		for(File f:fileList){
			BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
			Date lmDate = new Date(attrs.lastModifiedTime().toMillis());
			@SuppressWarnings("deprecation")
			String zipName = (lmDate.getMonth() + 1) + "_" + (lmDate.getYear() + 1900) + ".zip";
			if(hasZip(pathfile, zipName)){
				File zipFile = new File(pathfile + File.separator + zipName);
				addFilesToExistingZip(zipFile, f);
				f.delete();
			}else{
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(pathfile + File.separator + zipName));
				ZipEntry ze = new ZipEntry(f.getName());
		        zos.putNextEntry(ze);
		        zos.closeEntry();
		        zos.close();
		        f.delete();
			}
		}
			
	}
	

	private void addFilesToExistingZip(File zipFile, File file) throws IOException {
      
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();

		boolean renameOk=zipFile.renameTo(tempFile);
		if (!renameOk)
		{
			throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
		}
		byte[] buf = new byte[1024];

		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			boolean notInFiles = true;
       
			if (file.getName().equals(name)) {
               notInFiles = false;
               break;
			}
       
			if (notInFiles) {
				out.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = zin.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		zin.close();
		for (int i = 0; i < 1; i++) {
			InputStream in = new FileInputStream(file);
			out.putNextEntry(new ZipEntry(file.getName()));
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.closeEntry();
			in.close();
		}
		out.close();
		tempFile.delete();
	}

	

}
