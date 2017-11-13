package org.escola.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.escola.model.Carro;
import org.escola.model.Faturamento;
import org.escola.util.Constant;
import org.escola.util.Service;

@Stateless
public class FaturamentoService extends Service {

	@Inject
	private Logger log;
	
	@Inject
	private TurmaService carroService;

	@Inject
	private RelatorioService relatorioService;

	
	@PersistenceContext(unitName = "EscolarDS")
	private EntityManager em;

	public Faturamento findById(EntityManager em, Long id) {
		return em.find(Faturamento.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Faturamento> findFaturamentoPorMes(Date mes) {
		List<Faturamento> faturamentos = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT fat from  Faturamento fat ");
		sql.append("where 1=1 ");
		sql.append(" and fat.data =   ");
		sql.append(mes);
		sql.append(" and fat.anoLetivo = ");
		sql.append(Constant.anoLetivoAtual);

		Query query = em.createQuery(sql.toString());
		faturamentos = query.getResultList();
		return faturamentos;

	}

	
	//Gera o faturamento dos carros no primeiro dia do mes atual
	public void gerarFaturamento() throws ParseException {
		Calendar data = new GregorianCalendar();
		data.set(Calendar.DAY_OF_MONTH, 1);
		
		Map<String, Object> filtros = new HashMap<>();
		
		for(Carro carro: carroService.findAll()){
			filtros.put("carroLevaParaEscola", carro);
			filtros.put("carroLevaParaEscolaTroca", carro);
			filtros.put("carroPegaEscola", carro);
			filtros.put("carroPegaEscolaTroca", carro);
			
			Faturamento faturamento = new Faturamento();
			faturamento.setData(data.getTime());
			faturamento.setCarro(carro);
			faturamento.setAnoLetivo(Constant.anoLetivoAtual);
			faturamento.setQuantidadeCriancas(relatorioService.countCriancasCarro(filtros));
			faturamento.setValor(relatorioService.getValorTotal(filtros));
			em.persist(faturamento);
		}
		
		
	}

	@SuppressWarnings("unchecked")
	public List<Faturamento> findFaturamentoPorCarro(Carro carro) {
		try{
			List<Faturamento> faturamentos = new ArrayList<>();

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT fat from  Faturamento fat ");
			sql.append("where 1=1 ");
			sql.append(" and fat.carro.id =   ");
			sql.append(carro.getId());
			sql.append(" and fat.anoLetivo = ");
			sql.append(Constant.anoLetivoAtual);
			sql.append( "order by fat.data");

			Query query = em.createQuery(sql.toString());
			faturamentos = (List<Faturamento>)query.getResultList();
			return faturamentos;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

}
