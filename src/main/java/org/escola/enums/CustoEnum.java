package org.escola.enums;

public enum CustoEnum {

	SALARIO_MOTORISTA("Salário Motorista"),
	SALARIO_MONITOR("Salário monitor"),
	COMBUSTIVEL("Combustivel"),
	MECANICA("Mecânica"),
	ELETRICA("Elétrica"),
	PRESTACAO_CARRO("Prestação Carro"),
	OUTROS("Outros");
	
	
	private String name;
	
	CustoEnum(String name){
		this.name = name;
		
	}

	public String getName() {
		return name;
	}

}
