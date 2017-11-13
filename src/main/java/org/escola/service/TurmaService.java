
package org.escola.service;

import java.util.ArrayList;
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
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.escola.model.FuncionarioCarro;
import org.escola.model.Carro;
import org.escola.util.Constant;
import org.escola.util.Service;


@Stateless
public class TurmaService extends Service {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "EscolarDS")
	private EntityManager em;

	public Carro findById(EntityManager em, Long id) {
		return em.find(Carro.class, id);
	}

	public Carro findById(Long id) {
		return em.find(Carro.class, id);
	}

	public List<Carro> findAll() {
		try{
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Carro> criteria = cb.createQuery(Carro.class);
			Root<Carro> member = criteria.from(Carro.class);
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

	public Carro findByName(String name) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT c from  Carro c ");
		sql.append("where 1=1 ");
		sql.append("and c.nome like '%");
		sql.append(name);
		sql.append("%'");

		Query query = em.createQuery(sql.toString());
		return (Carro) query.getSingleResult();
	}

	
	public Carro save(Carro professor) {
		Carro user = null;
		try {

			log.info("Registering " + professor.getNome());
		
			if (professor.getId() != null && professor.getId() != 0L) {
				user = findById(professor.getId());
			} else {
				user = new Carro();
			}
			
			user.setNome(professor.getNome());
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

	public String remove(Long idTurma) {
		/**CASCADE*/
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT pt from  FuncionarioCarro pt ");
		sql.append("where pt.turma.id =   ");
		sql.append(idTurma);

		Query query = em.createQuery(sql.toString());
		
		 
		try{
			List<FuncionarioCarro> professorTurmas = query.getResultList();
			for(FuncionarioCarro profT : professorTurmas){
				em.remove(profT);
			}
		
			em.remove(findById(idTurma));
			
		}catch(NoResultException noResultException){
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		return "ok";
	}

	public List<Carro> findAll(Long idProfessor) {
		List<Carro> turmasDoProfessor = new ArrayList<>();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT pt from  FuncionarioCarro pt ");
		sql.append("where pt.professor.id =   ");
		sql.append(idProfessor);
		Query query = em.createQuery(sql.toString());
		
		 
		try{
			List<FuncionarioCarro> professorTurmas = query.getResultList();
			for(FuncionarioCarro profT : professorTurmas){
				turmasDoProfessor.add(profT.getTurma());
			}
		
		}catch(NoResultException noResultException){
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return turmasDoProfessor;
	}

/*	public Usuario findMaiorPontuadorSemana() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT u from  Usuario u ");
		sql.append("where pontosSemana =  (SELECT MAX(pontosSemana) FROM Usuario) ");

		Query query = em.createQuery(sql.toString());
		return (Usuario) query.getResultList().get(0);
	}

	public Usuario findMaiorPontuadorMes() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT u from  Usuario u ");
		sql.append("where pontosMes =  (SELECT MAX(pontosMes) FROM Usuario) ");

		Query query = em.createQuery(sql.toString());
		return (Usuario) query.getResultList().get(0);

	}

	public Usuario findMaiorPontuadorGeral() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT u from  Usuario u ");
		sql.append("where pontosGeral =  (SELECT MAX(pontosGeral) FROM Usuario) ");

		Query query = em.createQuery(sql.toString());
		return (Usuario) query.getResultList().get(0);
	}
*/
}
