package PocTJDF.PocTJDF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Pegador {
	
	String path;
	String URL = "http://www.buriti.df.gov.br/ftp/";
	CloseableHttpClient client;
    static int[] anos = {
            2016, 2017, 2018
    };
	
	static String[] meses = {
          "01_Janeiro",
          "02_Fevereiro",
          "03_Março",
          "04_Abril",
          "05_Maio",
          "06_Junho",
          "07_Julho",
          "08_Agosto",
          "09_Setembro",
          "10_Outubro",
          "11_Novembro",
          "12_Dezembro"
  };
	
	public Pegador(String path ) {
		this.path = path;
	}

	public static void main(String[] args) {
		System.out.println("===================");
		if( args.length == 0 ) {
			System.out.println("informe onde sera descarregado os arquivos?");
			System.exit(-1);
		}
		
		String path = args[0];
		
		System.out.println( path );
		try {
			new Pegador(path).exec();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exec() throws IOException {
		try {			
			client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost( URL+"default.asp" );
			
			for(int ano: this.anos) {
				System.out.println(ano);
				for(String mes: this.meses) {
					System.out.println( mes);
					
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add( new BasicNameValuePair("Ano", ano+"") );
					params.add( new BasicNameValuePair("Mes", mes) );
					
					httpPost.setEntity(new UrlEncodedFormEntity(params));
					
					CloseableHttpResponse response = client.execute(httpPost);
					
					if( response.getStatusLine().getStatusCode() == 200) {
						trataPrimeiraFase(response);
					}

				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if( this.client != null) {
				client.close();
			}
		}
	}

	private void trataPrimeiraFase(CloseableHttpResponse response) {
		try {
			String result = resultBody(response);
			
			Document doc = Jsoup.parse(result );
			
			Elements links = doc.select("a");
			if (links != null) {
				for (Element link : links) {
					String uri = link.attr("href");
					trataSegundaFase( uri );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void trataSegundaFase(String attr) {
		try {
			List<NameValuePair> params = montaParamFromLink( attr);
			
			HttpPost http = new HttpPost( URL);//+attr );
			http.setEntity(new UrlEncodedFormEntity(params));
			
			CloseableHttpResponse response = client.execute(http);
			if( response.getStatusLine().getStatusCode() == 200) {
				String html = resultBody(response);
				Document doc = Jsoup.parse( html );
				Elements links = doc.select("a");
				if (links != null) {
					for (Element link : links) {
						String uri = link.attr("href");
						trataTerceiraFase( uri );
					}
				}
			}else {
				System.out.println( response.getStatusLine().getStatusCode() );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void trataTerceiraFase(String uri) {
		System.out.println( uri );
		String nomePdf = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		try {
			String link = "";
			String arr[] = uri.split("/");
			for(String s: arr) {
				if( s.contains(" ")) {
					s = s.replaceAll(" ", "%20");
					link +="/"+s;
				}else {
					if(!Pattern.matches("[a-zA-Z0-9 ]+", s)) {
						s = URLEncoder.encode(s, "UTF-8");
						link +="/"+s.replaceAll(" ", "%20");
					}else {
						link +="/"+s;
					}
				}
			}
			
			
			URL url = new URL( URL+ link.substring(1, link.length()) ); // para tirar o primeiro caracter
            InputStream in = url.openStream();
            
            Files.copy(in, Paths.get(path + "/" + nomePdf), StandardCopyOption.REPLACE_EXISTING);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	private List<NameValuePair> montaParamFromLink(String attr) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if( attr != null && !attr.isEmpty()) {
			//default.asp?dir=DODF 014 08-03-2018 EDICAO EXTRA&ano=2018&mes=03_Março
			if( attr.contains("?")) {
				attr = attr.substring( attr.indexOf("?")+1, attr.length() );
			}
			String arr[] = attr.split("&");
			for(String part: arr) {
				String keyValue[] = part.split("=");
				params.add( new BasicNameValuePair(keyValue[0], keyValue[1]) );
			}
		}
		return params;
	}

	private String resultBody(CloseableHttpResponse response) throws UnsupportedOperationException, IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "ISO-8859-1"));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		return result.toString();
	}
}
