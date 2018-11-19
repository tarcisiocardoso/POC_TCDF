package PocTJDF.PocTJDF;

import java.io.File;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class PdfItextTeste {

	public static void main(String[] args) throws Exception {
		System.out.println("LALALAL");
		
//		File fs = new File("/home/tarcisio/trabalho/EDS/projetos/TJDF/tmp");
//		for (File f : fs.listFiles()) {
//			if( f.getName().endsWith("pdf")) {
				System.out.println( "/home/tarcisio/trabalho/EDS/projetos/TJDF/tmp/DODF 185 27-09-2018 INTEGRA.pdf"); //f.getCanonicalPath() );
				PdfReader reader = new PdfReader( "/home/tarcisio/trabalho/EDS/projetos/TJDF/tmp/DODF 185 27-09-2018 INTEGRA.pdf");//f.getCanonicalPath());
			    int numPagina = reader.getNumberOfPages();
			    
			    for(int i =0; i < numPagina; i++) {
			    	String page = PdfTextExtractor.getTextFromPage(reader, i+1);
			    	System.out.println( page);
			    	System.out.println(">>>>====<<<<");
			    }
//			}
//		}
	}
}
