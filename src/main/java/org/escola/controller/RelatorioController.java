/*

 * JBoss, Home of Professional Open Source
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
package org.escola.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.enums.CanalMensagem;
import org.escolar.enums.EscolaEnum;
import org.escolar.enums.PerioddoEnum;
import org.escolar.enums.Serie;
import org.escolar.enums.StatusContratoEnum;
import org.escolar.enums.TipoMensagem;
import org.escolar.enums.TipoMobilidadeEnum;
import org.escolar.model.Aluno;
import org.escolar.model.Carro;
import org.escolar.model.Configuracao;
import org.escolar.model.ContratoAluno;
import org.escolar.model.MensagemAluno;
import org.escolar.model.PromessaPagamentoBoleto;
import org.escolar.service.AlunoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.FinanceiroService;
import org.escolar.service.MensagemAlunoService;
import org.escolar.service.PromessaPagamentoService;
import org.escolar.service.RelatorioService;
import org.escolar.service.TabelaPrecoService;
import org.escolar.util.Util;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.com.aaf.base.base.EnviadorWhats;
import br.com.aaf.base.whats.model.Parametro;

@Named
@ViewScoped
public class RelatorioController implements Serializable {

	/****/
	private static final long serialVersionUID = 1L;

	@Inject
	private RelatorioService relatorioService;

	@Inject
	private AlunoService alunoService;

	@Inject
	private MensagemAlunoService mensagemAlunoService;

	@Inject
	private ConfiguracaoService configuracaoService;

	private Configuracao configuracao;

	@Inject
	private FinanceiroService financeiroService;
	
	@Inject
	private PromessaPagamentoService promessaPagamentoService;

	private Aluno aluno;

	private Integer anoSelecionado;
	
	private StatusContratoEnum statusContrato;
	
	private Integer mesSelecionadoRelatorio;

	private List<Aluno> devedores = new ArrayList<Aluno>();

	@PostConstruct
	private void init() {
		setConfiguracao(configuracaoService.getConfiguracao());
		Object obj = Util.getAtributoSessao("aluno");
		if (obj != null) {
			setAluno((Aluno) obj);
		}
		Object obj2 = Util.getAtributoSessao("anoSelecionado");
		if (obj2 != null) {
			anoSelecionado = (Integer) obj2;
		} else {
			anoSelecionado = configuracao.getAnoLetivo();
		}
		
		Object obj3 = Util.getAtributoSessao("mesSelecionadoRelatorio");
		if (obj3 != null) {
			mesSelecionadoRelatorio = (Integer) obj3;
		} else {
			Calendar c = Calendar.getInstance();
			
			mesSelecionadoRelatorio = c.get(Calendar.MONTH)+1;
		}
		
		

	}

	private Serie maternal;
	private Serie jardimI;
	private Serie jardimII;
	private Serie pre;
	private Serie primeiro;
	private Serie segundo;
	private Serie terceiro;
	private Serie quarto;
	private Serie quinto;

	private PerioddoEnum manha;
	private PerioddoEnum tarde;
	private PerioddoEnum integral;

	private int quantidadeAtrasados;
	private int mesAtrasado;

	private int mesSelecionado = 1;

	private LazyDataModel<Aluno> lazyListDataModelQuantidade;
	private LazyDataModel<Aluno> lazyListDataModelMes;
	private LazyDataModel<Aluno> lazyListDataModelContrato;
	
	@Inject
	private TabelaPrecoService tabelaPrecoService;

	public double getNotasEnviadas(int mes) {
		return relatorioService.getTotalNotasEmitidas(mes);
	}

	public List<String> getResponsaveisNotasEnviadas(int mes) {
		return relatorioService.getResponsaveisNotasEnviadas(mes);
	}

	public List<String> getResponsaveisNotasEnviadas() {
		return relatorioService.getResponsaveisNotasEnviadas(mesSelecionado);
	}

	public int quantidadeIrmaos(Aluno aluno) {
		int quatidade = 1;
		if (aluno.getIrmao1() != null) {
			quatidade++;
		}
		if (aluno.getIrmao2() != null) {
			quatidade++;
		}
		if (aluno.getIrmao3() != null) {
			quatidade++;
		}

		return quatidade;
	}

	public int quantidadeCarrosIda(Aluno aluno) {
		int carros = 1;
		if (aluno.getIdaVolta() == 2) { // Soh volta
			return 0;
		} else {
			if (aluno.isTrocaIDA()) {
				carros++;
			}
			if ((aluno.isTrocaIDA2() != null && aluno.isTrocaIDA2())) {
				carros++;
			}
			if (aluno.isTrocaIDA3() != null && aluno.isTrocaIDA3()) {
				carros++;
			}
		}

		return carros;
	}

	public int quantidadeCarrosVolta(Aluno aluno) {
		int carros = 1;
		if (aluno.getIdaVolta() == 1) { // Soh Vai
			return 0;
		} else {
			if (aluno.isTrocaVolta()) {
				carros++;
			}
			if (aluno.isTrocaVolta2() != null && aluno.isTrocaVolta2()) {
				carros++;
			}
			if (aluno.isTrocaVolta3() != null && aluno.isTrocaVolta3()) {
				carros++;
			}

		}

		return carros;
	}

	public boolean leva(Aluno aluno, Carro carro) {
		try {
			if (aluno.getCarroLevaParaEscola() != null && aluno.getCarroLevaParaEscola().equals(carro)) {
				return true;
			} else if (aluno.getCarroLevaParaEscolaTroca() != null
					&& aluno.getCarroLevaParaEscolaTroca().equals(carro)) {
				return true;
			} else if (aluno.getCarroLevaParaEscolaTroca2() != null
					&& aluno.getCarroLevaParaEscolaTroca2().equals(carro)) {
				return true;
			} else if (aluno.getCarroLevaParaEscolaTroca3() != null
					&& aluno.getCarroLevaParaEscolaTroca3().equals(carro)) {
				return true;
			}

		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public boolean pega(Aluno aluno, Carro carro) {
		try {
			if (aluno.getCarroPegaEscola() != null && aluno.getCarroPegaEscola().equals(carro)) {
				return true;
			} else if (aluno.getCarroPegaEscolaTroca() != null && aluno.getCarroPegaEscolaTroca().equals(carro)) {
				return true;
			} else if (aluno.getCarroPegaEscolaTroca2() != null && aluno.getCarroPegaEscolaTroca2().equals(carro)) {
				return true;
			} else if (aluno.getCarroPegaEscolaTroca3() != null && aluno.getCarroPegaEscolaTroca3().equals(carro)) {
				return true;
			}

		} catch (Exception e) {
			return false;

		}
		return false;
	}

	public double getTotalAlunos() {
		Map<String, Object> filtros = new HashMap<>();
		return relatorioService.countAlunos(filtros);
	}

	public double getTotalAlunosManha() {
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("periodo", PerioddoEnum.MANHA);
		return relatorioService.countAlunos(filtros);
	}

	public double getTotalAlunosTarde() {
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("periodo", PerioddoEnum.TARDE);
		return relatorioService.countAlunos(filtros);
	}

	public long getTotalAlunosIntegral() {
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("periodo", PerioddoEnum.INTEGRAL);
		return relatorioService.count(filtros);
	}

	public double getTotalAlunos(Serie serie, PerioddoEnum periodo) {
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("serie", getSerie(serie));
		filtros.put("periodo", getPeriodo(periodo));
		return relatorioService.countAlunos(filtros);
	}

	public int getTotalAlunos(Carro carro) {
		int quantidade = 0;
		List<Aluno> todosAlunos = alunoService.findAll();
		for (Aluno aluno : todosAlunos) {
			boolean pega = pega(aluno, carro);
			boolean leva = leva(aluno, carro);

			if (pega || leva) {
				quantidade++;
			}
		}

		return quantidade;
	}

	public double getValorTotal2(Carro carro) {
		double totalCriancasSoLeva = relatorioService.getValorTotalMensalidade(carro, TipoMobilidadeEnum.SO_VAI);
		double totalCriancasSoLevaComTrocaIda = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.SO_VAI_TROCA_IDA) / 2;

		double totalCriancasSoPega = relatorioService.getValorTotalMensalidade(carro, TipoMobilidadeEnum.SO_VOLTA);
		double totalCriancasSoPegaComTroca = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.SO_VOLTA_TROCA_VOLTA) / 2;

		double totalvaiVoltaComOutroCarro = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.VAI_VOLTA_IDA_CARRO_DIFERENTE);
		double totalvaiVoltaComOutroCarroComtrocaIda = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.VAI_VOLTA_IDA_COM_TROCA_IDA);
		double totalvaiVoltaComOutroCarroComtrocaIdaeVolta = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.VAI_VOLTA_IDA_COM_TROCA_IDA_E_COM_TROCA_VOLTA);
		double totalvaiVoltaComOutroCarroComtrocaVolta = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.VAI_VOLTA_IDA_COM_TROCA_VOLTA);

		double totalvaivoltaVaicomOutroCarro = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.VAI_VOLTA_VOLTA_CARRO_DIFERENTE);
		double totalvaivoltaVaicomOutrocarroTrocaIda = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.VAI_VOLTA_VOLTA_COM_TROCA_IDA);
		double totalvaivoltaVaicomOutrocarroTrocaIdaENaVolta = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.VAI_VOLTA_VOLTA_COM_TROCA_IDA_E_COM_TROCA_VOLTA);
		double totalvaivoltaVaicomOutrocarroTrocaNaVolta = relatorioService.getValorTotalMensalidade(carro,
				TipoMobilidadeEnum.VAI_VOLTA_VOLTA_COM_TROCA_VOLTA);

		// TROCA na IDA e TROCA na VOLTA carros iguais
		return totalCriancasSoLeva + totalCriancasSoLevaComTrocaIda + totalCriancasSoPega + totalCriancasSoPegaComTroca
				+ totalvaiVoltaComOutroCarro + totalvaiVoltaComOutroCarroComtrocaIda
				+ totalvaiVoltaComOutroCarroComtrocaIdaeVolta + totalvaiVoltaComOutroCarroComtrocaVolta
				+ totalvaivoltaVaicomOutroCarro + totalvaivoltaVaicomOutrocarroTrocaIda
				+ totalvaivoltaVaicomOutrocarroTrocaIdaENaVolta + totalvaivoltaVaicomOutrocarroTrocaNaVolta;
	}

	public double getValorTotal(Carro carro) {
		Double valorTotal = 0D;
		List<Aluno> todosAlunos = alunoService.findAll();
		for (Aluno aluno : todosAlunos) {
			if (aluno.getRemovido() != null && aluno.getRemovido() != null) {
				boolean pega = pega(aluno, carro);
				boolean leva = leva(aluno, carro);

				int qdadeIda = quantidadeCarrosIda(aluno);
				int qdadeVolta = quantidadeCarrosVolta(aluno);

				double valorCrianca = 0;
				if (pega || leva) {
					if (qdadeVolta == 0) { // soh vai
						if (leva) {
							valorCrianca = aluno.getContratoVigente().getValorMensal() / qdadeIda;
						}
					} else if (qdadeIda == 0) { // soh volta
						if (pega) {
							valorCrianca = (aluno.getContratoVigente().getValorMensal() - 20) / qdadeVolta;
						}
					} else { // vai e volta
						if (pega) {
							valorCrianca = ((aluno.getContratoVigente().getValorMensal() - 20) / qdadeVolta) / 2;
						}
						if (leva) {
							valorCrianca += ((aluno.getContratoVigente().getValorMensal() - 20) / qdadeIda) / 2;
						}
					}
					int quantidadeIrmaos = quantidadeIrmaos(aluno);
					if (quantidadeIrmaos > 1) {
						valorTotal += (valorCrianca / quantidadeIrmaos);

					} else {
						valorTotal += valorCrianca;
					}
				}

			}

		}
		return valorTotal;
	}

	public double getValorTotalMensalidade(Carro carro) {
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("carroLevaParaEscola", carro);
		filtros.put("carroLevaParaEscolaTroca", carro);
		filtros.put("carroPegaEscola", carro);
		filtros.put("carroPegaEscolaTroca", carro);
		return relatorioService.getValor(filtros);
	}

	public double getValorTotalMensalidade() {
		Map<String, Object> filtros = new HashMap<>();
		return relatorioService.getValor(filtros);
	}

	// TODO nao pergunte =) converter para o server funcionar
	private Serie getSerie(Serie serie) {
		return serie;
	}

	// TODO nao pergunte =) converter para o server funcionar
	private PerioddoEnum getPeriodo(PerioddoEnum periodo) {
		return periodo;
	}

	public Serie getMaternal() {
		return Serie.MATERNAL;
	}

	public void setMaternal(Serie maternal) {
		this.maternal = maternal;
	}

	public Serie getJardimI() {
		return Serie.JARDIM_I;
	}

	public void setJardimI(Serie jardimI) {
		this.jardimI = jardimI;
	}

	public Serie getJardimII() {
		return Serie.JARDIM_II;
	}

	public void setJardimII(Serie jardimII) {
		this.jardimII = jardimII;
	}

	public Serie getPre() {
		return Serie.PRE;
	}

	public void setPre(Serie pre) {
		this.pre = pre;
	}

	public Serie getPrimeiro() {
		return Serie.PRIMEIRO_ANO;
	}

	public void setPrimeiro(Serie primeiro) {
		this.primeiro = primeiro;
	}

	public Serie getSegundo() {
		return Serie.SEGUNDO_ANO;
	}

	public void setSegundo(Serie segundo) {
		this.segundo = segundo;
	}

	public Serie getTerceiro() {
		return Serie.TERCEIRO_ANO;
	}

	public void setTerceiro(Serie terceiro) {
		this.terceiro = terceiro;
	}

	public Serie getQuarto() {
		return Serie.QUARTO_ANO;
	}

	public void setQuarto(Serie quarto) {
		this.quarto = quarto;
	}

	public Serie getQuinto() {
		return Serie.QUINTO_ANO;
	}

	public void setQuinto(Serie quinto) {
		this.quinto = quinto;
	}

	public PerioddoEnum getManha() {
		return PerioddoEnum.MANHA;
	}

	public void setManha(PerioddoEnum manha) {
		this.manha = manha;
	}

	public PerioddoEnum getTarde() {
		return PerioddoEnum.TARDE;
	}

	public void setTarde(PerioddoEnum tarde) {
		this.tarde = tarde;
	}

	public PerioddoEnum getIntegral() {
		return PerioddoEnum.INTEGRAL;
	}

	public void setIntegral(PerioddoEnum integral) {
		this.integral = integral;
	}

	public Configuracao getConfiguracao() {
		return configuracao;
	}

	public void setConfiguracao(Configuracao configuracao) {
		this.configuracao = configuracao;
	}

	public int getMesSelecionado() {
		return mesSelecionado;
	}

	public void setMesSelecionado(int mesSelecionado) {
		this.mesSelecionado = mesSelecionado;
	}

	public String getTotalCriancasDevendo(int mesBusca) {
		return String.valueOf(financeiroService.getQuantidadeBoletosAtrasadosPorMes(mesBusca, anoSelecionado));
	}

	public String getQuantidadeAtrasadosPorQuantidade(int quantidade) {
		return String.valueOf(financeiroService.getQuantidadeBoletosAtrasadosPorQuantidade(quantidade, anoSelecionado));
	}

	public String getQuantidadeBoletosAtrasados() {
		return String.valueOf(financeiroService.getQuantidadeBoletosAtrasados(anoSelecionado));
	}

	public String getValorTotalBoletosAtrasados() {
		return String.valueOf(financeiroService.getValorTotalAtrasado(anoSelecionado));
	}

	public String getValorBoletosAtrasadosPorMes(int mes) {

		return String.valueOf(financeiroService.getValorBoletosAtrasadosPorMes(mes, anoSelecionado));
	}

	public String rotaMes(int mes) {
		mesAtrasado = mes;
		Util.addAtributoSessao("mesAtrasado", mesAtrasado);
		Util.addAtributoSessao("anoSelecionado", anoSelecionado);
		return "listagemAlunosAtrasadosMes";
	}

	public String rotaQuantidade(int quantidade) {
		quantidadeAtrasados = quantidade;
		Util.addAtributoSessao("quantidadeAtrasados", quantidadeAtrasados);
		Util.addAtributoSessao("anoSelecionado", anoSelecionado);
		return "listagemAlunosAtrasadosQuantidade";
	}

	public String getCriancas() {
		Object obj = Util.getAtributoSessao("quantidadeAtrasados");
		if (obj != null) {
			quantidadeAtrasados = (int) obj;
		}
		Object obj2 = Util.getAtributoSessao("mesAtrasado");
		if (obj2 != null) {
			mesAtrasado = (int) obj2;
		}

		String g = quantidadeAtrasados + "";
		String w = mesAtrasado + "";
		return g + w;
	}

	public LazyDataModel<Aluno> getLazyDataModelQuantidade() {
		if (lazyListDataModelQuantidade == null) {

			lazyListDataModelQuantidade = new LazyDataModel<Aluno>() {

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

					Object obj = Util.getAtributoSessao("quantidadeAtrasados");
					if (obj != null) {
						quantidadeAtrasados = (int) obj;
					}
					filtros.put("quantidadeAtrasados", quantidadeAtrasados);
					filtros.put("anoSelecionado", anoSelecionado);

					List<Aluno> ol = financeiroService.findAlunoQuantidade(first, pageSize, orderByParam, orderParam,
							filtros);
					if (ol != null && ol.size() > 0) {
						lazyListDataModelQuantidade.setRowCount(ol.size() +1);
						return ol;
					}

					this.setRowCount(Integer.valueOf(getQuantidadeAtrasadosPorQuantidade(quantidadeAtrasados)));
					return null;
				}
			};
			lazyListDataModelQuantidade
					.setRowCount(Integer.valueOf(getQuantidadeAtrasadosPorQuantidade(quantidadeAtrasados)));
		}
		return lazyListDataModelQuantidade;
	}

	public String getNomeDevedor(int posicao) {
		Map<String, Object> filtros = new HashMap<String, Object>();
		filtros.put("quantidadeDevedores", 30);
		filtros.put("anoSelecionado", anoSelecionado);
		if (devedores == null || devedores.isEmpty()) {
			devedores = financeiroService.findMaioresDevedores(filtros);
		}
		if (devedores.size() <= posicao) {
			return "";
		}

		return devedores.get(posicao).getNomeAluno();
	}

	public String verDevedor(int posicao) {
		if (devedores != null) {
			aluno = alunoService.findById(devedores.get(posicao).getId());
			Util.addAtributoSessao("aluno", aluno);
		}
		return "verdevedor";
	}

	public Long getIdDevedor(int posicao) {
		if (devedores.size() <= posicao) {
			return 0L;
		}
		devedores.get(posicao).getId();
		if (devedores != null) {
			return devedores.get(posicao).getId();
		}
		return 0L;
	}

	public String getValorDevidoDevedor(int posicao) {
		Map<String, Object> filtros = new HashMap<String, Object>();
		filtros.put("quantidadeDevedores", 30);
		filtros.put("anoSelecionado", anoSelecionado);
		if (devedores == null || devedores.isEmpty()) {
			devedores = financeiroService.findMaioresDevedores(filtros);
		}
		if (devedores.size() <= posicao) {
			return "";
		}
		return Util.formatarDouble2Decimais(devedores.get(posicao).getTotalABerto());
	}

	public String editar(Long idprof) {
		setAluno(alunoService.findById(idprof));
		Util.addAtributoSessao("aluno", getAluno());
		return "cadastrar";
	}

	public String editar() {
		return editar(getAluno().getId());
	}

	public boolean isAlunoSelecionado() {
		if (getAluno() == null) {
			return false;
		}
		return getAluno().getId() != null ? true : false;
	}

	public void onRowSelect(SelectEvent event) {
		Aluno al = (Aluno) event.getObject();
		FacesMessage msg = new FacesMessage("Car Selected");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public LazyDataModel<Aluno> getLazyDataModelMes() {
		if (lazyListDataModelMes == null) {

			lazyListDataModelMes = new LazyDataModel<Aluno>() {

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

					Object obj = Util.getAtributoSessao("mesAtrasado");
					if (obj != null) {
						mesAtrasado = (int) obj;
					}
					filtros.put("mesAtrasado", mesAtrasado);
					filtros.put("anoSelecionado", anoSelecionado);

					List<Aluno> ol = financeiroService.findAlunoMes(first, pageSize, orderByParam, orderParam, filtros);
					if (ol != null && ol.size() > 0) {
						lazyListDataModelMes.setRowCount(ol.size() +1);
						return ol;
					}

					this.setRowCount(Integer.parseInt(getTotalCriancasDevendo(mesAtrasado)));
					return null;
				}
			};
			lazyListDataModelMes.setRowCount(Integer.parseInt(getTotalCriancasDevendo(mesAtrasado)));
		}
		return lazyListDataModelMes;
	}
	
	public LazyDataModel<Aluno> getLazyDataModelContrato() {
		if (lazyListDataModelContrato == null) {

			lazyListDataModelContrato = new LazyDataModel<Aluno>() {

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
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}
					
					if (statusContrato != null ) {
						filtros.put("statusContrato", statusContrato);
					}

					if (filtros.containsKey("escola")) {
						String escolaSelecionada = filtros.get("escola").toString();
						
						for(EscolaEnum escola :EscolaEnum.values()) {
							if (escolaSelecionada.equals(escola.name())) {
								filtros.put("escola", escola);
							}
						}
					}

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					Object obj = Util.getAtributoSessao("mesAtrasado");
					if (obj != null) {
						mesAtrasado = (int) obj;
					}

					List<Aluno> ol = financeiroService.findAlunosStatusContrato(first, pageSize, orderByParam, orderParam, filtros);
					if (ol != null && ol.size() > 0) {
						lazyListDataModelContrato.setRowCount(Integer.parseInt(getTotalCriancasDevendo(mesAtrasado)));
						return ol;
					}

					this.setRowCount(Integer.parseInt(getTotalCriancasDevendo(mesAtrasado)));
					return null;
				}
			};
			lazyListDataModelContrato.setRowCount(Integer.parseInt(getTotalCriancasDevendo(mesAtrasado)));
		}
		return lazyListDataModelContrato;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public Integer getAnoSelecionado() {
		return anoSelecionado;
	}

	public void setAnoSelecionado(Integer anoSelecionado) {
		this.anoSelecionado = anoSelecionado;
		Util.addAtributoSessao("anoSelecionado", anoSelecionado);
	}

	public void enviarMensagemBoletoAtrasado() {
		Map<String, Object> filtros = new HashMap<String, Object>();
		filtros.put("mesAtrasado", mesAtrasado);
		filtros.put("anoSelecionado", anoSelecionado);

		List<Aluno> ol = financeiroService.findAlunoMes(0, 0, "", "", filtros);
		for (Aluno aluno : ol) {
			if(!possuiMensagemEnviada(aluno.getId(), mesAtrasado, anoSelecionado)){
				if(!possuiPromessaAberta(aluno)){
					if(!aluno.getRemovido()){
						enviarMensagemBoletoAtrasado(aluno.getId());
					}
				}
			}
		}
	}
	
	
	public List<PromessaPagamentoBoleto> getPromessasContratoAberta(Aluno aluno) {
		List<PromessaPagamentoBoleto> promessas = new ArrayList<>();
		if (aluno.getId() != null) {
			promessas = promessaPagamentoService.findByAluno(aluno.getId(),true);
		}
		
		return promessas;
	}
	
	public boolean possuiPromessaAberta(Aluno aluno) {
		return !getPromessasContratoAberta(aluno).isEmpty();
	}
	
	public boolean possuiMensagemEnviada(long idAluno ){
		return possuiMensagemEnviada(idAluno, mesAtrasado, anoSelecionado);
	}
	
	public boolean possuiMensagemEnviada(long idAluno,int mes, int ano ){
		List<MensagemAluno> mensagens = mensagemAlunoService.findByParam(idAluno, String.valueOf(mes), String.valueOf(ano), CanalMensagem.WHATSAPP);
		return !mensagens.isEmpty();
	}
	
	public void saveStatusContrato(Aluno aluno) {
		alunoService.saveStatusCntrato(aluno);
	}
	
	public void enviarMensagemBoletoAtrasado(long idAluno) {
		Aluno aluno = alunoService.findById(idAluno);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, mesAtrasado -1 );
		c.set(Calendar.YEAR,anoSelecionado);
		
		Calendar hj = Calendar.getInstance();
		
		
		String  mensagem = "atrasado";
		if(c.get(Calendar.YEAR) < hj.get(Calendar.YEAR)) {
			mensagem = "aviso_vencimento_boleto2";
		}
		
		if(c.get(Calendar.YEAR) <= hj.get(Calendar.YEAR)) {
			if(c.get(Calendar.MONTH) < hj.get(Calendar.MONTH)) {
				mensagem = "aviso_vencimento_boleto2";
			}
		}
		
		if(c.get(Calendar.YEAR) <= hj.get(Calendar.YEAR)) {
			if(c.get(Calendar.MONTH) <= hj.get(Calendar.MONTH)) {
				if( hj.get(Calendar.DAY_OF_MONTH) > 20) {
					mensagem = "aviso_vencimento_boleto2";
				}
			}
		}
		
		
		List<Parametro> parametros = new ArrayList<Parametro>();
		
		
		parametros.add(new Parametro("1", getNomeResponsavelDevedor(aluno, anoSelecionado)));
		parametros.add(new Parametro("2", getMes(mesAtrasado)));

		List<String> telefones = aluno.contatosWhatsValido();
		
		

		if (aluno.isJaTestousContatosWhats() != null && aluno.isJaTestousContatosWhats()) {
			for (String telefone : telefones) {
				
				boolean enviado = EnviadorWhats.enviarWhats(mensagem, telefone, parametros);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Aviso de Vencimento boleto Olá nome da pessoa, verificamos em nosso sistema que o seu boleto com vencimento em MESVENCIMENTO encontra-se aberto. Lembramos que conforme contrato o transporte fica sujeito a suspensão e posterior cancelamento caso a situação não seja regularizada em até 10 dias. caso tenha efetuado o pagamento favor desconsiderar. Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_BOLETO_ATRASAO_MES);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefone);
					mensagemAlunoService.save(msg);
				}

			}
		} else {

			if (telefones.get(0) != null) {
				boolean enviado = EnviadorWhats.enviarWhats(mensagem, telefones.get(0), parametros);
				aluno.setContato1WhatsValido(enviado);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Aviso de Vencimento boleto Olá nome da pessoa, verificamos em nosso sistema que o seu boleto com vencimento em MESVENCIMENTO encontra-se aberto. Lembramos que conforme contrato o transporte fica sujeito a suspensão e posterior cancelamento caso a situação não seja regularizada em até 10 dias. caso tenha efetuado o pagamento favor desconsiderar. Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_BOLETO_ATRASAO_MES);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(0));
					mensagemAlunoService.save(msg);
				}
			}

			if (telefones.get(1) != null) {
				boolean enviado = EnviadorWhats.enviarWhats(mensagem, telefones.get(1), parametros);
				aluno.setContato2WhatsValido(enviado);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Aviso de Vencimento boleto Olá nome da pessoa, verificamos em nosso sistema que o seu boleto com vencimento em MESVENCIMENTO encontra-se aberto. Lembramos que conforme contrato o transporte fica sujeito a suspensão e posterior cancelamento caso a situação não seja regularizada em até 10 dias. caso tenha efetuado o pagamento favor desconsiderar. Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_BOLETO_ATRASAO_MES);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(1));
					mensagemAlunoService.save(msg);

				}
			}

			if (telefones.get(2) != null) {
				boolean enviado = EnviadorWhats.enviarWhats(mensagem, telefones.get(2), parametros);
				aluno.setContato3WhatsValido(enviado);
				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Aviso de Vencimento boleto Olá nome da pessoa, verificamos em nosso sistema que o seu boleto com vencimento em MESVENCIMENTO encontra-se aberto. Lembramos que conforme contrato o transporte fica sujeito a suspensão e posterior cancelamento caso a situação não seja regularizada em até 10 dias. caso tenha efetuado o pagamento favor desconsiderar. Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_BOLETO_ATRASAO_MES);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(2));
					mensagemAlunoService.save(msg);
				}

			}

			if (telefones.get(3) != null) {
				boolean enviado = EnviadorWhats.enviarWhats(mensagem, telefones.get(3), parametros);
				aluno.setContato4WhatsValido(enviado);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Aviso de Vencimento boleto Olá nome da pessoa, verificamos em nosso sistema que o seu boleto com vencimento em MESVENCIMENTO encontra-se aberto. Lembramos que conforme contrato o transporte fica sujeito a suspensão e posterior cancelamento caso a situação não seja regularizada em até 10 dias. caso tenha efetuado o pagamento favor desconsiderar. Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_BOLETO_ATRASAO_MES);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(3));
					mensagemAlunoService.save(msg);
				}
			}

			if (telefones.get(4) != null) {
				boolean enviado = EnviadorWhats.enviarWhats(mensagem, telefones.get(4), parametros);
				aluno.setContato5WhatsValido(enviado);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Aviso de Vencimento boleto Olá nome da pessoa, verificamos em nosso sistema que o seu boleto com vencimento em MESVENCIMENTO encontra-se aberto. Lembramos que conforme contrato o transporte fica sujeito a suspensão e posterior cancelamento caso a situação não seja regularizada em até 10 dias. caso tenha efetuado o pagamento favor desconsiderar. Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_BOLETO_ATRASAO_MES);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(4));
					mensagemAlunoService.save(msg);
				}

			}

			aluno.setJaTestousContatosWhats(true);
			alunoService.saveContado(aluno);
		}
	}
	
	public void enviarMensagemSuspensaoContrato(long idAluno) {
		Aluno aluno = alunoService.findById(idAluno);
		List<Parametro> parametros = new ArrayList<Parametro>();
		parametros.add(new Parametro("1", getNomeResponsavelDevedor(aluno, anoSelecionado)));
		parametros.add(new Parametro("2", getMes(mesAtrasado)));

		List<String> telefones = aluno.contatosWhatsValido();

		if (aluno.isJaTestousContatosWhats() != null && aluno.isJaTestousContatosWhats()) {
			for (String telefone : telefones) {
				boolean enviado = EnviadorWhats.enviarWhats("suspensao_contrato", telefone, parametros);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Olá {{1}}, devido ao NÃO pagamento do boleto de  {{2}} e conforme contrato que prevê a suspensão do transporte 10 dias após o vencimento, a Tefamel transporte escolar está suspendendo o transporte até que seja regularizado os pagamentos."
+"Lembramos que após a suspensão do contrato, fica possível o CANCELAMENTO do contrato e a vaga da criança fica disponível para outra."
+"Para evitar o cancelamento e a perda da vaga, entre em contato imediatamente para regularizar sua situação."
+"Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_TRANSPORTE_SERA_SUSPENSO_AMANHA);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefone);
					mensagemAlunoService.save(msg);
				}

			}
		} else {

			if (telefones.get(0) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(0), parametros);
				aluno.setContato1WhatsValido(enviado);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Olá {{1}}, devido ao NÃO pagamento do boleto de  {{2}} e conforme contrato que prevê a suspensão do transporte 10 dias após o vencimento, a Tefamel transporte escolar está suspendendo o transporte até que seja regularizado os pagamentos."
+"Lembramos que após a suspensão do contrato, fica possível o CANCELAMENTO do contrato e a vaga da criança fica disponível para outra."
+"Para evitar o cancelamento e a perda da vaga, entre em contato imediatamente para regularizar sua situação."
+"Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_TRANSPORTE_SERA_SUSPENSO_AMANHA);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(0));
					mensagemAlunoService.save(msg);
				}
			}

			if (telefones.get(1) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(1), parametros);
				aluno.setContato2WhatsValido(enviado);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Olá {{1}}, devido ao NÃO pagamento do boleto de  {{2}} e conforme contrato que prevê a suspensão do transporte 10 dias após o vencimento, a Tefamel transporte escolar está suspendendo o transporte até que seja regularizado os pagamentos."
+"Lembramos que após a suspensão do contrato, fica possível o CANCELAMENTO do contrato e a vaga da criança fica disponível para outra."
+"Para evitar o cancelamento e a perda da vaga, entre em contato imediatamente para regularizar sua situação."
+"Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_TRANSPORTE_SERA_SUSPENSO_AMANHA);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(1));
					mensagemAlunoService.save(msg);

				}
			}

			if (telefones.get(2) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(2), parametros);
				aluno.setContato3WhatsValido(enviado);
				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Olá {{1}}, devido ao NÃO pagamento do boleto de  {{2}} e conforme contrato que prevê a suspensão do transporte 10 dias após o vencimento, a Tefamel transporte escolar está suspendendo o transporte até que seja regularizado os pagamentos."
+"Lembramos que após a suspensão do contrato, fica possível o CANCELAMENTO do contrato e a vaga da criança fica disponível para outra."
+"Para evitar o cancelamento e a perda da vaga, entre em contato imediatamente para regularizar sua situação."
+"Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_TRANSPORTE_SERA_SUSPENSO_AMANHA);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(2));
					mensagemAlunoService.save(msg);
				}

			}

			if (telefones.get(3) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(3), parametros);
				aluno.setContato4WhatsValido(enviado);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Olá {{1}}, devido ao NÃO pagamento do boleto de  {{2}} e conforme contrato que prevê a suspensão do transporte 10 dias após o vencimento, a Tefamel transporte escolar está suspendendo o transporte até que seja regularizado os pagamentos."
+"Lembramos que após a suspensão do contrato, fica possível o CANCELAMENTO do contrato e a vaga da criança fica disponível para outra."
+"Para evitar o cancelamento e a perda da vaga, entre em contato imediatamente para regularizar sua situação."
+"Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_TRANSPORTE_SERA_SUSPENSO_AMANHA);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(3));
					mensagemAlunoService.save(msg);
				}
			}

			if (telefones.get(4) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(4), parametros);
				aluno.setContato5WhatsValido(enviado);

				if (enviado) {
					MensagemAluno msg = new MensagemAluno();
					msg.setAluno(aluno);
					msg.setMes(mesAtrasado);
					msg.setAno(anoSelecionado);
					msg.setMensagem(
							"Olá {{1}}, devido ao NÃO pagamento do boleto de  {{2}} e conforme contrato que prevê a suspensão do transporte 10 dias após o vencimento, a Tefamel transporte escolar está suspendendo o transporte até que seja regularizado os pagamentos."
+"Lembramos que após a suspensão do contrato, fica possível o CANCELAMENTO do contrato e a vaga da criança fica disponível para outra."
+"Para evitar o cancelamento e a perda da vaga, entre em contato imediatamente para regularizar sua situação."
+"Tefamel - Transporte Escolar Favo de Mel");
					msg.setTipo(TipoMensagem.AVISO_TRANSPORTE_SERA_SUSPENSO_AMANHA);
					msg.setCanalMensagem(CanalMensagem.WHATSAPP);
					msg.setNumeroWhats(telefones.get(4));
					mensagemAlunoService.save(msg);
				}

			}

			aluno.setJaTestousContatosWhats(true);
			alunoService.saveContado(aluno);
		}
	}
	
	private double getValor(int mesMatricula, Aluno aluno2, int anoRematricula) {
		double valor = tabelaPrecoService.getValor(mesMatricula, aluno2, anoRematricula);
		return valor;
	}
	
	public void enviarMensagemRematricula(long idAluno) {
		Aluno aluno = alunoService.findById(idAluno);
		List<Parametro> parametros = new ArrayList<Parametro>();
		parametros.add(new Parametro("name", getNomeResponsavelDevedor(aluno, anoSelecionado)));
		parametros.add(new Parametro("1", getValor(1, aluno, configuracao.getAnoRematricula()) + ""));
		parametros.add(new Parametro("2", getValor(2, aluno, configuracao.getAnoRematricula()) +""));


		List<String> telefones = aluno.contatosWhatsValido();

		if (aluno.isJaTestousContatosWhats() != null && aluno.isJaTestousContatosWhats()) {
			for (String telefone : telefones) {
			boolean enviado =	EnviadorWhats.enviarWhats("rematricula", telefone, parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}
		} else {

			if (telefones.get(0) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula", telefones.get(0), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}

			if (telefones.get(1) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula", telefones.get(1), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}

			if (telefones.get(2) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula", telefones.get(2), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}

			if (telefones.get(3) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula", telefones.get(3), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}

			if (telefones.get(4) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula", telefones.get(4), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}
		
		}
		
	}
	
	public void enviarMensagemRematricula2(long idAluno) {
		Aluno aluno = alunoService.findById(idAluno);
		List<Parametro> parametros = new ArrayList<Parametro>();
		//parametros.add(new Parametro("name", getNomeResponsavelDevedor(aluno, anoSelecionado)));
		//parametros.add(new Parametro("1", getValor(1, aluno, configuracao.getAnoRematricula()) + ""));
		//parametros.add(new Parametro("2", getValor(2, aluno, configuracao.getAnoRematricula()) +""));


		List<String> telefones = aluno.contatosWhatsValido();

		if (aluno.isJaTestousContatosWhats() != null && aluno.isJaTestousContatosWhats()) {
			for (String telefone : telefones) {
			boolean enviado =	EnviadorWhats.enviarWhats("rematricula_terceira", telefone, parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}
		} else {

			if (telefones.get(0) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula_terceira", telefones.get(0), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}

			if (telefones.get(1) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula_terceira", telefones.get(1), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}

			if (telefones.get(2) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula_terceira", telefones.get(2), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}

			if (telefones.get(3) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula_terceira", telefones.get(3), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}

			if (telefones.get(4) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("rematricula_terceira", telefones.get(4), parametros);
				if (enviado) {
					alunoService.setStatusCONVITE_ENVIADO(aluno);
				}
			}
		
		}
		
	}
	
	
	public void enviarMensagemCancelamentoContrato(long idAluno) {
		Aluno aluno = alunoService.findById(idAluno);
		List<Parametro> parametros = new ArrayList<Parametro>();
		parametros.add(new Parametro("1", getNomeResponsavelDevedor(aluno, anoSelecionado)));
		parametros.add(new Parametro("2", getMes(mesAtrasado)));

		List<String> telefones = aluno.contatosWhatsValido();

		if (aluno.isJaTestousContatosWhats() != null && aluno.isJaTestousContatosWhats()) {
			for (String telefone : telefones) {
				boolean enviado = EnviadorWhats.enviarWhats("suspensao_contrato", telefone, parametros);
				if (enviado) {
					mensagemAlunoService.save(montarMensagemCancelamento(telefone));
				}
			}
		} else {

			if (telefones.get(0) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(0), parametros);
				aluno.setContato1WhatsValido(enviado);

				if (enviado) {
					mensagemAlunoService.save(montarMensagemCancelamento(telefones.get(0)));
				}
			}

			if (telefones.get(1) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(1), parametros);
				aluno.setContato2WhatsValido(enviado);

				if (enviado) {
					mensagemAlunoService.save(montarMensagemCancelamento(telefones.get(1)));
				}
			}

			if (telefones.get(2) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(2), parametros);
				aluno.setContato3WhatsValido(enviado);
				if (enviado) {
					mensagemAlunoService.save(montarMensagemCancelamento(telefones.get(2)));
				}

			}

			if (telefones.get(3) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(3), parametros);
				aluno.setContato4WhatsValido(enviado);

				if (enviado) {
					mensagemAlunoService.save(montarMensagemCancelamento(telefones.get(3)));
				}
			}

			if (telefones.get(4) != null) {
				boolean enviado = EnviadorWhats.enviarWhats("aviso_vencimento_boleto2", telefones.get(4), parametros);
				aluno.setContato5WhatsValido(enviado);

				if (enviado) {
					mensagemAlunoService.save(montarMensagemCancelamento(telefones.get(4)));
				}
			}

			aluno.setJaTestousContatosWhats(true);
			alunoService.saveContado(aluno);
		}
	}

	private MensagemAluno montarMensagemCancelamento(String telefone){
		MensagemAluno msg = new MensagemAluno();
		msg.setAluno(aluno);
		msg.setMes(mesSelecionado);
		msg.setAno(anoSelecionado);
		msg.setMensagem(
				"Olá {{1}}, devido ao NÃO pagamento do boleto de  {{2}} , conforme contrato que prevê o cancelamento do transporte 10 dias após o vencimento e a falta de retorno para tentar um acordo, a Tefamel transporte escolar está CANCELANDO o transporte."
				+"Lembramos que as parcelas NÃO pagas ficam sujeitas ao cadastro no sistema de proteção de credito (SPC) e protesto conforme contrato."
				+"Tefamel - Transporte Escolar Favo de Mel");
		msg.setTipo(TipoMensagem.AVISO_TRANSPORTE_SERA_CANCELADO_AMANHA);
		msg.setCanalMensagem(CanalMensagem.WHATSAPP);
		msg.setNumeroWhats(telefone);
		
		return msg;
	}
	
	private String getMes(int mes) {
		switch (mes) {

		case 1:
			return "Janeiro";

		case 2:
			return "Fevereiro";

		case 3:
			return "Março";

		case 4:
			return "Abril";

		case 5:
			return "Maio";

		case 6:
			return "Junho";

		case 7:
			return "Julho";

		case 8:
			return "Agosto";

		case 9:
			return "Setembro";

		case 10:
			return "Outubro";

		case 11:
			return "Novembro";

		case 12:
			return "Dezembro";

		default:
			return "erro";
		}
	}
	
	public String marcarLinha(Long id) {
		Aluno a = alunoService.findById(id);
		String cor = "";
		if (a != null) {
			if (a.getRemovido() != null && a.getRemovido()) {
				cor = "marcarLinhaVermelho";
				return cor;
			}
		}
		return cor;
	}
	
	public String getNomeResponsavelDevedor(Aluno al, int ano) {
		String nomeResponsavelDev = "";
		ContratoAluno ca =  al.getContratoVigente(ano);
		nomeResponsavelDev += ca.getNomeResponsavel();
		if(nomeResponsavelDev != null && !nomeResponsavelDev.equalsIgnoreCase("")){
			return nomeResponsavelDev;
		}

		for (ContratoAluno ca2 : al.getContratos()) {
			nomeResponsavelDev = ca2.getNomeResponsavel();
		}
		return nomeResponsavelDev;
	}

	public Integer getMesSelecionadoRelatorio() {
		return mesSelecionadoRelatorio;
	}

	public void setMesSelecionadoRelatorio(Integer mesSelecionadoRelatorio) {
		Util.addAtributoSessao("mesSelecionadoRelatorio", mesSelecionadoRelatorio);
		this.mesSelecionadoRelatorio = mesSelecionadoRelatorio;
	}

	public StatusContratoEnum getStatusContrato() {
		return statusContrato;
	}

	public void setStatusContrato(StatusContratoEnum statusContrato) {
		this.statusContrato = statusContrato;
	}


}
