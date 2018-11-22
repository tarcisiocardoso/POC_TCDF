package PocTJDF.PocTJDF;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import PocTJDF.PocTJDF.App.Arquivo;
import PocTJDF.PocTJDF.App.Registro;

public class DataBase {

	private static DataBase instancia;

	Connection connection = null;
	Statement stmt = null;
	PreparedStatement pstmt = null;
	PreparedStatement pstmtArquivo = null;
	PreparedStatement pstmtUpdateArquivo = null;
	PreparedStatement pstmtGrupo = null;
	PreparedStatement pstmtSubGrupo = null;
	PreparedStatement pstmtUpdateRegra = null;
	PreparedStatement pstmtUpdateRegraConteudo = null;

	private DataBase() {
		try {
			Properties prop = App.prop;
			System.out.println("conectando ao banco: "+prop.getProperty("url") );

			Class.forName( prop.getProperty("driver"));

			connection = DriverManager.getConnection(prop.getProperty("url"), prop.getProperty("user"), prop.getProperty("pass"));
			stmt = connection.createStatement();
			pstmtUpdateRegra = connection.prepareStatement("update registro set tipo = ?, dado = ?::json WHERE id= ? ");
			pstmtUpdateRegraConteudo = connection.prepareStatement("update registro set tipo = ?, conteudo =?, dado = ?::json WHERE id= ? ");
			pstmt = connection.prepareStatement("insert into registro (idSubGrupo, tipo, conteudo, dado) values(?, ?, ?, ?::json) ");
			pstmtArquivo = connection.prepareStatement("insert into arquivo (nome) values(?) ", Statement.RETURN_GENERATED_KEYS);
			pstmtUpdateArquivo = connection.prepareStatement("update arquivo set problema = ? where id=? ");
			pstmtGrupo = connection.prepareStatement("insert into grupo (idArquivo, linha, pagina, nome, resumo, problema ) values(?, ?, ?, ?, ?, ?) ", Statement.RETURN_GENERATED_KEYS);
			pstmtSubGrupo = connection.prepareStatement("insert into subGrupo (idGrupo, linha, nome) values(?, ?, ?) ", Statement.RETURN_GENERATED_KEYS);
						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static DataBase getInstancia() {
		if (instancia == null) {
			instancia = new DataBase();
		}

		return instancia;
	}
	
	public Connection getConn() {
		return this.connection;
	}
	
	public long insertSubGrupo(App.SubGrupo sub) {
		try {
			ResultSet rsq = stmt.executeQuery("select id from subGrupo where idGrupo = "+sub.idGrupo+" and linha = "+sub.linha );
			if( rsq.next() ) {
				return rsq.getLong("id");
			}
			if( sub.nome.length() > 200 ) {
				sub.nome = sub.nome.substring(0, 150)+"...";
			}
			
			pstmtSubGrupo.clearParameters();
			pstmtSubGrupo.setLong(1, sub.idGrupo);
			pstmtSubGrupo.setLong(2, sub.linha);
			pstmtSubGrupo.setString(3, sub.nome);
			pstmtSubGrupo.executeUpdate();
			
			ResultSet rs = pstmtSubGrupo.getGeneratedKeys();
			if(rs.next()) {
				long id = rs.getLong(1);
				return id;
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public long insertGrupo(App.Grupo grupo) {
		try {
			
			ResultSet rsQlt = stmt.executeQuery("select id from grupo where idArquivo = "+grupo.idArquivo+" and nome = '"+grupo.nome+"'");
			if( rsQlt.next() ) {
				return rsQlt.getLong("id");
			}
			
			pstmtGrupo.clearParameters();
			pstmtGrupo.setLong(1, grupo.idArquivo);
			pstmtGrupo.setLong(2, grupo.linha);
			pstmtGrupo.setLong(3, grupo.pagina);
			pstmtGrupo.setString(4, grupo.nome);
			pstmtGrupo.setString(5, grupo.resumo);
			pstmtGrupo.setString(6, grupo.problema);
			pstmtGrupo.executeUpdate();
			
			ResultSet rs = pstmtGrupo.getGeneratedKeys();
			if(rs.next()) {
				long id = rs.getLong(1);
				return id;
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public long updateArquivo(Arquivo arquivo) {
		try {
			if( arquivo.problema != null && arquivo.problema.length() > 200) {
				arquivo.problema = arquivo.problema.substring(0, 195)+"...";
			}
			pstmtUpdateArquivo.clearParameters();
			pstmtUpdateArquivo.setString(1, arquivo.problema);
			pstmtUpdateArquivo.setLong(2, arquivo.id);
			pstmtUpdateArquivo.executeUpdate();
			
			ResultSet rs = pstmtArquivo.getGeneratedKeys();
			if(rs.next()) {
				long id = rs.getLong(1);
				return id;
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public long insertArquivo(String nome) {
		try {
			
			ResultSet rsQlt = stmt.executeQuery("select id from arquivo where nome = '"+nome+"'");
			if( rsQlt.next() ) {
				return rsQlt.getLong("id");
			}
			
			
			pstmtArquivo.clearParameters();
			pstmtArquivo.setString(1, nome);
			pstmtArquivo.executeUpdate();
			
			ResultSet rs = pstmtArquivo.getGeneratedKeys();
			if(rs.next()) {
				long id = rs.getLong(1);
				return id;
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public void updateRegistroConteudo(Registro reg) {
		try {
			if( reg.dado == null) reg.dado = "{}";
			
			pstmtUpdateRegraConteudo.clearParameters();
			pstmtUpdateRegraConteudo.setString(1, reg.tipo);
			pstmtUpdateRegraConteudo.setString(2, reg.conteudo); //, conteudo, dado
			pstmtUpdateRegraConteudo.setString(3, reg.dado); //, conteudo, dado
			pstmtUpdateRegraConteudo.setLong(4, reg.id);
			pstmtUpdateRegraConteudo.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void insertObjeto(Registro reg) {
		try {
			if( reg.dado == null) reg.dado = "{}";
			if( reg.tipo != null && reg.tipo.length() >= 100) {
				reg.tipo = reg.tipo.substring(0, 90)+"...";
			}
			
			if( reg.id > 0 ) {
				
				pstmtUpdateRegra.clearParameters();
				pstmtUpdateRegra.setString(1, reg.tipo);
				pstmtUpdateRegra.setString(2, reg.dado); //, conteudo, dado
				pstmtUpdateRegra.setLong(3, reg.id);
				
				pstmtUpdateRegra.executeUpdate();
				return;
			}
		
			pstmt.clearParameters();
			pstmt.setLong(1, reg.idSubGrupo);
			pstmt.setString(2, reg.tipo); //, conteudo, dado
			pstmt.setString(3, reg.conteudo);
			pstmt.setString(4, reg.dado);
			
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void close() {
		try {
			stmt.close();
			pstmt.close();
			pstmtArquivo.close();
			pstmtGrupo.close();
			connection.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("----------------------");
		
		try {
			Statement stmt = DataBase.getInstancia().getConn().createStatement();
			
			
			ResultSet rs = stmt.executeQuery("select count(*) from registro");
			while( rs.next() ) {
				System.out.println( rs.getObject(1) );
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<App.Arquivo> getArquivos() {
		List<App.Arquivo> lst = new ArrayList<Arquivo>();
		try {
			ResultSet rs = stmt.executeQuery("select * from arquivo"); //where id = 7226");
			while( rs.next() ) {
				App.Arquivo arquivo = new Arquivo();
				int index = 1;
				arquivo.id = rs.getLong(index++);
				arquivo.nome = rs.getString(index++);
				arquivo.problema = rs.getString(index++);
				lst.add(arquivo);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lst;
	}
	public List<App.Registro> getRegistro(Arquivo a) {
		List<App.Registro> lst = new ArrayList<Registro>();
		try {
			
			ResultSet rs = stmt.executeQuery("select id, idSubGrupo, tipo, conteudo from registro where idSubGrupo in "
					+ "( select id from subgrupo where idGrupo in (select id from grupo where idArquivo = "+a.id+")) ");// and id = 7967");
			
//			ResultSet rs = stmt.executeQuery("select id, idSubGrupo, tipo, conteudo from registro where id = 10469 ");// and id = 7967");
			
			
			while( rs.next() ) {
				App.Registro reg = new Registro();
				int index = 1;
				reg.id = rs.getLong(index++);
				reg.idSubGrupo = rs.getLong(index++);
				reg.tipo = rs.getString(index++);
				reg.conteudo   = rs.getString(index++);
				lst.add(reg);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lst;
	}

	public String buscaProblemaGrupo(long id) {
		try {
			ResultSet rs = stmt.executeQuery("select problema from grupo g\n" + 
					"inner join subgrupo sb on sb.idgrupo = g.id\n" + 
					"where sb.id = "+id +"");
			if( rs.next() ) {
				return rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}
}
