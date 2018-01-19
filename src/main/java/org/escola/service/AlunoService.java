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
import org.escola.model.Evento;
import org.escola.model.Funcionario;
import org.escola.model.ObjetoRota;
import org.escola.util.Constant;
import org.escola.util.Service;
import org.escola.util.UtilFinalizarAnoLetivo;

@Stateless
public class AlunoService extends Service {

	@Inject
	private Logger log;

	@Inject
	private EventoService eventoService;

	@Inject
	private UtilFinalizarAnoLetivo finalizarAnoLetivo;

	@PersistenceContext(unitName = "EscolarDS")
	private EntityManager em;

	public Aluno findById(EntityManager em, Long id) {
		return em.find(Aluno.class, id);
	}

	public Aluno findById(Long id) {
		Aluno al = em.find(Aluno.class, id);
		if (al.getIrmao1() != null) {
			al.getIrmao1().getAnoLetivo();
		}

		if (al.getIrmao2() != null) {
			al.getIrmao2().getAnoLetivo();
		}

		if (al.getIrmao3() != null) {
			al.getIrmao3().getAnoLetivo();
		}

		if (al.getIrmao4() != null) {
			al.getIrmao4().getAnoLetivo();
		}
		al.getBoletos().size();
		return al;
	}

	public Custo findHistoricoById(Long id) {
		return em.find(Custo.class, id);
	}

	public List<Aluno> findAll() {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aluno> criteria = cb.createQuery(Aluno.class);
			Root<Aluno> member = criteria.from(Aluno.class);
			// Swap criteria statements if you would like to try out type-safe
			// criteria queries, a new
			// feature in JPA 2.0
			// criteria.select(member).orderBy(cb.asc(member.get(Member_.name)));
			criteria.select(member).orderBy(cb.asc(member.get("nomeAluno")));
			return em.createQuery(criteria).getResultList();

		} catch (NoResultException nre) {
			return new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<Aluno> findAll(Serie serie, PerioddoEnum periodo) {
		List<Aluno> alunos = new ArrayList<>();
		alunos.addAll(find(serie, periodo));
		if (periodo != null && periodo != PerioddoEnum.INTEGRAL) {
			alunos.addAll(find(serie, PerioddoEnum.INTEGRAL));
		} else if (periodo != null && periodo.equals(PerioddoEnum.INTEGRAL)) {
			alunos.addAll(find(serie, PerioddoEnum.MANHA));
			alunos.addAll(find(serie, PerioddoEnum.TARDE));
		}
		return alunos;
	}

	public List<Aluno> find(Serie serie, PerioddoEnum periodo) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Aluno> criteria = cb.createQuery(Aluno.class);
			Root<Aluno> member = criteria.from(Aluno.class);

			Predicate whereSerie = null;
			Predicate wherePeriodo = null;

			StringBuilder sb = new StringBuilder();
			if (serie != null) {
				sb.append("A");
				whereSerie = cb.equal(member.get("serie"), serie);
			}

			if (periodo != null) {
				sb.append("B");
				wherePeriodo = cb.equal(member.get("periodo"), periodo);
			}

			switch (sb.toString()) {

			case "A":
				criteria.select(member).where(whereSerie);
				break;

			case "B":
				criteria.select(member).where(wherePeriodo);
				break;

			case "AB":
				criteria.select(member).where(whereSerie, wherePeriodo);
				break;
			default:
				break;
			}

			criteria.select(member).orderBy(cb.asc(member.get("nomeAluno")));
			return em.createQuery(criteria).getResultList();

		} catch (NoResultException nre) {
			return new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Aluno> findAlunoByCarro(long idCarro) {
		List<Aluno> alunos = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT al from  Aluno al ");
		sql.append("where 1=1 ");
		sql.append(" and al.carroLevaParaEscola.id =   ");
		sql.append(idCarro);
		sql.append(" or al.carroLevaParaEscolaTroca.id =   ");
		sql.append(idCarro);
		sql.append(" or al.carroPegaEscola.id =   ");
		sql.append(idCarro);
		sql.append(" or al.carroPegaEscolaTroca.id =   ");
		sql.append(idCarro);
		sql.append(" and al.removido = false ");
		sql.append(" and al.anoLetivo = ");
		sql.append(Constant.anoLetivoAtual);

		Query query = em.createQuery(sql.toString());
		alunos = query.getResultList();
		return alunos;

	}

	@SuppressWarnings("unchecked")
	public List<Aluno> findAlunoByCarroIDA(long idCarro, PerioddoEnum periodo) {
		List<Aluno> alunos = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT al from  Aluno al ");
		sql.append("where 1=1 ");
		if (periodo != null) {
			sql.append(" and al.periodo =  ");
			sql.append(periodo.ordinal());
		}
		sql.append(" and (");
		sql.append(" al.carroLevaParaEscola.id =   ");
		sql.append(idCarro);
		sql.append(" ) ");
		sql.append(" and al.removido = false ");
		sql.append(" and al.anoLetivo = ");
		sql.append(Constant.anoLetivoAtual);
		Query query = em.createQuery(sql.toString());

		alunos = query.getResultList();

		return alunos;

	}

	@SuppressWarnings("unchecked")
	public List<Aluno> findAlunoByCarroVolta(long idCarro, PerioddoEnum periodo) {
		List<Aluno> alunos = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT al from  Aluno al ");
		sql.append("where 1=1 ");
		if (periodo != null) {
			sql.append(" and al.periodo =  ");
			sql.append(periodo.ordinal());
		}
		sql.append(" and (");
		sql.append(" al.carroPegaEscola.id =   ");
		sql.append(idCarro);
		sql.append(" or al.carroPegaEscolaTroca.id =   ");
		sql.append(idCarro);
		sql.append(" ) ");
		sql.append(" and al.removido = false ");
		sql.append(" and al.anoLetivo = ");
		sql.append(Constant.anoLetivoAtual);

		sql.append(" and (al.idaVolta = 0 or al.idaVolta = 2 )");
		Query query = em.createQuery(sql.toString());
		alunos = query.getResultList();
		return alunos;

	}

	@SuppressWarnings("unchecked")
	public List<Aluno> findAlunoTurmaBytTurma(long idTurma) {
		List<Aluno> alunos = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT pt from  AlunoCarro pt ");
		sql.append("where pt.carro.id =   ");
		sql.append(idTurma);

		Query query = em.createQuery(sql.toString());

		try {
			List<AlunoCarro> alunosTurmas = query.getResultList();
			for (AlunoCarro profT : alunosTurmas) {
				Aluno pro = profT.getAluno();
				alunos.add(pro);
			}

		} catch (NoResultException noResultException) {

		} catch (Exception e) {
			e.printStackTrace();
		}

		return alunos;

	}

	@SuppressWarnings("unchecked")
	public List<Aluno> findAlunoTurmaBytTurma(List<Carro> turmas) {
		List<Aluno> alunos = new ArrayList<>();

		for (Carro turma : turmas) {
			alunos.addAll(findAlunoTurmaBytTurma(turma.getId()));
		}
		return alunos;

	}

	public Aluno save(Aluno aluno) {
		return saveAluno(aluno, true);
	}

	public void clone(Aluno aluno, Aluno user) {
		// user.setAdministrarParacetamol(aluno.isAdministrarParacetamol());
		user.setValorMensal(aluno.getValorMensal());
		user.setDataMatricula(aluno.getDataMatricula());
		// user.setAdministrarParacetamol(aluno.isAdministrarParacetamol());
		user.setCodigo(aluno.getCodigo());

		user.setNomeAvoHPaternoMae(aluno.getNomeAvoHPaternoMae());
		user.setAnuidade(aluno.getAnuidade() != null ? aluno.getAnuidade() : 0);
		user.setCpfMae(aluno.getCpfMae());
		user.setCpfPai(aluno.getCpfPai());
		user.setCpfResponsavel(aluno.getCpfResponsavel());
		user.setRgResponsavel(aluno.getRgResponsavel());
		user.setDataMatricula(aluno.getDataMatricula());
		user.setEmailMae(aluno.getEmailMae());
		user.setEmailPai(aluno.getEmailPai());
		user.setEmpresaTrabalhaMae(aluno.getEmpresaTrabalhaMae());
		user.setEmpresaTrabalhaPai(aluno.getEmpresaTrabalhaPai());
		user.setLogin(aluno.getLogin());
		user.setNaturalidadeMae(aluno.getNaturalidadeMae());
		user.setNaturalidadePai(aluno.getNaturalidadePai());
		user.setNomeAvoHPaternoMae(aluno.getNomeAvoHPaternoMae());
		user.setNomeAvoHPaternoPai(aluno.getNomeAvoHPaternoPai());
		user.setNomeAvoPaternoMae(aluno.getNomeAvoPaternoMae());
		user.setNomeAvoPaternoPai(aluno.getNomeAvoPaternoPai());
		user.setNomeMaeAluno(aluno.getNomeMaeAluno());
		user.setNomePaiAluno(aluno.getNomePaiAluno());
		user.setNomeResponsavel(aluno.getNomeResponsavel());
		user.setNumeroParcelas(aluno.getNumeroParcelas());
		user.setObservacaoSecretaria(aluno.getObservacaoSecretaria());
		user.setValorMensal(aluno.getValorMensal());
		user.setTelefoneResidencialPai(aluno.getTelefoneResidencialPai());
		user.setRgMae(aluno.getRgMae());
		user.setRgPai(aluno.getRgPai());
		user.setSenha(aluno.getSenha());
		user.setTelefone(aluno.getTelefone());
		user.setEscola(aluno.getEscola());

		user.setNomePaiResponsavel(aluno.getNomePaiResponsavel());
		user.setNomeMaeResponsavel(aluno.getNomeMaeResponsavel());
		user.setAnuidade(aluno.getAnuidade());
		user.setBairro(aluno.getBairro());
		user.setCep(aluno.getCep());
		user.setCidade(aluno.getCidade());
		user.setCpfMae(aluno.getCpfMae());
		user.setCpfPai(aluno.getCpfPai());
		user.setEndereco(aluno.getEndereco());
		user.setRgResponsavel(aluno.getRgResponsavel());

		if (aluno.getRemovido() == null) {
			user.setRemovido(false);
		} else {
			user.setRemovido(aluno.getRemovido());
		}

	}

	public String removeCaracteresEspeciais(String texto){
		texto = texto.replaceAll("[^aA-zZ-Z1-9 ]", "");
		return texto;
	}

	private Aluno saveAluno(Aluno aluno, boolean saveBrother) {
		Aluno user = null;
		try {

			log.info("Registering " + aluno.getNomeAluno());

			if (aluno.getId() != null && aluno.getId() != 0L) {
				user = findById(aluno.getId());
			} else {
				user = new Aluno();
				user.setAnoLetivo(Constant.anoLetivoAtual);
			}
			user.setIdaVolta(aluno.getIdaVolta());
			// user.setAdministrarParacetamol(aluno.isAdministrarParacetamol());
			user.setNomeAluno(aluno.getNomeAluno().toUpperCase());
			user.setPeriodo(aluno.getPeriodo());
			user.setSerie(aluno.getSerie());
			if (aluno.getEndereco() != null) {
				user.setEndereco(removeCaracteresEspeciais(aluno.getEndereco()));
			}
			user.setBairro(aluno.getBairro());
			user.setCep(aluno.getCep());
			user.setCidade(aluno.getCidade());
			// user.setNacionalidade(aluno.getNacionalidade());
			user.setValorMensal(aluno.getValorMensal());
			user.setDataNascimento(aluno.getDataNascimento());
			user.setDataMatricula(aluno.getDataMatricula());
			// user.setAdministrarParacetamol(aluno.isAdministrarParacetamol());
			user.setCodigo(aluno.getCodigo());

			user.setCodigo(aluno.getCodigo());
			user.setNomeAvoHPaternoMae(aluno.getNomeAvoHPaternoMae());
			user.setAnuidade(aluno.getAnuidade() != null ? aluno.getAnuidade() : 0);
			user.setBairro(aluno.getBairro());
			user.setCep(aluno.getCep());
			user.setCidade(aluno.getCidade());
			user.setCpfMae(aluno.getCpfMae());
			user.setCpfPai(aluno.getCpfPai());
			if(aluno.getCpfResponsavel() != null){
				user.setCpfResponsavel(aluno.getCpfResponsavel().replace(".", "").replace("-", ""));
			}
			user.setRgResponsavel(aluno.getRgResponsavel());
			user.setDataMatricula(aluno.getDataMatricula());
			user.setEmailMae(aluno.getEmailMae());
			user.setEmailPai(aluno.getEmailPai());
			user.setEmpresaTrabalhaMae(aluno.getEmpresaTrabalhaMae());
			user.setEmpresaTrabalhaPai(aluno.getEmpresaTrabalhaPai());
			user.setLogin(aluno.getLogin());
			user.setNaturalidadeMae(aluno.getNaturalidadeMae());
			user.setNaturalidadePai(aluno.getNaturalidadePai());
			user.setNomeAvoHPaternoMae(aluno.getNomeAvoHPaternoMae());
			user.setNomeAvoHPaternoPai(aluno.getNomeAvoHPaternoPai());
			user.setNomeAvoPaternoMae(aluno.getNomeAvoPaternoMae());
			user.setNomeAvoPaternoPai(aluno.getNomeAvoPaternoPai());
			if (aluno.getNomeMaeAluno() != null) {
				user.setNomeMaeAluno(aluno.getNomeMaeAluno().toUpperCase());
			}
			if (aluno.getNomePaiAluno() != null) {
				user.setNomePaiAluno(aluno.getNomePaiAluno().toUpperCase());
			}
			if (aluno.getNomeResponsavel() != null) {
				user.setNomeResponsavel(aluno.getNomeResponsavel().toUpperCase());
			}
			user.setNumeroParcelas(aluno.getNumeroParcelas());
			user.setObservacaoSecretaria(aluno.getObservacaoSecretaria());
			// if(saveBrother){
			user.setValorMensal(aluno.getValorMensal());

			// }else{
			// user.setValorMensal(0);
			// }
			user.setTelefoneResidencialPai(aluno.getTelefoneResidencialPai());
			user.setRgMae(aluno.getRgMae());
			user.setRgPai(aluno.getRgPai());
			user.setSenha(aluno.getSenha());
			user.setTelefone(aluno.getTelefone());
			user.setEscola(aluno.getEscola());
			if (aluno.getNomePaiResponsavel() != null) {
				user.setNomePaiResponsavel(aluno.getNomePaiResponsavel().toUpperCase());
			}
			if (aluno.getNomeMaeResponsavel() != null) {
				user.setNomeMaeResponsavel(aluno.getNomeMaeResponsavel().toUpperCase());
			}
			user.setContatoEmail1(aluno.getContatoEmail1());
			user.setContatoEmail2(aluno.getContatoEmail2());
			user.setContatoNome1(aluno.getContatoNome1());
			user.setContatoNome2(aluno.getContatoNome2());
			user.setContatoNome3(aluno.getContatoNome3());
			user.setContatoNome4(aluno.getContatoNome4());
			user.setContatoNome5(aluno.getContatoNome5());

			user.setContatoTelefone1(aluno.getContatoTelefone1());
			user.setContatoTelefone2(aluno.getContatoTelefone2());
			user.setContatoTelefone3(aluno.getContatoTelefone3());
			user.setContatoTelefone4(aluno.getContatoTelefone4());
			user.setContatoTelefone5(aluno.getContatoTelefone5());

			user.setDataCancelamento(aluno.getDataCancelamento());
			user.setCnabEnviado(aluno.getCnabEnviado());
			
			if (aluno.getRemovido() == null) {
				user.setRemovido(false);
			} else {
				user.setRemovido(aluno.getRemovido());
			}

			user.setTrocaIDA(aluno.isTrocaIDA());
			user.setTrocaVolta(aluno.isTrocaVolta());
			user.setCarroLevaParaEscola(aluno.getCarroLevaParaEscola());
			user.setCarroLevaParaEscolaTroca(aluno.getCarroLevaParaEscolaTroca());
			user.setCarroPegaEscola(aluno.getCarroPegaEscola());
			user.setCarroPegaEscolaTroca(aluno.getCarroPegaEscolaTroca());

			em.persist(user);

			if (user.getDataNascimento() != null) {
				Evento aniversario = eventoService.findByCodigo(user.getCodigo());

				if (aniversario == null) {
					aniversario = new Evento();
				}

				aniversario.setDataFim(finalizarAnoLetivo.mudarAno(user.getDataNascimento(), Constant.anoLetivoAtual));
				aniversario
						.setDataInicio(finalizarAnoLetivo.mudarAno(user.getDataNascimento(), Constant.anoLetivoAtual));
				aniversario.setCodigo(user.getCodigo());
				aniversario.setDescricao(" Aniversário do(a) aluno(a) " + user.getNomeAluno());
				aniversario.setNome(
						" Aniversário do(a) aluno(a) " + user.getNomeAluno() + " " + aluno.getSerie().getName());
				eventoService.save(aniversario);

			}

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

		List<Boleto> boletos = null;
		if (saveBrother) {
			salvarIrmaos(user, aluno);
			if (aluno.getId() == null || aluno.getId() == 0L) {
				boletos = gerarBoletos(user);
			}
		}
		if (aluno.getId() == null || aluno.getId() == 0L) {
			user.setBoletos(boletos);
		}

		return user;

	}

	public List<Boleto> gerarBoletos(Aluno user) {
		List<Boleto> boletos = new ArrayList<>();
		int i = 12 - user.getNumeroParcelas();
		long nossoNumero = getProximoNossoNumero();
		while (i < 12) {
			Boleto boleto = new Boleto();
			Calendar vencimento = Calendar.getInstance();
			vencimento.set(Calendar.DAY_OF_MONTH, 10);
			vencimento.set(Calendar.MONTH, i);
			vencimento.set(Calendar.YEAR, Constant.anoLetivoAtual);
			vencimento.set(Calendar.HOUR, 0);
			vencimento.set(Calendar.MINUTE, 0);
			vencimento.set(Calendar.SECOND, 0);
			boleto.setVencimento(vencimento.getTime());
			boleto.setEmissao(new Date());
			boleto.setValorNominal(user.getValorMensal());
			boleto.setPagador(user);
			boleto.setNossoNumero(nossoNumero);
			em.persist(boleto);
			nossoNumero++;
			boletos.add(boleto);

			i++;
		}
		return boletos;
	}

	public void gerarBoletos() {
		List<Aluno> alunos = findAll();
		for (Aluno al : alunos) {
			if (al.getRemovido() != null && !al.getRemovido()) {
				if (al.getBoletos() == null || al.getBoletos().size() == 0) {
					if((al.getAnoLetivo() == Constant.anoLetivoAtual) || (al.getRematricular() != null && al.getRematricular()) ){
						if (!irmaoJaTemBoleto(al)) {
							List<Boleto> boletos = gerarBoletos(al);
							al.setBoletos(boletos);
							em.persist(al);
						}	
					}
					
				} else if (al.getBoletos() != null && al.getBoletos().size() > 0) {
					List<Boleto> boletos = al.getBoletos();
					for (Boleto b : boletos) {
						if(b.getAlteracaoBoletoManual() == null || !b.getAlteracaoBoletoManual()){
							b.setValorNominal(al.getValorMensal());
						}
						em.merge(b);
					}
				}
			}
		}
	}

	private boolean irmaoJaTemBoleto(Aluno aluno) {
		Aluno irmao1 = aluno.getIrmao1();
		Aluno irmao2 = aluno.getIrmao2();
		Aluno irmao3 = aluno.getIrmao3();
		Aluno irmao4 = aluno.getIrmao4();

		if (irmao1 != null) {
			if (irmao1.getBoletos() != null && irmao1.getBoletos().size() > 0) {
				return true;
			}
		}

		if (irmao2 != null) {
			if (irmao2.getBoletos() != null && irmao2.getBoletos().size() > 0) {
				return true;
			}
		}

		if (irmao3 != null) {
			if (irmao3.getBoletos() != null && irmao3.getBoletos().size() > 0) {
				return true;
			}
		}

		if (irmao4 != null) {
			if (irmao4.getBoletos() != null && irmao4.getBoletos().size() > 0) {
				return true;
			}
		}

		return false;
	}

	public List<Boleto> gerarBoletos(Aluno user, boolean setUser) {
		user = findById(user.getId());
		List<Boleto> boletos = gerarBoletos(user);
		user.setBoletos(boletos);
		em.persist(user);
		return boletos;
	}

	private void salvarIrmaos(Aluno aluno, Aluno unMerge) {
		Aluno irmao1 = unMerge.getIrmao1();
		boolean tem1Irmao = irmao1 != null ? true : false;
		Aluno irmao2 = unMerge.getIrmao2();
		boolean tem2Irmao = irmao2 != null ? true : false;
		Aluno irmao3 = unMerge.getIrmao3();
		boolean tem3Irmao = irmao3 != null ? true : false;
		Aluno irmao4 = unMerge.getIrmao4();
		boolean tem4Irmao = irmao4 != null ? true : false;

		if (tem1Irmao) {
			clone(aluno, irmao1);
			irmao1 = saveAluno(irmao1, false);

			aluno.setIrmao1(irmao1);
			irmao1.setIrmao1(aluno);

			if (tem2Irmao) {
				clone(aluno, irmao2);
				irmao2 = saveAluno(irmao2, false);
				aluno.setIrmao2(irmao2);
				irmao1.setIrmao1(aluno);
				irmao1.setIrmao2(irmao2);
				irmao2.setIrmao1(aluno);
				irmao2.setIrmao2(irmao1);
			}
			if (tem3Irmao) {
				clone(aluno, irmao3);
				irmao3 = saveAluno(irmao3, false);
				aluno.setIrmao3(irmao3);
				irmao3.setIrmao1(aluno);
				irmao3.setIrmao2(irmao1);
				irmao3.setIrmao3(irmao2);

				irmao1.setIrmao1(aluno);
				irmao1.setIrmao2(irmao2);
				irmao1.setIrmao3(irmao3);
				irmao2.setIrmao1(aluno);
				irmao2.setIrmao2(irmao1);
				irmao2.setIrmao3(irmao3);
			}
			if (tem4Irmao) {
				clone(aluno, irmao4);
				irmao4 = saveAluno(irmao4, false);
				aluno.setIrmao4(irmao4);
				irmao4.setIrmao1(aluno);
				irmao4.setIrmao2(irmao1);
				irmao4.setIrmao3(irmao2);
				irmao4.setIrmao4(irmao3);

				irmao1.setIrmao1(aluno);
				irmao1.setIrmao2(irmao2);
				irmao1.setIrmao3(irmao3);
				irmao1.setIrmao4(irmao4);
				irmao2.setIrmao1(aluno);
				irmao2.setIrmao2(irmao1);
				irmao2.setIrmao3(irmao3);
				irmao2.setIrmao4(irmao4);

				irmao3.setIrmao1(aluno);
				irmao3.setIrmao2(irmao1);
				irmao3.setIrmao3(irmao2);
				irmao3.setIrmao4(irmao4);
			}
		}

		if (tem2Irmao) {
			clone(aluno, irmao2);
			irmao2 = saveAluno(irmao2, false);
			aluno.setIrmao2(irmao2);
			irmao2.setIrmao1(aluno);

			if (tem3Irmao) {
				clone(aluno, irmao3);
				irmao3 = saveAluno(irmao3, false);
				aluno.setIrmao3(irmao3);
				irmao3.setIrmao1(aluno);
				irmao3.setIrmao3(irmao2);

				irmao2.setIrmao1(aluno);
				irmao2.setIrmao3(irmao3);
			}
			if (tem4Irmao) {
				clone(aluno, irmao4);
				irmao4 = saveAluno(irmao4, false);
				aluno.setIrmao4(irmao4);
				irmao4.setIrmao1(aluno);
				irmao4.setIrmao3(irmao2);
				irmao4.setIrmao4(irmao3);

				irmao2.setIrmao1(aluno);
				irmao2.setIrmao3(irmao3);
				irmao2.setIrmao4(irmao4);

				irmao3.setIrmao1(aluno);
				irmao3.setIrmao3(irmao2);
				irmao3.setIrmao4(irmao4);
			}

		}
		if (tem3Irmao) {
			clone(aluno, irmao3);
			irmao3 = saveAluno(irmao3, false);
			aluno.setIrmao3(irmao3);
			irmao3.setIrmao1(aluno);

			if (tem4Irmao) {
				clone(aluno, irmao4);
				irmao4 = saveAluno(irmao4, false);
				aluno.setIrmao4(irmao4);
				irmao4.setIrmao1(aluno);
				irmao4.setIrmao4(irmao3);
				irmao3.setIrmao1(aluno);
				irmao3.setIrmao4(irmao4);
			}
		}

		if (tem4Irmao) {
			clone(aluno, irmao4);
			irmao4 = saveAluno(irmao4, false);
			aluno.setIrmao4(irmao4);
			irmao4.setIrmao1(aluno);
		}
	}

	public String remover(Long idAluno) {
		Aluno al = findById(idAluno);
		al.setRemovido(true);
		al.setDataCancelamento(new Date());
		em.persist(al);
		return "ok";
	}

	public String restaurar(Long idAluno) {
		Aluno al = findById(idAluno);
		al.setRemovido(false);
		em.persist(al);
		return "ok";
	}

	public void saveAlunoTurma(List<Aluno> target, Carro turma) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT at from  AlunoCarro at ");
		sql.append("where at.turma.id =   ");
		sql.append(turma.getId());

		Query query = em.createQuery(sql.toString());

		try {
			List<AlunoCarro> alunosTurmas = query.getResultList();
			for (AlunoCarro profT : alunosTurmas) {
				em.remove(profT);
				em.flush();
			}

			for (Aluno prof : target) {
				AlunoCarro pt = new AlunoCarro();
				pt.setAnoLetivo(Constant.anoLetivoAtual);
				pt.setAluno(prof);
				pt.setCarro(em.find(Carro.class, turma.getId()));
				em.persist(pt);
			}

		} catch (NoResultException noResultException) {

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Funcionario getProfessor(Long idAluno) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT at.turma from  AlunoCarro at ");
		sql.append("where 1 = 1");
		sql.append("and  at.aluno.id = ");
		sql.append(idAluno);
		Query query = em.createQuery(sql.toString());
		Carro t = (Carro) query.getResultList().get(0);

		StringBuilder sql2 = new StringBuilder();
		sql2.append("SELECT pt.professor from  FuncionarioCarro pt ");
		sql2.append("where 1 = 1");
		sql2.append("and  pt.turma.id = ");
		sql2.append(t.getId());
		sql2.append("and  pt.professor.especialidade = 0");
		Query query2 = em.createQuery(sql2.toString());
		return (Funcionario) query2.getResultList().get(0);

	}

	public float getNota(Long idAluno, DisciplinaEnum disciplina, BimestreEnum bimestre, boolean recupecacao) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT avg(av.nota) from  AlunoAvaliacao av ");
		sql.append("where 1 = 1");
		sql.append(" and  av.aluno.id = ");
		sql.append(idAluno);
		sql.append(" and  av.avaliacao.disciplina = ");
		sql.append(disciplina.ordinal());
		if (bimestre != null) {
			sql.append(" and  av.avaliacao.bimestre = ");
			sql.append(bimestre.ordinal());
		}
		sql.append(" and  av.avaliacao.recuperacao = ");
		sql.append(recupecacao);
		Query query = em.createQuery(sql.toString());

		Object valor = query.getSingleResult();
		if (valor != null) {
			valor = (double) valor;
		} else {
			return 0;
		}

		return Float.parseFloat(String.valueOf(valor));
	}

	public float getNota(Long idAluno, DisciplinaEnum disciplina, boolean recuperacao, boolean provafinal) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT avg(av.nota) from  AlunoAvaliacao av ");
		sql.append("where 1 = 1");
		sql.append(" and  av.aluno.id = ");
		sql.append(idAluno);
		sql.append(" and  av.avaliacao.disciplina = ");
		sql.append(disciplina.ordinal());
		sql.append(" and  av.avaliacao.provaFinal = ");
		sql.append(provafinal);
		sql.append(" and  av.avaliacao.recuperacao = ");
		sql.append(recuperacao);
		Query query = em.createQuery(sql.toString());

		Object valor = query.getSingleResult();
		if (valor != null) {
			valor = (double) valor;
		} else {
			return 0;
		}

		return Float.parseFloat(String.valueOf(valor));
	}

	public void saveAlunoRota(ObjetoRota objetoRota) {
		log.info("Registering " + objetoRota.getNome());
		ObjetoRota obj;

		if (objetoRota.getId() != null && objetoRota.getId() != 0L) {
			obj = em.find(ObjetoRota.class, objetoRota.getId());
		} else {
			obj = new ObjetoRota();
		}
		if (objetoRota.getAluno() != null) {
			obj.setAluno(findById(objetoRota.getAluno().getId()));
		}
		obj.setNome(objetoRota.getNome());
		obj.setEscola(objetoRota.getEscola());
		obj.setPosicao(objetoRota.getPosicao() - 1);
		obj.setQuantidadeAlunos(objetoRota.getQuantidadeAlunos());
		obj.setPegarEntregar(objetoRota.getPegarEntregar());

		obj.setIdCarro(objetoRota.getIdCarro());
		obj.setPeriodo(objetoRota.getPeriodo());
		obj.setEscola(objetoRota.getEscola());
		obj.setDescricao(objetoRota.getDescricao());
		obj.setIdCarroAlvo(objetoRota.getIdCarroAlvo());
		atualizarIndices(obj);
		if (obj.getId() == null) {
			em.persist(obj);
		} else {
			em.merge(obj);
		}

	}

	private void atualizarIndices(ObjetoRota obj) {
		List<ObjetoRota> objs = getObjetosRotasSeguintes(obj);
		if (objs != null && !objs.isEmpty() && objs.get(0) != null && objs.get(0).getPosicao() == obj.getPosicao()) {
			int indice = 0;
			for (ObjetoRota ob : objs) {
				ob.setPosicao(obj.getPosicao() + indice);
				indice++;
				em.merge(ob);
			}
		}
	}

	private List<ObjetoRota> getObjetosRotasSeguintes(ObjetoRota obj) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT obj from ObjetoRota obj ");
		sql.append("where 1 = 1 ");
		sql.append("and obj.posicao>=");
		sql.append(obj.getPosicao());
		sql.append(" and ");
		sql.append(" obj.periodo = ");
		sql.append(obj.getPeriodo().ordinal());
		sql.append(" and ");
		sql.append(" obj.idCarro = ");
		sql.append(obj.getIdCarro());
		sql.append(" order by obj.posicao ");
		Query query = em.createQuery(sql.toString());
		List<ObjetoRota> t = new ArrayList<>();
		try {
			t = (List<ObjetoRota>) query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return t;
		}

		return t;

	}

	@SuppressWarnings("unchecked")
	public List<Aluno> find(int first, int size, String orderBy, String order, Map<String, Object> filtros) {
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
				// cq.where(pred);
			}

			cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
			cq.orderBy((order.equals("asc") ? cb.asc(member.get(orderBy)) : cb.desc(member.get(orderBy))));
			Query q = em.createQuery(criteria);
			q.setFirstResult(first);
			q.setMaxResults(size);
			return (List<Aluno>) q.getResultList();

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

	/*
	 * public long sum(Map<String, Object> filtros) { try { CriteriaBuilder cb =
	 * em.getCriteriaBuilder(); CriteriaQuery<Double> countQuery =
	 * cb.createQuery(Double.class); Root<Aluno> member =
	 * countQuery.from(Aluno.class);
	 * countQuery.select(cb.sumAsDouble(member.get("valorMensal")));
	 * 
	 * final List<Predicate> predicates = new ArrayList<Predicate>(); if
	 * (filtros != null) { for (Map.Entry<String, Object> entry :
	 * filtros.entrySet()) {
	 * 
	 * Predicate pred = cb.and(); if (entry.getValue() instanceof String) { pred
	 * = cb.and(pred, cb.like(member.<String> get(entry.getKey()), "%" +
	 * entry.getValue() + "%")); } else { pred =
	 * cb.equal(member.get(entry.getKey()), entry.getValue()); }
	 * predicates.add(pred);
	 * 
	 * } countQuery.where(cb.and(predicates.toArray(new
	 * Predicate[predicates.size()]))); }
	 * 
	 * Query q = em.createQuery(countQuery); return (long) q.getSingleResult();
	 * 
	 * } catch (NoResultException nre) { return 0; } catch (Exception e) {
	 * e.printStackTrace(); return 0; } }
	 */

	public List<Custo> getHistoricoAluno(Aluno aluno) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Custo> criteria = cb.createQuery(Custo.class);
			Root<Custo> member = criteria.from(Custo.class);
			CriteriaQuery cq = criteria.select(member);

			Predicate pred = cb.and();
			pred = cb.equal(member.get("aluno"), aluno);
			cq.where(pred);

			cq.orderBy(cb.desc(member.get("ano")));
			Query q = em.createQuery(criteria);
			return (List<Custo>) q.getResultList();

		} catch (NoResultException nre) {
			return new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public void removerHistorico(long idHistorico) {
		em.remove(findHistoricoById(idHistorico));
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

	public List<ObjetoRota> findObjetosRota(List<Aluno> alunos) {
		// TODO aqui ta loco
		List<ObjetoRota> ors = new ArrayList<>();
		try {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ObjetoRota> criteria = cb.createQuery(ObjetoRota.class);
			Root<ObjetoRota> member = criteria.from(ObjetoRota.class);
			CriteriaQuery cq = criteria.select(member);

			final List<Predicate> predicates = new ArrayList<Predicate>();

			for (Aluno al : alunos) {
				Predicate pred = cb.or();

				pred = cb.equal(member.get("aluno").get("id"), al.getId());
				predicates.add(pred);

			}

			try {
				cq.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
				cq.orderBy(cb.asc(member.get("posicao")));
				Query q = em.createQuery(criteria);
				ors.addAll((List<ObjetoRota>) q.getResultList());
			} catch (Exception e) {
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ors;
	}

	public List<ObjetoRota> findObjetosRota(Long idCarro, PerioddoEnum periodo) {
		List<ObjetoRota> ors = new ArrayList<>();
		try {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ObjetoRota> criteria = cb.createQuery(ObjetoRota.class);
			Root<ObjetoRota> member = criteria.from(ObjetoRota.class);
			CriteriaQuery cq = criteria.select(member);

			final List<Predicate> predicates = new ArrayList<Predicate>();

			Predicate pred = cb.equal(member.get("idCarro"), idCarro);
			predicates.add(pred);

			Predicate pred2 = cb.equal(member.get("periodo"), periodo.ordinal());
			predicates.add(pred2);

			try {
				cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
				cq.orderBy(cb.asc(member.get("posicao")));
				Query q = em.createQuery(criteria);
				ors.addAll((List<ObjetoRota>) q.getResultList());
			} catch (Exception e) {
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ors;
	}

	public ObjetoRota findObjetosRota(Long idCarro, PerioddoEnum periodo, Long idCarroAlvo,
			PegarEntregarEnun pegarEntregarEnun) {
		try {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ObjetoRota> criteria = cb.createQuery(ObjetoRota.class);
			Root<ObjetoRota> member = criteria.from(ObjetoRota.class);
			CriteriaQuery cq = criteria.select(member);

			final List<Predicate> predicates = new ArrayList<Predicate>();

			Predicate pred = cb.equal(member.get("idCarro"), idCarro);
			predicates.add(pred);

			Predicate pred2 = cb.equal(member.get("periodo"), periodo.ordinal());
			predicates.add(pred2);

			Predicate pred3 = cb.equal(member.get("idCarroAlvo"), idCarroAlvo);
			predicates.add(pred3);

			Predicate pred4 = cb.equal(member.get("pegarEntregar"), pegarEntregarEnun.ordinal());
			predicates.add(pred4);

			try {
				cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
				cq.orderBy(cb.asc(member.get("posicao")));
				Query q = em.createQuery(criteria);
				return (ObjetoRota) q.getSingleResult();
			} catch (Exception e) {
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Aluno> findAlunos(List<ObjetoRota> objRts) {
		List<Aluno> ors = new ArrayList<>();
		try {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ObjetoRota> criteria = cb.createQuery(ObjetoRota.class);
			Root<ObjetoRota> member = criteria.from(ObjetoRota.class);
			CriteriaQuery cq = criteria.select(member);

			Predicate pred = cb.and();

			Query q = em.createQuery(criteria);
			for (ObjetoRota al : objRts) {
				pred = cb.equal(member.get("aluno").get("id"), al.getAluno().getId());
				cq.where(pred);
			}
			try {
				List<ObjetoRota> objR = (List<ObjetoRota>) q.getResultList();
				for (ObjetoRota ob : objR) {
					ors.add(ob.getAluno());
				}
			} catch (Exception e) {
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ors;
	}

	public List<Aluno> findAlunos(Long idCarro, PerioddoEnum periodo, EscolaEnum escola,
			PegarEntregarEnun pegarEntregar) {
		List<Aluno> ors = new ArrayList<>();
		try {
			final List<Predicate> predicates = new ArrayList<Predicate>();

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ObjetoRota> criteria = cb.createQuery(ObjetoRota.class);
			Root<ObjetoRota> member = criteria.from(ObjetoRota.class);
			CriteriaQuery cq = criteria.select(member);

			Predicate pred = cb.equal(member.get("idCarro"), idCarro);
			predicates.add(pred);

			Predicate pred2 = cb.equal(member.get("periodo"), periodo.ordinal());
			predicates.add(pred2);

			Predicate pred3 = cb.equal(member.get("aluno").get("escola"), escola.ordinal());
			predicates.add(pred3);

			Predicate pred4 = cb.equal(member.get("pegarEntregar"), pegarEntregar);
			predicates.add(pred4);

			cq.where(pred);
			try {
				cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
				Query q = em.createQuery(criteria);

				List<ObjetoRota> objR = (List<ObjetoRota>) q.getResultList();
				for (ObjetoRota ob : objR) {
					ors.add(ob.getAluno());
				}
			} catch (Exception e) {
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ors;
	}

	public void removerAlunoRota(Long idAluno) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ObjetoRota> criteria = cb.createQuery(ObjetoRota.class);
			Root<ObjetoRota> member = criteria.from(ObjetoRota.class);
			CriteriaQuery cq = criteria.select(member);

			Predicate pred = cb.and();
			pred = cb.equal(member.get("aluno").get("id"), idAluno);
			cq.where(pred);

			Query q = em.createQuery(criteria);
			List<ObjetoRota> objRotas = (List<ObjetoRota>) q.getResultList();

			for (ObjetoRota obj : objRotas) {
				em.remove(obj);
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void removerObjetoRota(Long idAluno) {
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ObjetoRota> criteria = cb.createQuery(ObjetoRota.class);
			Root<ObjetoRota> member = criteria.from(ObjetoRota.class);
			CriteriaQuery cq = criteria.select(member);

			Predicate pred = cb.and();
			pred = cb.equal(member.get("id"), idAluno);
			cq.where(pred);

			Query q = em.createQuery(criteria);
			List<ObjetoRota> objRotas = (List<ObjetoRota>) q.getResultList();

			for (ObjetoRota obj : objRotas) {
				em.remove(obj);
			}

		} catch (NoResultException nre) {
			nre.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<Carro> findCarrosTroca(Carro carro, PerioddoEnum periodo, Boolean pego) {
		String sql = "";
		if (periodo.equals(PerioddoEnum.MANHA)) {
			if (pego) {
				sql = SQLs.getSQLCarrosTrocaManhaPego();
			} else {
				sql = SQLs.getSQLCarrosTrocaManha();
			}
		} else {
			if (pego) {
				sql = SQLs.getSQLCarrosTrocaTardePego();
			} else {
				sql = SQLs.getSQLCarrosTrocaTarde();
			}
		}

		sql = sql.replace("?1", carro.getId().toString());
		sql = sql.replace("?2", String.valueOf(periodo.ordinal()));

		Query query = em.createQuery(sql);
		List<Carro> t = new ArrayList<>();
		try {
			t = (List<Carro>) query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return t;
		}

		return t;
	}

	public List<Carro> findCarrosTrocaMeioDia(Carro carro, PerioddoEnum periodo, Boolean pego) {
		String sql = SQLs.getSQLTrocaMeioDia();
		sql = sql.replace("?1", carro.getId().toString());
		sql = sql.replace("?2", String.valueOf(periodo.TARDE.ordinal()));
		Query query = em.createQuery(sql.toString());
		List<Carro> t = new ArrayList<>();
		try {
			t = (List<Carro>) query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			return t;
		}
		return t;
	}

	public List<Aluno> findAlunosVoltam(Carro carro, Carro carroTroca, PerioddoEnum periodo) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.carroPegaEscola.id= ");
		sql.append(carro.getId());
		sql.append(" and al.carroPegaEscolaTroca.id= ");
		sql.append(carroTroca.getId());
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append(periodo.ordinal());
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");
		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();

		return t;
	}

	public List<Aluno> findAlunosVao(Carro carro, Carro carroTroca, PerioddoEnum periodo, Boolean pego) {
		String sql = "";
		if (pego) {
			sql = SQLs.getSQLAlunosVaoPego();
		} else {
			sql = SQLs.getSQLAlunosVao();
		}

		sql = sql.replace("?1", carro.getId().toString());
		sql = sql.replace("?2", carroTroca.getId().toString());
		sql = sql.replace("?3", String.valueOf(periodo.ordinal()));

		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();

		return t;
	}

	public List<Aluno> findAlunosTrocaMeioDia(Carro carro, Carro carroTroca, PerioddoEnum periodo) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.carroPegaEscola.id= ");
		sql.append(carro.getId());
		sql.append(" and al.carroPegaEscolaTroca.id= ");
		sql.append(carroTroca.getId());
		sql.append(" and");
		sql.append("  al.periodo = ");
		sql.append(PerioddoEnum.MANHA.ordinal());
		sql.append(" or (");

		sql.append(" al.carroLevaParaEscola.id= ");
		sql.append(carroTroca.getId());
		sql.append(" and al.carroLevaParaEscolaTroca.id= ");
		sql.append(carro.getId());
		sql.append(" and");
		sql.append("  al.periodo = ");
		sql.append(PerioddoEnum.TARDE.ordinal());

		sql.append(" )");

		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();

		return t;
	}

	public List<Aluno> findAlunoPegoOutroCarroTarde(PerioddoEnum periodo, Carro carro) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.trocaVolta= ");
		sql.append(true);
		sql.append(" and al.carroPegaEscolaTroca.id= ");
		sql.append(carro.getId());
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append(periodo.ordinal());
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");
		sql.append(" and ( al.idaVolta = 0 or al.idaVolta = 2)");

		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();

		return t;
	}

	public List<Aluno> findAlunoPegaEscolaTarde(EscolaEnum escola, Carro carro) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.escola= ");
		sql.append(escola.ordinal());
		sql.append(" and al.carroPegaEscola.id= ");
		sql.append(carro.getId());
		sql.append(" and ( al.idaVolta = 0 or al.idaVolta = 2)");
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append(PerioddoEnum.TARDE.ordinal());
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");

		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();

		return t;
	}

	public List<Aluno> findAlunoPegaEscolaManha(EscolaEnum escola, Carro carro) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.escola= ");
		sql.append(escola.ordinal());
		sql.append(" and ( (al.carroLevaParaEscola.id= ");
		sql.append(carro.getId());
		sql.append(" and al.trocaIDA = false)");
		sql.append(" or al.carroLevaParaEscolaTroca.id= ");
		sql.append(carro.getId());
		sql.append("  ) and ( al.idaVolta = 0 or al.idaVolta = 2)");
		sql.append(" and");
		sql.append(" ( al.periodo = ");
		sql.append(PerioddoEnum.MANHA.ordinal());
		sql.append(" or al.periodo = ");
		sql.append(PerioddoEnum.INTEGRAL.ordinal());
		sql.append(" )");

		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();

		return t;
	}

	public List<Aluno> findAlunoPegaEscolaMeioDia(EscolaEnum escola, Carro carro) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.escola= ");
		sql.append(escola.ordinal());
		sql.append(" and al.carroPegaEscola.id= ");
		sql.append(carro.getId());

		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();

		return t;
	}

	public List<Aluno> findAlunoLevaEscolaMeioDia(EscolaEnum escola, Carro carro) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.escola= ");
		sql.append(escola.ordinal());
		sql.append(" and ( (al.carroLevaParaEscola.id= ");
		sql.append(carro.getId());
		sql.append(" and al.trocaIDA = false)");
		sql.append(" or al.carroLevaParaEscolaTroca.id= ");
		sql.append(carro.getId());
		sql.append("  ) and ( al.idaVolta = 0 or al.idaVolta = 1)");
		sql.append(" and");
		sql.append("  al.periodo = ");
		sql.append(PerioddoEnum.TARDE.ordinal());

		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();
		return t;
	}

	public List<Aluno> findAlunoTrocaNoite(Long idCarro, Long idCarroTroca) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al)  from Aluno al ");
		sql.append(" where 1 = 1");
		sql.append(" and al.carroPegaEscola.id= ");
		sql.append(idCarro);
		sql.append(" and al.trocaVolta = true)");
		sql.append("  and al.carroPegaEscolaTroca.id");
		sql.append(idCarroTroca);

		Query query = em.createQuery(sql.toString());
		List<Aluno> t = (List<Aluno>) query.getResultList();

		return t;
	}

	public Long getProximoCodigo() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT max(al.codigo)  from Aluno al ");

		Query query = em.createQuery(sql.toString());
		String codigo = (String) query.getSingleResult();
		return Long.parseLong(codigo) + 1;
	}

	public Long getProximoNossoNumero() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT max(bol.nossoNumero) from Boleto bol ");

		Query query = em.createQuery(sql.toString());
		Long codigo = null;
		try {
			codigo = (Long) query.getSingleResult();

		} catch (Exception e) {
			codigo = 10000L;
		}
		if (codigo == null) {
			codigo = 10000L;
		}
		if (codigo < 10000) {
			codigo = 10000L;
		}

		return codigo + 1;
	}

	public List<Aluno> findAluno(String nome, String nomeResponsavel, String cpf, String numeroDocumento) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(al) from  Boleto bol ");
		sql.append("left join bol.pagador al ");
		sql.append("where 1=2 ");
		if (nome != null && !nome.equalsIgnoreCase("")) {
			sql.append(" or al.nomeAluno like '%");
			sql.append(nome);
			sql.append("%' ");
		} else {

		}
		if (nomeResponsavel != null && !nomeResponsavel.equalsIgnoreCase("")) {
			sql.append(" or al.nomeResponsavel like '%");
			sql.append(nomeResponsavel);
			sql.append("%' ");
		}

		if (cpf != null && !cpf.equalsIgnoreCase("")) {
			sql.append(" or al.cpfResponsavel like '%");
			sql.append(cpf);
			sql.append("%' ");
		}
		
		if (numeroDocumento != null && !numeroDocumento.equalsIgnoreCase("")) {
			sql.append(" or bol.nossoNumero = ");
			sql.append(numeroDocumento);
		}

		Query query = em.createQuery(sql.toString());

		@SuppressWarnings("unchecked")
		List<Aluno> alunos = query.getResultList();
		for (Aluno al : alunos) {
			al.getBoletos().size();
		}

		return alunos;
	}
	
	public void removerCnabEnviado(Long id) {
		Aluno aluno = findById(id);
		aluno.setCnabEnviado(false);
		em.merge(aluno);
	}

	public void enviarCnab(Long id) {
		Aluno aluno = findById(id);
		aluno.setCnabEnviado(true);
		em.merge(aluno);
	}

}
