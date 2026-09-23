/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.escola.util;

import java.io.InputStream;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author abimael
 */
public class FileDownload {

    public static StreamedContent getContentDoc(InputStream stream, String nomeArquivoSaida){
    	return new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", nomeArquivoSaida);
    }
}
