package PocTJDF.PocTJDF;

import java.io.File;
import java.io.IOException;

public class MontaCP {

	String tudo = "";
	
	public static void main(String[] args) {
		String home = System.getProperty("user.home");
		System.out.println(home);
		
		File fs = new File(home+"/.m2");//"/home/tarcisio/trabalho/EDS/projetos/TJDF/tmp");
		
		MontaCP cp = new MontaCP();
		
		try {
			cp.buscaRerursivo(fs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("export CLASS_TUDO=\""+cp.tudo+"\"");
	}
	
	private void buscaRerursivo(File fs) throws IOException {
		for (File f : fs.listFiles()) {
			if( f.getName().endsWith(".jar")) {
				tudo += f.getCanonicalPath()+":";
			}else if( f.isDirectory() ) {
				buscaRerursivo(f);
			}
		}
	}
}
