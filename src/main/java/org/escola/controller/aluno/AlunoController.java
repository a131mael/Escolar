/*
n * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.escola.controller.aluno;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.aaf.financeiro.model.Boleto;
import org.aaf.financeiro.model.Pagador;
import org.aaf.financeiro.util.CNAB240_REMESSA_SICOOB;
import org.aaf.financeiro.util.CNAB240_SICOOB;
import org.escola.controller.OfficeDOCUtil;
import org.escola.controller.OfficePDFUtil;
import org.escola.enums.BimestreEnum;
import org.escola.enums.DisciplinaEnum;
import org.escola.enums.EscolaEnum;
import org.escola.enums.PerioddoEnum;
import org.escola.enums.Serie;
import org.escola.model.Aluno;
import org.escola.model.AlunoAvaliacao;
import org.escola.model.Custo;
import org.escola.service.AlunoService;
import org.escola.service.AvaliacaoService;
import org.escola.service.ConfiguracaoService;
import org.escola.service.TurmaService;
import org.escola.util.Constant;
import org.escola.util.CurrencyWriter;
import org.escola.util.FileDownload;
import org.escola.util.ImpressoesUtils;
import org.escola.util.Util;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

@Named
@ViewScoped
public class AlunoController implements Serializable {

	/****/
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private Aluno aluno;

	@Produces
	@Named
	private Boleto boleto;

	private boolean irmao1;

	private boolean irmao2;

	private boolean irmao3;

	private boolean irmao4;

	@Produces
	@Named
	private List<Aluno> alunos;

	@Inject
	private AlunoService alunoService;

	@Inject
	private ConfiguracaoService configuracaoService;

	@Inject
	private AvaliacaoService avaliacaoService;

	@Inject
	private TurmaService carroService;
	
	private OfficeDOCUtil officeDOCUtil;
	CurrencyWriter cw;

	@Named
	private BimestreEnum bimestreSel;
	@Named
	private DisciplinaEnum disciplinaSel;

	private LazyDataModel<Aluno> lazyListDataModel;

	private LazyDataModel<Boleto> lazyListDataModelBoletos;

	private LazyDataModel<Aluno> lazyListDataModelExAlunos;

	private LazyDataModel<Aluno> lazyListDataModelUltimoAnoLetivo;

	private long total = 0;
	
	public LazyDataModel<Aluno> getLazyDataModel() {
		if (lazyListDataModel == null) {

			lazyListDataModel = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return alunoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.put("anoLetivo", Constant.anoLetivoAtual);

					filtros.putAll(where);
					filtros.put("removido", false);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}
					
					if (filtros.containsKey("carroLevaParaEscola")) {
						String carro = filtros.get("carroLevaParaEscola").toString();
						filtros.put("carroLevaParaEscola", carroService.findByName(carro));
					}
					
					if (filtros.containsKey("carroPegaEscola")) {
						String carro = filtros.get("carroPegaEscola").toString();
						filtros.put("carroPegaEscola", carroService.findByName(carro));
					}

					if (filtros.containsKey("escola")) {
						String escolaSelecionada =filtros.get("escola").toString(); 
						if (escolaSelecionada.equals(EscolaEnum.ADONAI.name())) {
							filtros.put("escola", EscolaEnum.ADONAI);
						}else if(escolaSelecionada.equals(EscolaEnum.CEMA.name())){
							filtros.put("escola", EscolaEnum.CEMA);
						}else if(escolaSelecionada.equals(EscolaEnum.CETEK.name())){
							filtros.put("escola", EscolaEnum.CETEK);
						}else if(escolaSelecionada.equals(EscolaEnum.DOM_JAIME.name())){
							filtros.put("escola", EscolaEnum.DOM_JAIME);
						}else if(escolaSelecionada.equals(EscolaEnum.ELCANA.name())){
							filtros.put("escola", EscolaEnum.ELCANA);
						}else if(escolaSelecionada.equals(EscolaEnum.ELCANANINHA.name())){
							filtros.put("escola", EscolaEnum.ELCANANINHA);
						}else if(escolaSelecionada.equals(EscolaEnum.EVANDRA_SUELI.name())){
							filtros.put("escola", EscolaEnum.EVANDRA_SUELI);
						}else if(escolaSelecionada.equals(EscolaEnum.INES_MARTA.name())){
							filtros.put("escola", EscolaEnum.INES_MARTA);
						}else if(escolaSelecionada.equals(EscolaEnum.INOVACAO.name())){
							filtros.put("escola", EscolaEnum.INOVACAO);
						}else if(escolaSelecionada.equals(EscolaEnum.ITERACAO.name())){
							filtros.put("escola", EscolaEnum.ITERACAO);
						}else if(escolaSelecionada.equals(EscolaEnum.JOAO_SILVEIRA.name())){
							filtros.put("escola", EscolaEnum.JOAO_SILVEIRA);
						}else if(escolaSelecionada.equals(EscolaEnum.MARIA_DO_CARMO.name())){
							filtros.put("escola", EscolaEnum.MARIA_DO_CARMO);
						}else if(escolaSelecionada.equals(EscolaEnum.MARIA_JOSE_MEDEIROS.name())){
							filtros.put("escola", EscolaEnum.MARIA_JOSE_MEDEIROS);
						}else if(escolaSelecionada.equals(EscolaEnum.ZILAR_ROSAR.name())){
							filtros.put("escola", EscolaEnum.ZILAR_ROSAR);
						}else if(escolaSelecionada.equals(EscolaEnum.VOVO_MARIA.name())){
							filtros.put("escola", EscolaEnum.VOVO_MARIA);
						}else if(escolaSelecionada.equals(EscolaEnum.VOO_LIVRE.name())){
							filtros.put("escola", EscolaEnum.VOO_LIVRE);
						}else if(escolaSelecionada.equals(EscolaEnum.VIVENCIA.name())){
							filtros.put("escola", EscolaEnum.VIVENCIA);
						}else if(escolaSelecionada.equals(EscolaEnum.VENCESLAU.name())){
							filtros.put("escola", EscolaEnum.VENCESLAU);
						}else if(escolaSelecionada.equals(EscolaEnum.RODA_PIAO.name())){
							filtros.put("escola", EscolaEnum.RODA_PIAO);
						}else if(escolaSelecionada.equals(EscolaEnum.PROJETO_ESPERANCA.name())){
							filtros.put("escola", EscolaEnum.PROJETO_ESPERANCA);
						}else if(escolaSelecionada.equals(EscolaEnum.MODELO.name())){
							filtros.put("escola", EscolaEnum.MODELO);
						}else if(escolaSelecionada.equals(EscolaEnum.MULLER.name())){
							filtros.put("escola", EscolaEnum.MULLER);
						}else if(escolaSelecionada.equals(EscolaEnum.MULTIPLA_ESCOLHA.name())){
							filtros.put("escola", EscolaEnum.MULTIPLA_ESCOLHA);
						}else if(escolaSelecionada.equals(EscolaEnum.N_S_FATIMA.name())){
							filtros.put("escola", EscolaEnum.N_S_FATIMA);
						}else if(escolaSelecionada.equals(EscolaEnum.NOVA_ESPERANCA.name())){
							filtros.put("escola", EscolaEnum.NOVA_ESPERANCA);
						}else if(escolaSelecionada.equals(EscolaEnum.PARAISO_DO_AMOR.name())){
							filtros.put("escola", EscolaEnum.PARAISO_DO_AMOR);
						}else if(escolaSelecionada.equals(EscolaEnum.PROF_GUILHERME.name())){
							filtros.put("escola", EscolaEnum.PROF_GUILHERME);
						}else if(escolaSelecionada.equals(EscolaEnum.PROJETO_ESPERANCA.name())){
							filtros.put("escola", EscolaEnum.PROJETO_ESPERANCA);
						}
						
					}
					
					if (filtros.containsKey("serie")) {
						if (filtros.get("serie").equals(Serie.JARDIM_I.toString())) {
							filtros.put("serie", Serie.JARDIM_I);
						} else if (filtros.get("serie").equals(Serie.JARDIM_II.toString())) {
							filtros.put("serie", Serie.JARDIM_II);
						} else if (filtros.get("serie").equals(Serie.MATERNAL.toString())) {
							filtros.put("serie", Serie.MATERNAL);
						} else if (filtros.get("serie").equals(Serie.PRE.toString())) {
							filtros.put("serie", Serie.PRE);
						} else if (filtros.get("serie").equals(Serie.PRIMEIRO_ANO.toString())) {
							filtros.put("serie", Serie.PRIMEIRO_ANO);
						} else if (filtros.get("serie").equals(Serie.SEGUNDO_ANO.toString())) {
							filtros.put("serie", Serie.SEGUNDO_ANO);
						} else if (filtros.get("serie").equals(Serie.TERCEIRO_ANO.toString())) {
							filtros.put("serie", Serie.TERCEIRO_ANO);
						} else if (filtros.get("serie").equals(Serie.QUARTO_ANO.toString())) {
							filtros.put("serie", Serie.QUARTO_ANO);
						} else if (filtros.get("serie").equals(Serie.QUINTO_ANO.toString())) {
							filtros.put("serie", Serie.QUINTO_ANO);
						}

					}

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = alunoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						long count =alunoService.count(filtros);
						lazyListDataModel.setRowCount((int) count);
						total = count;
//						FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
//						RequestContext.getCurrentInstance().update("tbl:total");

						return ol;
					}
					long count =alunoService.count(filtros);
					total = count;
//					FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
//					RequestContext.getCurrentInstance().update("tbl:total");
					this.setRowCount((int) count);
					return null;

				}
			};
			long count =alunoService.count(null);
			total = count;
//			FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
//			RequestContext.getCurrentInstance().update("tbl:total");
			lazyListDataModel.setRowCount((int) count);
			

		}

		return lazyListDataModel;

	}

	
	public double valorTotal(Aluno aluno) {
		if (aluno != null && aluno.getNumeroParcelas() != null) {
			return aluno.getValorMensal() * aluno.getNumeroParcelas();
		} else {
			return 0;
		}
	}

	public LazyDataModel<Aluno> getLazyDataModelExAlunos() {
		if (lazyListDataModelExAlunos == null) {

			lazyListDataModelExAlunos = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return alunoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					filtros.put("removido", true);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("serie")) {
						if (filtros.get("serie").equals(Serie.JARDIM_I.toString())) {
							filtros.put("serie", Serie.JARDIM_I);
						} else if (filtros.get("serie").equals(Serie.JARDIM_II.toString())) {
							filtros.put("serie", Serie.JARDIM_II);
						} else if (filtros.get("serie").equals(Serie.MATERNAL.toString())) {
							filtros.put("serie", Serie.MATERNAL);
						} else if (filtros.get("serie").equals(Serie.PRE.toString())) {
							filtros.put("serie", Serie.PRE);
						} else if (filtros.get("serie").equals(Serie.PRIMEIRO_ANO.toString())) {
							filtros.put("serie", Serie.PRIMEIRO_ANO);
						} else if (filtros.get("serie").equals(Serie.SEGUNDO_ANO.toString())) {
							filtros.put("serie", Serie.SEGUNDO_ANO);
						} else if (filtros.get("serie").equals(Serie.TERCEIRO_ANO.toString())) {
							filtros.put("serie", Serie.TERCEIRO_ANO);
						} else if (filtros.get("serie").equals(Serie.QUARTO_ANO.toString())) {
							filtros.put("serie", Serie.QUARTO_ANO);
						} else if (filtros.get("serie").equals(Serie.QUINTO_ANO.toString())) {
							filtros.put("serie", Serie.QUINTO_ANO);
						}

					}

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = alunoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelExAlunos.setRowCount((int) alunoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) alunoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelExAlunos.setRowCount((int) alunoService.count(null));

		}

		return lazyListDataModelExAlunos;

	}

	public LazyDataModel<Aluno> getLazyDataModelUltimoAnoLetivo() {
		if (lazyListDataModelUltimoAnoLetivo == null) {

			lazyListDataModelUltimoAnoLetivo = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return alunoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);

					filtros.put("anoLetivo", Constant.anoLetivoAtual - 1);

					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("serie")) {
						if (filtros.get("serie").equals(Serie.JARDIM_I.toString())) {
							filtros.put("serie", Serie.JARDIM_I);
						} else if (filtros.get("serie").equals(Serie.JARDIM_II.toString())) {
							filtros.put("serie", Serie.JARDIM_II);
						} else if (filtros.get("serie").equals(Serie.MATERNAL.toString())) {
							filtros.put("serie", Serie.MATERNAL);
						} else if (filtros.get("serie").equals(Serie.PRE.toString())) {
							filtros.put("serie", Serie.PRE);
						} else if (filtros.get("serie").equals(Serie.PRIMEIRO_ANO.toString())) {
							filtros.put("serie", Serie.PRIMEIRO_ANO);
						} else if (filtros.get("serie").equals(Serie.SEGUNDO_ANO.toString())) {
							filtros.put("serie", Serie.SEGUNDO_ANO);
						} else if (filtros.get("serie").equals(Serie.TERCEIRO_ANO.toString())) {
							filtros.put("serie", Serie.TERCEIRO_ANO);
						} else if (filtros.get("serie").equals(Serie.QUARTO_ANO.toString())) {
							filtros.put("serie", Serie.QUARTO_ANO);
						} else if (filtros.get("serie").equals(Serie.QUINTO_ANO.toString())) {
							filtros.put("serie", Serie.QUINTO_ANO);
						}

					}

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = alunoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelUltimoAnoLetivo.setRowCount((int) alunoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) alunoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelUltimoAnoLetivo.setRowCount((int) alunoService.count(null));

		}

		return lazyListDataModelUltimoAnoLetivo;

	}

	@PostConstruct
	private void init() {
		if (aluno == null) {
			Object obj = Util.getAtributoSessao("aluno");
			if (obj != null) {
				aluno = (Aluno) obj;
				if (aluno.getIrmao1() != null) {
					irmao1 = true;
				}
				if (aluno.getIrmao2() != null) {
					irmao2 = true;
				}
				if (aluno.getIrmao3() != null) {
					irmao3 = true;
				}
				if (aluno.getIrmao4() != null) {
					irmao4 = true;
				}
			} else {
				aluno = new Aluno();
				aluno.setCodigo(alunoService.getProximoCodigo()+"");
			}
		}

		officeDOCUtil = new OfficeDOCUtil();
		cw = new CurrencyWriter();
	}

	public boolean estaEmUmaTurma(long idAluno) {
		boolean estaNaTurma = alunoService.estaEmUmaTUrma(idAluno);
		return estaNaTurma;
	}

	private Float maior(Float float1, Float float2) {
		return float1 > float2 ? float1 : float2;
	}

	private String mostraNotas(Float nota) {
		if (nota == null || nota == 0) {
			return "";
		} else {
			return String.valueOf(Math.round(nota * 100) / 100);
		}
	}

	private Float media(Float... notas) {
		int qtade = 0;
		float sum = 0;
		for (Float f : notas) {
			sum += f;
			qtade++;
		}
		return qtade > 0 ? sum / qtade : 0;
	}

	public float getNota(DisciplinaEnum disciplina, BimestreEnum bimestre) {
		return alunoService.getNota(aluno.getId(), disciplina, bimestre, false);
	}

	public List<Custo> getHistoricoAluno(Aluno aluno) {
		return alunoService.getHistoricoAluno(aluno);
	}

	public boolean renderDisciplina(int ordinal) {
		if (getDisciplinaSel() == null) {
			return true;
		}

		return ordinal == getDisciplinaSel().ordinal();
	}

	public void saveAvaliacaoAluno(AlunoAvaliacao alav) {
		avaliacaoService.saveAlunoAvaliacao(alav);
	}

	public void saveAvaliacaoAluno(Long idAluAv, Float nota) {
		avaliacaoService.saveAlunoAvaliacao(idAluAv, nota);
	}

	public HashMap<String, String> montarAtestadoNegativoDebito(Aluno aluno) {
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
		String dataExtenso = formatador.format(new Date());

		HashMap<String, String> trocas = new HashMap<>();
		trocas.put("adonainomealuno", aluno.getNomeAluno());
		trocas.put("adonaiturma", aluno.getSerie().getName());
		trocas.put("adonaiperiodo", aluno.getPeriodo().getName());
		trocas.put("adonaidata", dataExtenso);
		trocas.put("adonaicpfresponsavel", aluno.getCpfResponsavel());
		trocas.put("adonainomeresponsavel", aluno.getNomeResponsavel());

		return trocas;
	}

	public HashMap<String, String> montarAtestadoVaga(Aluno aluno) {
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
		String dataExtenso = formatador.format(new Date());

		HashMap<String, String> trocas = new HashMap<>();
		trocas.put("adonainomealuno", aluno.getNomeAluno());
		trocas.put("adonaiturma", aluno.getSerie().getName());
		trocas.put("adonaiperiodo", aluno.getPeriodo().getName());
		trocas.put("adonaidata", dataExtenso);

		return trocas;
	}

	public HashMap<String, String> montarContrato(Aluno aluno) {
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
		String dataExtenso = formatador.format(new Date());
		Calendar dataLim = Calendar.getInstance();
		dataLim.add(Calendar.MONTH, 1);
		String dataLimiteExtenso = formatador.format(dataLim.getTime());

		DateFormat formatadorAno = DateFormat.getDateInstance(DateFormat.YEAR_FIELD, new Locale("pt", "BR"));
		String ano = formatadorAno.format(new Date());

		// int anoInt = Integer.parseInt(ano);

		HashMap<String, String> trocas = new HashMap<>();
		trocas.put("#ANOCONTRATO", "2018"); // TODO colocar o ano atual +1
		trocas.put("#CONTRATANTECID", "Palhoça"); // TODO COLOCAR CIDADE DO
													// Contratado
		trocas.put("#DATAEXTENSO", dataExtenso);

		trocas.put("#CONTRATANTENOME", aluno.getNomeResponsavel());
		trocas.put("#CONTRATANTERG", aluno.getRgResponsavel());
		trocas.put("#CONTRATANTECPF", aluno.getCpfResponsavel());
		trocas.put("#CONTRATANTERUA", aluno.getEndereco() + ", " + aluno.getBairro());

		String nomeAluno = aluno.getNomeAluno();
		if (aluno.getIrmao1() != null) {
			nomeAluno += ", " + aluno.getIrmao1().getNomeAluno();
		}
		if (aluno.getIrmao2() != null) {
			nomeAluno += ", " + aluno.getIrmao2().getNomeAluno();
		}
		if (aluno.getIrmao3() != null) {
			nomeAluno += ", " + aluno.getIrmao3().getNomeAluno();
		}
		if (aluno.getIrmao4() != null) {
			nomeAluno += ", " + aluno.getIrmao4().getNomeAluno();
		}

		trocas.put("#TRANSPORTADONOME", nomeAluno);
		trocas.put("#TRANSPORTADORUA", aluno.getEndereco() + ", " + aluno.getBairro());
		trocas.put("#TRANSPORTADOESCOLA", aluno.getEscola().getName());

		String periodo1 = "";
		if (aluno.getPeriodo().equals(PerioddoEnum.INTEGRAL) || aluno.getPeriodo().equals(PerioddoEnum.MANHA)) {
			periodo1 = "06:30";
		} else {
			periodo1 = "11:30";
		}
		String periodo2 = "";
		if (aluno.getPeriodo().equals(PerioddoEnum.INTEGRAL) || aluno.getPeriodo().equals(PerioddoEnum.TARDE)) {
			periodo2 = "19:30";
		} else {
			periodo2 = "13:30";
		}

		trocas.put("#DADOSGERAISHORARIO1", periodo1);
		trocas.put("#DADOSGERAISHORARIO2", periodo2);
		trocas.put("#DADOSGERAISMES1", getMesInicioPagamento(aluno));
		trocas.put("#DADOSGERAISMES2", "Dezembro");
		trocas.put("#DADOSGERAISPARCELAS", aluno.getNumeroParcelas() + "");
		// BigDecimal valorTotal = (new
		// BigDecimal(contrato.getValorTotal())).multiply(((new
		// BigDecimal(contrato.getParcelas()))));
		// trocas.put("#DADOSGERAISTOTAL", valorTotal.toString());
		trocas.put("#DADOSGERAISTOTAL", String.valueOf(valorTotal(aluno))); // TODO
																			// ver
		// contrato.setValorTotal(contrato.getValorTotal().replace(",", "."));
		trocas.put("#DADOSGERAISTOTALEXTENSO", cw.write(new BigDecimal(valorTotal(aluno))));
		trocas.put("#DADOSGERAISQTADEPARCELAS", aluno.getNumeroParcelas() + "");
		trocas.put("#DADOSGERAISEXTENSOPARCELA", cw.write(new BigDecimal(aluno.getValorMensal())));
		trocas.put("#DADOSGERAISPARCELA", aluno.getValorMensal() + "");/// valor
																		/// da
																		/// parcela

		String idaEVolta = "CLAUSULA 6ª – O CONTRATANTE compromete-se a deixar o TRANSPORTADO pronto e aguardando pelo CONTRATADO no endereço e hora combinada, ou seja, na rua  #CONTRATANTERUA   as #DADOSGERAISHORARIO1,  não tolerando qualquer tipo de atraso ou mudança de endereço.";
		String ida = "CLAUSULA 6ª - O CONTRATADO SO SE RESPONSABILIZARA PELO TRANSPORTE DE IDA PARA A ESCOLA, O TRANSPORTE DE VOLTA DA ESCOLA È DE RESPONSABILIDADE DO CONTRATANTE.";
		String volta = "CLAUSULA 6ªB – O CONTRATADO SO SE RESPONSABILIZARA PELO TRANSPORTE DE VOLTA DA ESCOLA, O TRANSPORTE DE IDA PARA A ESCOLA È DE RESPONSABILIDADE DO CONTRATANTE.";

		switch (aluno.getIdaVolta()) {
		case 0:
			trocas.put("#TIPOCONTRATO", idaEVolta);
			break;

		case 1:
			trocas.put("#TIPOCONTRATO", ida);
			break;

		case 2:
			trocas.put("#TIPOCONTRATO", volta);
			break;

		default:
			trocas.put("#TIPOCONTRATO", idaEVolta);
			break;
		}

		return trocas;
	}

	private String getMesInicioPagamento(Aluno aluno2) {
		String mes = "Janeiro";
		switch (aluno.getNumeroParcelas()) {
		case 12:
			break;

		case 11:
			mes = "Fevereiro";
			break;

		case 10:
			mes = "Março";
			break;

		case 9:
			mes = "Abril";
			break;

		case 8:
			mes = "Maio";
			break;

		case 7:
			mes = "Junho";
			break;

		case 6:
			mes = "Julho";
			break;

		case 5:
			mes = "Agosto";
			break;

		case 4:
			mes = "Setembro";
			break;

		case 3:
			mes = "Outubro";
			break;

		case 2:
			mes = "Novembro";
			break;

		case 1:
			mes = "Dezembro";
			break;

		default:
			break;
		}
		return mes;
	}

	public void onRowSelect(SelectEvent event) {
		Aluno al = (Aluno) event.getObject();
		FacesMessage msg = new FacesMessage("Car Selected");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void onRowUnselect(UnselectEvent event) {
		Aluno al = (Aluno) event.getObject();
		FacesMessage msg = new FacesMessage("Car Unselected");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public boolean isAlunoSelecionado() {
		return aluno.getId() != null ? true : false;
	}

	public StreamedContent imprimirNegativoDebito(Aluno aluno) throws IOException {
		String nomeArquivo = "";
		if (aluno != null && aluno.getId() != null) {
			nomeArquivo = aluno.getId() + "f";
			ImpressoesUtils.imprimirInformacoesAluno(aluno, "modeloNegativoDebito2017.docx",
					montarAtestadoNegativoDebito(aluno), nomeArquivo);

			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "modeloNegativoDebito2017.docx";
		}

		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nomeArquivo;
		InputStream stream = new FileInputStream(caminho);
		return FileDownload.getContentDoc(stream, nomeArquivo);
	}

	public StreamedContent imprimirNegativoDebito() throws IOException {
		return imprimirNegativoDebito(aluno);
	}

	public StreamedContent imprimirContrato(Aluno aluno) throws IOException {
		String nomeArquivo = "";
		if (aluno != null && aluno.getId() != null) {
			nomeArquivo = aluno.getId() + "g";
			ImpressoesUtils.imprimirInformacoesAluno(aluno, "MODELO1-2.doc", montarContrato(aluno), nomeArquivo);
			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "MODELO1-1.doc";
		}

		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nomeArquivo;
		InputStream stream = new FileInputStream(caminho);
		return FileDownload.getContentDoc(stream, nomeArquivo);
	}

	public StreamedContent imprimirContrato() throws IOException {
		return imprimirContrato(aluno);
	}

	public StreamedContent imprimirAtestadoVaga(Aluno aluno) throws IOException {
		String nomeArquivo = "";
		if (aluno != null && aluno.getId() != null) {
			nomeArquivo = aluno.getId() + "h";
			ImpressoesUtils.imprimirInformacoesAluno(aluno, "modeloAtestadoVaga2017.docx", montarAtestadoVaga(aluno),
					nomeArquivo);
			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "modeloAtestadoVaga2017.docx";
		}

		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nomeArquivo;
		InputStream stream = new FileInputStream(caminho);
		return FileDownload.getContentDoc(stream, nomeArquivo);
	}

	public StreamedContent imprimirAtestadoVaga() throws IOException {
		return imprimirAtestadoVaga(aluno);
	}

	public List<Aluno> getAlunos() {

		return alunoService.findAll();
	}

	public String salvar() {
		alunoService.save(aluno);
		Util.removeAtributoSessao("aluno");
		return "index";
	}

	public String voltar() {
		return "index";
	}

	public String editar(Long idprof) {
		aluno = alunoService.findById(idprof);
		Util.addAtributoSessao("aluno", aluno);
		return "cadastrar";
	}

	public String visualizar(Aluno aluno) {
		aluno = alunoService.findById(aluno.getId());
		Util.addAtributoSessao("aluno", aluno);

		return "cadastrar";
	}

	public String editar() {
		return editar(aluno.getId());
	}

	public String remover(Long idTurma) {
		alunoService.remover(idTurma);
		return "index";
	}

	public String restaurar(Long idTurma) {
		alunoService.restaurar(idTurma);
		return "index";
	}

	public String adicionarNovo() {
		return "cadastrar";
	}

	public void adicionarIrmao() {
		if (!isIrmao1()) {
			setIrmao1(true);
			aluno.setIrmao1(new Aluno());
		} else if (!isIrmao2()) {
			setIrmao2(true);
			aluno.setIrmao2(new Aluno());
		} else if (!isIrmao3()) {
			setIrmao3(true);
			aluno.setIrmao3(new Aluno());
		} else if (!isIrmao4()) {
			setIrmao4(true);
			aluno.setIrmao4(new Aluno());
		}
	}

	public void removerIrmao() {
		if (isIrmao4()) {
			setIrmao4(false);
		} else if (isIrmao3()) {
			setIrmao3(false);
		} else if (isIrmao2()) {
			setIrmao2(false);
		} else if (isIrmao1()) {
			setIrmao1(false);
		}
	}

	public void removerHistorico(long idHistorico) {
		alunoService.removerHistorico(idHistorico);
	}

	public String cadastrarNovo() {

		return "exibirAluno";
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public DisciplinaEnum getDisciplinaSel() {
		return disciplinaSel;
	}

	public void setDisciplinaSel(DisciplinaEnum disciplinaSel) {
		this.disciplinaSel = disciplinaSel;
	}

	public BimestreEnum getBimestreSel() {
		return bimestreSel;
	}

	public void setBimestreSel(BimestreEnum bimestreSel) {
		this.bimestreSel = bimestreSel;
	}

	public boolean isIrmao1() {
		return irmao1;
	}

	public void setIrmao1(boolean irmao1) {
		this.irmao1 = irmao1;
	}

	public boolean isIrmao2() {
		return irmao2;
	}

	public void setIrmao2(boolean irmao2) {
		this.irmao2 = irmao2;
	}

	public boolean isIrmao3() {
		return irmao3;
	}

	public void setIrmao3(boolean irmao3) {
		this.irmao3 = irmao3;
	}

	public boolean isIrmao4() {
		return irmao4;
	}

	public void setIrmao4(boolean irmao4) {
		this.irmao4 = irmao4;
	}

	public StreamedContent downloadBoleto(org.escola.model.Boleto boleto) {
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(boleto.getVencimento());
			CNAB240_SICOOB cnab = new CNAB240_SICOOB();
			String nomeArquivo = aluno.getCodigo() + c.get(Calendar.MONTH) + ".pdf";

			Pagador pagador = new Pagador();
			pagador.setBairro(aluno.getBairro());
			pagador.setCep(aluno.getCep());
			pagador.setCidade(aluno.getCidade() != null ? aluno.getCidade() : "PALHOCA");
			pagador.setCpfCNPJ(aluno.getCpfResponsavel());
			pagador.setEndereco(aluno.getEndereco());
			pagador.setNome(aluno.getNomeResponsavel());
			pagador.setNossoNumero(boleto.getNossoNumero()+"");
			pagador.setUF("SC");
			List<Boleto> boletos = new ArrayList<>();
			Boleto b = new Boleto();
			b.setEmissao(boleto.getEmissao());
			b.setId(boleto.getId());
			b.setNossoNumero(boleto.getNossoNumero());
			b.setValorNominal(boleto.getValorNominal());
			b.setVencimento(boleto.getVencimento());
			boletos.add(b);
			pagador.setBoletos(boletos);

			byte[] pdf = cnab.getBoletoPDF(pagador);

			OfficePDFUtil.geraPDF(nomeArquivo, pdf);

			String temp = System.getProperty("java.io.tmpdir");
			String caminho = temp + File.separator + nomeArquivo;

			InputStream stream;
			stream = new FileInputStream(caminho);
			return FileDownload.getContentDoc(stream, nomeArquivo);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void gerarBoletoModel(){
		alunoService.gerarBoletos(aluno,true);
	}
	
	public StreamedContent gerarBoleto() {
		try {

			CNAB240_SICOOB cnab = new CNAB240_SICOOB();
			/*Calendar c = Calendar.getInstance();
			c.setTime(boleto.getVencimento());*/
			String nomeArquivo = aluno.getCodigo() + ".pdf";


			Pagador pagador = new Pagador();
			pagador.setBairro(aluno.getBairro());
			pagador.setCep(aluno.getCep());
			pagador.setCidade(aluno.getCidade() != null ? aluno.getCidade() : "PALHOCA");
			pagador.setCpfCNPJ(aluno.getCpfResponsavel());
			pagador.setEndereco(aluno.getEndereco());
			pagador.setNome(aluno.getNomeResponsavel());
			pagador.setNossoNumero(aluno.getCodigo());
			pagador.setUF("SC");
			pagador.setBoletos(aluno.getBoletosFinanceiro());

			byte[] pdf = cnab.getBoletoPDF(pagador);

			OfficePDFUtil.geraPDF(nomeArquivo, pdf);

			String temp = System.getProperty("java.io.tmpdir");
			String caminho = temp + File.separator + nomeArquivo;

			InputStream stream;
			stream = new FileInputStream(caminho);
			return FileDownload.getContentDoc(stream, nomeArquivo);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public StreamedContent gerarCNB240() {
		try {
			String sequencialArquivo = configuracaoService.getSequencialArquivo() + "";
			String nomeArquivo = "CNAB240_" + aluno.getCodigo() + ".txt";

			Pagador pagador = new Pagador();
			pagador.setBairro(aluno.getBairro());
			pagador.setCep(aluno.getCep());
			pagador.setCidade(aluno.getCidade() != null ? aluno.getCidade() : "PALHOCA");
			pagador.setCpfCNPJ(aluno.getCpfResponsavel());
			pagador.setEndereco(aluno.getEndereco());
			pagador.setNome(aluno.getNomeResponsavel());
			pagador.setNossoNumero(aluno.getCodigo());
			pagador.setUF("SC");
			pagador.setBoletos(aluno.getBoletosFinanceiro());
			byte[] arquivo = CNAB240_REMESSA_SICOOB.geraRemessa(pagador, sequencialArquivo);

			try {
				configuracaoService.incrementaSequencialArquivoCNAB();
				InputStream stream = new ByteArrayInputStream(arquivo);
				return FileDownload.getContentDoc(stream, nomeArquivo);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Double getDesconto(org.escola.model.Boleto boleto) {
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH) + 1);
		if (boleto.getVencimento().compareTo(tomorrow.getTime()) == 1) {
			return 20d;
		} else {
			return 0d;
		}

	}

	public String getDescontoString(org.escola.model.Boleto boleto) {
			return Util.formatarDouble2Decimais(getDesconto(boleto));
	}

	
	public Double getJurosMulta(org.escola.model.Boleto boleto) {
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH) + 1);
		double multa = boleto.getValorNominal() * 0.02;
		long diasVencimento = Util.diferencaEntreDatas(tomorrow.getTime(),boleto.getVencimento());
		if(diasVencimento >0){
			double juros = (diasVencimento / 2); // juros de 50 centavos
			return multa + juros;
			
		}else{
			return 0D;
		}

	}
	public String getJurosMultaString(org.escola.model.Boleto boleto) {
		return Util.formatarDouble2Decimais(getJurosMulta(boleto));
	}

	public Double getValorFinal(org.escola.model.Boleto boleto) {

		return boleto.getValorNominal() + getJurosMulta(boleto) - getDesconto(boleto);
	}

	public String getValorFinalString(org.escola.model.Boleto boleto) {

		return Util.formatarDouble2Decimais(getValorFinal(boleto));
	}


	public long getTotal() {
		return total;
	}


	public void setTotal(long total) {
		this.total = total;
	}

}
