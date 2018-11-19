package PocTJDF.PocTJDF;

import java.util.HashMap;
import java.util.List;

import PocTJDF.PocTJDF.App.Arquivo;
import PocTJDF.PocTJDF.App.Registro;
import PocTJDF.PocTJDF.rules.Abertura;
import PocTJDF.PocTJDF.rules.DispensaLicitacao;
import PocTJDF.PocTJDF.rules.NotaEmpenho;
import PocTJDF.PocTJDF.rules.Regra;
import PocTJDF.PocTJDF.rules.TermoAditivo;

public class AppProcessaRegistros {

	HashMap<String, Regra> regras = new HashMap<String, Regra>(){{
		put("AVISO DE ABERTURA", new Abertura() );
		put("AVISO DE LICITAÇÃO", new Abertura() );
		put("DISPENSA DE LICITAÇÃO", new DispensaLicitacao() );
		put("EXTRATO DE NOTA DE EMPENHO", new NotaEmpenho() );
		put("TERMO ADITIVO", new TermoAditivo() );
		put("INEXIGIBILIDADE", new Inexigibilidade() );
	}};
	
	public AppProcessaRegistros() {
		
	}
	
	private void preProcessa() {
		App.Arquivo a = new Arquivo();
		List<App.Registro> lst = DataBase.getInstancia().getRegistro( a );
		
		for( App.Registro reg: lst) {
			if( regTemProblema(reg ).contains("Fim da linha indetermiado") ) {
				if( reg.conteudo.toUpperCase().contains("DISPENSA") ) {
					if( reg.tipo.contains("EXTRATO")) {
						//TODO validação EXTRATO.
					}else {
						reg.tipo = "DISPENSA DE LICITAÇÃO";
					}
					reg.dado = "{}";
					DataBase.getInstancia().insertObjeto(reg);
				}
			}
			if( reg.tipo.contains("DISPENSA")) {
				reg.tipo = "DISPENSA DE LICITAÇÃO";
				reg.dado = "{}";
				DataBase.getInstancia().insertObjeto(reg);
			}else if ( reg.tipo.contains("EXTRATO DE NOTA DE EMPENHO") ) {
				if( !reg.tipo.equals("EXTRATO DE NOTA DE EMPENHO")) {
					String tipo = reg.tipo;
					reg.tipo = "EXTRATO DE NOTA DE EMPENHO";
					reg.dado = "{}";
					reg.conteudo = tipo+"\n"+reg.conteudo;
					DataBase.getInstancia().updateRegistroConteudo(reg);
				}
			}else if ( reg.tipo.contains("TERMO ADITIVO")) {
				if( !reg.tipo.equals("TERMO ADITIVO")) {
					String tipo = reg.tipo;
					reg.tipo = "TERMO ADITIVO";
					reg.dado = "{}";
					reg.conteudo = tipo+"\n"+reg.conteudo;
				}
				DataBase.getInstancia().updateRegistroConteudo(reg);
			}else if( reg.tipo.contains("INEXIGIBILIDADE")) {
				if( !reg.tipo.equals("INEXIGIBILIDADE")) {
					String tipo = reg.tipo;
					reg.tipo = "INEXIGIBILIDADE";
					reg.dado = "{}";
					reg.conteudo = tipo+"\n"+reg.conteudo;
					DataBase.getInstancia().updateRegistroConteudo(reg);
				}
			}else if( reg.tipo.contains("AVISO") && reg.tipo.contains("ABERTU")) {
				if( !reg.tipo.equals("AVISO DE ABERTURA")) {
					String tipo = reg.tipo;
					reg.tipo = "AVISO DE ABERTURA";
					reg.dado = "{}";
					reg.conteudo = tipo+"\n"+reg.conteudo;
					DataBase.getInstancia().updateRegistroConteudo(reg);
				}
			}
		}
	}
	
	private String regTemProblema(Registro reg) {
		String problema = DataBase.getInstancia().buscaProblemaGrupo( reg.idSubGrupo );
		
		return problema == null ? "": problema;
	}
	public void processa() {
		App.Arquivo a = new Arquivo();
		List<App.Registro> lst = DataBase.getInstancia().getRegistro( a );
		
		for( App.Registro reg: lst) {
			Regra r = lookup(reg);
			if( execRegra(r, reg) ) {
				updateRegra( reg );
				//break;
			}else {
				System.err.println("não implementado");
			}
		}
	}
	
	private void updateRegra(Registro reg) {
		DataBase.getInstancia().insertObjeto(reg);
	}

	private boolean execRegra(Regra r, App.Registro reg) {
		if( r != null ) {
			Regra proxima = r.execute(reg);
			if(proxima != null) {
				return execRegra(proxima, reg);
			}
			return true;
		}
		return false;
	}

	private Regra lookup(Registro reg) {
		if( regras.containsKey(reg.tipo)) {
			return regras.get(reg.tipo);
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println("===============");
		AppProcessaRegistros app =new AppProcessaRegistros();
		
		app.preProcessa();
		
		app.processa();
		
		
		
	}
	
}
