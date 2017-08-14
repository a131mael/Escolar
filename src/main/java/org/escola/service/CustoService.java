package org.escola.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.escola.model.Aluno;
import org.escola.model.Carro;
import org.escola.model.Custo;
import org.escola.util.Service;


@Stateless
public class CustoService extends Service {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "EscolarDS")
	private EntityManager em;

	public Custo findById(EntityManager em, Long id) {
		return em.find(Custo.class, id);
	}

	public Custo findById(Long id) {
		return em.find(Custo.class, id);
	}
	
	public Carro findCarroById(Long id) {
		return em.find(Carro.class, id);
	}
	
	public Custo findByCodigo(Long id) {
		return em.find(Custo.class, id);
	}
	
	public String remover(Long idEvento){
		em.remove(findById(idEvento));
		return "index";
	}

	public List<Custo> findAll() {
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Custo> criteria = cb.createQuery(Custo.class);
			Root<Custo> member = criteria.from(Custo.class);
			// Swap criteria statements if you would like to try out type-safe
			// criteria queries, a new
			// feature in JPA 2.0
			// criteria.select(member).orderBy(cb.asc(member.get(Member_.name)));
			criteria.select(member).orderBy(cb.asc(member.get("id")));
			return em.createQuery(criteria).getResultList();
	
		}catch(NoResultException nre){
			return new ArrayList<>();
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public Custo save(Custo evento) {
		Custo user = null;
		try {

			if (evento.getId() != null && evento.getId() != 0L) {
				user = findById(evento.getId());
			} else {
				user = new Custo();
			}
			
			user.setData(evento.getData());
			user.setDescricao(evento.getDescricao());
			user.setNome(evento.getNome());
			user.setTipoCusto(evento.getTipoCusto());
			user.setValor(evento.getValor());
			user.setId(evento.getId());
			if(evento.getCarro() != null){
				user.setCarro(findCarroById(evento.getCarro().getId()));
			}
			
			em.persist(user);
			
		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			// builder = createViolationResponse(ce.getConstraintViolations());
		} catch (ValidationException e) {
			// Handle the unique constrain violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("email", "Email taken");

		} catch (Exception e) {
			// Handle generic exceptions
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("error", e.getMessage());

			e.printStackTrace();
		}

		return user;
	}

	public Custo findByAluno(Long idAluno) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Custo> criteria = cb.createQuery(Custo.class);
			Root<Custo> member = criteria.from(Custo.class);

			Predicate whereSerie = null;

			whereSerie = cb.equal(member.get("aluno").get("id"), idAluno);
			criteria.select(member).where(whereSerie);

			criteria.select(member);
			return em.createQuery(criteria).getSingleResult();

		} catch (NoResultException nre) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	
	
	public long count(Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Custo> member = countQuery.from(Custo.class);
			countQuery.select(cb.count(member));
			
			final List<Predicate> predicates = new ArrayList<Predicate>();
			Calendar dtInicio = null;
			Calendar dtFIm = null;
			if (filtros != null) {
				
				for (Map.Entry<String, Object> entry : filtros.entrySet()) {

					Predicate pred = cb.and();
					if(entry.getKey().equals("dateBetween")){
						
						Date dataInicio =   ((List<Date>)entry.getValue()).get(0);
						dtInicio = Calendar.getInstance();
						dtInicio.setTime(dataInicio);

						Date dataFim =   ((List<Date>)entry.getValue()).get(1);
						dtFIm = Calendar.getInstance();
						dtFIm.setTime(dataFim);

						 ParameterExpression<Date> parameter = cb.parameter(Date.class, "dataInicio");
						 pred = cb.greaterThanOrEqualTo(member.<Date> get("data"), parameter);
						 predicates.add(pred);
						
						 ParameterExpression<Date> parameter2 = cb.parameter(Date.class, "dataFim");
						 pred = cb.lessThanOrEqualTo(member.<Date> get("data"), parameter2);
						 predicates.add(pred);
					}else if (entry.getValue() instanceof String) {
						pred = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" + entry.getValue() + "%"));
						predicates.add(pred);
					} else {
						pred = cb.equal(member.get(entry.getKey()), entry.getValue());
						predicates.add(pred);
					}
					countQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
				}

			}

			Query q = em.createQuery(countQuery);
			if(dtInicio != null){
				q.setParameter("dataInicio", dtInicio.getTime());
			}
			if(dtFIm != null){
				q.setParameter("dataFim", dtFIm.getTime());
			}
			return (long) q.getSingleResult();

		} catch (NoResultException nre) {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Custo> find(int first, int size, String orderBy, String order, Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Custo> criteria = cb.createQuery(Custo.class);
			Root<Custo> member = criteria.from(Custo.class);
			CriteriaQuery cq = criteria.select(member);

			final List<Predicate> predicates = new ArrayList<Predicate>();
			Calendar dtInicio = null;
			Calendar dtFIm = null;
			for (Map.Entry<String, Object> entry : filtros.entrySet()) {

				Predicate pred = cb.and();
				if(entry.getKey().equals("dateBetween")){
					
					Date dataInicio =   ((List<Date>)entry.getValue()).get(0);
					dtInicio = Calendar.getInstance();
					dtInicio.setTime(dataInicio);

					Date dataFim =   ((List<Date>)entry.getValue()).get(1);
					dtFIm = Calendar.getInstance();
					dtFIm.setTime(dataFim);

					 ParameterExpression<Date> parameter = cb.parameter(Date.class, "dataInicio");
					 pred = cb.greaterThanOrEqualTo(member.<Date> get("data"), parameter);
					 predicates.add(pred);
					
					 ParameterExpression<Date> parameter2 = cb.parameter(Date.class, "dataFim");
					 pred = cb.lessThanOrEqualTo(member.<Date> get("data"), parameter2);
					 predicates.add(pred);
				}else if (entry.getValue() instanceof String) {
					
					pred = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" + entry.getValue() + "%"));
					predicates.add(pred);
				} else {
					pred = cb.equal(member.get(entry.getKey()), entry.getValue());
					predicates.add(pred);
				}
				 
				//cq.where(pred);
			}

			cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
			cq.orderBy((order.equals("asc") ? cb.asc(member.get(orderBy)) : cb.desc(member.get(orderBy))));
			Query q = em.createQuery(criteria);
			q.setFirstResult(first);
			q.setMaxResults(size);
			if(dtInicio != null){
				q.setParameter("dataInicio", dtInicio.getTime());
			}
			if(dtFIm != null){
				q.setParameter("dataFim", dtFIm.getTime());
			}
			return (List<Custo>) q.getResultList();

		} catch (NoResultException nre) {
			return new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}

	}


}

