package org.escola.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultUploadedFile;
import org.primefaces.model.UploadedFile;

@Named
@ViewScoped
public class FileUploadMB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UploadedFile file;
	private byte[] bts = null;

	public FileUploadMB() {
	}

	public void handleFileUpload(FileUploadEvent event) throws IOException {
		file = event.getFile();
		setBts(file.getContents());
		
		getBase64FromByte(bts);
	}

	public String getBase64FromByte(byte[] bytes) throws IOException {
		byte[] encoded = Base64.getEncoder().encode(bytes);
		String encodedString = new String(encoded);

		return encodedString;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public byte[] getBts() {
		return bts;
	}

	public void setBts(byte[] bts) {
		this.bts = bts;
	}
}