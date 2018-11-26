package PocTJDF.PocTJDF.rules;

import org.json.JSONObject;

import PocTJDF.PocTJDF.App.Registro;

public class Util {

	public static String arrEvento [] = {
			"abertura", "adiamento", "adjudicação", "anulação", "encerramento", "habilitação", "homologação",
			"julgamento", "prosseguimento", "reabertura", "revogação", "suspensão", "correção", "retificação"
	};
	
	public String getKeyValue(String key, String dado ) {
		if( dado.toUpperCase().contains(key.toUpperCase())) {
			int pos = dado.toUpperCase().indexOf(key.toUpperCase());
			if( pos > 0 && pos+20 < dado.length() ) {
				String s = dado.substring(pos+key.length(), dado.length() );
				if( key.toUpperCase().equals("CNPJ")) {
					pos = s.lastIndexOf('.');
					s = s.substring(0,  pos);
				}else {
					s = s.split("\\.")[0];
				}
				s = s.replaceAll(":", "");
				s = s.split("\n")[0];
				s = s.trim();
				pos = s.indexOf(' ');
				if( pos > 0 ) {
					return s.substring(0, pos);
				}
				return s;
			}
		}
		return null;
	}
	
	public String retiraCharInicial(String dado) {
		
		String arr[] = dado.trim().split(" ");
		
		if( arr[0].length() == 1) {
			dado = retiraCharInicial(dado.substring(1, dado.length() ));
		}
		
		return dado;
	}
	@Deprecated
	public void montaProcesso(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("\n-", "");
		montaProcesso(json, dado);
	}
	public void montaProcesso(JSONObject json, String dado) {
		dado = dado.toUpperCase();
		
		String arr[] = dado.split("PROCESSO");
		
		int index = 0;
		String valor = null;
		for(String s: arr) {
			if( index++ == 0 ) {
				continue;
			}
			int pos =0;

			while( pos < s.length() && !isNum(s.charAt(pos)) ) {
				pos++;
			}
			if( pos >= 0 && pos < s.length() ) {
				s = s.substring(pos, s.length() );
				if( s.indexOf(' ') > 0 ) {
					s = s.substring(0, s.indexOf(' '));
				}else if( s.indexOf('\n')> 0) {
					s = s.substring(0, s.indexOf('\n'));
				}
			}
			s = s.replaceAll(",", "");
			if( isStringNumero(s) ) {
				valor = s;
			}
			if( valor != null ) {
				if( valor.endsWith(".")) valor = valor.substring(0, valor.length()-1);
				
				json.put("processo", valor);
				break;
			}
		}
		/*
		if( dado.toUpperCase().contains("processo".toUpperCase())) {
			int pos = dado.toUpperCase().indexOf("processo".toUpperCase());
			String s = dado.substring(pos+8, dado.length() );
			pos =0;

			int init = pos;
			while( pos < s.length() && !isNum(s.charAt(pos)) ) {
				pos++;
			}
			if( pos >= 0 && pos < s.length() ) {
				s = s.substring(pos, s.length() );
				if( s.indexOf(' ') > 0 ) {
					s = s.substring(0, s.indexOf(' '));
				}else if( s.indexOf('\n')> 0) {
					s = s.substring(0, s.indexOf('\n'));
				}
			}
			s = s.replaceAll(",", "");
			
			json.put("processo", s);
		}
		*/
	}
	

	public void montaResponsavel(JSONObject json, Registro reg) {
		String dado = reg.conteudo;
		JSONObject j = new JSONObject();
		if( dado.toUpperCase().contains("PREGOEIR")) {
			String nome = null;
			String arr[] = dado.split("\n");
			for( int i=0; i< arr.length; i++) {
				String s = arr[i];
				if( s.toUpperCase().contains("PREGOEIR") ) {
					if( i+1 < arr.length ) {
						if( arr[i+1].toUpperCase().equals(arr[i+1])) {
							nome = arr[i+1];
							break;
						}else if( i> 1 && ( arr[i-1].toUpperCase().equals(arr[i-1])) ) {
							nome = arr[i-1];
							break;
						}
					}else if( i > 0 && arr[i-1].toUpperCase().equals(arr[i-1])) {
						nome = arr[i-1];
						break;
					}else {
						String ss[] = s.split("\\.");
						for(String nm: ss) {
							if( !nm.toUpperCase().contains("PREGOI") ){
								if( nm.toUpperCase().equals(nm)) {
									nome = nm.trim();
								}
							}
						}
					}
				}
			}
			if( nome != null ) {
				j.put("cargo", "pregoeiro");
				j.put("nome", nome);
			}
		}else if( dado.toUpperCase().contains("Superintendente".toUpperCase())) {
			String nome = null;
			int pos = dado.toUpperCase().indexOf( "Superintendente".toUpperCase() );
			
			int index = pos;
			String s = "";
			//buscar de traz para frente
			while( index> 0 && index < dado.length() && (dado.charAt(--index) == dado.toUpperCase().charAt(index)) ) {
				char c = dado.charAt(index);
				
				if( c == '.' || (!s.isEmpty() && c == '\n')) break;
				s= dado.charAt(index)+s;
			}
			nome = s.replaceAll(",", "");

			if( !nome.toUpperCase().equals(nome)) {
				while( index> 0 && index < dado.length() && (dado.charAt(index++) == dado.toUpperCase().charAt(index)) ) {
					char c = dado.charAt(index);
					
					if( c == '.' || (!s.isEmpty() && c == '\n')) break;
					s= dado.charAt(index)+s;
				}
				nome = s.replaceAll(",", "");				
			}
			
			j.put("cargo", "Superintendente");
			j.put("nome", nome.trim() );
		}else if( dado.toUpperCase().contains("Secretári".toUpperCase())) {
			String nome = null;
			
			int pos = dado.toUpperCase().indexOf( "Secretári".toUpperCase() );
			
			String cargo = dado.substring(pos, dado.length() ).replaceAll("\n", " ");
			cargo = cargo.replaceAll("\\.", "");
			int index = pos;
			String s = "";
			//buscar de traz para frente
			while( index> 0 && index < dado.length() && (dado.charAt(--index) == dado.toUpperCase().charAt(index)) ) {
				char c = dado.charAt(index);
				
				if( c == '.' || (!s.isEmpty() && c == '\n')) break;
				s= dado.charAt(index)+s;
			}
			nome = s.replaceAll(",", "");

			if( !nome.toUpperCase().equals(nome)) {
				while( index> 0 && index < dado.length() && (dado.charAt(index++) == dado.toUpperCase().charAt(index)) ) {
					char c = dado.charAt(index);
					
					if( c == '.' || (!s.isEmpty() && c == '\n')) break;
					s= dado.charAt(index)+s;
				}
				nome = s.replaceAll(",", "");				
			}
			j.put("cargo", cargo);
			j.put("nome", nome.trim() );
		}else if ( dado.toUpperCase().contains("Executor".toUpperCase()) ) {
			int pos = dado.toUpperCase().indexOf("Executor".toUpperCase() );
			String s = dado.substring(pos+9, dado.length() );
			s = s.split("\\.")[0];		
			j.put("cargo", "executor");
			j.put("nome", s.trim() );
		}
		
		if( !j.isEmpty()) {
			json.put("responsavel", j);
		}
	}
	private boolean isStringNumero(String valor) {
		String s = valor.replaceAll("\\.", "");
		s = s.replaceAll("-", "");
		s = s.replaceAll("/", "");
		try {
			Long.parseLong(s.trim());
			return true;
		}catch(Exception e) {}
		return false;
	}
	public boolean isNum(char c) {
		if( "0123456789".indexOf(c)>= 0 ) {
			return true;
		}
		return false;
	}
	public void montaEvento(JSONObject json, Registro reg) {
		String dado = reg.conteudo;
		String evento = null;//reg.tipo;
		for( String s: arrEvento) {
			if( dado.toLowerCase().contains(s)) {
				evento = s;
				break;
			}
		}
		if( evento == null ) {
//			evento = reg.tipo.split(" ")[0];
			for( String s: arrEvento) {
				if( reg.tipo.toLowerCase().contains(s)) {
					evento = s;
					break;
				}
			}
		}
		json.put("evento", evento);	
	}

	@Deprecated
	public void montaValor(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n ", "");
		montaValor(json, dado);
	}
	public void montaValor(JSONObject json, String dado) {
		String valor = null;
		
		if( dado.contains("R$") ) {
			int pos = dado.indexOf("R$");
			if( pos > 0 ) {
				String s = dado.substring(pos+2, dado.length()).trim();
				pos = s.indexOf(' ');
				if( pos < 0 ) {
					//TODO implementar quando não existe espaço em branco.
					pos = s.length();
				}
				valor = s.substring(0, pos).trim();
				if( valor.endsWith(".")) valor = valor.substring(0, valor.length()-1 );
				valor = valor.split("\n")[0];
				
			}
		}else if( dado.toUpperCase().contains("VALOR") ){
			int pos = dado.toUpperCase().indexOf("VALOR")+5;
			String s = dado.substring(pos, dado.length() );
			
			pos =0;
			while(pos < s.length() && !isNum(s.charAt(pos)) ) {
				pos++;
			}
			if( pos >= 0 && pos < s.length() ) {
				s = s.substring(pos, s.length() );
				
				if( s.indexOf(' ') > 0 ) {
					s = s.substring(0, s.indexOf(' '));
				}
				if( s.indexOf('\n')> 0) {
					s = s.substring(0, s.indexOf('\n'));
				}
				if( s.startsWith(";")) s = s.substring(1, s.length());
				
				s = s.trim();
				valor =s ;
			}
		}
		if( valor != null ) valor = valor.replaceAll(";", "");
		if( valor != null ) json.put("valor", valor);
	}
	@Deprecated
	public void montaCNPJ(JSONObject json, Registro reg) {
		String dado =  reg.conteudo.replaceAll("\n", "");
		montaCNPJ(json, dado);
	}
	public void montaCNPJ(JSONObject json, String dado) {
		if( dado.toUpperCase().contains("cnpj".toUpperCase() )) {
			int pos = dado.toUpperCase().indexOf("CNPJ");
			String s = dado.substring(pos+4, dado.length() );
			pos =0;

			int init = pos;
			while( !isNum(s.charAt(pos)) ) {
				pos++;
				if( pos - init > 20) {
					pos = -1;
					break;
				}
			}
			if( pos >= 0 && pos < s.length() ) {
				s = s.substring(pos, s.length() );
				if( s.indexOf(',') > 0 ) {
					s = s.substring(0, s.indexOf(','));
				}
				if( s.indexOf(' ') > 0 ) {
					s = s.substring(0, s.indexOf(' '));
				}
				if( s.indexOf('\n')> 0) {
					s = s.substring(0, s.indexOf('\n'));
				}
				if( s.startsWith(":")) s = s.substring(1, s.length());
				if( s.endsWith(".") )  s = s.substring(0, s.length()-1);
			}
			json.put("cnpj", s);
		}
	}
	
	public void montaTipo(JSONObject json, Registro reg) {
		String dado = reg.conteudo;
		if( dado.toUpperCase().contains("ERRATA")) {
			json.put("tipo","ERRATA");
		}else if( dado.toUpperCase().contains("TIPO")) {
			int pos = dado.toUpperCase().indexOf("TIPO");
			if( pos >= 0 ) {
				String s = dado.substring(pos, dado.length());
				s = s.split("\\.")[0];
				s = s.replaceAll("\n", " ");
				
				s = s.toUpperCase();
				
				s = s.replaceAll("TIPO", "");
				s = s.replaceAll(":", "").trim();
				
				if( s.contains("MENOR PRE") ) {
					json.put("tipo", "Menor Preço");
				}else if( s.contains("TÉCNICA E PREÇO") ) {
					json.put("tipo","Técnica e preço");
				}else if( s.contains("MELHOR PRE") ) {
					json.put("tipo","Melhor Preço");
				}else if( s.contains("MAIOR DESCONTO") ) {
					json.put("tipo","Maior Desconto");
				}else if( s.contains("MAIOR LANCE") ) {
					json.put("tipo","Maior lance ou oferta");
				}
			}
		}
	}
	
	public void montaChamamento(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("\n-", "");
		dado = dado.replaceAll("\n", " ");
		if( dado.toUpperCase().contains("Chamamento".toUpperCase()) ) {
			int pos = dado.toUpperCase().indexOf("Chamamento".toUpperCase() );
			String s = dado.substring(pos+"Chamamento".length(), dado.length());
			
			pos =0;

			int init = pos;
			while( !isNum(s.charAt(pos)) ) {
				pos++;
				if( pos - init > 20) {
					pos = -1;
					break;
				}
			}
			if( pos >= 0 && pos < s.length() ) {
				s = s.substring(pos, s.length() );
				if( s.indexOf(' ') > 0 ) {
					s = s.substring(0, s.indexOf(' '));
				}else if( s.indexOf('\n')> 0) {
					s = s.substring(0, s.indexOf('\n'));
				}
				s = s.replaceAll(",", "");
			}
			json.put("chamamento", s);
		}
	}

}
