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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.model.Carro;
import org.escolar.model.Faturamento;
import org.escolar.service.AlunoService;
import org.escolar.service.FaturamentoService;
import org.escolar.service.TurmaService;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

@Named
@ViewScoped
public class GraficosController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LineChartModel lineModel1;
    
	@Inject
	private FaturamentoService faturamentoService;
	
	@Inject
	private TurmaService carroService;
	
	
    @PostConstruct
    public void init() {
        createLineModels();
    }
    
    private void createLineModels() {
    	  lineModel1 = initLinearModel();
          lineModel1.setTitle("Faturamento");
          lineModel1.setLegendPosition("e");
          Axis yAxis = lineModel1.getAxis(AxisType.Y);
          yAxis.setMin(0);
          yAxis.setMax(15000);
    }
    
    private LineChartModel initLinearModel() {
        LineChartModel model = new LineChartModel();
        List<Carro> carros = carroService.findAll();
        try{
        	for(Carro carro : carros){
            	LineChartSeries series = new LineChartSeries();
            	series.setLabel(carro.getNome());
            	List<Faturamento> faturamentosCarro = faturamentoService.findFaturamentoPorCarro(carro);
            	for(int i=0;i<faturamentosCarro.size();i++){
            		series.set(i, faturamentosCarro.get(i).getValor());
                }
            	model.addSeries(series);
            }
         	
        }catch(Exception e){
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
    
}
