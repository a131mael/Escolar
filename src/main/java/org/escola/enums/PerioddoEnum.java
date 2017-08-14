package org.escola.enums;

public enum PerioddoEnum {

	MANHA("Manha"),
	
	TARDE("Tarde"),
	
	INTEGRAL("Integral");
	
	private String tipo;
	
	PerioddoEnum(String tipo){
		this.tipo = tipo;
	}

	public String getName() {
		return tipo;
	}

	public static PerioddoEnum getPeriodo(int ordinal){
		PerioddoEnum per = null;
		for(PerioddoEnum p : PerioddoEnum.values()){
			if(p.ordinal() == ordinal){
				per = p;
			}
		}
		
		return per;
	}
}
