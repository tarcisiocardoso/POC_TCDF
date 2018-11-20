package PocTJDF.PocTJDF;

import java.util.ArrayList;
import java.util.List;

import PocTJDF.PocTJDF.App.Grupo;
import PocTJDF.PocTJDF.App.SubGrupo;

public class LeitorSubGrupo {

	String linha = null;
	public String[] linhas;
	static List<App.Grupo> lstGrupo = null;
	static List<SubGrupo> lstSubGrupo = new ArrayList<App.SubGrupo>();
	
	String palavraRestritiva[] = new String[]{
		"LICITAÇÃO", "EXTRATO", "AVISO", "PREGÃO", "RETIFICAÇÃO",
		"ADITIVO", "__", "LEIA-SE", "EDITAL", "CNPJ", "OBJETO", "ENTREGA", "RECEBIMENTO",
		"PROCESSO", "CHAMAMENTO", "TERMO", "RESULTADO", "PROGRAMA", "ADULTO", "R$", "NÃO", "CM ", "RECONHECIMENTO",
		"UASG", "LTDA"
	};

	public void montaSubGrupo( List<App.Grupo> lst) {
		
		lstGrupo = lst;
		int init = App.paginaSecaoIII+1;
		for( Grupo g: lstGrupo) {
			
			for( int i= init; i< g.linha; i++) {
				linha = linhas[i];
				if( linha.indexOf("12361622149760002; 12362622149769534;")>=0 ) {
					System.out.println("xxxxxx");
				}
				if( linha.toUpperCase().equals(linha )) {
					if(isNotValid()) continue;
					if( verificaFimSubGrupo(i+1) ) continue;
					
					SubGrupo sg = new SubGrupo();
					sg.idGrupo = g.id;
					sg.nome = linha;
					sg.linha = i;
					lstSubGrupo.add(sg);
				}
			}
			init = buscaProximaLinha(g);//g.linha+1;
			if( init < 0 ) break;
		}
		
		reavaliaSubGrupo();
	}


	private void reavaliaSubGrupo() {

		List<SubGrupo> lst= new ArrayList<App.SubGrupo>();
		for( SubGrupo sg: lstSubGrupo ) {
			if( isProximoGrupo(sg) ) continue;
			if( isProximoSubGrupo(sg ) ) continue;
			lst.add(sg);
		}
		lstSubGrupo = lst;
	}


	private boolean isProximoSubGrupo(SubGrupo sg) {
		for( SubGrupo ss: lstSubGrupo ) {
//			if( ss.nome.equals("ADMINISTRAÇÃO REGIONAL DE BRAZLÂNDIA")) {
//				System.out.println();
//			}
			if( sg.linha < ss.linha && sg.linha+5 >= ss.linha) return true;
			
			if( ss.linha > sg.linha) break;
		}
		return false;
	}


	private boolean isProximoGrupo(SubGrupo sg) {
		for( Grupo g: lstGrupo) {
			if( sg.linha == g.linha ) return true;
			if( sg.linha < g.linha && sg.linha+3 >= g.linha)  return true;
		}
		return false;
	}


	private int buscaProximaLinha(Grupo g) {
		int linha = g.linha+1;
		if (linha >= linhas.length ) { //acabou a linha, acabou o grupo
			return -1;
		}
		while( g.resumo.indexOf(linhas[linha].replaceAll("\n", "").replaceAll(" ", ""))>=0 ) {
			//System.out.println( linhas[linha] );
			linha++;
			if (linha >= linhas.length ) { //acabou a linha, acabou o grupo
				break;
			}
		}
		return linha;
	}

	private boolean verificaFimSubGrupo(int i) {
		for(int ii=i; ii < i+5 && ii < linhas.length; ii++) {
			if( linhas[ii].toUpperCase().startsWith("PREGOEIR") || linhas[ii].toUpperCase().startsWith("DIRETOR") ) {
				return true;
			}
		
		}
		if( linhas[i].indexOf("Processo:")>= 0 ) return true;
		//if( linhas[i].indexOf("Pregoeir")>=0 ) return true;
		if( linhas[i].indexOf("__")>=0 ) return true;
		return false;
	}

	private boolean isNotValid() {
		if( linha.endsWith("."))return true;
		if( linha.trim().length() < 7 )return true;
		for(String s: palavraRestritiva ) {
			if( linha.indexOf(s)>= 0 ) return true;
		}
		
		for(Grupo g: lstGrupo ) {
			if( g.nome.indexOf(linha)>= 0 ) return true;
		}
		if( linha.split("/").length > 1 ) return true;
		
		if( linha.split("\\.").length > 1 ) return true;
		
		if( linha.split(";").length > 1) return true;
		
		
		return false;
	}


//	private int buscaPaginaSecaoIII() {
//		for(int i=100; i < linhas.length; i++) { // nunca a SEÇÃO III estará antes da linha 100
//			linha = linhas[i];
//			if( linha.equals("SEÇÃO III")) {
//				return i;
//			}
//		}
//		return 0;
//	}
}
