package org.escola.enums;

public enum Serie {

	MATERNAL("Maternal"),
	
	JARDIM_I("Jardmin I"),
	
	JARDIM_II("Jardmin II"),
	
	PRE("PRÉ"),
	
	PRIMEIRO_ANO("1º ano"),
	
	SEGUNDO_ANO("2º ano"),
	
	TERCEIRO_ANO("3º ano"),
	
	QUARTO_ANO("4º ano"),
	
	QUINTO_ANO("5º ano"),
	
	SEXTO_ANO("6º ano"),
	
	SETIMO_ANO("7º ano"),
	
	OITAVO_ANO("8º ano"),
	
	NONO_ANO("9º ano");
	
	private String name;
	
	Serie(String name){
		this.name = name;
		
	}

	public String getName() {
		return name;
	}

}
