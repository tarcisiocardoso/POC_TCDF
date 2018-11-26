package PocTJDF.PocTJDF.rules;

import org.json.JSONObject;

import PocTJDF.PocTJDF.App.Registro;

public class ResultadoPregao extends Util implements Regra{

	
	/*
Tornamos Público o Resultado do Julgamento do Pregão supracitado, processo nº
113.012190/2016. Empresa vencedora: VITANET COMERCIAL LTDA, no valor total para
o Lote 01 de R$ 231.397,98.
	 * @see PocTJDF.PocTJDF.rules.Regra#execute(PocTJDF.PocTJDF.App.Registro)
	 */
	public Regra execute(Registro reg) {
		JSONObject json = new JSONObject();
		String dado = reg.conteudo;
		
		montaTipo(json, reg);
		montaProcesso(json, reg);
		montaResponsavel(json, reg);
		
		montaVencedoras(json, dado);
		reg.dado = json.toString();
		
		return null;
	}

	private void montaVencedoras(JSONObject json, String dado) {
		String arr[] = dado.toUpperCase().split("EMPRESA");
		if( arr.length < 2 ) {
			arr = dado.toUpperCase().split("VENCEDOR");
		}
		if( arr.length < 2) {
			throw new RuntimeException("Falha ao buscar a vencedora.");
		}else {
			int index =0;
			for(String s: arr) {
				JSONObject j = new JSONObject();
				if( index == 0 ) {
					montaObjeto(json, s);
				}else {
					montaValor(j, s);
					montaEmpresa(j, s);
					montaCNPJ(j, s);
				}
				index++;
				if( !j.isEmpty() ) {
					json.append("vencedor", j);
				}
			}
		}
	}

	private void montaEmpresa(JSONObject j, String dado) {
		String valor = null;
		if( dado.contains(":")) {
			String s = dado.split(":")[1];
			if( s.contains(",")) {
				s = s.split(",")[0];
			}
			valor = s.trim();
		}
		if( valor != null ) {
			j.put("nome", valor);
		}
	}

	private void montaObjeto(JSONObject json, String dado) {
		String valor = null;
		if( dado.contains("RESULTADO")) {
			if ( dado.indexOf(".") > 0 ) {
				valor = dado.split("\\.")[0];
				if( valor.contains("PROCESSO")) {
					int pos = valor.indexOf("PROCESSO");
					valor = valor.substring(0,  pos);
				}
			}
		}
		if( valor != null ) {
			valor = valor.trim();
			if( valor.endsWith(",")) {
				valor = valor.substring(0, valor.length()-1 );
			}
			json.put("objeto", valor.toLowerCase());
		}
	}
}
