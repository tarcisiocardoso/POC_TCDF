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
				String s = dado.substring(pos+key.length(), pos+key.length()+20 );
				if( key.toUpperCase().equals("CNPJ")) {
					pos = s.lastIndexOf('.');
					s = s.substring(0,  pos);
				}else {
					s = s.replaceAll("\\.", "");
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
	
	public void montaProcesso(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("\n-", "");
		
		if( dado.toUpperCase().contains("processo".toUpperCase())) {
			int pos = dado.toUpperCase().indexOf("processo".toUpperCase());
			String s = dado.substring(pos+8, dado.length() );
			pos =0;

			int init = pos;
			while( pos < s.length() && !isNum(s.charAt(pos)) ) {
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
			}
			json.put("processo", s);
		}
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
						}else if( arr[i-1].toUpperCase().equals(arr[i-1])) {
							nome = arr[i-1];
							break;
						}
					}else if( arr[i-1].toUpperCase().equals(arr[i-1])) {
						nome = arr[i-1];
						break;
					}
				}
			}
			j.put("cargo", "pregoeiro");
			j.put("nome", nome);
			
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

	public void montaValor(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n ", "");
		
		if( dado.contains("R$") ) {
			String valor = null;
			String literal = null;
			
			int pos = dado.indexOf("R$");
			if( pos > 0 ) {
				String s = dado.substring(pos+2, dado.length()).trim();
				pos = s.indexOf(' ');
				valor = s.substring(0, pos).trim();
				if( valor.endsWith(".")) valor = valor.substring(0, valor.length()-1 );
				valor = valor.split("\n")[0];
				
				if( s.contains(")") && ( s.indexOf("(") < s.indexOf(")") ) ){
					pos = s.indexOf("(");
					if (pos > s.length() ) {
						literal = "";
						return;
					}
					s = s.substring(pos, s.indexOf(')'));
					literal = s;
					literal = literal.replaceAll("\n", " ");
				}
			}
			if( valor != null ) json.put("valor", valor);
			if( literal != null) json.put("valorLiteral", literal );
		}
	}
	public void montaCNPJ(JSONObject json, Registro reg) {
		String dado =  reg.conteudo.replaceAll("\n", "");
				
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
