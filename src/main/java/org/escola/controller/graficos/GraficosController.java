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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escola.controller.ConfiguracaoController;
import org.escolar.enums.BairroEnum;
import org.escolar.enums.EscolaEnum;
import org.escolar.enums.PerioddoEnum;
import org.escolar.model.Aluno;
import org.escolar.model.Carro;
import org.escolar.model.Faturamento;
import org.escolar.service.AlunoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.FaturamentoService;
import org.escolar.service.FinanceiroService;
import org.escolar.service.TurmaService;
import org.escolar.util.Util;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BubbleChartModel;
import org.primefaces.model.chart.BubbleChartSeries;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.chart.PieChartModel;

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

	private PieChartModel pieModelAlunosTroca;

	private String faturamentoAtual;
	
	private String quantidadeContratos;
	
	private String quantidadeAlunos;

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

	@PostConstruct
	public void init() {
		createLineModels();
		createPieModel1AlunoEscola();
		createPieModel1AlunoBairro();
		createPieModel1AlunoPeriodo();
		createPieModel1AlunoTroca();
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
			
			for (Aluno al :alunoService.findAlunoDoAnoLetivo()) {
				if(!al.isTrocaIDA() && !al.isTrocaVolta()){
					semtroca++;
				}else if(!Util.nullOrTrue(al.isTrocaIDA()) && Util.nullOrTrue(al.isTrocaVolta()) && !Util.nullOrTrue(al.isTrocaVolta2()) ){
					umaTroca++;
				}else if(Util.nullOrTrue(al.isTrocaIDA()) && !Util.nullOrTrue(al.isTrocaIDA2()) && !Util.nullOrTrue(al.isTrocaVolta())  ){
					umaTroca++;
				}else if(Util.nullOrTrue(al.isTrocaIDA()) && !Util.nullOrTrue(al.isTrocaIDA2()) && Util.nullOrTrue(al.isTrocaVolta()) && !Util.nullOrTrue(al.isTrocaVolta2()) ){
					duasTroca++;
				}else if(Util.nullOrTrue(al.isTrocaIDA()) && Util.nullOrTrue(al.isTrocaIDA2()) && !Util.nullOrTrue(al.isTrocaVolta()) && !Util.nullOrTrue(al.isTrocaVolta2()) ){
					duasTroca++;
				}else if(!Util.nullOrTrue(al.isTrocaIDA()) && !Util.nullOrTrue(al.isTrocaIDA2()) && Util.nullOrTrue(al.isTrocaVolta()) && Util.nullOrTrue(al.isTrocaVolta2()) ){
					duasTroca++;
				}else{
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

	public String getFaturamentoAtual() {
		Calendar c = Calendar.getInstance();
		return String.valueOf(financeiroService.getPrevisto(c.get(Calendar.MONTH)+1));
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
		return String.valueOf(financeiroService.countContratos(c.get(Calendar.MONTH)+1));
	}

	public void setQuantidadeContratos(String quantidadeContratos) {
		this.quantidadeContratos = quantidadeContratos;
	}

}
