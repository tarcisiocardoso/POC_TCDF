package PocTJDF.PocTJDF.rules;

import org.json.JSONObject;

import PocTJDF.PocTJDF.App.Registro;

public class TermoAditivo extends Util implements Regra{

	public Regra execute(Registro reg) {
		
		JSONObject json = new JSONObject();
		
		//json.put("TermoAditivo", "não implementado");
		
		reg.dado = json.toString();
		
		return null;
	}

}
