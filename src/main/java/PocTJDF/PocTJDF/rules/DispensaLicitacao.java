package PocTJDF.PocTJDF.rules;

import org.json.JSONObject;

import PocTJDF.PocTJDF.App.Registro;

public class DispensaLicitacao extends Util implements Regra{

	public Regra execute(Registro reg) {
		System.out.println( reg.conteudo );
		
		JSONObject json = new JSONObject();
		
		montaTipo(json, reg);
		
		montaObjeto(json, reg);
		
//		json.put("CNPJ", getKeyValue("cnpj", reg.conteudo));
		
		montaResponsavel(json, reg);
		
		montaValor( json, reg);
		
		montaContratada(json, reg);
		
		reg.dado = json.toString();
		
		return null;
	}

//	private void montaValor(JSONObject json, Registro reg) {
//		String dado = reg.conteudo.replaceAll("-\n ", "");
//		
//		if( dado.contains("R$") ) {
//			String valor = null;
//			String literal = null;
//			
//			int pos = dado.indexOf("R$");
//			if( pos > 0 ) {
//				String s = dado.substring(pos+2, dado.length()).trim();
//				pos = s.indexOf(' ');
//				valor = "R$ "+s.substring(0, pos).trim();
//				if( valor.endsWith(".")) valor = valor.substring(0, valor.length()-1 );
//				if( s.contains("(") && s.contains(")") ){
//					pos = s.indexOf("(");
//					s = s.substring(pos, s.indexOf(')')+1);
//					literal = s;
//					literal = literal.replaceAll("\n", " ");
//				}
//			}
//			if( valor != null ) json.put("valor", valor);
//			if( literal != null) json.put("valorLiteral", literal );
//		}
//	}

	private void montaContratada(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n", "");
		JSONObject j = new JSONObject();
		
		if( dado.toUpperCase().contains("da empresa".toUpperCase()) ) {
			int pos = dado.toUpperCase().indexOf("da empresa".toUpperCase() );
			String s = dado.substring(pos+ "da empresa".length(), dado.length());
			pos = s.indexOf(",");
			s = s.substring(0, pos);
			
			j.put("nome", s.trim());
		}
		String cnpj = getKeyValue("cnpj", reg.conteudo);
		if( cnpj != null ) {
			j.put("cnpj", cnpj);
		}
		if( !j.isEmpty() ) {
			json.put("contratado", j);
		}
	}

	private void montaObjeto(JSONObject json, Registro reg) {
		String s = reg.conteudo;
		
		s = s.replaceAll("-\n", "");
		String objeto = ">>>não indentificado<<<";
		if( s.toUpperCase().contains("Contratação".toUpperCase() ) ) {
			int pos = s.toUpperCase().indexOf("Contratação".toUpperCase() );
			s = s.substring(pos, s.length());
			s = s.split("\\. ")[0];
			objeto = s;
		}else if ( s.toUpperCase().contains("objeto".toUpperCase())) {
			int pos = s.toUpperCase().indexOf("objeto".toUpperCase())+6;
			s = s.substring(pos, s.length() );
			s = s.split("\\. ")[0];
			if( s.length() > 50 && s.contains(",")) {
				s = s.substring(0,  s.indexOf(","));
			}
			s = s.replaceAll("-\n", "");
			s = s.replaceAll("\n", " ");
			objeto = retiraCharInicial(s.trim());
		}
		
		json.put("objeto", objeto.trim() );
	}

	private void montaTipo(JSONObject json, Registro reg) {		
		if( reg.conteudo.contains("RATIFICO") || reg.conteudo.contains("RATIFICA") ) {
			json.put("tipo", "ratificação");
		}else if ( reg.conteudo.contains("emergencial") ) {
			json.put("tipo", "emergencial");
		}else {
			json.put("tipo", ">>>nao identificado<<<");
		}
	}
	
	
}