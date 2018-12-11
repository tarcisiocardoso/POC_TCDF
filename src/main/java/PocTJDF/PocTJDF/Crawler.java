package PocTJDF.PocTJDF;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Crawler {
    static String[] meses = {
//            "01_Janeiro",
//            "02_Fevereiro",
            "03_Março",
//            "04_Abril",
//            "05_Maio",
//            "06_Junho",
//            "07_Julho",
//            "08_Agosto",
//            "09_Setembro",
//            "10_Outubro",
//            "11_Novembro",
//            "12_Dezembro"
    };

    static int[] anos = {
            2016, 2017, 2018
    };

    public static Elements getLinksData(int ano, String mes) {
        try {
            URL url = new URL("http://www.buriti.df.gov.br/ftp/default.asp");
            Map<String, Object> params = new LinkedHashMap<String, Object>();
            params.put("Ano", String.valueOf(ano));
            params.put("Mes", mes);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "ISO-8859-1"));
            }
            byte[] postDataBytes = postData.toString().getBytes("ISO-8859-1");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));

            String s = "";
            for (int c; (c = in.read()) >= 0; )
                s += new Character((char) c).toString();

            System.out.println( s );
            Document doc = Jsoup.parse(s);

            Elements links = doc.select("a");

            return links;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getLinkPdf(String link) {
        try {
//            link = link.replaceAll(" ", "%20");
        	String arr[] = link.split("\\?");
        	String sUrl = arr[0]+"?";
        	arr = arr[1].split("&");
        	for(String s: arr) {
        		if( sUrl.length() > 13)sUrl+= "&";
        		String ss[] = s.split("=");
        		String key = ss[0];
        		String value = ss[1];
        		if( value.contains(" ")) {
        			String branco[] = value.split(" ");
        			String novoValor = "";
        			for(String b: branco) {
        				if( !novoValor.isEmpty() ) novoValor +="%20";
        				novoValor += URLEncoder.encode(b, "UTF-8");
        			}
        			value = novoValor;
        		}else {
        			value = URLEncoder.encode(value, "UTF-8");
        		}
        		sUrl += key+"="+value;
        	}
        	
            URL url = new URL("http://www.buriti.df.gov.br/ftp/" + sUrl);
            Map<String, Object> params = new LinkedHashMap<>();

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
//            conn.setDoOutput(true);
//            conn.getOutputStream().write(postDataBytes);


            String linkPdf = "";
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String s = "";
            for (int c; (c = in.read()) >= 0; )
                s += new Character((char) c).toString();

            System.out.println(s );
            Document doc = Jsoup.parse(s);

            Elements links = doc.select("a");

            linkPdf = "http://www.buriti.df.gov.br/ftp/";
            for (Element element : links) {
                linkPdf += element.attr("href");
                break;
            }
            return linkPdf;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void main(String[] args) {
        String path = System.getProperty("user.home") + "/.crawler/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        for (Integer ano : anos) {
            for (String mes : meses) {
                Elements links = getLinksData(ano, mes);
                if (links != null) {
                    for (Element link : links) {
                        String linkPdf = getLinkPdf(link.attr("href"));
                        System.out.println("Download realizado com sucesso: " + linkPdf);
                        String nomePdf = linkPdf.substring(linkPdf.lastIndexOf("/") + 1, linkPdf.length());

                        if (!"".equals(linkPdf)) {
                            try {
                                URL url = new URL(linkPdf.replaceAll(" ", "%20"));
                                InputStream in = url.openStream();
                                Files.copy(in, Paths.get(path + "/" + nomePdf), StandardCopyOption.REPLACE_EXISTING);
                                in.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}