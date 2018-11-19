package PocTJDF.PocTJDF.rules;

import org.json.JSONObject;

import PocTJDF.PocTJDF.App.Registro;

public class NotaEmpenho extends Util implements Regra{

	public Regra execute(Registro reg) {
		
		JSONObject json = new JSONObject();
		
		montaProcesso(json, reg);
		
		montaPartes(json, reg);
		
		montaValor(json, reg);

		montaEvento(json, reg);
		
		montaCNPJ(json, reg);
		
		montaObjeto(json, reg);
		
		montaPrazoEntrega(json, reg);
		
		montaChamamento(json, reg);
		
		reg.dado = json.toString();
		
		return null;
	}

	private void montaPrazoEntrega(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n", "");
		dado = dado.replaceAll("\n", " ");
		
		if( dado.toUpperCase().contains("PRAZO DE ENTREGA".toUpperCase()) ) {
			int pos = dado.toUpperCase().indexOf("PRAZO DE ENTREGA".toUpperCase() );
			String s = dado.substring(pos+"PRAZO DE ENTREGA".length(), dado.length() );
			
			if( s.toUpperCase().contains("Data do Empenho".toUpperCase()) ) {
				pos = s.toUpperCase().indexOf("Data do Empenho".toUpperCase() );
				s = s.substring(0, pos);
				s = s.replaceAll(":", "");
			}else {
				s = ">>>>não implementado<<<<";
			}
			json.put("prazo", s.trim());
		}
	}

	private void montaObjeto(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n", "");
		dado = dado.replaceAll("\n", " ");
		
		if( dado.toUpperCase().contains("OBJETO") ) {
			int pos = dado.toUpperCase().indexOf("OBJETO" );
			
			String s = dado.substring(pos+7, dado.length());
			
			if( s.contains(".")) {
				pos = s.indexOf('.');
				s = s.substring(0, pos);
			}
			if( s.length() > 100) {
				if( s.contains(",")) {
					pos = s.indexOf(',');
					s = s.substring(0, pos);
				}
			}
			
			json.put("objeto", s.trim());
		}
	}

	private void montaPartes(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n", "");
		dado = dado.replaceAll("\n", " ");
		
		if( dado.toUpperCase().contains("Partes".toUpperCase()) ) {
			
			int pos = dado.toUpperCase().indexOf("Partes".toUpperCase() );
			
			String s = dado.substring(pos+"partes".length(), dado.length());
			s = s.replaceAll(":", "").trim();

			String primeiraParte = s;
			String segundaParte = s;
			if( primeiraParte.contains(" e a ")) {
				pos = primeiraParte.indexOf(" e a ");
				primeiraParte = primeiraParte.substring(0, pos );
				segundaParte = segundaParte.substring(pos+5, segundaParte.length() );
				
				pos = segundaParte.toUpperCase().indexOf("cnpj".toUpperCase() );
				if( pos >=0 ) {
					segundaParte = segundaParte.substring(0, pos);
				}else {
					segundaParte = ">>>>não implementado<<<";
				}
			}else {
				//TODO implementar
				primeiraParte = ">>>>não implementado<<<";
				segundaParte = ">>>>não implementado<<<";
			}
			json.put("parte1", primeiraParte);
			json.put("parte2", segundaParte);
		}
		
	}
}
