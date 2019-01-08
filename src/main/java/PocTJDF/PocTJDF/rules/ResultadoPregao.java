package PocTJDF.PocTJDF.rules;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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
		if( reg.conteudo.contains("PROTECTOR INDÚSTRIA E COMÉRCIO DE PRODUTOS MÉDICO")) {
			System.out.println("xoxoxoxoxoox");
		}
		JSONObject json = new JSONObject();
		String dado = reg.conteudo;
		
		montaProcesso(json, reg);
		montaResponsavel(json, reg);
		
		montaVencedoras(json, dado);
		
		montaValor(json, dado);

		reg.dado = json.toString();

		return null;
	}
	/*
PREGÃO ELETRÔNICO POR SRP Nº 325/2017
Pregoeira Substituta
A Pregoeira da Central de Compras/SUAG da Secretaria de Estado de Saúde do Distrito
Federal comunica que, no Pregão Eletrônico por SRP nº 325/2017, sagraram-se vencedoras
(empresas, itens, valores): 
BRAKKO COMÉRCIO E IMPORTAÇÃO LTDA, CNPJ:01.085.207/0001-79, itens: 01 (R$ 780,00), 03 (R$ 970,00) e 05 (R$ 1.065,00); POINTER
SERVIÇOS HOSPITALARES LTDA EPP- ME, CNPJ: 03.098.826/0001-23, itens: 02 (R$
780,00), 04 (R$ 970,00) e 06 (R$ 1.065,00); perfazendo o valor total licitado de R$
1.632,500,00. Os itens 07 e 08 fracassaram.
PRISCILLA MOREIRA FALCÃO FIGUEIREDO	 
	 */
	private void montaVencedoras(JSONObject json, String dado) {
		int pos = dado.toUpperCase().indexOf("EMPRESA");
		if( pos < 0) pos = dado.toUpperCase().indexOf("VENCEDOR");
		
		if( pos < 0 ) throw new RuntimeException("problema de tipo. Não parece ser resultado de pregão.");
		
		String s = dado.substring(pos, dado.length());
		s = s.replaceAll("\n", " ");
		boolean start = false;
		boolean isBuscaCnpj = false;
		boolean isItens = false;
		boolean isValor = false;
		String nome = "";		
		String valor = "";
		String arr[] = s.split(" ");
//		int qtdCnpj = dado.toUpperCase().split("CNPJ").length;
		
		JSONObject jEmpresa = new JSONObject();
		JSONObject jItens = new JSONObject();
		List<JSONObject> lstItens = new ArrayList<JSONObject>();
		
		for(String palavra: arr) {
			if( palavra.isEmpty() )continue;
			if( palavra.contains(":")) {
				if( !start) {
					start = true;
					continue;
				}
			}else if( palavra.toUpperCase().contains("ITENS")  ) {
				if( !start) {
					start = true;
				}
			}
			if( start ) {
				if (palavra.toUpperCase().contains("CNPJ")) {
					isBuscaCnpj = true;
					isItens = false;
					isValor = false;
					continue;
				}				
				if( palavra.toUpperCase().contains("ITENS") || palavra.toUpperCase().contains("ITEM")) {
					isItens = true;
					continue;
				}
				if( palavra.contains("$") || palavra.toUpperCase().contains("VALOR")) {
					isValor = true;
					continue;
				}
				if( isItens ) {
					if( isStringNumero(palavra)) {
						if( palavra.contains(",")) {
							jItens.put("valor", trataValor(palavra) );
							if( lstItens.isEmpty() ) {
								jEmpresa.append("itens", jItens );
							}else {
								lstItens.add(jItens);
								for(JSONObject item: lstItens ) {
									item.put("valor", palavra);
									jEmpresa.append("itens", item );	
								}
							}
							jItens = new JSONObject();
							isBuscaCnpj = false;
							isValor = false;
							valor = palavra;
						}else {
							if( lstItens.isEmpty() ) {
								jItens.put("item", palavra);
							}else {
								jItens.put("item", palavra);
								lstItens.add( jItens);
								jItens = new JSONObject();// novo item, mesmo tendo uma lista de itens. Só para não dar problema.
							}
							continue;
						}
					}else if( palavra.contains("$")) {
						isValor = true;
						continue;
					}else if( palavra.equals("e")) {
						if( !jItens.isEmpty() ) {
							lstItens.add( jItens);
							jItens = new JSONObject();// novo item, mesmo tendo uma lista de itens. Só para não dar problema.
							continue;
						}
					}
					if( palavra.endsWith(";") || palavra.endsWith(".") ) {
						if( !jEmpresa.isEmpty() && jEmpresa.has("cnpj") && jEmpresa.has("valor") ) {
							if( !jEmpresa.has("itens") ) {
								if( !lstItens.isEmpty() ) {
									for(JSONObject item: lstItens ) {
										item.put("valor", valor );
										jEmpresa.append("itens", item );	
									}
								}else if( !jItens.isEmpty() ) {
									jEmpresa.append("itens", jItens );
								}
							}
							json.append("vencedores", jEmpresa);
							jEmpresa = new JSONObject();
							jItens = new JSONObject();
							lstItens = new ArrayList<JSONObject>();
							isBuscaCnpj = false;
							isValor = false;
							isBuscaCnpj = false;
							nome = ""; valor = "";
							continue;
						}
					}
				}
				if( isValor ) {
					if( isStringNumero(palavra) && palavra.contains(",")) {
						isValor = false;
						if( !isItens ) {
							jEmpresa.put("valor", palavra);
						}
						valor = palavra;
					}
				}
				if( isBuscaCnpj ){
					if( isStringNumero(palavra) ) {
						isBuscaCnpj = false;
						jEmpresa.put("cnpj", palavra);
						if( isNomeValido(nome ) ) {
							jEmpresa.put("nome", nome.trim());
							nome = "";
							continue;
						}
					}
				}
				if( palavra.endsWith(".") || palavra.endsWith(";")) {
					if( !jEmpresa.isEmpty() && jEmpresa.has("cnpj") && jEmpresa.has("valor") ) {
						if( !jEmpresa.has("itens") ) {
							if( !lstItens.isEmpty() ) {
								for(JSONObject item: lstItens ) {
									item.put("valor", valor );
									jEmpresa.append("itens", item );	
								}
							}else if( !jItens.isEmpty() ) {
								if( !jItens.has("valor")) jItens.put("valor", valor);
								jEmpresa.append("itens", jItens );
							}
						}
						json.append("vencedores", jEmpresa);
						jEmpresa = new JSONObject();
						lstItens = new ArrayList<JSONObject>();
						jItens = new JSONObject();
						isBuscaCnpj = false;
						isValor = false;
						isBuscaCnpj = false;
						nome = ""; valor = "";
						continue;
					}
				}
				if (nome.isEmpty() && palavra.length() < 2) continue;
				nome += " "+palavra;
			}
		}
		if( !jEmpresa.isEmpty() && (jEmpresa.has("cnpj") || jEmpresa.has("valor")) ) {
			if( !jEmpresa.has("itens") ) {
				if( !lstItens.isEmpty() ) {
					for(JSONObject item: lstItens ) {
						item.put("valor", valor );
						jEmpresa.append("itens", item );	
					}
				}else if( !jItens.isEmpty() ) {
					jEmpresa.append("itens", jItens );
				}
			}
			json.append("vencedores", jEmpresa);
		}
	}

	private void montaVencedoras_old(JSONObject json, String dado) {
		String arr[] = dado.toUpperCase().split("EMPRESA");
		int tipoBusca =  0; //VENCEDOR
		if( arr.length < 3 ) {
			arr = dado.toUpperCase().split("VENCEDOR");
		}
		if( arr.length < 3 ) {
			tipoBusca = 1; //cnpj
			arr = dado.toUpperCase().split("CNPJ");
		}
		if( arr.length < 3) {
			throw new RuntimeException("Falha ao buscar a vencedora.");
		}else {
			int index =0;
			for(String s: arr) {
				JSONObject j = new JSONObject();
				if( index == 0 ) {
					montaObjeto(json, s);
				}
				{
					if( tipoBusca == 0 ) {
						montaEmpresa(j, s);
						montaCNPJ(j, s);
					}else {
						if( index < arr.length-1 ) {
							montaCNPJEspecifico(j, arr[index+1]);
							montaNomeEmpresaTipoBuscaCnpj(j, s);
						}
					}
					String valores[] = s.split("R\\$");
					if( valores.length > 1) {
						montaListaValores(j, valores);
					}else {
						montaValor(j, s);
					}
				}
				index++;
				if( !j.isEmpty() ) {
					json.append("vencedor", j);
				}
			}
		}
	}
	private boolean isNomeValido(String nome) {
		if( nome.isEmpty()) return false;
		if( isNum( nome.charAt(0))) return false;//começa com numero
		return true;
	}
	private void montaNomeEmpresaTipoBuscaCnpj(JSONObject j, String s) {
		s = s.replaceAll("\n", " ");
		String valor = null;
		int pos = s.lastIndexOf(":");
		if( pos >=0 ) {
			s = s.substring(pos+1, s.length() ).trim();
			valor = s;
		}
		{
			pos = s.lastIndexOf(";");
			if( pos >=0 ) {
				s = s.substring(pos+1, s.length() ).trim();
				valor = s;
			}
		}
		
		if( valor != null ) {
			j.put("nome", valor);
		}
	}

	private void montaCNPJEspecifico(JSONObject j, String s) {
		int pos =0;
		while( pos < s.length() && !isNum(s.charAt(pos)) ) {
			pos++;
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
		j.put("cnpj", s);
	}

	//itens: 01 (R$ 780,00), 03 (R$ 970,00) e 05 (R$ 1.065,00);
	private void montaListaValores(JSONObject json, String[] valores) {
		String item = null;
		String valor = null;
		for(String s: valores) {
			if( item == null) {
				if( s.contains("ITENS")) {
					int pos = s.indexOf("ITENS");
					s = s.substring(pos, s.length() );
				}
				int pos =0;
				while(pos < s.length() && !isNum(s.charAt(pos)) ) {
					pos++;
				}
				item = s.substring(pos, s.length());
				item = item.replaceAll("\\)", "").replaceAll("\\(", "").trim();
			}else {
				int pos =0;
				while(pos < s.length() && !isNum(s.charAt(pos)) ) {
					pos++;
				}
				valor = s.substring(pos, s.length()).trim();
				pos = valor.indexOf(' ');
				if( pos < 0 ) pos = valor.indexOf('\n');
				if( pos < 0 ) throw new RuntimeException("Não pode desncobrir onde começa o item.");
				String proximoItem = valor.substring(pos, valor.length());
				valor = valor.substring(0, pos);
				valor = valor.replaceAll("\\)", "").replaceAll(";", "").trim();
				JSONObject j = new JSONObject();
				j.put("item", item);
				j.put("valor", valor);
				json.append("itens", j);
				item = proximoItem;
				pos =0;
				while(pos < item.length() && !isNum(item.charAt(pos)) ) {
					pos++;
				}
				item = item.substring(pos, item.length());
				item = item.replaceAll("\\)", "").replaceAll("\\(", "").trim();
			}
		}
	}

	private void montaEmpresa(JSONObject j, String dado) {
		String valor = null;
		if( dado.contains(":")) {
			String arr[] = dado.split(":");
			if( arr.length > 2) {
				for(String s: arr) {
					if( s.isEmpty() || isStringNumero( s )) {
						continue;
					}else {						
						int pos = 0 ;
						while(pos < s.length() && !isNum(s.charAt(pos)) ) {
							System.out.print( s.charAt(pos) );
							pos++;
						}
						if( pos < s.length()-1 ) {
							continue;
						}
					}
					//TODO como confirmar se é um nome de empresa
					valor = s;
					break;
				}
			}else {
				String s = dado.split(":")[1];
				if( s.contains(",")) {
					s = s.split(",")[0];
				}
				valor = s.trim();
			}
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
