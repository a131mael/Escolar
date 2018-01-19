package org.escola.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.aaf.financeiro.model.Boleto;
import org.escola.model.Aluno;
import org.escola.util.Service;

@Stateless
public class FinanceiroService extends Service {

	@PersistenceContext(unitName = "EscolarDS")
	private EntityManager em;

	// TODO pegar da configuracao
	private int anoLetivo = 2018;

	public Boleto findBoletoByNumero(String numeroBoleto) {
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT bol from Boleto bol ");
			sql.append("where 1=1 ");
			sql.append(" and bol.nossoNumero = '");
			sql.append(numeroBoleto);
			sql.append("'");

			Query query = em.createQuery(sql.toString());
			Boleto boleto = (Boleto) query.getSingleResult();
			return boleto;
		} catch (NoResultException nre) {
			return null;
		}
	}

	public org.escola.model.Boleto findBoletoByNumeroModel(String numeroBoleto) {
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT bol from Boleto bol ");
			sql.append("where 1=1 ");
			sql.append(" and bol.nossoNumero = '");
			sql.append(numeroBoleto);
			sql.append("'");

			Query query = em.createQuery(sql.toString());
			org.escola.model.Boleto  boleto = (org.escola.model.Boleto ) query.getSingleResult();
			return boleto;
		} catch (NoResultException nre) {
			return null;
		}
	}

	
	public org.escola.model.Boleto findBoletoByID(Long id) {
		try {
			return em.find(org.escola.model.Boleto.class, id);
		} catch (NoResultException nre) {
			return null;
		}
	}

	public void save(org.escola.model.Boleto boleto) {
		if (boleto.getId() != null) {
			org.escola.model.Boleto bol = findBoletoByID(boleto.getId());
			bol.setValorPago(boleto.getValorPago());
			bol.setDataPagamento(boleto.getDataPagamento());
			bol.setBaixaManual(boleto.getBaixaManual());
			bol.setConciliacaoPorExtrato(boleto.getConciliacaoPorExtrato());
			em.merge(bol);
		}
	}

	public List<org.escola.model.Boleto> getBoletoMes(int mes) {
		if (mes >= 0) {
			try {
				Calendar c = Calendar.getInstance();
				c.set(anoLetivo, mes, 1, 0, 0, 0);
				Calendar c2 = Calendar.getInstance();
				c2.set(anoLetivo, mes, c.getMaximum(Calendar.MONTH), 23, 59, 59);

				StringBuilder sql = new StringBuilder();
				sql.append("SELECT bol from Boleto bol ");
				sql.append("where 1=1 ");
				sql.append(" and bol.vencimento >= '");
				sql.append(c.getTime());
				sql.append("'");
				sql.append(" and bol.vencimento < '");
				sql.append(c2.getTime());
				sql.append("'");

				Query query = em.createQuery(sql.toString());
				List<org.escola.model.Boleto> boleto = (List<org.escola.model.Boleto>) query.getResultList();
				return boleto;
			} catch (NoResultException nre) {
				return null;
			}
		}
		return null;
	}

	public List<org.escola.model.Boleto> getBoletosParaBaixa() {
		try {
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT bol from Boleto bol ");
			sql.append("where 1=1 ");
			sql.append(" and (bol.baixaManual = true ");
			sql.append(" or bol.conciliacaoPorExtrato = true )");
			sql.append(" and (bol.baixaGerada = false or bol.baixaGerada is null) ");
			

			Query query = em.createQuery(sql.toString());
			List<org.escola.model.Boleto> boleto = (List<org.escola.model.Boleto>) query.getResultList();
			return boleto;
		} catch (NoResultException nre) {
			return null;
		}
	}
	
	public double getPrevisto(int mes) {
		if (mes >= 0) {
			try {
				Calendar c = Calendar.getInstance();
				c.set(anoLetivo, mes, 1, 0, 0, 0);
				Calendar c2 = Calendar.getInstance();
				c2.set(anoLetivo, mes, c.getMaximum(Calendar.MONTH), 23, 59, 59);

				StringBuilder sql = new StringBuilder();
				sql.append("SELECT sum(bol.valorNominal-20) from Boleto bol ");
				sql.append("where 1=1 ");
				sql.append(" and bol.vencimento >= '");
				sql.append(c.getTime());
				sql.append("'");
				sql.append(" and bol.vencimento < '");
				sql.append(c2.getTime());
				sql.append("'");

				Query query = em.createQuery(sql.toString());
				Double boleto = (Double) query.getSingleResult();
				return boleto;
			} catch (NoResultException nre) {
				return 0D;
			}
		}
		return 0D;

	}

	public Double getPago(int mes) {
		if (mes >= 0) {
			try {
				Calendar c = Calendar.getInstance();
				c.set(anoLetivo, mes, 1, 0, 0, 0);
				Calendar c2 = Calendar.getInstance();
				c2.set(anoLetivo, mes, c.getMaximum(Calendar.MONTH), 23, 59, 59);

				StringBuilder sql = new StringBuilder();
				sql.append("SELECT sum(bol.valorPago) from Boleto bol ");
				sql.append("where 1=1 ");
				sql.append(" and bol.dataPagamento >= '");
				sql.append(c.getTime());
				sql.append("'");
				sql.append(" and bol.dataPagamento < '");
				sql.append(c2.getTime());
				sql.append("'");

				Query query = em.createQuery(sql.toString());
				Object retorno = query.getSingleResult();
				Double boleto = null;
				if (retorno != null) {
					boleto = (Double) retorno;
				} else {
					boleto = 0D;
				}

				return boleto;
			} catch (NoResultException nre) {
				return 0D;
			}
		}
		return 0D;

	}

	@SuppressWarnings("unchecked")
	public List<org.escola.model.Boleto> find(int first, int size, String orderBy, String order,
			Map<String, Object> filtros) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Boleto> criteria = cb.createQuery(Boleto.class);
			Root<Boleto> member = criteria.from(Boleto.class);
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
			return (List<org.escola.model.Boleto>) q.getResultList();

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
			Root<Boleto> member = countQuery.from(Boleto.class);
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

			}
			countQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
			Query q = em.createQuery(countQuery);
			return (long) q.getSingleResult();

		} catch (NoResultException nre) {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();	
			return 0;
		}
	}

	public void alterarBoletoManualmente(org.escola.model.Boleto boleto) {
		org.escola.model.Boleto boletoMerged = em.find(org.escola.model.Boleto.class, boleto.getId());
		boletoMerged.setAlteracaoBoletoManual(true);
		boletoMerged.setVencimento(boleto.getVencimento());
		boletoMerged.setValorNominal(boleto.getValorNominal());
		em.merge(boletoMerged);
	}

	public List<Aluno> getAlunosCNABNaoEnviado() {
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT distinct(al) from  Aluno al ");
			sql.append("where 1=1 ");
			sql.append("and (al.cnabEnviado = false or al.cnabEnviado is null )");
			sql.append("and al.removido=false");
			
			Query query = em.createQuery(sql.toString());

			@SuppressWarnings("unchecked")
			List<Aluno> alunos = query.getResultList();

			return alunos;
		} catch (NoResultException nre) {
			return null;
		}
	}


}
