
package org.escola.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.escola.model.Aluno;
import org.escola.model.AlunoCarro;
import org.escola.util.Service;


@Stateless
public class RelatorioService extends Service {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "EscolarDS")
	private EntityManager em;


	public long count(Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Aluno> member = countQuery.from(Aluno.class);
			countQuery.select(cb.count(member));

			final List<Predicate> predicates = new ArrayList<Predicate>();
			if (filtros != null) {
				for (Map.Entry<String, Object> entry : filtros.entrySet()) {

					Predicate pred = cb.and();
					if (entry.getValue() instanceof String) {
						pred = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" + entry.getValue() + "%"));
					} else {
						pred = cb.equal(member.get(entry.getKey()), entry.getValue());
					}
					predicates.add(pred);
				}
				countQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

			}

			Query q = em.createQuery(countQuery);
			return (long) q.getSingleResult();

		} catch (NoResultException nre) {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}


	public long countCriancasCarro(Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Aluno> member = countQuery.from(Aluno.class);
			countQuery.select(cb.count(member));

			final List<Predicate> predicates = new ArrayList<Predicate>();
			if (filtros != null) {
				for (Map.Entry<String, Object> entry : filtros.entrySet()) {

					Predicate pred = cb.and();
					if (entry.getValue() instanceof String) {
						pred = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" + entry.getValue() + "%"));
					} else {
						pred = cb.equal(member.get(entry.getKey()), entry.getValue());
					}
					predicates.add(pred);
				}
				countQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

			}

			Query q = em.createQuery(countQuery);
			
			
			
			return (long) q.getSingleResult();

		} catch (NoResultException nre) {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@SuppressWarnings("unchecked")
	public long getValorTotal(Map<String, Object> filtros) {
		long total = 0;
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aluno> criteria = cb.createQuery(Aluno.class);
			Root<Aluno> member = criteria.from(Aluno.class);
			CriteriaQuery cq = criteria.select(member);

			final List<Predicate> predicates = new ArrayList<Predicate>();
			for (Map.Entry<String, Object> entry : filtros.entrySet()) {

				Predicate pred = cb.and();
				if (entry.getValue() instanceof String) {
					pred = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" + entry.getValue() + "%"));
				} else {
					pred = cb.equal(member.get(entry.getKey()), entry.getValue());
				}
				 predicates.add(pred);
				//cq.where(pred);
			}
			

			cq.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
			Query q = em.createQuery(criteria);
			List<Aluno> alunos = (List<Aluno>) q.getResultList();
			
			for(Aluno al :alunos){
				if(al.getIdaVolta() == 0){
					if(!al.isTrocaIDA()){
						if(al.getCarroLevaParaEscola() != null && al.getCarroLevaParaEscola().equals(filtros.get("carroLevaParaEscola"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/2;
						}
					}else{
						if(al.getCarroLevaParaEscola()!= null && al.getCarroLevaParaEscolaTroca().equals(filtros.get("carroLevaParaEscola"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/4;
						}
						
						if(al.getCarroLevaParaEscolaTroca() != null && al.getCarroLevaParaEscolaTroca().equals(filtros.get("carroLevaParaEscolaTroca"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/4;
						}
					}
					
					if(!al.isTrocaVolta()){
						if(al.getCarroPegaEscola() != null && al.getCarroPegaEscola().equals(filtros.get("carroPegaEscola"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/2;
						}
					}else{
						if(al.getCarroPegaEscola() != null && al.getCarroPegaEscola().equals(filtros.get("carroPegaEscola"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/4;
						}
						
						if(al.getCarroPegaEscolaTroca() != null && al.getCarroPegaEscolaTroca().equals(filtros.get("carroPegaEscolaTroca"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/4;
						}
					}
					
							
					
				//SOMENTE IDA
				}else if(al.getIdaVolta() == 1){
					if(!al.isTrocaIDA()){
						if(al.getCarroLevaParaEscola() != null && al.getCarroLevaParaEscola().equals(filtros.get("carroLevaParaEscola"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas());
						}
					}else{
						if(al.getCarroLevaParaEscola() != null && al.getCarroLevaParaEscola().equals(filtros.get("carroLevaParaEscola"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/2;
						}
						
						if(al.getCarroLevaParaEscolaTroca() != null && al.getCarroLevaParaEscolaTroca().equals(filtros.get("carroLevaParaEscolaTroca"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/2;
						}
					}
				//SOMENTE Volta
				}else{
					if(!al.isTrocaVolta()){
						if(al.getCarroPegaEscola() != null && al.getCarroPegaEscola().equals(filtros.get("carroPegaEscola"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas());
						}
					}else{
						if(al.getCarroPegaEscola() != null && al.getCarroPegaEscola().equals(filtros.get("carroPegaEscola"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/2;
						}
						
						if(al.getCarroPegaEscolaTroca() != null && al.getCarroPegaEscolaTroca().equals(filtros.get("carroPegaEscolaTroca"))){
							total+= (al.getValorMensal()*al.getNumeroParcelas())/2;
						}
					}
				}
			}
			
			return total;

		} catch (NoResultException nre) {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}

}

