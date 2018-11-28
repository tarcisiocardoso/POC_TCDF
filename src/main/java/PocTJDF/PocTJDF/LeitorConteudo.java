package PocTJDF.PocTJDF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import PocTJDF.PocTJDF.App.Grupo;
import PocTJDF.PocTJDF.App.Registro;
import PocTJDF.PocTJDF.App.SubGrupo;

public class LeitorConteudo {

	public String linha;
	public String[] linhas;
	public List<JSONObject> lstJson = new ArrayList<JSONObject>();
	boolean fimComplexo = false;
	BlocoDeDado blocoDeDado = null;
	String modalidade = null;
	Fim regraFimComplexo = null;
	
	
	String identificaTipo[] = new String[]{
		"EXTRATO", "AVISO", "TERMO ADITIVO", "RATIFICA", "DISPENSA", "RETIFICAÇÃO",
		"RESULTADO", "PREGÃO", "CHAMAMENTO", "CONVOCAÇÃO"
	};
	
	String estruturaDado[] = new String[]{
		"PREGÃO ELETRÔNICO N"
	};
	
	public void ler() {
		if( App.paginaSecaoIII == 0 ) return;
		
		linhas = App.linhas;
		
//		int init = App.paginaSecaoIII+1;
		int index = 0;
		for( SubGrupo sub: App.lstSubGrupo) {
			
			SubGrupo proximoSub = proximoSubGrupo(++index);
			int linhaProximoSub = getLinhaProximoSubGrupo(proximoSub);
			boolean isPrimeiraLinha = true;
			blocoDeDado = null;
			for( int i= sub.linha+1; i< linhaProximoSub && i < linhas.length; i++) {
				linha = linhas[i];
				if( linha.contains("EDITAL Nº 05/2017")) {
					System.out.println("....");
				}
				if( fimDePagina(i) ) {
					continue;
				}
				if( isPrimeiraLinha ) {
					blocoDeDado = new BlocoDeDado();
					isPrimeiraLinha = false;
					if( isTipo() ) {
						continue;
					}
				}else {
					if( isTipo() ) {
						if( linha.contains("PREGÃO ELETRÔNICO")) {
							if( i-1 > 0 ) {
								if( linhas[i-1].contains("RESULTADO") ) {
									modalidade = "RESULTADO PREGÃO";
								}
							}
						}
						continue;
					}
				}
				
				addBlocoDado(blocoDeDado);
				
				if( fimBloco(i)) {
					montaDadoJson( blocoDeDado, sub );
					blocoDeDado = new BlocoDeDado();
				}
				if( isFimGrupo( i ) ) {
					modalidade = null;
					break;
				}
			}
		}
	}
	private boolean isFimGrupo(int i) {
		for(App.Grupo g: App.lstGrupo ) {
			if( g.linha == i ) {
				return true;
			}
		}
		return false;
	}
	private boolean fimDePagina(int i) {
		// TODO como é a quebra de pagina em todos os arquivos
		if( linha.equals("SECRETARIA DE ESTADO DE SAÚDE")) {
			if( linhas[i+1].contains("PÁGINA") ) {
				return true;
			}
		}
		if( linha.contains("ÁGINA") ) {
			int pos = linha.indexOf("ÁGINA");
			if( pos + 9 >= linha.length() )return true;
			
			String s = linha.substring(pos+5, pos+5+3 ).trim();
			try {
				Integer.parseInt(s);
				return true;
			}catch( Exception e) {}
			
			return false;
		}
		if( linhas[i-1].contains("ÁGINA") ) {
			int pos = linhas[i-1].indexOf("ÁGINA");
			if( pos + 9 >= linhas[i-1].length() )return true;
			
			String s = linhas[i-1].substring(pos+5, pos+5+3 ).trim();
			try {
				Integer.parseInt(s);
				return true;
			}catch( Exception e) {}
			
			return false;
		}
		if( linha.endsWith("ICP-Brasil.")) {
			return true;
		}
		return false;
	}
	
	private boolean isTipo() {
		if( linha.toUpperCase().equals( linha )) {
			for(String s: identificaTipo) {
				if( linha.startsWith(s)) {
					modalidade = linha;
					return true;
				}
			}
		}
		return false;
	}
	private int getLinhaProximoSubGrupo(SubGrupo proximoSub) {
		if( proximoSub != null) {
			return proximoSub.linha;
		}else {
			
			if( App.lstGrupo.size() == 0 ) {
				return -1;
			}
				
			return App.lstGrupo.get(App.lstGrupo.size()-1).linha;
			
		}
	}
	private SubGrupo proximoSubGrupo(int i) {
		if( i < App.lstSubGrupo.size() ) {
			return App.lstSubGrupo.get(i);
		}
		return null;
	}

	private void montaDadoJson(BlocoDeDado b, SubGrupo sub) {
		
		//TODO valida registro antes de inserir
		String arr[] = b.bloco.toString().split("\n");
		for( int i=0; i< arr.length; i++) {
			if( arr[i].startsWith("_") && arr[i].endsWith("_") && i+1 < arr.length) {
				if(arr[i+1].startsWith("(*)") ){
					return;//e apenas uma nota de fim de registro.
				}
			}
		}
		
		if( sub.id == 0 ) {
			sub.id = DataBase.getInstancia().insertSubGrupo(sub);
		}
		if( modalidade == null) {
			String linha = linhas[sub.linha-1];
			if( b.bloco.toString().contains("CHAMAMENTO")) {
				modalidade = "CHAMAMENTO";
			}else {
				modalidade = linha;
			}
		}
		if( !registroValido(b, modalidade) ) {
			return;
		}
		
		Registro registro =new Registro();
		registro.idSubGrupo = sub.id;
		registro.tipo = modalidade;
		registro.conteudo = b.bloco.toString();
		registro.dado = "{}";
		
		DataBase.getInstancia().insertObjeto(registro);
		
		
		/*
		JSONObject pai = new JSONObject();
		String bloco = corrigeDado( b.bloco.toString() );
		Object json = montaRecursivo(pai, bloco, "\\. ");
		*/

	}

	private boolean registroValido(BlocoDeDado b, String tipo) {
		String dado = b.bloco.toString();
		String ls[] = dado.split("\n");
		if( ls.length < 3 ) return false;
		if( dado.contains("Este documento pode ser verificado")) {
			if( dado.contains("ICP-Brasil")) {
				return false;
			}else if( ls.length < 8) return false;
		}
		if( dado.contains("TABELAS DE CUSTOS UNITÁRIOS BÁSICOS DE CONSTRUÇÃO")) {
			modalidade = null;
			return false;
		}
		String s = tipo.replaceAll(",", "");
		ls = s.split(" ");
		for(String item: ls) {
			try {
				Integer.parseInt(item.trim()); // naõ pode existir numero no tipo
				return false;
			}catch(Exception e) {}
		}
		return true;
	}
	
	private Object montaRecursivo(JSONObject pai, String dado, String delimitador) {
		String macro[] = dado.trim().split(delimitador);
		if( macro.length == 0 ) return null;
		
		for(String m: macro) {
			if( m.contains(": ")) {
				int pos = m.indexOf(":");
				String key = m.substring(0, pos);
				String conteudo = m.substring(pos+1, m.length() );
				if( conteudo.contains("; ")) {
					pai.append(key, montaRecursivo(new JSONObject(), conteudo, "; ") );
				}else {
					pai.put(key, conteudo.trim());
				}
			}else {
				pai.append("info", m.trim());
			}
		}
		return pai;
	}
	private Object montaRecursivo1(JSONObject pai, String dado, String delimitador) {
		
		String macro[] = dado.trim().split(delimitador);
		if( macro.length == 0 ) return null;
		
		for(String m: macro) {
			
			/*if( m.contains("; ")) {
//				if( pai.has("nv1")) {
					pai.append("nv1", montaRecursivo(new JSONObject(), m, "; "));
//				}else {
//					pai.put("nv1", montaRecursivo(new JSONObject(), m, "; "));
//				}
			}else if( m.contains(", ")) {
				//if( pai.has("nv2")) {
					pai.append("nv2", montaRecursivo(new JSONObject(), m, ", "));
				//}else {
				//	pai.put("nv2", montaRecursivo(new JSONObject(), m, ", "));
				//}
			}else */if( m.contains(": ")) {
				int pos = m.indexOf(":");
				String key = m.substring(0, pos);
				String conteudo = m.substring(pos+1, m.length() );
				pai.append(key, conteudo.trim());
			}
			
			/*int pos = m.indexOf(":");
			if( pos > 0) {
				String key = m.substring(0, pos);
				String conteudo = m.substring(pos+1, m.length() );
				if( conteudo.indexOf("; ")> 0 ) {
					pai.put(key, montaRecursivo(new JSONObject(), conteudo, "; "));
				}else if( conteudo.indexOf(", ")> 0) {
					pai.put(key, montaRecursivo(new JSONObject(), conteudo, ", "));
				}else if( conteudo.indexOf(": ")> 0) {
					pai.put(key, montaRecursivo(new JSONObject(), conteudo, ": "));
				}else {
					pai.put(key, conteudo.trim());
				}
			}else {
				if( m.indexOf("; ")> 0 ) {
					JSONArray arr = new JSONArray(Arrays.asList( m.split(";")));
					if( pai.has("lista") ) {
						pai.append("lista", arr);
					}else {
						pai.put("lista", arr);
					}
				}else if( m.indexOf(", ")> 0) {
					JSONArray arr = new JSONArray(Arrays.asList( m.split(", ")));
					if(pai.has("lista") ) {
						pai.append("lista", arr);
					}else {
						pai.put("lista", arr);
					}
				}else {
					if( m.startsWith("CNPJ")) {
						pai.put("CNPJ", m.substring(4, m.length()).trim());
					}else {
						pai.append("arr", m.trim());
					}
				}
			}*/
			
		}
		return pai;
	}

	/**
	 * Modalidade: Ordinário,VALOR: R$ 16.305,00 (dezesseis mil trezentos e cinco reais).
	 * @param dado
	 * @return
	 */
	private String corrigeDado(String dado) {
		if( dado.contains("R$") ) {
			
			String arr[] = dado.split("R\\$");
			StringBuilder sb = new StringBuilder();
			for(int i=0; i< arr.length; i++) {
				String s = arr[i];
				if( i < arr.length-1 ) {
					if( s.length() > 8) {
						String ver = s.substring(s.length()-6, s.length() );
						if( !ver.contains(":")) {
							s = s.trim()+": ";
						}
					}else if( s.trim().length() == 0 ) continue;
					sb.append(s).append(" R$ ");
				}else {
					sb.append(s);
				}
			}
			dado = sb.toString();


/*			int pos = dado.lastIndexOf("R$");

			if( dado.charAt(pos+2) != ' ') {
				String ini = dado.substring(0, pos);
				String fim = dado.substring(pos+2, dado.length() );
				dado = ini+" "+fim;
			}
			if( pos - 6 > 0) { //se não tem : acrescenta
				String doisPontos = dado.substring(pos-6, pos);
				if( !doisPontos.contains(":") ) {
					int pos2 = doisPontos.indexOf(" ");
					if( pos2 > 0 ) {
						String ini = dado.substring(0, pos);
						String fim = dado.substring(pos+2, dado.length() );
						dado = ini+" "+fim;
					}
				}
			}
			
			if( pos + 50 < dado.length() ) {
				String parte = dado.substring(pos, pos+50);
				int pos2 = parte.indexOf("(");
				if( pos2 > 0) {
					String init = dado.substring(0, pos + pos2);
					String fim = dado.substring(pos+pos2, dado.length() );
					dado = init+", VALOR_LITERAL: "+fim;
				}
						
			}
			*/
		}
		
//		if( dado.toUpperCase().indexOf("VALOR:")>0) {
//			int pos = dado.toUpperCase().indexOf("VALOR:");
//			if( dado.charAt(pos-1) != ' ') {
//				String ini = dado.substring(0, pos);
//				String fim = dado.substring(pos, dado.length() );
//				dado = ini+" "+fim;
//			}
//		}
		
		for(String s: estruturaDado ) {
			if( dado.contains(s)) {
				int pos = dado.indexOf(s);
				String ini = dado.substring(0, pos+s.length());
				String fim = dado.substring(pos+s.length(), dado.length() );
				
				pos = fim.indexOf(" ");
				if( pos > 0 ) {
					String meio = fim.substring(0, pos);
					dado = ini+meio+": "+fim.substring(pos, fim.length());
				}else {
					dado = ini+": "+fim.substring(pos, fim.length());
				}
			}
		}
		return dado;
	}

	private boolean fimBloco(int i) {
		if( linha.endsWith(".")) {
			if( identificaFimComplexo(i) ) {
				if( regraFimComplexo != null ) {
					if( regraFimComplexo.isFim() ) {
						regraFimComplexo = null;
						fimComplexo = false;
						return true;
					}
				}else {
					return false;
				}
			}else {
				if( i+1 < linhas.length && !linhas[i+1].equals( linhas[i+1].toUpperCase() )) {
					regraFimComplexo = null;
					fimComplexo = false;
					return false;
				}else {
					regraFimComplexo = null;
					fimComplexo = false;
					
					return (i+1 < linhas.length && !linhas[i+1].contains(":") );
				}
			}
		}else {
			if( regraFimComplexo != null ) {
				if( regraFimComplexo.isFim() ) {
					regraFimComplexo = null;
					fimComplexo = false;
					return true;
				}
			}
		}
		return false;
	}

	private boolean identificaFimComplexo(int i) {
		if( fimComplexo ) {
			return regraFimComplexo.isFim();
		}else { // se é necessario uma analise de fim complexo
			String bloco = blocoDeDado.bloco.toString();
			if( i+1 >= linhas.length ) {
				fimComplexo = true;
				return regraFimComplexo != null;
			}
			if( linhas[i+1].contains("_") || linhas[i+1].contains("*")) {
				fimComplexo = true;
				regraFimComplexo = new FimCaracterMaluco();
				return true;
			}
			if( bloco.length() > 50 ) {
				if( bloco.substring(0, 50).contains(" Pregoeir")) {
					fimComplexo = true;
					regraFimComplexo = new FimPregao();
					return true;
				}else if( linha.toUpperCase().contains("PREGOEIR")) {
					fimComplexo = true;
					regraFimComplexo = new FimPregao();
					return true;
				}
			}
			if( i+2 < linhas.length ) {
				if( linhas[i+1].toUpperCase().equals(linhas[i+1])) {//TODA LINHA MAIUSCULA
					String l = linhas[i+2];
					if( l.toUpperCase().startsWith("PREGOEIR") || l.toUpperCase().startsWith("DIRETOR") ) {
						fimComplexo = true;
						regraFimComplexo = new FimterminaComPregoiro();
						return true;
					}
				}
			}
			if( linha.length() > 80 ) {
				if( i+1 < linhas.length ) {
					if( !linhas[i+1].toUpperCase().equals(linhas[i+1]) ) {
						return true;
					}
				}
					
			}
		}
		return false;
	}
	private void addBlocoDado(BlocoDeDado blocoDeDado) {
//		if( linha.trim().endsWith("-")) {
//			linha = linha.substring(0, linha.trim().length()-1);
//		}
//		if( linha.toUpperCase().equals(linha)) {
//			if( linha.contains("PREGÃO")) { // tem que indicar fim de dado
//				linha = linha.trim()+". ";
//			}
//		}
		blocoDeDado.bloco.append(linha.trim()).append("\n");
	}
/*
	private boolean verificaInicioBloco(int i, SubGrupo sg) {
		if( sg == null ) return false;
		if( i == sg.linha ) return false;
		
		for(String s: identificaTipo) {
			if( linha.startsWith(s)) {
				modalidade = linha;
				devePularLinha = true;
				return true;
			}
		}
		if( linhas[i-1].trim().endsWith(".")) {
			devePularLinha = false;
			return true;
		}
		return false;
	}
*/


	private SubGrupo subGrupoAtual(List<SubGrupo> lst, int i) {
		SubGrupo sb = null;
		for(SubGrupo s: lst) {
			if( s.linha == i || s.linha+5 <=i) {
				sb = s;
				break;
			}
		}
		return sb;
	}

	private List<SubGrupo> buscaSubGrupoDoGrupo(Grupo g) {
		List<SubGrupo> lst = new ArrayList<App.SubGrupo>();
		for( SubGrupo sg: App.lstSubGrupo) {
			if( sg.idGrupo == g.id) {
				lst.add(sg);
			}
		}
		return lst;
	}

	public class BlocoDeDado{
		SubGrupo subGrupo;
		StringBuilder bloco = new StringBuilder();
		String tipo;
	}
	
	public class FimCaracterMaluco implements Fim{
		public boolean isFim() {
			
			if( linha.contains("___") || linha.contains("(*)")) {
				return false;
			}
			return false;
		}
	}
	public class FimPregao implements Fim{
		
		public boolean isFim() {
			int pos = linha.lastIndexOf(' ');
			if( pos >= 0) {
				String last = linha.substring(pos, linha.length() );
				try {
					Integer.parseInt(last.trim());// indica que a linha tem ano: 2018
				}catch( Exception e) {
					if( linha.toUpperCase().equals(linha)) {
						return true;
					}
				}
			}
			String s = linha.replaceAll("\\.", "");
			if( s.toUpperCase().endsWith("PREGOEIRO") || s.toUpperCase().endsWith("PREGOEIRA")) {
				pos = s.toUpperCase().indexOf("PREGOEIR");
				s = s.substring(0, pos).trim();
				String arr[] = s.split(" ");
				s = arr[ arr.length-1];
				if( s.toUpperCase().equals(s)) return true;
			}
			return false;
		}
	}
	public class FimterminaComPregoiro implements Fim{

		public boolean isFim() {
			if( linha.toUpperCase().startsWith("PREGOEIR") || linha.toUpperCase().startsWith("DIRETOR")) {
				return true;
			}
			return false;
		}
		
	}
}
