package PocTJDF.PocTJDF.rules;

import PocTJDF.PocTJDF.App.Registro;
import org.json.JSONObject;

import com.itextpdf.text.pdf.PdfSpotColor;

public class Abertura extends Util implements Regra{

	String keyPregao[] = {
			"PREGÃO ELETRÔNICO", "POR SRP N", " SRP N"
	};
	public Regra execute(Registro reg) {
		String dado = reg.conteudo;

		JSONObject json = new JSONObject();
		
		String value = getNumeroPregao(dado);
		if( value != null ) {
			json.put("numero", value);
		}
		value = getNumeroDODF(dado);
		if( value != null ) {
			json.put("DODF", value);
		}

		value = getKeyValue("UASG", dado);
		if( value != null ) {
			json.put("UASG", value);
		}

		value = getKeyValue("processo", dado);
		if( value != null ) {
			json.put("processo", value);
		}

		value = getObjeto(dado);
		if( value != null ) {
			json.put("objeto", value);
		}

		montaProcesso(json, reg);
		
		montaValor(json, reg);
		

		montaModalidade(json, dado);
		
		montaData(json, dado);
		
		montaEvento(json, reg);
		
		montaResponsavel(json, reg);
		
		
		value = getTipo( dado);
		if( value != null ) {
			json.put("tipo", value);
		}
		reg.dado = json.toString();
		
		return null;//sem outra regra para execução
	}
	
	private void montaModalidade(JSONObject json, String dado) {
		if( dado.toUpperCase().contains("PREGÃO")) {
			json.put("modalidade", "pregão");
		}else {
			json.put("modalidade", ">>não implementado<<");
		}
	}

	private void montaData(JSONObject json, String dado) {
		if( dado.toUpperCase().contains("DATA")) {
			
			String []arr = dado.toLowerCase().split("data");
			for( String s: arr) {
				int pos = -1;
				if( s.contains("proposta")) {
					pos = s.indexOf("proposta");
					while( (s.charAt(pos) != ' ' && s.charAt(pos) != '\n') ){
						if(pos++ > s.length()) {
							pos = -1;
							break;
						}
					}

//					if( pos > 0 ) {
//						String tipo = s.substring(pos, s.length() );
//						tipo = tipo.replaceAll(":", "").trim();
//						evento.put("tipo", tipo);
//					}

				}else if( s.contains("abertura")) {
					//TODO procurar definição de prazo complexo.
//					evento.put("tipo", ">>>a ser implementado<<<");
				}
				if( pos >= 0 ) {
					//busca primeiro numero numerico
					while( !isNum(s.charAt(pos)) ) {
						if(pos++ >= s.length()-1) {
							pos = -1;
							break;
						}
						
					}
					if( pos > 0 ) {
						String data = s.substring(pos, s.length() );
						
						data = data.split("\n")[0];
						if( data.indexOf(" ") > 0 ) {
							data = data.substring(0, data.indexOf(" "));
						}
						
						data = data.replaceAll(",", "").trim();
						json.put("data", data);
					}
				}
			}
		}
	}

	private String getTipo(String dado) {
		if( dado.toUpperCase().contains("ERRATA")) {
			return "ERRATA";
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
					return "Menor Preço";
				}else if( s.contains("TÉCNICA E PREÇO") ) {
					return "Técnica e preço";
				}else if( s.contains("MELHOR PRE") ) {
					return "Melhor Preço";
				}else if( s.contains("MAIOR DESCONTO") ) {
					return "Maior Desconto";
				}else if( s.contains("MAIOR LANCE") ) {
					return "Maior lance ou oferta";
				}
			}
		}
		return null;
	}

	private String getObjeto(String dado) {
		if( dado.toUpperCase().contains("OBJETO")) {
			int pos = dado.toUpperCase().indexOf("OBJETO")+6;
			if( pos > 6 && pos < dado.length() ) {
				String s = dado.substring(pos, dado.length());
				s = s.replaceAll(":", "").trim();
				s = s.split("\\.")[0];
				if( s.length() > 50 && s.contains(",getObjeto")) {
					s = s.substring(0,  s.indexOf(","));
				}
				s = s.replaceAll("-\n", "");
				s = s.replaceAll("\n", " ");
				
				if( s.toLowerCase().contains("pregão")){
					if(dado.toUpperCase().contains("RETIFICAÇÃO")) {
						pos = dado.toUpperCase().indexOf("RETIFICAÇÃO");
						s = dado.substring(pos, dado.length());
						pos = 11;
						//procurando :
						while( s.charAt(pos) != ':' ) {
							pos++;
							if( pos >= s.length()) {
								pos = -1;
								break;
							}
						}
						if( pos >= 0 ) {
							s = s.substring(pos+1, s.length());
							s = s.split("\\.")[0]; //pega ate o primeiro ponto
							if( s.length() > 50 ) {
								s = s.substring(0,  s.indexOf(","));
							}
							s = s.replaceAll("-\n", "");
							s = s.replaceAll("\n", " ");
						}else {
							return "retificação";
						}
					}
				}
				return s.trim();
			}
		}
		return null;
	}
//DODF nº 180, de 20 de setembro de 2018
	private String getNumeroDODF(String dado) {
		if( dado.contains("DODF")) {
			int pos = dado.indexOf("DODF");
			int init = pos;
			while( pos < dado.length() && !isNum(dado.charAt(pos)) ) {
				pos++;
				if( pos - init > 20) {
					pos = -1;
					break;
				}
			}
			if( pos >= 0 && pos < dado.length()) {
				String s = dado.substring(pos, pos+10);
				if( s.indexOf(' ') > 0 ) {
					s = s.substring(0, s.indexOf(' '));
				}else if( s.indexOf('\n')> 0) {
					s = s.substring(0, s.indexOf('\n'));
				}
				s = s.replaceAll(",", "");
				return s;
			}
		}
		return null;
	}

	private String getNumeroPregao(String dado) {
		for( String key: keyPregao) {
			if( dado.toUpperCase().contains(key)) {
				int pos = dado.toUpperCase().indexOf(key)+key.length();
				int init = pos;
				while( !isNum(dado.charAt(pos)) ) {
					
					pos++;
					if( pos - init > 20) {
						pos = -1;
						break;
					}
				}
				if( pos >= 0 ) {
					String s = dado.substring(pos, pos+10);
					if( s.indexOf(' ') > 0 ) {
						s = s.substring(0, s.indexOf(' '));
					}else if( s.indexOf('\n')> 0) {
						s = s.substring(0, s.indexOf('\n'));
					}
					return s;
				}
				
			}
		}
		return null;
	}

}
