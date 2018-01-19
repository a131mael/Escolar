package org.escola.enums;

public enum TipoMembro {

	SECRETARIA("Secretaria"),
	
	MOTORISTA("Motorista"),
	
	ADMIM("Administrador"),
	
	ALUNO("Aluno"),
	
	MONITOR("Monitor"),
	
	MESTRE("Mestre"),
	
	FINANCEIRO("Financeiro");
	
	private String tipo;
	
	TipoMembro(String tipo){
		this.tipo = tipo;
	}

	public String getName() {
		
		return tipo;
	}

}
