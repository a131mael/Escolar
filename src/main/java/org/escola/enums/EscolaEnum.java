package org.escola.enums;

public enum EscolaEnum {

	ADONAI("Adonai"),
	
	CEMA("Cema"),
	
	VENCESLAU("Venceslau Bueno"),
	
	JOAO_SILVEIRA("João Silveira"),
	
	EVANDRA_SUELI("Evandra Sueli"),
	
	INES_MARTA("Inês Marta"),
	
	VOVO_MARIA("Vovó Maria"),
	
	VOO_LIVRE("Voo Livre"),

	DOM_JAIME("Dom Jaime"),
	
	ITERACAO("Iteração"),
	
	PROJETO_ESPERANCA("Projeto Esperança"),
	
	PROF_GUILHERME("Prof. Guilherme"),
	
	MULLER("Muller"),
	
	MODELO("Modelo"),
	
	MULTIPLA_ESCOLHA("Multipla Escolha"),
	
	RODA_PIAO("Roda Pião"),

	ELCANA("Elcana"),
	
	ELCANANINHA("Elcaninha"),
	
	
	VIVENCIA("Vivência"),
	
	PARAISO_DO_AMOR("Paraíso do Amor"),
	
	ZILAR_ROSAR("Zilar Rosar"),
	
	NOVA_ESPERANCA("Nova Esperança"),
	
	MARIA_JOSE_MEDEIROS("Maria José de Medeiros"),
	
	N_S_FATIMA("N.S. Fátima"),

	INOVACAO("Inovação"),
	
	MARIA_DO_CARMO("Maria do Carmo"),
	
	CETEK("CETEK"),
	
	CENTRO_COMUNITARIO_ARIRIU("Centro comunitario aririu"),
	
	CENTRO_COMUNITARIO_ALTO_ARIRIU("Centro comunitario alto aririu"),
	
	NICOLINA("Nicolina Tancredo");
	
	
	private String name;
	
	EscolaEnum(String name){
		this.name = name;
		
	}

	public String getName() {
		return name;
	}

}
