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
package org.escola.controller.graficos;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escola.controller.RelatorioController;
import org.escolar.enums.BairroEnum;
import org.escolar.enums.EscolaEnum;
import org.escolar.enums.PerioddoEnum;
import org.escolar.model.Aluno;
import org.escolar.model.Carro;
import org.escolar.model.Faturamento;
import org.escolar.service.AlunoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.ExtratoBancarioService;
import org.escolar.service.FaturamentoService;
import org.escolar.service.FinanceiroService;
import org.escolar.service.TurmaService;
import org.escolar.util.Util;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BubbleChartModel;
import org.primefaces.model.chart.BubbleChartSeries;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.PieChartModel;

import br.com.aaf.base.importacao.extrato.ExtratoGruposPagamentoRecebimentoEnum;
import br.com.aaf.base.importacao.extrato.ExtratoTiposEntradaSaidaEnum;

@Named
@ViewScoped
public class GraficosController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LineChartModel lineModel1;
	private BubbleChartModel lineModelAlunosEscola;

	private PieChartModel pieModelAlunosEscola;

	private PieChartModel pieModelAlunosIdade;

	private PieChartModel pieModelAlunosBairro;

	private PieChartModel pieModelAlunosPeriodo;

	private PieChartModel pieModelRecebimentoGruposMes;

	private PieChartModel pieModelGastosGrupoMes;

	private PieChartModel pieModelAlunosTroca;

	private String faturamentoAtual;

	private String quantidadeContratos;

	private String quantidadeAlunos;
	
	private double totalReceitaGrupoMes = 0d;
	private double totalGastoGrupoMes = 0d;
	

	@Inject
	private FaturamentoService faturamentoService;

	@Inject
	private TurmaService carroService;

	@Inject
	private AlunoService alunoService;

	@Inject
	private FinanceiroService financeiroService;

	@Inject
	private ConfiguracaoService configuracaoService;

	@Inject
	private RelatorioController relatorioController;

	@Inject
	private ExtratoBancarioService extratoBancarioService;

	@PostConstruct
	public void init() {
		createLineModels();
		createPieModel1AlunoEscola();
		createPieModel1AlunoBairro();
		createPieModel1AlunoPeriodo();
		createPieModel1AlunoTroca();
		createPieModelRecebimentosGrupoMes();
		createPieModelGastosMesGrupo();
	}

	public void remontarGraficosComMes(){
		createPieModelRecebimentosGrupoMes();
		createPieModelGastosMesGrupo();
	}
	
	private void createPieModelRecebimentosGrupoMes() {
		totalReceitaGrupoMes = 0d;
		pieModelRecebimentoGruposMes = new PieChartModel();
		pieModelRecebimentoGruposMes.setTitle("Recebimento Por Grupo ");
		pieModelRecebimentoGruposMes.setLegendPosition("s");
		pieModelRecebimentoGruposMes.setLegendCols(12);
		pieModelRecebimentoGruposMes.setShadow(false);

		Map<String, Object> filtros = new HashMap<String, Object>();
		filtros.put("ano", relatorioController.getAnoSelecionado());
		filtros.put("mes", relatorioController.getMesSelecionadoRelatorio());
		filtros.put("tipoEntradaSaida", ExtratoTiposEntradaSaidaEnum.ENTRADA);

		try {
			for (int i = 0; i < ExtratoGruposPagamentoRecebimentoEnum.values().length; i++) {
				filtros.put("grupoRecebimento", ExtratoGruposPagamentoRecebimentoEnum.values()[i]);
				double quantidade = (double) extratoBancarioService.count(filtros);
				if (quantidade > 0) {
					pieModelRecebimentoGruposMes.set(ExtratoGruposPagamentoRecebimentoEnum.values()[i].getNome(),	quantidade);
					totalReceitaGrupoMes += quantidade;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void itemSelect(ItemSelectEvent event) {
		Set<Entry<String, Number>> objetoGrafico = pieModelGastosGrupoMes.getData().entrySet();

		// Set<Map.Entry<String, Double>> objetoGrafico =
		// pieModelGastosGrupoMes.getData().entrySet().toArray()[event.getItemIndex()];
		Entry<String, Number> value = (Entry<String, Number>) objetoGrafico.toArray()[event.getItemIndex()];
		System.out.println(value.getKey());
		System.out.println(value.getValue());
	}

	public List<Entry<String, Number>> getGastosPorGrupo() {
		Set<Entry<String, Number>> objetoGrafico = pieModelGastosGrupoMes.getData().entrySet();
		List<Entry<String, Number>> lista = new ArrayList<Map.Entry<String,Number>>(objetoGrafico);
		Collections.sort(lista, new Comparator<Entry<String, Number>>() {  
            @Override  
            public int compare(Entry<String, Number> p1, Entry<String, Number> p2) {  
            	if((p1.getValue() == p2.getValue())){
            		return 0;
            	}else if(p1.getValue().doubleValue() > p2.getValue().doubleValue()){
            		return -1;
            	}else{
            		return 1;
            	}
            }  
     });  
		
		return lista;
	}

	public List<Entry<String, Number>> getReceitasPorGrupo() {
		Set<Entry<String, Number>> objetoGrafico = pieModelRecebimentoGruposMes.getData().entrySet();
		List<Entry<String, Number>> lista = new ArrayList<Map.Entry<String,Number>>(objetoGrafico);
		Collections.sort(lista, new Comparator<Entry<String, Number>>() {  
            @Override  
            public int compare(Entry<String, Number> p1, Entry<String, Number> p2) {  
            	if((p1.getValue() == p2.getValue())){
            		return 0;
            	}else if(p1.getValue().doubleValue() > p2.getValue().doubleValue()){
            		return -1;
            	}else{
            		return 1;
            	}
            }  
     });  
		
		return lista;
	}
	
	private void createPieModelGastosMesGrupo() {
		totalGastoGrupoMes = 0D;
		pieModelGastosGrupoMes = new PieChartModel();
		pieModelGastosGrupoMes.setTitle("Gastos Por Grupo ");
		pieModelGastosGrupoMes.setLegendPosition("s");
		pieModelGastosGrupoMes.setLegendCols(12);
		pieModelGastosGrupoMes.setShadow(false);

		Map<String, Object> filtros = new HashMap<String, Object>();
		filtros.put("ano", relatorioController.getAnoSelecionado());
		filtros.put("mes", relatorioController.getMesSelecionadoRelatorio());
		filtros.put("tipoEntradaSaida", ExtratoTiposEntradaSaidaEnum.SAIDA);

		try {
			for (int i = 0; i < ExtratoGruposPagamentoRecebimentoEnum.values().length; i++) {
				filtros.put("grupoRecebimento", ExtratoGruposPagamentoRecebimentoEnum.values()[i]);
				double quantidade = (double) extratoBancarioService.count(filtros);
				if (quantidade > 0) {
					pieModelGastosGrupoMes.set(ExtratoGruposPagamentoRecebimentoEnum.values()[i].getNome(), quantidade);
					totalGastoGrupoMes += quantidade;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createPieModel1AlunoEscola() {
		pieModelAlunosEscola = new PieChartModel();
		pieModelAlunosEscola.setTitle("Crianças por Escola");
		pieModelAlunosEscola.setLegendPosition("s");
		pieModelAlunosEscola.setLegendCols(12);
		pieModelAlunosEscola.setShadow(false);

		Map<String, Object> filtros = new HashMap<String, Object>();
		filtros.put("anoLetivo", configuracaoService.getConfiguracao().getAnoLetivo());
		filtros.put("removido", false);
		try {
			for (int i = 0; i < EscolaEnum.values().length; i++) {
				filtros.put("escola", EscolaEnum.values()[i]);
				int quantidade = (int) alunoService.count(filtros);
				if (quantidade > 0) {
					pieModelAlunosEscola.set(EscolaEnum.values()[i].getName(), quantidade);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createPieModel1AlunoBairro() {
		pieModelAlunosBairro = new PieChartModel();
		pieModelAlunosBairro.setTitle("Crianças por Bairro");
		pieModelAlunosBairro.setLegendPosition("s");
		pieModelAlunosBairro.setLegendCols(12);
		pieModelAlunosBairro.setShadow(false);

		Map<String, Object> filtros = new HashMap<String, Object>();
		filtros.put("anoLetivo", configuracaoService.getConfiguracao().getAnoLetivo());
		filtros.put("removido", false);
		try {
			for (int i = 0; i < BairroEnum.values().length; i++) {
				filtros.put("bairroAluno", BairroEnum.values()[i]);
				int quantidade = (int) alunoService.count(filtros);
				if (quantidade > 0) {
					pieModelAlunosBairro.set(BairroEnum.values()[i].getName(), quantidade);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createPieModel1AlunoPeriodo() {
		pieModelAlunosPeriodo = new PieChartModel();
		pieModelAlunosPeriodo.setTitle("Crianças por Periodo");
		pieModelAlunosPeriodo.setLegendPosition("s");
		pieModelAlunosPeriodo.setLegendCols(12);
		pieModelAlunosPeriodo.setShadow(false);

		Map<String, Object> filtros = new HashMap<String, Object>();
		filtros.put("anoLetivo", configuracaoService.getConfiguracao().getAnoLetivo());
		filtros.put("removido", false);
		try {
			for (int i = 0; i < PerioddoEnum.values().length; i++) {
				filtros.put("periodo", PerioddoEnum.values()[i]);
				int quantidade = (int) alunoService.count(filtros);
				if (quantidade > 0) {
					pieModelAlunosPeriodo.set(PerioddoEnum.values()[i].getName(), quantidade);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createPieModel1AlunoTroca() {
		pieModelAlunosTroca = new PieChartModel();
		pieModelAlunosTroca.setTitle("Crianças por Periodo");
		pieModelAlunosTroca.setLegendPosition("s");
		pieModelAlunosTroca.setLegendCols(12);
		pieModelAlunosTroca.setShadow(false);

		try {
			int semtroca = 0;
			int umaTroca = 0;
			int duasTroca = 0;
			int maisDe2 = 0;

			for (Aluno al : alunoService.findAlunoDoAnoLetivo()) {
				if (!al.isTrocaIDA() && !al.isTrocaVolta()) {
					semtroca++;
				} else if (!Util.nullOrTrue(al.isTrocaIDA()) && Util.nullOrTrue(al.isTrocaVolta())
						&& !Util.nullOrTrue(al.isTrocaVolta2())) {
					umaTroca++;
				} else if (Util.nullOrTrue(al.isTrocaIDA()) && !Util.nullOrTrue(al.isTrocaIDA2())
						&& !Util.nullOrTrue(al.isTrocaVolta())) {
					umaTroca++;
				} else if (Util.nullOrTrue(al.isTrocaIDA()) && !Util.nullOrTrue(al.isTrocaIDA2())
						&& Util.nullOrTrue(al.isTrocaVolta()) && !Util.nullOrTrue(al.isTrocaVolta2())) {
					duasTroca++;
				} else if (Util.nullOrTrue(al.isTrocaIDA()) && Util.nullOrTrue(al.isTrocaIDA2())
						&& !Util.nullOrTrue(al.isTrocaVolta()) && !Util.nullOrTrue(al.isTrocaVolta2())) {
					duasTroca++;
				} else if (!Util.nullOrTrue(al.isTrocaIDA()) && !Util.nullOrTrue(al.isTrocaIDA2())
						&& Util.nullOrTrue(al.isTrocaVolta()) && Util.nullOrTrue(al.isTrocaVolta2())) {
					duasTroca++;
				} else {
					maisDe2++;
				}
			}

			pieModelAlunosTroca.set("Não faz troca", semtroca);
			pieModelAlunosTroca.set("Faz 1 troca", umaTroca);
			pieModelAlunosTroca.set("Faz 2 troca", duasTroca);
			pieModelAlunosTroca.set("Faz 3 ou mais", maisDe2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createLineModels() {
		lineModel1 = initLinearModel();
		lineModel1.setTitle("Faturamento");
		lineModel1.setLegendPosition("e");
		Axis yAxis = lineModel1.getAxis(AxisType.Y);
		yAxis.setMin(0);
		yAxis.setMax(15000);

		// grafico criancaPor escola bubble
		lineModelAlunosEscola = initBubbleModelEscolaCrianca();
		lineModelAlunosEscola.setTitle("Crianças por Escola");
		lineModelAlunosEscola.getAxis(AxisType.X).setLabel("Escolas");
		Axis yAxis2 = lineModelAlunosEscola.getAxis(AxisType.Y);
		yAxis2.setMin(0);
		yAxis2.setMax(50);
		yAxis2.setLabel("Quantidade");
	}

	private LineChartModel initLinearModel() {
		LineChartModel model = new LineChartModel();
		List<Carro> carros = carroService.findAll();
		try {
			for (Carro carro : carros) {
				LineChartSeries series = new LineChartSeries();
				series.setLabel(carro.getNome());
				List<Faturamento> faturamentosCarro = faturamentoService.findFaturamentoPorCarro(carro);
				for (int i = 0; i < faturamentosCarro.size(); i++) {
					series.set(i, faturamentosCarro.get(i).getValor());
				}
				model.addSeries(series);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	private LineChartModel initLinearModelCriancaPorEscola() {
		Map<String, Object> filtros = new HashMap<String, Object>();

		filtros.put("anoLetivo", configuracaoService.getConfiguracao().getAnoLetivo());
		filtros.put("removido", false);

		LineChartModel model = new LineChartModel();
		try {
			for (int i = 0; i < EscolaEnum.values().length; i++) {
				LineChartSeries series = new LineChartSeries();
				series.setLabel(EscolaEnum.values()[i].getName());
				filtros.put("escola", EscolaEnum.values()[i]);
				series.set(i + 1, alunoService.count(filtros));
				model.addSeries(series);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	private BubbleChartModel initBubbleModelEscolaCrianca() {
		BubbleChartModel model = new BubbleChartModel();
		// Primeiro valor nome escola
		// Segundo valor posição eixo x
		// Terceiro valor posicao eixo y
		// Quarto Tamanho da bola
		Map<String, Object> filtros = new HashMap<String, Object>();

		filtros.put("anoLetivo", configuracaoService.getConfiguracao().getAnoLetivo());
		filtros.put("removido", false);

		try {
			for (int i = 0; i < EscolaEnum.values().length; i++) {
				filtros.put("escola", EscolaEnum.values()[i]);
				model.add(new BubbleChartSeries(EscolaEnum.values()[i].getName(), i,
						Integer.parseInt(alunoService.count(filtros) + ""),
						Integer.parseInt(alunoService.count(filtros) + "")));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;

	}

	public LineChartModel getLineModel1() {
		return lineModel1;
	}

	public void setLineModel1(LineChartModel lineModel1) {
		this.lineModel1 = lineModel1;
	}

	public BubbleChartModel getLineModelAlunosEscola() {
		return lineModelAlunosEscola;
	}

	public void setLineModelAlunosEscola(BubbleChartModel lineModelAlunosEscola) {
		this.lineModelAlunosEscola = lineModelAlunosEscola;
	}

	public String getGasto(Long mes) {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(financeiroService.getGasto(mes.intValue()));
	}

	public Double getGastoDouble(Long mes) {
		return financeiroService.getGasto(mes.intValue());
	}

	public String getGasto() {
		Calendar c = Calendar.getInstance();
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(financeiroService.getGasto(c.get(Calendar.MONTH) + 1));
	}

	public Double getGastoDouble() {
		Calendar c = Calendar.getInstance();
		return financeiroService.getGasto(c.get(Calendar.MONTH) + 1);
	}

	public String getDiferencaGastoRecebido(Long mes) {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		Double recebido = getRecebidoDouble(mes);
		Double gasto = getGastoDouble(mes);

		double diferenca = recebido - gasto;

		return formatter.format(diferenca);
	}

	public String getDiferencaGastoRecebido() {
		Calendar c = Calendar.getInstance();
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		Double recebido = getRecebidoDouble();
		Double gasto = getGastoDouble();
		double diferenca = recebido - gasto;

		return formatter.format(diferenca);
	}

	public String getRecebido(Long mes) {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(financeiroService.getRecebido(mes.intValue()));
	}

	public Double getRecebidoDouble(Long mes) {
		return financeiroService.getRecebido(mes.intValue());
	}

	public String getRecebido() {
		Calendar c = Calendar.getInstance();
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(financeiroService.getRecebido(c.get(Calendar.MONTH) + 1));
	}

	public Double getRecebidoDouble() {
		Calendar c = Calendar.getInstance();
		return financeiroService.getRecebido(c.get(Calendar.MONTH) + 1);
	}

	public String getFaturamentoAtual() {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		Calendar c = Calendar.getInstance();
		return formatter.format(financeiroService.getPrevisto(c.get(Calendar.MONTH) + 1));
	}

	public String getFaturamentoAtual(Long mes) {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(financeiroService.getPrevisto(mes.intValue()));
	}

	public void setFaturamentoAtual(String faturamentoAtual) {
		this.faturamentoAtual = faturamentoAtual;
	}

	public PieChartModel getPieModelAlunosEscola() {
		return pieModelAlunosEscola;
	}

	public void setPieModelAlunosEscola(PieChartModel pieModelAlunosEscola) {
		this.pieModelAlunosEscola = pieModelAlunosEscola;
	}

	public PieChartModel getPieModelAlunosBairro() {
		return pieModelAlunosBairro;
	}

	public void setPieModelAlunosBairro(PieChartModel pieModelAlunosBairro) {
		this.pieModelAlunosBairro = pieModelAlunosBairro;
	}

	public PieChartModel getPieModelAlunosPeriodo() {
		return pieModelAlunosPeriodo;
	}

	public void setPieModelAlunosPeriodo(PieChartModel pieModelAlunosPeriodo) {
		this.pieModelAlunosPeriodo = pieModelAlunosPeriodo;
	}

	public PieChartModel getPieModelAlunosTroca() {
		return pieModelAlunosTroca;
	}

	public void setPieModelAlunosTroca(PieChartModel pieModelAlunosTroca) {
		this.pieModelAlunosTroca = pieModelAlunosTroca;
	}

	public PieChartModel getPieModelAlunosIdade() {
		return pieModelAlunosIdade;
	}

	public void setPieModelAlunosIdade(PieChartModel pieModelAlunosIdade) {
		this.pieModelAlunosIdade = pieModelAlunosIdade;
	}

	public String getQuantidadeAlunos() {
		return String.valueOf(alunoService.findAlunoDoAnoLetivo().size());
	}

	public void setQuantidadeAlunos(String quantidadeAlunos) {
		this.quantidadeAlunos = quantidadeAlunos;
	}

	public String getQuantidadeContratos() {
		Calendar c = Calendar.getInstance();
		return String.valueOf(financeiroService.countContratos(c.get(Calendar.MONTH) + 1));
	}

	public void setQuantidadeContratos(String quantidadeContratos) {
		this.quantidadeContratos = quantidadeContratos;
	}

	public RelatorioController getRelatorioController() {
		return relatorioController;
	}

	public void setRelatorioController(RelatorioController relatorioController) {
		this.relatorioController = relatorioController;
	}

	public PieChartModel getPieModelRecebimentoGruposMes() {
		return pieModelRecebimentoGruposMes;
	}

	public void setPieModelRecebimentoGruposMes(PieChartModel pieModelRecebimentoGruposMes) {
		this.pieModelRecebimentoGruposMes = pieModelRecebimentoGruposMes;
	}

	public PieChartModel getPieModelGastosGrupoMes() {
		return pieModelGastosGrupoMes;
	}

	public void setPieModelGastosGrupoMes(PieChartModel pieModelGastosGrupoMes) {
		this.pieModelGastosGrupoMes = pieModelGastosGrupoMes;
	}

	public double getTotalReceitaGrupoMes() {
		return totalReceitaGrupoMes;
	}

	public void setTotalReceitaGrupoMes(double totalReceitaGrupoMes) {
		this.totalReceitaGrupoMes = totalReceitaGrupoMes;
	}

	public double getTotalGastoGrupoMes() {
		return totalGastoGrupoMes;
	}

	public void setTotalGastoGrupoMes(double totalGastoGrupoMes) {
		this.totalGastoGrupoMes = totalGastoGrupoMes;
	}

}
