package org.escola.service;

import org.escola.enums.PerioddoEnum;

public class SQLs {
	
	
	public static String getSQLAlunosVao(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.carroLevaParaEscola.id= ");
		sql.append("?1");
		sql.append(" and al.carroLevaParaEscolaTroca.id= ");
		sql.append("?2");
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append("?3");
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");
		return sql.toString();
	}
	
	
	public static String getSQLAlunosVaoPego(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.carroLevaParaEscolaTroca.id= ");
		sql.append("?1");
		sql.append(" and al.carroLevaParaEscola.id= ");
		sql.append("?2");
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append("?3");
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");
		return sql.toString();
	}
	
	public static String getSQLCarrosTrocaManhaPego(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al.carroLevaParaEscola) from Aluno al ");
		sql.append("where 1 = 1 ");
		sql.append("and al.carroLevaParaEscolaTroca.id= ");
		sql.append("?1");
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append("?2");
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");
		sql.append(" and al.trocaIDA is true");
		return sql.toString();
	}
	
	public static String getSQLCarrosTrocaManha(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al.carroLevaParaEscolaTroca) from Aluno al ");
		sql.append("where 1 = 1 ");
		sql.append("and al.carroLevaParaEscola.id= ");
		sql.append("?1");
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append("?2");
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");
		sql.append(" and al.trocaIDA is true");
		return sql.toString();
	}
	
	public static String getSQLCarrosTrocaTardePego(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al.carroPegaEscola) from Aluno al ");
		sql.append("where 1 = 1 ");
		sql.append("and al.carroPegaEscolaTroca.id= ");
		sql.append("?1");
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append("?2");
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");
		return sql.toString();
	}
	
	public static String getSQLCarrosTrocaTarde(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al.carroPegaEscolaTroca) from Aluno al ");
		sql.append("where 1 = 1 ");
		sql.append("and al.carroPegaEscola.id= ");
		sql.append("?1");
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append("?2");
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");
		return sql.toString();
	}
	
	public static String getSQLTrocaMeioDia(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al.carroLevaParaEscola) from Aluno al ");
		sql.append("where 1 = 1 ");
		sql.append("and al.carroLevaParaEscolaTroca.id= ");
		sql.append("?1");
		sql.append(" and");
		sql.append("  al.periodo = ");
		sql.append("?2");
		return sql.toString();
	}



}
