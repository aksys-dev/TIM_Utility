package it.telecomitalia.TIMgamepad2.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamHelper {
	public static InputStream getInputStream(FileInputStream fileInput) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = -1;
		InputStream inputStream = null;
		try {
			while ((n = fileInput.read(buffer)) != -1) {
				baos.write(buffer, 0, n);

			}
			byte[] byteArray = baos.toByteArray();
			inputStream = new ByteArrayInputStream(byteArray);
			return inputStream;

		} catch (FileNotFoundException e) {
			//  Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					//  Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static InputStream getInputStream(String filepath)
			throws IOException {
		FileInputStream fileInput = new FileInputStream(new File(filepath));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = -1;
		InputStream inputStream = null;
		try {
			while ((n = fileInput.read(buffer)) != -1) {
				baos.write(buffer, 0, n);

			}
			byte[] byteArray = baos.toByteArray();
			inputStream = new ByteArrayInputStream(byteArray);
			fileInput.close();
			return inputStream;

		} catch (FileNotFoundException e) {
			//  Auto-generated catch block
			e.printStackTrace();
			fileInput.close();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			fileInput.close();
			return null;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					//  Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}
