package kz.lof.scheduler.tasks;

import kz.pchelka.log.LogFiles;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class LogsZip implements Job {

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		int cutOffDays = 7;
		LogFiles logs = new LogFiles();
		String pathfile = logs.logDir.getAbsolutePath();

		GregorianCalendar today = new GregorianCalendar();
		ArrayList<File> fileList = new ArrayList<>();

		for (File file : logs.getLogFileList()) {
			long dst = TimeUnit.DAYS.convert(today.getTimeInMillis() - file.lastModified(), TimeUnit.MILLISECONDS);

			if (dst >= cutOffDays && !file.getName().contains(".zip")) {
				fileList.add(file);
			}
		}

		for (File file : fileList) {
			Date lmDate = new Date(file.lastModified());

			@SuppressWarnings("deprecation")
			String zipName = (lmDate.getMonth() + 1) + "_" + (lmDate.getYear() + 1900) + ".zip";
			if(logs.getLogFileList().contains(new File(zipName))){
				File zipFile = new File(pathfile + File.separator + zipName);
				try {
					addFilesToExistingZip(zipFile, file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				file.delete();
			} else {
				try(FileOutputStream fos = new FileOutputStream(pathfile + File.separator + zipName);
					ZipOutputStream zos = new ZipOutputStream(fos)){

					ZipEntry ze = new ZipEntry(file.getName());
					zos.putNextEntry(ze);
					zos.closeEntry();
				} catch (IOException e) {
					e.printStackTrace();
				}
				file.delete();
			}
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void addFilesToExistingZip(File zipFile, File file) throws IOException {

		File tempFile = File.createTempFile(zipFile.getName(), null);

		if (!zipFile.renameTo(tempFile)) {
			throw new IOException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
		}

		byte[] buf = new byte[1024];

		try (InputStream is = new FileInputStream(tempFile);
			 ZipInputStream zin = new ZipInputStream(is);
			 OutputStream os = new FileOutputStream(zipFile);
			 ZipOutputStream out = new ZipOutputStream(os);
			 InputStream in = new FileInputStream(file)
		){

			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				String name = entry.getName();

				if (file.getName().equals(name)) {
					break;
				}

				out.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = zin.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				entry = zin.getNextEntry();
			}

			out.putNextEntry(new ZipEntry(file.getName()));
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.closeEntry();
		} catch (IOException e) {
			e.printStackTrace();
		}


		tempFile.delete();
	}

}
