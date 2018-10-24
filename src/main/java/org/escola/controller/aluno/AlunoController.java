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
import java.text.ParseException;
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
import org.aaf.financeiro.sicoob.util.CNAB240_REMESSA_SICOOB;
import org.aaf.financeiro.sicoob.util.CNAB240_RETORNO_SICOOB;
import org.aaf.financeiro.sicoob.util.CNAB240_SICOOB;
import org.aaf.financeiro.util.ImportadorArquivo;
import org.aaf.financeiro.util.OfficeUtil;
import org.aaf.financeiro.util.constantes.Constante;
import org.apache.shiro.SecurityUtils;
import org.escola.util.FileDownload;
import org.escolar.controller.OfficeDOCUtil;
import org.escolar.controller.OfficePDFUtil;
import org.escolar.enums.BimestreEnum;
import org.escolar.enums.DisciplinaEnum;
import org.escolar.enums.EscolaEnum;
import org.escolar.enums.PerioddoEnum;
import org.escolar.enums.Serie;
import org.escolar.enums.StatusBoletoEnum;
import org.escolar.enums.TipoMembro;
import org.escolar.model.Aluno;
import org.escolar.model.AlunoAvaliacao;
import org.escolar.model.Configuracao;
import org.escolar.model.Custo;
import org.escolar.model.Member;
import org.escolar.model.TabelaPrecos;
import org.escolar.rotinasAutomaticas.EnviadorEmail;
import org.escolar.service.AlunoService;
import org.escolar.service.AvaliacaoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.DevedorService;
import org.escolar.service.FinanceiroService;
import org.escolar.service.TabelaPrecoService;
import org.escolar.service.TurmaService;
import org.escolar.util.CompactadorZip;
import org.escolar.util.Constant;
import org.escolar.util.CurrencyWriter;
import org.escolar.util.FileUtils;
import org.escolar.util.Formatador;
import org.escolar.util.ImpressoesUtils;
import org.escolar.util.Util;
import org.escolar.util.Verificador;
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
	private DevedorService devedorService ;
	
	@Inject
	private TabelaPrecoService tabelaPrecoService ;

	@Inject
	private FinanceiroService financeiroService;

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

	private LazyDataModel<Aluno> lazyListDataModelCanceladas;

	private LazyDataModel<Boleto> lazyListDataModelBoletos;

	private LazyDataModel<Aluno> lazyListDataModelExAlunos;

	private LazyDataModel<Aluno> lazyListDataModelUltimoAnoLetivo;

	private Configuracao configuracao;
	
	private long total = 0;
	private Double valorTotal = 0D;
	
	@PostConstruct
	private void init() {
		if (aluno == null) {
			Object obj = Util.getAtributoSessao("aluno");
			if (obj != null) {
				aluno = (Aluno) obj;
				if (aluno.getIrmao1() != null
						&& (aluno.getIrmao1().getRemovido() == null || !aluno.getIrmao1().getRemovido())) {
					irmao1 = true;
				}
				if (aluno.getIrmao2() != null
						&& (aluno.getIrmao2().getRemovido() == null || !aluno.getIrmao2().getRemovido())) {
					irmao2 = true;
				}
				if (aluno.getIrmao3() != null
						&& (aluno.getIrmao3().getRemovido() == null || !aluno.getIrmao3().getRemovido())) {
					irmao3 = true;
				}
				if (aluno.getIrmao4() != null
						&& (aluno.getIrmao4().getRemovido() == null || !aluno.getIrmao4().getRemovido())) {
					irmao4 = true;
				}

			} else {
				aluno = new Aluno();
				aluno.setCodigo(alunoService.getProximoCodigo() + "");
			}
		}

		officeDOCUtil = new OfficeDOCUtil();
		cw = new CurrencyWriter();
		configuracao = configuracaoService.getConfiguracao();
	}

	public LazyDataModel<Aluno> getLazyDataModelCanceladas() {
		if (lazyListDataModelCanceladas == null) {

			lazyListDataModelCanceladas = new LazyDataModel<Aluno>() {

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

					filtros.put("anoLetivo", configuracao.getAnoLetivo());

					filtros.putAll(where);
					filtros.put("removido", true);

					if (filtros.containsKey("nomeAluno")) {
						filtros.put("nomeAluno", ((String) filtros.get("nomeAluno")).toUpperCase());
					}

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
						String escolaSelecionada = filtros.get("escola").toString();
						if (escolaSelecionada.equals(EscolaEnum.ADONAI.name())) {
							filtros.put("escola", EscolaEnum.ADONAI);
						} else if (escolaSelecionada.equals(EscolaEnum.CEMA.name())) {
							filtros.put("escola", EscolaEnum.CEMA);
						} else if (escolaSelecionada.equals(EscolaEnum.CETEK.name())) {
							filtros.put("escola", EscolaEnum.CETEK);
						} else if (escolaSelecionada.equals(EscolaEnum.DOM_JAIME.name())) {
							filtros.put("escola", EscolaEnum.DOM_JAIME);
						} else if (escolaSelecionada.equals(EscolaEnum.ELCANA.name())) {
							filtros.put("escola", EscolaEnum.ELCANA);
						} else if (escolaSelecionada.equals(EscolaEnum.ELCANANINHA.name())) {
							filtros.put("escola", EscolaEnum.ELCANANINHA);
						} else if (escolaSelecionada.equals(EscolaEnum.EVANDRA_SUELI.name())) {
							filtros.put("escola", EscolaEnum.EVANDRA_SUELI);
						} else if (escolaSelecionada.equals(EscolaEnum.INES_MARTA.name())) {
							filtros.put("escola", EscolaEnum.INES_MARTA);
						} else if (escolaSelecionada.equals(EscolaEnum.INOVACAO.name())) {
							filtros.put("escola", EscolaEnum.INOVACAO);
						} else if (escolaSelecionada.equals(EscolaEnum.ITERACAO.name())) {
							filtros.put("escola", EscolaEnum.ITERACAO);
						} else if (escolaSelecionada.equals(EscolaEnum.JOAO_SILVEIRA.name())) {
							filtros.put("escola", EscolaEnum.JOAO_SILVEIRA);
						} else if (escolaSelecionada.equals(EscolaEnum.MARIA_DO_CARMO.name())) {
							filtros.put("escola", EscolaEnum.MARIA_DO_CARMO);
						} else if (escolaSelecionada.equals(EscolaEnum.MARIA_JOSE_MEDEIROS.name())) {
							filtros.put("escola", EscolaEnum.MARIA_JOSE_MEDEIROS);
						} else if (escolaSelecionada.equals(EscolaEnum.ZILAR_ROSAR.name())) {
							filtros.put("escola", EscolaEnum.ZILAR_ROSAR);
						} else if (escolaSelecionada.equals(EscolaEnum.VOVO_MARIA.name())) {
							filtros.put("escola", EscolaEnum.VOVO_MARIA);
						} else if (escolaSelecionada.equals(EscolaEnum.VOO_LIVRE.name())) {
							filtros.put("escola", EscolaEnum.VOO_LIVRE);
						} else if (escolaSelecionada.equals(EscolaEnum.VIVENCIA.name())) {
							filtros.put("escola", EscolaEnum.VIVENCIA);
						} else if (escolaSelecionada.equals(EscolaEnum.VENCESLAU.name())) {
							filtros.put("escola", EscolaEnum.VENCESLAU);
						} else if (escolaSelecionada.equals(EscolaEnum.RODA_PIAO.name())) {
							filtros.put("escola", EscolaEnum.RODA_PIAO);
						} else if (escolaSelecionada.equals(EscolaEnum.PROJETO_ESPERANCA.name())) {
							filtros.put("escola", EscolaEnum.PROJETO_ESPERANCA);
						} else if (escolaSelecionada.equals(EscolaEnum.MODELO.name())) {
							filtros.put("escola", EscolaEnum.MODELO);
						} else if (escolaSelecionada.equals(EscolaEnum.MULLER.name())) {
							filtros.put("escola", EscolaEnum.MULLER);
						} else if (escolaSelecionada.equals(EscolaEnum.MULTIPLA_ESCOLHA.name())) {
							filtros.put("escola", EscolaEnum.MULTIPLA_ESCOLHA);
						} else if (escolaSelecionada.equals(EscolaEnum.N_S_FATIMA.name())) {
							filtros.put("escola", EscolaEnum.N_S_FATIMA);
						} else if (escolaSelecionada.equals(EscolaEnum.NOVA_ESPERANCA.name())) {
							filtros.put("escola", EscolaEnum.NOVA_ESPERANCA);
						} else if (escolaSelecionada.equals(EscolaEnum.PARAISO_DO_AMOR.name())) {
							filtros.put("escola", EscolaEnum.PARAISO_DO_AMOR);
						} else if (escolaSelecionada.equals(EscolaEnum.PROF_GUILHERME.name())) {
							filtros.put("escola", EscolaEnum.PROF_GUILHERME);
						} else if (escolaSelecionada.equals(EscolaEnum.PROJETO_ESPERANCA.name())) {
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
					valorTotal = sumAll(ol);
					if (ol != null && ol.size() > 0) {
						long count = alunoService.count(filtros);
						lazyListDataModelCanceladas.setRowCount((int) count);
						total = count;

						// FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
						// RequestContext.getCurrentInstance().update("tbl:total");

						return ol;
					}
					long count = alunoService.count(filtros);
					total = count;
					// FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
					// RequestContext.getCurrentInstance().update("tbl:total");
					this.setRowCount((int) count);
					return null;

				}
			};
			long count = alunoService.count(null);
			total = count;
			// FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
			// RequestContext.getCurrentInstance().update("tbl:total");
			lazyListDataModelCanceladas.setRowCount((int) count);

		}

		return lazyListDataModelCanceladas;

	}

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

					filtros.put("anoLetivo", configuracao.getAnoLetivo());

					filtros.putAll(where);
					if (filtros.containsKey("nomeAluno")) {
						filtros.put("nomeAluno", ((String) filtros.get("nomeAluno")).toUpperCase());
					}

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
						String escolaSelecionada = filtros.get("escola").toString();
						if (escolaSelecionada.equals(EscolaEnum.ADONAI.name())) {
							filtros.put("escola", EscolaEnum.ADONAI);
						} else if (escolaSelecionada.equals(EscolaEnum.CEMA.name())) {
							filtros.put("escola", EscolaEnum.CEMA);
						} else if (escolaSelecionada.equals(EscolaEnum.CETEK.name())) {
							filtros.put("escola", EscolaEnum.CETEK);
						} else if (escolaSelecionada.equals(EscolaEnum.DOM_JAIME.name())) {
							filtros.put("escola", EscolaEnum.DOM_JAIME);
						} else if (escolaSelecionada.equals(EscolaEnum.ELCANA.name())) {
							filtros.put("escola", EscolaEnum.ELCANA);
						} else if (escolaSelecionada.equals(EscolaEnum.ELCANANINHA.name())) {
							filtros.put("escola", EscolaEnum.ELCANANINHA);
						} else if (escolaSelecionada.equals(EscolaEnum.EVANDRA_SUELI.name())) {
							filtros.put("escola", EscolaEnum.EVANDRA_SUELI);
						} else if (escolaSelecionada.equals(EscolaEnum.INES_MARTA.name())) {
							filtros.put("escola", EscolaEnum.INES_MARTA);
						} else if (escolaSelecionada.equals(EscolaEnum.INOVACAO.name())) {
							filtros.put("escola", EscolaEnum.INOVACAO);
						} else if (escolaSelecionada.equals(EscolaEnum.ITERACAO.name())) {
							filtros.put("escola", EscolaEnum.ITERACAO);
						} else if (escolaSelecionada.equals(EscolaEnum.JOAO_SILVEIRA.name())) {
							filtros.put("escola", EscolaEnum.JOAO_SILVEIRA);
						} else if (escolaSelecionada.equals(EscolaEnum.MARIA_DO_CARMO.name())) {
							filtros.put("escola", EscolaEnum.MARIA_DO_CARMO);
						} else if (escolaSelecionada.equals(EscolaEnum.MARIA_JOSE_MEDEIROS.name())) {
							filtros.put("escola", EscolaEnum.MARIA_JOSE_MEDEIROS);
						} else if (escolaSelecionada.equals(EscolaEnum.ZILAR_ROSAR.name())) {
							filtros.put("escola", EscolaEnum.ZILAR_ROSAR);
						} else if (escolaSelecionada.equals(EscolaEnum.VOVO_MARIA.name())) {
							filtros.put("escola", EscolaEnum.VOVO_MARIA);
						} else if (escolaSelecionada.equals(EscolaEnum.VOO_LIVRE.name())) {
							filtros.put("escola", EscolaEnum.VOO_LIVRE);
						} else if (escolaSelecionada.equals(EscolaEnum.VIVENCIA.name())) {
							filtros.put("escola", EscolaEnum.VIVENCIA);
						} else if (escolaSelecionada.equals(EscolaEnum.VENCESLAU.name())) {
							filtros.put("escola", EscolaEnum.VENCESLAU);
						} else if (escolaSelecionada.equals(EscolaEnum.RODA_PIAO.name())) {
							filtros.put("escola", EscolaEnum.RODA_PIAO);
						} else if (escolaSelecionada.equals(EscolaEnum.PROJETO_ESPERANCA.name())) {
							filtros.put("escola", EscolaEnum.PROJETO_ESPERANCA);
						} else if (escolaSelecionada.equals(EscolaEnum.MODELO.name())) {
							filtros.put("escola", EscolaEnum.MODELO);
						} else if (escolaSelecionada.equals(EscolaEnum.MULLER.name())) {
							filtros.put("escola", EscolaEnum.MULLER);
						} else if (escolaSelecionada.equals(EscolaEnum.MULTIPLA_ESCOLHA.name())) {
							filtros.put("escola", EscolaEnum.MULTIPLA_ESCOLHA);
						} else if (escolaSelecionada.equals(EscolaEnum.N_S_FATIMA.name())) {
							filtros.put("escola", EscolaEnum.N_S_FATIMA);
						} else if (escolaSelecionada.equals(EscolaEnum.NOVA_ESPERANCA.name())) {
							filtros.put("escola", EscolaEnum.NOVA_ESPERANCA);
						} else if (escolaSelecionada.equals(EscolaEnum.PARAISO_DO_AMOR.name())) {
							filtros.put("escola", EscolaEnum.PARAISO_DO_AMOR);
						} else if (escolaSelecionada.equals(EscolaEnum.PROF_GUILHERME.name())) {
							filtros.put("escola", EscolaEnum.PROF_GUILHERME);
						} else if (escolaSelecionada.equals(EscolaEnum.PROJETO_ESPERANCA.name())) {
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
					valorTotal = sumAll(ol);
					if (ol != null && ol.size() > 0) {
						long count = alunoService.count(filtros);
						lazyListDataModel.setRowCount((int) count);
						total = count;

						// FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
						// RequestContext.getCurrentInstance().update("tbl:total");

						return ol;
					}
					long count = alunoService.count(filtros);
					total = count;
					// FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
					// RequestContext.getCurrentInstance().update("tbl:total");
					this.setRowCount((int) count);
					return null;

				}
			};
			long count = alunoService.count(null);
			total = count;
			// FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("tbl:total");
			// RequestContext.getCurrentInstance().update("tbl:total");
			lazyListDataModel.setRowCount((int) count);

		}

		return lazyListDataModel;

	}

	private Double sumAll(List<Aluno> alunos) {
		Double total = 0D;
		for (Aluno a : alunos) {
			total += a.getValorMensal();
		}
		return total;
	}

	public void enviarEmailBoletoAtualEAtrasado(final Long idAluno) {
		new Thread() {
			@Override
			public void run() {
				EnviadorEmail email = new EnviadorEmail();
				email.enviarEmailBoletosMesAtualEAtrasados(idAluno);
			}
		}.start();
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

					filtros.put("anoLetivo", configuracao.getAnoLetivo() -1);

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
		
		DateFormat fomatadorData = DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale("pt", "BR"));
		String aniversario = fomatadorData.format(aluno.getDataNascimento());

		HashMap<String, String> trocas = new HashMap<>();
		if(aluno.getRematricular() != null && aluno.getRematricular()){
			trocas.put("#ANOCONTRATO", configuracao.getAnoRematricula()+"");
		}else{
			trocas.put("#ANOCONTRATO", configuracao.getAnoLetivo()+"");
		}
		trocas.put("#CONTRATANTECID", "Palhoça"); // TODO COLOCAR CIDADE DO
													// Contratado
		trocas.put("#DATAEXTENSO", dataExtenso);

		trocas.put("#CONTRATANTENOME", aluno.getNomeResponsavel());
		trocas.put("#CONTRATANTERG", aluno.getRgResponsavel());
		trocas.put("#CONTRATANTECPF", aluno.getCpfResponsavel());
		
		if(aluno.getEnderecoAluno() != null && !aluno.getEnderecoAluno().equalsIgnoreCase("")){
			trocas.put("#TRANSPORTADOALUNORUA", aluno.getEnderecoAluno());
		}else{
			trocas.put("#TRANSPORTADOALUNORUA", aluno.getEndereco() + ", " + aluno.getBairro());
		}
		
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
		trocas.put("#nomecrianca", nomeAluno);
		trocas.put("#TRANSPORTADORUA", aluno.getEndereco() + ", " + aluno.getBairro());
		trocas.put("#TRANSPORTADOESCOLA", aluno.getEscola().getName());
		trocas.put("#escola", aluno.getEscola().getName());

		trocas.put("#nascimento", aniversario);
		trocas.put("#CONTRATANTERUA", aluno.getEndereco() + ", " + aluno.getBairro());
		
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

	public HashMap<String, String> montarContratoRematricula(Aluno aluno) {
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
		String dataExtenso = formatador.format(new Date());
		Calendar dataLim = Calendar.getInstance();
		dataLim.add(Calendar.MONTH, 1);
		int ano = configuracao.getAnoLetivo();
		if(aluno.getRematricular() != null && aluno.getRematricular()){
			ano = configuracao.getAnoRematricula();
		}
		Calendar calendar = Calendar.getInstance();
		DateFormat fomatadorData = DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale("pt", "BR"));
		int anoNascimento  = 0;
		String aniversario = null;
		if(aluno.getDataNascimento() != null){
			Calendar calendarNascimento = Calendar.getInstance();
			calendarNascimento.setTime(aluno.getDataNascimento());
			anoNascimento = calendarNascimento.get(Calendar.YEAR);
			aniversario = fomatadorData.format(aluno.getDataNascimento());
			
		}
		HashMap<String, String> trocas = new HashMap<>();
		trocas.put("#ANOCONTRATO", ano +""); 
		trocas.put("#CONTRATANTECID", "Palhoca"); // TODO COLOCAR CIDADE DO
													// Contratado
		trocas.put("#DATAEXTENSO", dataExtenso);

		trocas.put("#CONTRATANTENOME", aluno.getNomeResponsavel());
		trocas.put("#CONTRATANTERG", aluno.getRgResponsavel());
		trocas.put("#CONTRATANTECPF", aluno.getCpfResponsavel());
		trocas.put("#CONTRATANTERUA", aluno.getEndereco() + ", " + aluno.getBairro());
		boolean devedor = devedorService.isdevedor(aluno);
		
		
		
		if(devedor){
			trocas.put("atencaodevedor", "ATENÇÃO! Consta em nosso sistema débitos pendentes relativos ao transporte escolar do ano letivo de " +ano +".");
			
			trocas.put("devedormensagem", "Caso já tenha efetuado o pagamento favor desconsiderar o aviso acima.");
			trocas.put("finalmensagem", "Informamos que a rematrícula do seu filho(a) está condicionada quitação total das parcelas em aberto até 31/12/"+ ano +". Para acertar os valores em aberto favor entrar em contato com o financeiro através dos telefones 3242-4194 ou 9 8837-5270.");	
		}else{
			trocas.put("atencaodevedor","");
			trocas.put("devedormensagem", "");
			trocas.put("finalmensagem", "");
		}
		
				
		trocas.put("cidaderesp", "");
		trocas.put("ruaaluno", aluno.getEnderecoAluno());
		trocas.put("BAIRROALUNO", aluno.getBairroAluno().getName());
		trocas.put("valorjaneiro", getValor(1,aluno)+"");
		trocas.put("valorfevereiro", getValor(2,aluno)+"");
		trocas.put("valormarco", getValor(3,aluno)+"");
		

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
		
		trocas.put("#TRANSPORTADORUA", aluno.getEndereco() + ", " + aluno.getBairro());
		trocas.put("#TRANSPORTADOESCOLA", aluno.getEscola().getName());
		trocas.put("#escola", aluno.getEscola().getName());

		if(aniversario != null){
			trocas.put("#nascimento", aniversario);
		}else{
			trocas.put("#nascimento", "");
		}
		trocas.put("#CONTRATANTERUA", aluno.getEndereco() + ", " + aluno.getBairro());
		if(anoNascimento>0){
			trocas.put("#idade", (ano-anoNascimento -1)+"");
		}else{
			trocas.put("#idade", "");
		}
		trocas.put("BAIRROESCOLA",aluno.getEscola().getBairro() );
		trocas.put("SERIEALUNO",aluno.getSerie().getName());
		trocas.put("NOMERESPONSAVEL",aluno.getNomeResponsavel());
		trocas.put("PAIRESPONSAVEL",aluno.getNomePaiResponsavel());
		trocas.put("MAERESPONSAVEL",aluno.getNomeMaeResponsavel());
		trocas.put("cpfresponsavel",aluno.getCpfResponsavel());
		trocas.put("#RG",aluno.getRgResponsavel());
		trocas.put("naturalidadealuno","");
		trocas.put("nascimentoresponsavel"," ");
		trocas.put("estadocivil"," ");
		trocas.put("nomeconjugue"," ");
		trocas.put("enderecoresponsavel",aluno.getEndereco());
		trocas.put("cepresponsavel",aluno.getCep());
		trocas.put("cidaderesponsavel",aluno.getCidade());
		trocas.put("#numero"," ");
		trocas.put("#bairro",aluno.getBairro());
		trocas.put("#email1",aluno.getContatoEmail1());
		trocas.put("#email2",aluno.getContatoEmail2());
		if(aluno.getContatoTelefone1() != null){
			trocas.put("telefoneum",!aluno.getContatoTelefone1().equalsIgnoreCase("") ?aluno.getContatoTelefone1():"_______________" );
			trocas.put("contatoum",!aluno.getContatoNome1().equalsIgnoreCase("") ?aluno.getContatoNome1():"____________" );
		}else{
			trocas.put("telefoneum","_______________" );
			trocas.put("contatoum","____________" );
		}
		if(aluno.getContatoTelefone2() != null){
			trocas.put("telefonedois",!aluno.getContatoTelefone2().equalsIgnoreCase("") ?aluno.getContatoTelefone2():"_______________" );
			trocas.put("contatodois",!aluno.getContatoNome2().equalsIgnoreCase("") ?aluno.getContatoNome2():"____________" );
		}else{
			trocas.put("telefonedois","_______________" );
			trocas.put("contatodois","____________" );
		}
		if(aluno.getContatoTelefone3() != null){
			trocas.put("telefonetres",!aluno.getContatoTelefone3().equalsIgnoreCase("") ?aluno.getContatoTelefone3():"_______________" );	
			trocas.put("contatotres",!aluno.getContatoNome3().equalsIgnoreCase("") ?aluno.getContatoNome3():"____________" );
		}else{
			trocas.put("telefonetres","_______________" );	
			trocas.put("contatotres","____________" );
		}if(aluno.getContatoTelefone4() != null){
			trocas.put("telefonequatro",!aluno.getContatoTelefone4().equalsIgnoreCase("") ?aluno.getContatoTelefone4():"_______________" );
			trocas.put("contatoquatro",!aluno.getContatoNome4().equalsIgnoreCase("") ?aluno.getContatoNome4():"____________" );
		}else{
			trocas.put("telefonequatro","_______________" );
			trocas.put("contatoquatro","____________" );
		}
		if(aluno.getContatoTelefone5() != null){
			trocas.put("telefonecinco",!aluno.getContatoTelefone5().equalsIgnoreCase("") ?aluno.getContatoTelefone5():"_______________" );
			trocas.put("contatocinco",!aluno.getContatoNome5().equalsIgnoreCase("") ?aluno.getContatoNome5():"____________" );
		}else{
			trocas.put("telefonecinco","_______________" );
			trocas.put("contatocinco","____________" );

		}
		
		trocas.put("contatoseis",!aluno.getContatoNome5().equalsIgnoreCase("") ?aluno.getContatoNome5():"____________" );
		trocas.put("#NOMEALUNO", nomeAluno);
		
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

	
	private double getValor(int mesMatricula, Aluno aluno2) {
		double valor = tabelaPrecoService.getValor(mesMatricula, aluno2);
		return valor;
	}

	private String getMesInicioPagamento(Aluno aluno2) {
		String mes = "Janeiro";
		switch (aluno2.getNumeroParcelas()) {
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
		if (aluno == null) {
			return false;
		}
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
			nomeArquivo = aluno.getId() + aluno.getNomeResponsavel() + "g";
			ImpressoesUtils.imprimirInformacoesAluno(aluno, "MODELO1-2.doc", montarContrato(aluno), nomeArquivo);
			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "MODELO1-1.doc";
		}

		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nomeArquivo;
		InputStream stream = new FileInputStream(caminho);
		return FileDownload.getContentDoc(stream, nomeArquivo);
	}
	
	public StreamedContent imprimirFichaRematricula(Aluno aluno) throws IOException {
		String nomeArquivo = gerarFichaRematricula(aluno);
		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" +  nomeArquivo;
		InputStream stream = new FileInputStream(caminho);
		return FileDownload.getContentDoc(stream, nomeArquivo);
	}
	
	public String gerarFichaRematricula(Aluno aluno) throws IOException {
		String nomeArquivo = "";
		CompactadorZip.createDir(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" +configuracaoService.getConfiguracao().getAnoLetivo());
		if (aluno != null && aluno.getId() != null) {
			nomeArquivo = configuracaoService.getConfiguracao().getAnoLetivo()+"\\" + aluno.getCodigo() + aluno.getNomeResponsavel() + "R";
			ImpressoesUtils.imprimirInformacoesAluno(aluno, "modeloRematricula.docx", montarContratoRematricula(aluno), nomeArquivo);
			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "modeloRematricula.docx";
		}

		return nomeArquivo;
	}
	
	public StreamedContent imprimirContrato() throws IOException {
		return imprimirContrato(aluno);
	}
	
	public StreamedContent imprimirFichaRematricula() throws IOException {
		return imprimirFichaRematricula(aluno);
	}

	public List<org.escolar.model.Boleto> getBoletosParaPagar(Aluno aluno) {
		List<org.escolar.model.Boleto> boletosParaPagar = new ArrayList<>();
		if (aluno.getBoletos() != null) {
			for (org.escolar.model.Boleto b : aluno.getBoletos()) {
				if ((!Verificador.getStatusEnum(b).equals(StatusBoletoEnum.PAGO))
						&& !(Verificador.getStatusEnum(b).equals(StatusBoletoEnum.CANCELADO))) {
					boletosParaPagar.add(b);
				}
			}

		}
		return boletosParaPagar;
	}

	public List<org.escolar.model.Boleto> getBoletosParaPagar(List<org.escolar.model.Boleto> boletos) {
		List<org.escolar.model.Boleto> boletosParaPagar = new ArrayList<>();
		if (aluno.getBoletos() != null) {
			for (org.escolar.model.Boleto b : boletos) {
				if ((!Verificador.getStatusEnum(b).equals(StatusBoletoEnum.PAGO))
						&& !(Verificador.getStatusEnum(b).equals(StatusBoletoEnum.CANCELADO))) {
					boletosParaPagar.add(b);
				}
			}

		}
		return boletosParaPagar;
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
		if (getLoggedUser().getTipoMembro().equals(TipoMembro.FINANCEIRO)) {
			return "indexFinanceiro";
		}
		return "index";
	}

	public String voltar() {
		if (getLoggedUser().getTipoMembro().equals(TipoMembro.FINANCEIRO)) {
			return "indexFinanceiro";
		}
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
		aluno = alunoService.findById(idTurma);
		Util.addAtributoSessao("aluno", aluno);
		/*alunoService.remover(idTurma);
		if (getLoggedUser().getTipoMembro().equals(TipoMembro.FINANCEIRO)) {
			return "indexFinanceiro";
		}*/
		return "remover";
	}

	public void removerBoleto(Long idBoleto) {
		org.escolar.model.Boleto b = alunoService.findBoletoById(idBoleto);
		for(org.escolar.model.Boleto bol :aluno.getBoletos()){
			if(bol.getId().equals(b.getId())){
				bol.setCancelado(true);
				bol.setValorPago((double) 0);
			}
		}
		//alunoService.removerBoleto(idBoleto);
	}
	
	
	public String removerAluno() {
		for(org.escolar.model.Boleto b : aluno.getBoletos()){
			if(b.getCancelado() == null || !b.getCancelado().booleanValue()){
				b.setManterAposRemovido(true);
			}
		}
		alunoService.remover(aluno);
		Util.removeAtributoSessao("aluno");
		if (getLoggedUser().getTipoMembro().equals(TipoMembro.FINANCEIRO)) {
			return "indexFinanceiro";
		}
		return "index";
	}


	
	public String restaurar(Long idTurma) {
		alunoService.restaurar(idTurma);
		if (getLoggedUser().getTipoMembro().equals(TipoMembro.FINANCEIRO)) {
			return "indexFinanceiro";
		}
		return "index";
	}

	public String restaurarCancelado(Long id) {
		alunoService.rematricularCancelado(id);
		return "ok";
	}

	public String restaurarCancelado() {
		return restaurarCancelado(aluno.getId());
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

	public StreamedContent downloadBoleto(org.escolar.model.Boleto boleto) {
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(boleto.getVencimento());
			CNAB240_SICOOB cnab = new CNAB240_SICOOB(1);
			String nomeArquivo = aluno.getCodigo() + c.get(Calendar.MONTH) + ".pdf";

			Pagador pagador = new Pagador();
			pagador.setBairro(aluno.getBairro());
			pagador.setCep(aluno.getCep());
			pagador.setCidade(aluno.getCidade() != null ? aluno.getCidade() : "PALHOCA");
			pagador.setCpfCNPJ(aluno.getCpfResponsavel());
			pagador.setEndereco(aluno.getEndereco());
			pagador.setNome(aluno.getNomeResponsavel());
			pagador.setNossoNumero(boleto.getNossoNumero() + "");
			pagador.setUF("SC");
			List<Boleto> boletos = new ArrayList<>();
			Boleto b = new Boleto();
			b.setEmissao(boleto.getEmissao());
			b.setId(boleto.getId());
			b.setNossoNumero(String.valueOf(boleto.getNossoNumero()));
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

	public void gerarBoletoModel() {
		alunoService.gerarBoletos();
	}

	public StreamedContent gerarBoleto() {
		try {

			CNAB240_SICOOB cnab = new CNAB240_SICOOB(1);
			/*
			 * Calendar c = Calendar.getInstance();
			 * c.setTime(boleto.getVencimento());
			 */
			String nomeArquivo = aluno.getCodigo() + aluno.getNomeResponsavel().replace(" ", "") + ".pdf";

			Pagador pagador = new Pagador();
			pagador.setBairro(aluno.getBairro());
			pagador.setCep(aluno.getCep());
			pagador.setCidade(aluno.getCidade() != null ? aluno.getCidade() : "PALHOCA");
			pagador.setCpfCNPJ(aluno.getCpfResponsavel());
			pagador.setEndereco(aluno.getEndereco());
			pagador.setNome(aluno.getNomeResponsavel());
			pagador.setNossoNumero(aluno.getCodigo());
			pagador.setUF("SC");
			pagador.setBoletos(Formatador.getBoletosFinanceiro(getBoletosParaPagar(aluno)));

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
			InputStream stream = FileUtils.gerarCNB240(sequencialArquivo, nomeArquivo, aluno);
			configuracaoService.incrementaSequencialArquivoCNAB();

			return FileDownload.getContentDoc(stream, nomeArquivo);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public StreamedContent gerarArquivoBaixa() {

		List<org.escolar.model.Boleto> boletosParaBaixa = financeiroService.getBoletosParaBaixa();
		String pastaTemporaria = FileUtils.getPastaTemporariaSistema().getAbsolutePath() + System.currentTimeMillis()
				+ File.separator;
		FileUtils.criarDiretorioSeNaoExiste(pastaTemporaria);
		for (org.escolar.model.Boleto boleto : boletosParaBaixa) {
			try {
				String sequencialArquivo = configuracaoService.getSequencialArquivo() + "";
				String nomeArquivo = pastaTemporaria + "CNAB240_" + boleto.getPagador().getCodigo() + "_BAIXA"
						+ System.currentTimeMillis() + ".txt";

				Pagador pagador = new Pagador();

				pagador.setBairro(boleto.getPagador().getBairro());
				pagador.setCep(boleto.getPagador().getCep());
				pagador.setCidade(
						boleto.getPagador().getCidade() != null ? boleto.getPagador().getCidade() : "PALHOCA");
				pagador.setCpfCNPJ(boleto.getPagador().getCpfResponsavel());
				pagador.setEndereco(boleto.getPagador().getEndereco());
				pagador.setNome(boleto.getPagador().getNomeResponsavel());
				pagador.setNossoNumero(boleto.getPagador().getCodigo());
				pagador.setUF("SC");

				pagador.setBoletos(getBoletoFinanceiro(boleto));

				CNAB240_REMESSA_SICOOB remessaCNAB240 = new CNAB240_REMESSA_SICOOB(1);
				byte[] arquivo = remessaCNAB240.geraBaixa(pagador, sequencialArquivo);

				FileUtils.escreveBinarioEmDiretorio(arquivo, new File(nomeArquivo));

				try {
					configuracaoService.incrementaSequencialArquivoCNAB();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		String nomeDoZip = pastaTemporaria + "_Baixa_.zip";

		try {
			CompactadorZip.compactarParaZip(nomeDoZip, pastaTemporaria);
			byte[] arquivoZip = FileUtils.getBytesFromFile(new File(nomeDoZip));
			InputStream stream = new ByteArrayInputStream(arquivoZip);
			return FileDownload.getContentDoc(stream, "baixa.zip");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	public StreamedContent gerarRematricula_TodosAlunos() {
		try {
			Map<String,Object> parametros = new HashMap<>();
			parametros.put("removido", false);
			parametros.put("anoLetivo", configuracaoService.getConfiguracao().getAnoLetivo());
			List<Aluno> todosAlunos = alunoService.findAll(parametros);

			String caminhoFinalPasta = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" 
					+configuracaoService.getConfiguracao().getAnoLetivo();
			CompactadorZip.createDir(caminhoFinalPasta);
			
			//todosAlunos.size();
			//int i=0;i<200; i++
			for(Aluno al:todosAlunos){
				//Aluno al = todosAlunos.get(i);
				if(al.getBairroAluno() != null){
					String nome = gerarFichaRematricula(al); //todosAlunos.get(i)
					String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nome;
					InputStream stream = new FileInputStream(caminho);
					
					System.out.println(caminho);
					FileUtils.inputStreamToFile(stream, nome);	
				}
			}
			
			String arquivoSaida = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\"+"escolartodasREMATRICULAS.zip";
			CompactadorZip.compactarParaZip(arquivoSaida, caminhoFinalPasta);
			
			InputStream stream2 =  new FileInputStream(arquivoSaida);
			return FileDownload.getContentDoc(stream2, "escolartodasREMATRICULAS.zip");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private List<Boleto> getBoletoFinanceiro(org.escolar.model.Boleto boleto) {
		List<org.aaf.financeiro.model.Boleto> boletosFinanceiro = new ArrayList<>();
		org.aaf.financeiro.model.Boleto boletoFinanceiro = new org.aaf.financeiro.model.Boleto();
		boletoFinanceiro.setEmissao(boleto.getEmissao());
		boletoFinanceiro.setId(boleto.getId());
		boletoFinanceiro.setValorNominal(boleto.getValorNominal());
		boletoFinanceiro.setVencimento(boleto.getVencimento());
		boletoFinanceiro.setNossoNumero(String.valueOf(boleto.getNossoNumero()));
		boletoFinanceiro.setDataPagamento(OfficeUtil.retornaDataSomenteNumeros(boleto.getDataPagamento()));
		boletoFinanceiro.setValorPago(boleto.getValorPago());
		boletosFinanceiro.add(boletoFinanceiro);
		return boletosFinanceiro;
	}

	public String getDescontoString(org.escolar.model.Boleto boleto) {
		return Util.formatarDouble2Decimais(Verificador.getDesconto(boleto));
	}

	public String getJurosMultaString(org.escolar.model.Boleto boleto) {
		return Util.formatarDouble2Decimais(Verificador.getJurosMulta(boleto));
	}

	public Double getValorFinal(org.escolar.model.Boleto boleto) {
		return Verificador.getValorFinal(boleto);
	}

	public String getValorFinalString(org.escolar.model.Boleto boleto) {

		return Util.formatarDouble2Decimais(getValorFinal(boleto));
	}

	public String getStatus(org.escolar.model.Boleto boleto) {
		return Verificador.getStatus(boleto);
	}

	public String marcarLinha(Long idAluno) {
		String cor = "";
		if (idAluno == null) {
			return "";
		}
		Aluno a = alunoService.findById(idAluno);
		if (!todosDadosContratoValidos(a)) {
			cor = "marcarLinhaVermelho";
		} else if (a.getRematricular() != null && a.getRematricular()) {
			cor = "marcarLinhaVerde";
		} else if (!cpfInvalido(a)) {
			cor = "marcarLinhaAmarelo";
		} else {
			boolean estaNaTurma = alunoService.estaEmUmaTUrma(idAluno);
			if (!estaNaTurma) {
				cor = "marcarLinha";
			}
		}
		return cor;
	}

	public boolean isCnabEnvado() {
		if (aluno == null) {
			return false;
		}
		if (aluno.getCnabEnviado() == null) {
			return false;
		}
		return aluno.getCnabEnviado();
	}

	public boolean isVerificadoOk() {
		if (aluno == null) {
			return false;
		}
		if (aluno.getVerificadoOk() == null) {
			return false;
		}
		return aluno.getVerificadoOk();
	}
	
	public boolean isVerificadoOk(Long idAluno) {
		Aluno a = alunoService.findById(idAluno);
		if (a == null) {
			return false;
		}
		if (a.getVerificadoOk() == null) {
			return false;
		}
		return a.getVerificadoOk();
	}
	
	public String enviarCnab() {
		return enviarCnab(aluno.getId());
	}

	public String removerCnabEnviado() {
		return removerCnabEnviado(aluno.getId());
	}

	private String removerCnabEnviado(Long id) {
		alunoService.removerCnabEnviado(id);
		return "ok";
	}

	private String enviarCnab(Long id) {
		alunoService.enviarCnab(id);
		return "ok";
	}

	public String verificarOk() {
		return verificarOk(aluno.getId());
	}

	public String removerVerificadoOk() {
		return removerVerificadoOk(aluno.getId());
	}

	private String removerVerificadoOk(Long id) {
		alunoService.removerVerificadoOk(id);
		return "ok";
	}

	private String verificarOk(Long id) {
		alunoService.verificadoOk(id);
		return "ok";
	}

	private boolean cpfInvalido(Aluno aluno) {
		return Verificador.isCPF(aluno.getCpfResponsavel());
	}

	private boolean todosDadosContratoValidos(Aluno aluno) {
		boolean todoDadosContratoValidos = true;

		if (aluno.getNomeAluno() == null || aluno.getNomeAluno().equalsIgnoreCase("")) {
			todoDadosContratoValidos = false;
		} else if (aluno.getCodigo() == null || aluno.getCodigo().equalsIgnoreCase("")) {
			todoDadosContratoValidos = false;
		} else if (aluno.getNomeResponsavel() == null || aluno.getNomeResponsavel().equalsIgnoreCase("")) {
			todoDadosContratoValidos = false;
		} else if (aluno.getCpfResponsavel() == null || aluno.getCpfResponsavel().equalsIgnoreCase("")
				|| aluno.getCpfResponsavel().length() < 11 || aluno.getCpfResponsavel().length() > 11) {
			todoDadosContratoValidos = false;
		} else if (aluno.getRgResponsavel() == null || aluno.getRgResponsavel().equalsIgnoreCase("")) {
			todoDadosContratoValidos = false;
		} else if (aluno.getEndereco() == null || aluno.getEndereco().equalsIgnoreCase("")) {
			todoDadosContratoValidos = false;
		} else if (aluno.getBairro() == null || aluno.getBairro().equalsIgnoreCase("")) {
			todoDadosContratoValidos = false;
		} else if (aluno.getCep() == null || aluno.getCep().equalsIgnoreCase("")) {
			todoDadosContratoValidos = false;
		} else if (aluno.getValorMensal() == 0 || aluno.getValorMensal() > 1000 || aluno.getValorMensal() < 150) {
			todoDadosContratoValidos = false;
		} else if (aluno.getBoletos() != null && aluno.getBoletos().size() < 1 && aluno.getBoletos().size() > 12) {
			todoDadosContratoValidos = false;
		}

		return todoDadosContratoValidos;
	}

	public void importarCNAB240Sicoob() throws ParseException {
		try {
			List<Pagador> boletosImportados = CNAB240_RETORNO_SICOOB.imporCNAB240(
					Constante.LOCAL_ARMAZENAMENTO_REMESSA + "tefamel" + File.separator + "importando" + File.separator);
			importarBoletos(boletosImportados, false);

		} catch (Exception e) {

		}
		try {
			ImportadorArquivo arquivo = new ImportadorArquivo();

			List<Pagador> boletosImportados = arquivo.importarExtratoBancarioConciliacao(1);
			importarBoletos(boletosImportados, true);

		} catch (Exception e) {

		}

	}

	public void importarBoletos(List<Pagador> boletosImportados, boolean extratoBancario) throws ParseException {
		int contador = 0;
		for (Pagador pagador : boletosImportados) {
			Boleto boletoCNAB = pagador.getBoletos().get(0);
			String numeroDocumento = boletoCNAB.getNossoNumero();
			contador += 1;
			System.out.println("contador : " + contador);
			System.out.println("Documento : " + numeroDocumento);
			if (numeroDocumento != null && !numeroDocumento.equalsIgnoreCase("") && !numeroDocumento.contains("-")) {
				try {
					numeroDocumento = numeroDocumento.trim().replace(" ", "").replace("/",
							"".replace("-", "").replace(".", ""));
					if (numeroDocumento.matches("^[0-9]*$")) {
						Long numeroDocumentoLong = Long.parseLong(numeroDocumento);
						org.escolar.model.Boleto boleto = null;
						if (!extratoBancario) {
							numeroDocumentoLong -= 10000;
							boleto = financeiroService.findBoletoByID(numeroDocumentoLong);
						} else {
							String numeroDocumentoExtrato = String.valueOf(numeroDocumentoLong);
							boleto = financeiroService.findBoletoByNumeroModel(
									numeroDocumentoExtrato.substring(0, numeroDocumentoExtrato.length() - 1));
						}
						System.out.println(numeroDocumento.trim().replace(" ", ""));
						if (boleto != null) {
							if (!Verificador.getStatusEnum(boleto).equals(StatusBoletoEnum.PAGO)) {
								boleto.setValorPago(boletoCNAB.getValorPago());
								boleto.setDataPagamento(OfficeUtil.retornaData(boletoCNAB.getDataPagamento()));
								boleto.setConciliacaoPorExtrato(extratoBancario);
								financeiroService.save(boleto);
								System.out.println("YESS, BOLETO PAGO");
							}
						}
					}

				} catch (ClassCastException cce) {
					cce.printStackTrace();
				}
			}
		}
	}

	public void alterarBoleto(org.escolar.model.Boleto boleto) {
		financeiroService.alterarBoletoManualmente(boleto);
	}

	public boolean cadastroOk(Long idAluno) {
		Aluno a = alunoService.findById(idAluno);
		boolean estaNaTurma = alunoService.estaEmUmaTUrma(idAluno);
		if (estaNaTurma) {
			return false;
		} else if (!Verificador.isCPF(a.getCpfResponsavel())) {
			return false;
		}
		return true;
	}

	public boolean cnabEnviado(Long idAluno) {
		Aluno a = alunoService.findById(idAluno);

		if (a.getCnabEnviado() != null && a.getCnabEnviado()) {
			return true;
		} else {
			return false;
		}
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public Double getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(Double valorTotal) {
		this.valorTotal = valorTotal;
	}

	public Member getLoggedUser() {
		try {

			Member user = null;
			if (SecurityUtils.getSubject().getPrincipal() != null) {
				System.out.println("CONSTRUIU O USUARIO LOGADO !!");
				user = (Member) SecurityUtils.getSubject().getPrincipal();
			}

			return user;

		} catch (Exception ex) {
			// Logger.getLogger(MemberController.class.getSimpleName()).log(Level.WARNING,
			// null, ex);
			ex.printStackTrace();
		}

		return null;
	}

}
