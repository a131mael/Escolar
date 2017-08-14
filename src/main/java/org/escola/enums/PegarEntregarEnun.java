package org.escola.enums;

public enum PegarEntregarEnun {

	
	PEGAR("Pegar","P"),
	
	ENTREGAR("Feminino","E");
	
	
	
	private String tipo;
	private String sigla;
	
	PegarEntregarEnun(String tipo,String sigla){
		this.tipo = tipo;
		this.sigla = sigla;
	}

	public String getName() {
		return tipo;
	}
	
	public String getSigla() {
		return sigla;
	}

}
