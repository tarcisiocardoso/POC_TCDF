package PocTJDF.PocTJDF;

import org.json.JSONObject;

import PocTJDF.PocTJDF.App.Registro;
import PocTJDF.PocTJDF.rules.Regra;
import PocTJDF.PocTJDF.rules.Util;

public class Inexigibilidade extends Util implements Regra{

	public Regra execute(Registro reg) {
		String dado = reg.conteudo.replaceAll("\n", " ");
		
		JSONObject json = new JSONObject();
		
		String valor = getKeyValue("Interessado", dado);
		if( valor != null ) {
			json.put("interessado", valor);
		}
		
		valor = getKeyValue("Assunto", dado);
		if( valor != null ) {
			json.put("objeto", valor);
		}
		
//		valor = getKeyValue("CNPJ", dado);
//		if( valor != null ) {
//			json.put("cnpj", valor);
//		}
		
		montaCNPJ(json, reg);
		
		montaProcesso(json, reg);
		
		montaValor(json, reg);
		empresaFavorecida(json, reg);
		
		montaResponsavel(json, reg);
		
		montaObjeto(json, reg);
		
		montaEvento(json, reg);
		
		montaValorGeral(json, reg);		
		
		reg.dado = json.toString();
		
		return null;
	}
	

	private void montaValorGeral(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n", "");
		dado = dado.replaceAll("\n", " ");
		
		String arr[] = dado.split(":");
		String key = null, valor = null;
		for(String s: arr) {
			s = s.trim();
			if( key == null ) {
				key = s.substring(s.lastIndexOf(" "), s.length() );
				key = key.toLowerCase().trim();
				if( !isValidKeyValorGeral(key ) ) {
					key = null;
					continue;
				}
			}else {
				if( key.trim().equals("22")) {
					System.out.println(".xxxx...");
				}
				if( s.indexOf(".") > 0 ) {
					valor = s.substring(0, s.indexOf("."));
					
					if( json.has(key)  ) break;
					json.put(key, valor.trim());
					
					if( s.lastIndexOf(" ") <= 0){
						key = null;
						continue;
					}
					key = s.substring(s.lastIndexOf(" "), s.length() );
					key = key.toLowerCase().trim();
					if( !isValidKeyValorGeral(key ) ) {
						key = null;
						continue;
					}
				}else if( s.indexOf(";") > 0 ) {
					valor = s.substring(0, s.indexOf(";"));
					
					if( json.has(key) ) break;
					json.put(key, valor.trim());
					
					key = s.substring(s.lastIndexOf(" "), s.length() );
					key = key.toLowerCase().trim();
					if( !isValidKeyValorGeral(key ) ) {
						key = null;
						continue;
					}
				}else if( s.indexOf(",") > 0 ) {
					valor = s.substring(0, s.indexOf(","));
					if( json.has(key) ) break;
					
					json.put(key, valor.trim());
					key = s.substring(s.lastIndexOf(" "), s.length() );
					key = key.toLowerCase().trim();
					if( !isValidKeyValorGeral(key ) ) {
						key = null;
						continue;
					}
				}
			}
		}
	}

	private boolean isValidKeyValorGeral(String key ) {	
		if( key.trim().isEmpty() ) return true;
		return !isNum(key.trim().charAt(0));
	}
	
	private void montaObjeto(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n", "");
		
		if( dado.toUpperCase().contains("referente".toUpperCase() )) {
			int pos = dado.toUpperCase().indexOf("referente".toUpperCase());
			String s = dado.substring(pos, dado.length() );
			s = s.split(",")[0];
			json.put("objeto", s);
		}else if( dado.toUpperCase().contains(" para ".toUpperCase()) ){
			int pos = dado.toUpperCase().indexOf(" para ".toUpperCase());
			String s = dado.substring(pos, dado.length());
			s = s.split(",")[0];
			json.put("objeto", s);
		}
	}


	private void empresaFavorecida(JSONObject json, Registro reg) {
		String dado = reg.conteudo.replaceAll("-\n", "");
		
		if( dado.toUpperCase().contains("favor da empresa".toUpperCase())) {
			String s = dado.split("favor da empresa")[1];
			int pos = s.indexOf(",");
			if( pos > 0 ) {
				s = s.substring(0, pos);
			}else {
				pos = s.indexOf(";");
				if( pos > 0 ) {
					s = s.substring(0, pos);
				}else {
					pos = s.indexOf(".");
					if( pos > 0 ) {
						s = s.substring(0, pos);
					}
				}
			}
			json.put("favorecido", s.trim());
		}
	}



}
