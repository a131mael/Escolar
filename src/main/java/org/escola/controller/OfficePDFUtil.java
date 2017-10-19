package org.escola.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.escola.enums.PegarEntregarEnun;
import org.escola.enums.PerioddoEnum;
import org.escola.model.Aluno;
import org.escola.model.Carro;
import org.escola.model.ObjetoRota;
import org.escola.service.AlunoService;
import org.escola.util.ServiceLocator;

import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.codec.Base64.OutputStream;

public class OfficePDFUtil {

	/**
	 * @param args
	 * @throws DocumentException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws DocumentException, MalformedURLException, IOException {

		OfficePDFUtil.gerarPDF(null,null,null,"");
		// TODO Auto-generated method stub

		// Listing 1. Instantiation of document object
		/*
		 * Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		 * 
		 * String temp = System.getProperty("java.io.tmpdir");
		 * 
		 * // Listing 2. Creation of PdfWriter object PdfWriter writer =
		 * PdfWriter.getInstance(document, new FileOutputStream(temp +
		 * File.separator + "ITextTest.pdf"));
		 * 
		 * document.open();
		 * 
		 * // Listing 3. Creation of paragraph object Anchor anchorTarget = new
		 * Anchor("First page of the document.");
		 * anchorTarget.setName("BackToTop"); Paragraph paragraph1 = new
		 * Paragraph();
		 * 
		 * paragraph1.setSpacingBefore(50);
		 * 
		 * paragraph1.add(anchorTarget); document.add(paragraph1);
		 * 
		 * document .add(new Paragraph(
		 * "Some more text on the first page with different color and font type."
		 * , FontFactory.getFont(FontFactory.COURIER, 14, Font.BOLD, new
		 * CMYKColor(0, 255, 0, 0))));
		 * 
		 * // Listing 4. Creation of chapter object Paragraph title1 = new
		 * Paragraph("Chapter 1", FontFactory.getFont( FontFactory.HELVETICA,
		 * 18, Font.BOLDITALIC, new CMYKColor(0, 255, 255, 17))); Chapter
		 * chapter1 = new Chapter(title1, 1); chapter1.setNumberDepth(0);
		 * 
		 * // Listing 5. Creation of section object Paragraph title11 = new
		 * Paragraph("This is Section 1 in Chapter 1",
		 * FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD, new
		 * CMYKColor(0, 255, 255, 17))); Section section1 =
		 * chapter1.addSection(title11); Paragraph someSectionText = new
		 * Paragraph( "This text comes as part of section 1 of chapter 1.");
		 * section1.add(someSectionText); someSectionText = new Paragraph(
		 * "Following is a 3 X 2 table."); section1.add(someSectionText);
		 * 
		 * // Listing 6. Creation of table object PdfPTable t = new
		 * PdfPTable(3);
		 * 
		 * t.setSpacingBefore(25); t.setSpacingAfter(25); PdfPCell c1 = new
		 * PdfPCell(new Phrase("Header1")); t.addCell(c1); PdfPCell c2 = new
		 * PdfPCell(new Phrase("Header2")); t.addCell(c2); PdfPCell c3 = new
		 * PdfPCell(new Phrase("Header3")); t.addCell(c3); t.addCell("1.1");
		 * t.addCell("1.2"); t.addCell("1.3"); section1.add(t);
		 * 
		 * // Listing 7. Creation of list object List l = new List(true, false,
		 * 10); l.add(new ListItem("First item of list")); l.add(new ListItem(
		 * "Second item of list")); section1.add(l);
		 * 
		 * // Listing 8. Adding image to the main document
		 * 
		 * //Image image2 = Image.getInstance("IBMLogo.bmp");
		 * //image2.scaleAbsolute(120f, 120f); //section1.add(image2);
		 * 
		 * // Listing 9. Adding Anchor to the main document. Paragraph title2 =
		 * new Paragraph("Using Anchor", FontFactory.getFont(
		 * FontFactory.HELVETICA, 16, Font.BOLD, new CMYKColor(0, 255, 0, 0)));
		 * section1.add(title2);
		 * 
		 * title2.setSpacingBefore(5000); Anchor anchor2 = new Anchor(
		 * "Back To Top"); anchor2.setReference("#BackToTop");
		 * 
		 * section1.add(anchor2);
		 * 
		 * 
		 * // Listing 10. Addition of a chapter to the main document
		 * document.add(chapter1); document.close();
		 */

	}

	public static void gerarPDF(java.util.List<ObjetoRota> rota, PerioddoEnum periodo,Carro carro, String nomeArquivo)
			throws DocumentException, MalformedURLException, IOException {
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		// Listing 2. Creation of PdfWriter object
		String temp = System.getProperty("java.io.tmpdir");
		PdfWriter writer = PdfWriter.getInstance(document,	new FileOutputStream(temp + File.separator + nomeArquivo));
		document.open();

		Paragraph title1 = new Paragraph(periodo.getName() + " - " +carro.getNome() ,FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC, new CMYKColor(0, 255, 255, 17)));
		Chapter chapter1 = new Chapter(1);
		chapter1.setNumberDepth(0);

		Section section1 = chapter1.addSection(title1); 	
		// Listing 7. Creation of list object
		List l = new List(true, false, 30);
		
		for(ObjetoRota obj:rota){
			String itemLista = ""; 
			if(obj.getAluno() != null){
				itemLista = obj.getPegarEntregar().getSigla() + " - " + obj.getAluno().getNomeAluno();
				l.add(new ListItem(itemLista));
			}else if(obj.getEscola() != null ){
				java.util.List<Aluno> alunos = new ArrayList<>();
				if (periodo.equals(PerioddoEnum.TARDE)) {
					alunos = getAlunoService().findAlunoPegaEscolaTarde(obj.getEscola(), carro);
				} else if (periodo.equals(PerioddoEnum.MANHA)) {
					alunos = getAlunoService().findAlunoPegaEscolaManha(obj.getEscola(), carro);
				} else {

					if (obj.getPegarEntregar() != null
							&& obj.getPegarEntregar().equals(PegarEntregarEnun.PEGAR)) {
						alunos = getAlunoService().findAlunoPegaEscolaMeioDia(obj.getEscola(), carro);
					} else {
						alunos = getAlunoService().findAlunoLevaEscolaMeioDia(obj.getEscola(), carro);
					}
				}
				
				List subLista = new List(true,false,30);
				for(Aluno al : alunos){
					ListItem li = new ListItem(al.getNomeAluno());
					subLista.add(li);					
				}
				ListItem li = new ListItem( obj.getPegarEntregar().getSigla() + " - "  +obj.getEscola().getName());
				l.add(li);
				l.add(subLista);
			}else{
				l.add(new ListItem("TROCA"));
				l.add(new ListItem(obj.getDescricao()));
				l.add(new ListItem("TROCA"));
				l.add(new ListItem(itemLista));
			}
		}
		
		section1.add(l);
		document.add(chapter1);
		document.close();
	}
	
	private static AlunoService getAlunoService(){
		try {
			return ServiceLocator.getInstance().getAlunoService(AlunoService.class.getSimpleName(), AlunoService.class.getName());
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	public static void geraPDF(String nomeArquivo,byte[] pdfByteArray){
		
		try {
			String temp = System.getProperty("java.io.tmpdir");
		FileOutputStream out;
			out = new FileOutputStream(temp + File.separator + nomeArquivo);
			out.write(pdfByteArray);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
