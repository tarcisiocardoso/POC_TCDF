package PocTJDF.PocTJDF;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Hello world!
 *
 */
public class App {
	static String dadosNoLimbo = "";
	static Registro licitacao = null;
	static Arquivo arquivo;
	static List<App.Grupo> lstGrupo = new ArrayList<App.Grupo>();
	static List<App.SubGrupo> lstSubGrupo = new ArrayList<App.SubGrupo>();
	static String linhas[] = null;
	static String linha = null;
	static int paginaSecaoIII = 0;
//	static int tamanhoPulo = 0;
	
	static LeitorGrupo leitorGrupo = new LeitorGrupo();
	static LeitorSubGrupo leitorSubGrupo = new LeitorSubGrupo();
	static LeitorConteudo leitorConteudo = new LeitorConteudo();
	
	public static Properties prop = new Properties();
	

	
	static List<String> regraNaoSubGrupo = new ArrayList<String>() {{
		add("\"");
		add(".");
		add("RESULTADO");
		add("PREGÃO");
		add("Pregoeira");
		add("AQUISIÇÃO");
	}};
	static List<String> subGrupo = new ArrayList<String>() {{
		add("\"");
		add("CONTROLADORIA");
		add("FUNDO");
		add("PROCESSO:");
	}};
	
	public static void main(String[] args) throws IOException {
		InputStream in = new App().getClass().getResourceAsStream("properties.conf");
		prop.load(in);
		in.close();

		System.out.println("===================");
		System.out.println("lendo conteudo do diretorio: "+prop.getProperty("workdir"));
		
		File fs = new File(prop.getProperty("workdir"));//"/home/tarcisio/trabalho/EDS/projetos/TJDF/tmp");
		for (File f : fs.listFiles()) {
			if( f.getName().endsWith("pdf")) {
				salvaArquivo( f.getName() );
				if( !recuperaDadosPDF(f) ) {
					//TODO implementar a leitura do aquivo por outra forma, exemplo ubuntu: "pdftotext file.pdf file.txt"
					continue;
				}
				leitorGrupo.montaGrupo();
				if( App.paginaSecaoIII == 0 ) continue;
				leitorSubGrupo.montaSubGrupo( leitorGrupo.lstGrupo );
				
				leitorConteudo.ler();
				//ler(f);
			}
		}
		System.out.println("===========================");
		DataBase.getInstancia().close();
	}
	

	private static boolean recuperaDadosPDF(File f) {
		try {
			PDDocument document = PDDocument.load(f);
			
			PDFTextStripper pdfStripper = new PDFTextStripper();
			
			String text = pdfStripper.getText(document);
						
			linhas = text.split("\n");
			
			document.close();
		} catch (Exception e) {
			arquivo.problema = e.getMessage();
			DataBase.getInstancia().updateArquivo(arquivo);
			System.err.println(e.getMessage());
			return false;
		}
		leitorGrupo.linhas = linhas;
		leitorSubGrupo.linhas = linhas;
		return true;
	}

	private static void salvaArquivo(String nome) {		
		long id= DataBase.getInstancia().insertArquivo( nome );
		arquivo = new Arquivo();
		arquivo.id = id;
		arquivo.nome = nome;
		
		leitorGrupo.arquivo = arquivo;
	}
	
	public static class Arquivo{
		public long id;
		public String nome;
		public String problema;
	}
	public static class Grupo{
		public long id;
		public long idArquivo;
		public int linha;
		public int pagina;
		public String nome;
		public String resumo;
		public String problema;
		
		public String toString() { return nome; }
	}
	
	public static class SubGrupo{
		public long id;
		public long idGrupo;
		public int linha;
		public String nome;
		
		public String toString() { return nome+" ["+linha+"]"; }
	}

	public static class Registro {
		public long id;
		public long idSubGrupo;
		public String tipo;
		public String dado;
		public String conteudo;
		
		public String toString() {
			return tipo;
		}
		
	}
}