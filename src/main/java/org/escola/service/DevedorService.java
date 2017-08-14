
package org.escola.service;

import java.util.ArrayList;
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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.escola.enums.BimestreEnum;
import org.escola.enums.DisciplinaEnum;
import org.escola.enums.EscolaEnum;
import org.escola.enums.PegarEntregarEnun;
import org.escola.enums.PerioddoEnum;
import org.escola.enums.Serie;
import org.escola.model.Aluno;
import org.escola.model.AlunoCarro;
import org.escola.model.Boleto;
import org.escola.model.Carro;
import org.escola.model.Custo;
import org.escola.model.Devedor;
import org.escola.model.Evento;
import org.escola.model.Funcionario;
import org.escola.model.ObjetoRota;
import org.escola.util.Constant;
import org.escola.util.Service;
import org.escola.util.UtilFinalizarAnoLetivo;

@Stateless
public class DevedorService extends Service {

	@Inject
	private Logger log;

	@Inject
	private EventoService eventoService;

	@Inject
	private UtilFinalizarAnoLetivo finalizarAnoLetivo;

	@PersistenceContext(unitName = "EscolarDS")
	private EntityManager em;

	public Devedor findById(EntityManager em, Long id) {
		return em.find(Devedor.class, id);
	}

	public Devedor findById(Long id) {
		Devedor dev =em.find(Devedor.class, id);
		dev.getBoletos().size();
		return dev;
	}

	public List<Devedor> findAll() {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Devedor> criteria = cb.createQuery(Devedor.class);
			Root<Devedor> member = criteria.from(Devedor.class);
			// Swap criteria statements if you would like to try out type-safe
			// criteria queries, a new
			// feature in JPA 2.0
			// criteria.select(member).orderBy(cb.asc(member.get(Member_.name)));
			criteria.select(member).orderBy(cb.asc(member.get("nome")));
			
			List<Devedor> dvs = new ArrayList<>();
			for(Devedor dev : em.createQuery(criteria).getResultList()){
				dev.getBoletos().size();
				dvs.add(dev);
			}
			
			return dvs;

		} catch (NoResultException nre) {
			return new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public Devedor save(Devedor aluno) {
		Devedor user = null;
		try {

			log.info("Registering " + aluno.getNome());

			if (aluno.getId() != null && aluno.getId() != 0L) {
				user = findById(aluno.getId());
			} else {
				user = new Devedor();
			}
			 user.setEnviadoParaCobrancaCDL(aluno.getEnviadoParaCobrancaCDL());
			user.setNome(aluno.getNome());
			user.setEndereco(aluno.getEndereco());
			user.setBairro(aluno.getBairro());
			user.setCep(aluno.getCep());
			user.setCidade(aluno.getCidade());
			user.setBairro(aluno.getBairro());
			user.setCep(aluno.getCep());
			user.setCidade(aluno.getCidade());
			user.setCpf(aluno.getCpf());
			user.setTelefoneCelular(aluno.getTelefoneCelular());
			user.setTelefoneCelular2(aluno.getTelefoneCelular2());
			user.setTelefoneResidencial(aluno.getTelefoneResidencial());
			user.setEnviadoSPC(aluno.getEnviadoSPC());
			user.setEnviadoParaCobrancaCDL(aluno.getEnviadoParaCobrancaCDL());
			user.setContratoTerminado(aluno.getContratoTerminado());
			user.setObservacao(aluno.getObservacao());
			
			List<Boleto> bs = new ArrayList<>();
			if(aluno.getBoletos() != null){
				for(Boleto b : aluno.getBoletos()){
					if(b.getNumero() != null && !b.getNumero().equalsIgnoreCase("")){
						bs.add(getBoletoAttached(b));
					}
				}
			}
			
			if (aluno.getRemovido() == null) {
				user.setRemovido(false);
			} else {
				user.setRemovido(aluno.getRemovido());
			}


			em.persist(user);
			user.setBoletos(bs);

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

	private Boleto getBoletoAttached(Boleto boleto){
		String numero = boleto.getNumero();
		String numeroContrato = boleto.getNumeroContrato();
		Date dataGeracao =boleto.getDataGeracao();
		Double valor = boleto.getValor();
		
		if(boleto.getId() != null){
			boleto  =em.find(Boleto.class, boleto.getId());
			boleto.setDataGeracao(dataGeracao);
			boleto.setNumeroContrato(numeroContrato);
			boleto.setDataGeracao(dataGeracao);
			boleto.setValor(valor);
		}
		return boleto;
	}
	
	public String remover(Long idDevedor) {
		Devedor al = findById(idDevedor);
		al.setRemovido(true);
		em.persist(al);
		return "ok";
	}

	public String restaurar(Long idDevedor) {
		Devedor al = findById(idDevedor);
		al.setRemovido(false);
		em.persist(al);
		return "ok";
	}


	@SuppressWarnings("unchecked")
	public List<Devedor> find(int first, int size, String orderBy, String order, Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Devedor> criteria = cb.createQuery(Devedor.class);
			Root<Devedor> member = criteria.from(Devedor.class);
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
				// cq.where(pred);
			}

			cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
			cq.orderBy((order.equals("asc") ? cb.asc(member.get(orderBy)) : cb.desc(member.get(orderBy))));
			Query q = em.createQuery(criteria);
			q.setFirstResult(first);
			q.setMaxResults(size);
			
			List<Devedor> ds = new ArrayList<>();
			for(Devedor d : (List<Devedor>) q.getResultList()){
				d.getBoletos().size();
				ds.add(d);
			}
			
			return ds;

		} catch (NoResultException nre) {
			return new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}

	}

	public long count(Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<Devedor> member = countQuery.from(Devedor.class);
			countQuery.select(cb.count(member));

			if (filtros != null) {
				for (Map.Entry<String, Object> entry : filtros.entrySet()) {

					Predicate pred = cb.and();
					if (entry.getValue() instanceof String) {
						pred = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" + entry.getValue() + "%"));
					} else {
						pred = cb.equal(member.get(entry.getKey()), entry.getValue());
					}
					countQuery.where(pred);
				}

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

	public boolean estaEmUmaTUrma(long idAluno) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<AlunoCarro> member = countQuery.from(AlunoCarro.class);
			countQuery.select(cb.count(member));

			Predicate pred = cb.and();
			pred = cb.equal(member.get("aluno").get("id"), idAluno);
			countQuery.where(pred);

			Query q = em.createQuery(countQuery);
			return ((long) q.getSingleResult()) > 0 ? true : false;

		} catch (NoResultException nre) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
}
