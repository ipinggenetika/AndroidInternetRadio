package com.dirt.radio;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

public class Utils {
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	public static void makeSdDir() {
		String thumbnailFolder = Environment.getExternalStorageDirectory()

		+ "//viplayproj"; // myb rename this to something less conspicuous
		File dir = new File(thumbnailFolder);
		try {
			if (dir.mkdir()) {
				System.out.println("Directory created");//
			} else {
				System.out.println("Directory is not created");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}