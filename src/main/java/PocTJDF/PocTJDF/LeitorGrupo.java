package PocTJDF.PocTJDF;

import java.util.ArrayList;
import java.util.List;

import PocTJDF.PocTJDF.App.Arquivo;
import PocTJDF.PocTJDF.App.Grupo;

public class LeitorGrupo {

	static String linhas[] = null;
	static String linha = null;
	
	public void montaGrupo() {
		boolean isSumario = false;
		for(int i=0; i < linhas.length; i++) {
			linha = linhas[i].trim();
			if( !isSumario) {
				if( linha.startsWith("SUMÁRIO") ) {
					isSumario = true;
				}
				continue;
			}
			if( linha.equals("PÁG.")) continue;
			
			if( isSumario && linha.endsWith("PÁGINA 2") ) {
				break;
			}else {
				//se passou da pagina 100 e ainda não econtrou o sumario
				if( i > 200) {
					break;
				}
			}
			
			int pos = linha.lastIndexOf(" ");
			if( pos > 0 ) {
				String num = linha.substring(pos, linha.length() );
				try {
					int numero = Integer.parseInt(num.trim() );
					if( linha.indexOf("...")> 0 && numero > 0 ) {
						pos = linha.indexOf(".");
						App.Grupo g = new Grupo();
						g.idArquivo = (Long)App.arquivo.id;
						g.nome = linha.substring(0, pos ).toUpperCase().trim();
						g.pagina = numero;
						App.lstGrupo.add(g);
					}else if( isSumario ) { //pode ser quebra de linha dentro de um grupo
						if(checkNomeIsGrupo(linha)) { //se a linha tem substantivo em maiusculo, tem forte chance de ser um grupo
							String linha2 = linhas[i+1];
							pos = linha2.lastIndexOf(" ");
							if( pos > 0 ) { // se passar pelo proximo criterio é um grupo que foi quebrado a linha
								num = linha2.substring(pos, linha2.length() );
								Integer.parseInt(num.trim() );
								
								pos = linha2.indexOf(".");
								App.Grupo g = new Grupo();
								g.idArquivo = (Long)App.arquivo.id;
								g.nome = linha+ "\n"+ linha2.substring(0, pos ).trim().toUpperCase();
								g.pagina = numero;
								App.lstGrupo.add(g);
								i++;
							}
						}else { //se é numero e esta dentro do sumario
							linha = linha.replaceAll(" ", "").trim();
							Integer.parseInt(linha ); // se toda a linha é numero, indica uma quebra de sumario
							linha =linhas[i-1].replaceAll("\\.", "");
							if( !linha.equals( App.lstGrupo.get(App.lstGrupo.size()-1).nome)) { //procura o grupo que ficou para traz. Para simplificar proucra apenas duas linhas para traz
								 String nome = linha;
								 linha =linhas[i-2].replaceAll("\\.", "");
								 if( !linha.equals( App.lstGrupo.get(App.lstGrupo.size()-1).nome)) {
									 if( linha.endsWith("-")) {
										 linha = linha.replaceAll("-", "");
										 nome = linha+nome;
									 }else {
										 nome = linha+" "+nome;
									 }
								 }
								 App.Grupo g = new Grupo();
								g.idArquivo = (Long)App.arquivo.id;
								g.nome = nome.trim().toUpperCase();
								g.pagina = numero;
								App.lstGrupo.add(g);
							}
							
						}
					}
				}catch(Exception e) {
					if( !isSumario ) continue;
					if(checkNomeIsGrupo(linha)) { //se a linha tem substantivo em maiusculo, tem forte chance de ser um grupo
						
						if( i+1 >= linhas.length ) continue;
						
						String linha2 = linhas[i+1];
						pos = linha2.lastIndexOf(" ");
						if( pos > 0 ) { // se pessar pelo proximo criterio é um grupo que foi quebrado a linha
							try {
								num = linha2.substring(pos, linha2.length() );
								int pagina = Integer.parseInt(num.trim() );
								
								pos = linha2.indexOf(".");
								App.Grupo g = new Grupo();
								g.idArquivo = (Long)App.arquivo.id;
								g.linha = App.lstGrupo.size() + 1;
								g.nome = (linha+ "\n"+ linha2.substring(0, pos )).trim().toUpperCase();
								g.pagina = pagina;
								App.lstGrupo.add(g);
								i++;
							}catch(Exception e2) {
							}
						}
						
					}
				}
			}else if( isSumario ) { //pode ser quebra de linha dentro de um grupo
				if(checkNomeIsGrupo(linha)) { //se a linha tem substantivo em maiusculo, tem forte chance de ser um grupo
					if( i+1 >= linhas.length) continue;
					
					String linha2 = linhas[i+1];
					pos = linha2.lastIndexOf(" ");
					if( pos > 0 ) { // se pessar pelo proximo criterio é um grupo que foi quebrado a linha
						try {
							String num = linha2.substring(pos, linha2.length() );
							int pagina = Integer.parseInt(num.trim() );
							
							pos = linha2.indexOf(".");
							App.Grupo g = new Grupo();
							g.idArquivo = (Long)App.arquivo.id;
							g.linha = App.lstGrupo.size() + 1;
							g.nome = linha+ "\n"+ linha2.substring(0, pos ).toUpperCase();
							g.pagina = pagina;
							App.lstGrupo.add(g);
						}catch(Exception e) {}
					}
					
				}
			}
		}
		montResumo();
		App.paginaSecaoIII = buscaPaginaSecaoIII();
		if( App.paginaSecaoIII  == 0 ) return;
		comparaSecaoIII( App.paginaSecaoIII );
		reestruturaSeGrupoTerminaNaPagina();
		salvaBanco();
		buscaGrupoEmOrdemLinha();
	}
	
	private void buscaGrupoEmOrdemLinha() {
		App.lstGrupo = DataBase.getInstancia().buscaGrupoEmOrdemLinha(App.arquivo.id);
		for (Grupo g : App.lstGrupo) {
			System.out.println(g);
		}
		
	}

	private void reestruturaSeGrupoTerminaNaPagina() {
		for (Grupo g : App.lstGrupo) {
			if( g.linha+1 < linhas.length ) {
				if( linhas[g.linha+1].indexOf("PÁGINA") > 0 ) {
					//g.linha += 4;
					g.problema = "Fim da linha indetermiado. Não tem como saber quais subgrupo pertence a esse ou o proximo grupo.";
				}
			}
		}		
	}

	private void salvaBanco() {
		for (Grupo g : App.lstGrupo) {
			System.out.println(g);
			inserir(g);
		}
	}

	private void comparaSecaoIII(int pagina) {
		List<App.Grupo> lst = new ArrayList<App.Grupo>();
		for (int i = pagina+1; i < linhas.length; i++) {
			linha = linhas[i].trim();
			if( isIgnoraLinha() ) continue;
			
			if (linha.toUpperCase().equals(linha)) { // pode ser um grupo
				for (Grupo g : App.lstGrupo) {

					if (linha.equals(g.nome)) {
						g.linha = i;
						lst.add(g);
						break;
					}else if( verificacaoComplexa(linha, g, i) ) {
						g.linha = i;
						lst.add(g);
						break;
					}
				}
			}
		}
		App.lstGrupo = lst;
	}

	private boolean verificacaoComplexa(String ln, Grupo g, int i) {
		if( g.nome.indexOf(ln)>=0 ) {
//			if( ln.endsWith(",")) {
				
				String s = "";
				int ii = i;
				while( ii < linhas.length && linhas[ii].toUpperCase().equals(linhas[ii]) ) {
					s += linhas[ii++];
				}
				if( !s.isEmpty() ) {
					s = s.replaceAll(" ", "");
					if( s.indexOf(g.resumo )>=0 ) return true;
				}
//			}
		}else {
			ln = ln.replaceAll(" ", "");
			if( ln.equals(g.nome)) return true;
		}
		return false;
	}

	private boolean isIgnoraLinha() {
		if( linha.endsWith(".") ) return true;
		if( linha.length() < 5) return true;
		return false;
	}

	private int buscaPaginaSecaoIII() {
		int pos = 0;
		for(int i=100; i < linhas.length; i++) { // nunca a SEÇÃO III estará antes da linha 100
			linha = linhas[i];
			if( linha.equals("SEÇÃO III")) {
				if( pos == 0 ) {
					pos = i;
				}else {
					pos = i;
				}				
			}
		}
		
		if( pos < 1000 && linhas.length > 1000) {
			System.err.println(">>>PROBLEMA SEÇÃO III NÃO FOI ENCONTRADA NO ALGORITMO PADRÃO "+App.arquivo.nome+" <<<");
			for(int i=pos; i < linhas.length; i++) { // nunca a SEÇÃO III estará antes da linha 100
				linha = linhas[i];
				if( linha.startsWith("SEÇÃO III")) {
					if( pos == 0 ) {
						pos = i;
					}else {
						pos = i;
					}				
				}
			}
		}
		return pos;
	}

	private void montResumo() {
		for( Grupo g: App.lstGrupo) {
			g.resumo = g.nome.replaceAll("\n", "");
			g.resumo = g.resumo.replaceAll(" ", "");
		}		
	}

	private static void inserir(Grupo g) {
		g.id = DataBase.getInstancia().insertGrupo(g);
	}
	private static boolean checkNomeIsGrupo(String liha) {
		String [] arr = linha.split(" ");
		int qtd = 0;
		for( String s: arr) {
			if( s.length() > 2) {
				String p = s.charAt(0)+"";
				if( p.toUpperCase().equals(p)) {
					qtd++;
				}else {
					qtd--;
				}
			}
		}
		return qtd > 0;
	}
}
