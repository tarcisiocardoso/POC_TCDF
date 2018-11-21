package PocTJDF.PocTJDF;

import java.io.FileInputStream;
import java.io.IOException;
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
	
	private void preProcessa(App.Arquivo a) {
		List<App.Registro> lst = DataBase.getInstancia().getRegistro( a );
		
		for( App.Registro reg: lst) {
			if( reg.tipo == null) reg.tipo = ">>>INDEFINICO<<<";
			
			System.out.println(">>>> ["+reg.id+"] "+reg.tipo+"<<<<");
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
	public void processa(App.Arquivo a) {
		List<App.Registro> lst = DataBase.getInstancia().getRegistro( a );
		
		for( App.Registro reg: lst) {
			Regra r = lookup(reg);
			if( execRegra(r, reg) ) {
				updateRegra( reg );
				//break;
			}else {
				System.err.println(reg.tipo+" ->não implementado");
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

	public static void main(String[] args)  throws IOException {
		System.out.println("===============");
		
		if( args.length == 0 ) {
			System.out.println("Informe o arquivo de propriedade");
			System.exit(1);
		}
		
		FileInputStream propFile =
	            new FileInputStream( args[0] );
		
		App.prop.load(propFile);
		propFile.close();
		
		AppProcessaRegistros app =new AppProcessaRegistros();
		List<App.Arquivo> lst = app.getArquivos();
		for( App.Arquivo arquivo: lst ) {
			System.out.println(">>>>["+arquivo.id+"] "+arquivo.nome+"<<<<");
			//app.preProcessa(arquivo);
			
			app.processa(arquivo);
		}
		
		System.out.println("===============");
		
	}

	private List<Arquivo> getArquivos() {
		List<App.Arquivo> lst = DataBase.getInstancia().getArquivos();
		return lst;
	}
	
}
