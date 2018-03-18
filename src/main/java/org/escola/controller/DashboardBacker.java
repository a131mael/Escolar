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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.map.HashedMap;
import org.escola.auth.AuthController;
import org.escola.enums.EscolaEnum;
import org.escola.enums.PegarEntregarEnun;
import org.escola.enums.PerioddoEnum;
import org.escola.model.Aluno;
import org.escola.model.Carro;
import org.escola.model.ObjetoRota;
import org.escola.service.AlunoService;
import org.escola.service.TurmaService;
import org.escola.util.FileDownload;
import org.escola.util.Util;
import org.primefaces.component.dashboard.Dashboard;
import org.primefaces.component.panel.Panel;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.primefaces.model.StreamedContent;

import com.lowagie.text.DocumentException;

@Named
@ViewScoped
public class DashboardBacker extends AuthController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private Carro carro;

	@Inject
	private AlunoService alunoService;

	public static final int DEFAULT_COLUMN_COUNT = 3;
	private int columnCount = DEFAULT_COLUMN_COUNT;

	private Dashboard dashboard;

	@Inject
	private TurmaService turmaService;

	private PerioddoEnum periodo;

	@PostConstruct
	private void init() {
		FacesContext fc = FacesContext.getCurrentInstance();
		Application application = fc.getApplication();

		dashboard = (Dashboard) application.createComponent(fc, "org.primefaces.component.Dashboard",
				"org.primefaces.component.DashboardRenderer");
		dashboard.setId("dashboard");

		DashboardModel model = new DefaultDashboardModel();
		for (int i = 0, n = getColumnCount(); i < n; i++) {
			DashboardColumn column = new DefaultDashboardColumn();
			model.addColumn(column);
		}
		dashboard.setModel(model);
		if (carro == null) {
			Object objectSessao = Util.getAtributoSessao("carro");
			if (objectSessao != null) {
				carro = (Carro) objectSessao;
				Util.removeAtributoSessao("carro");
			} else {
				carro = new Carro();
				carro.setId(21L);
			}
		}

		if (periodo == null) {
			Object objectSessao = Util.getAtributoSessao("periodo");
			if (objectSessao != null) {
				periodo = (PerioddoEnum) objectSessao;
				Util.removeAtributoSessao("periodo");
			}
		}

		popularDashBoard(application, fc, model);

	}

	public StreamedContent imprimirRota() {
		List<ObjetoRota> rota = alunoService.findObjetosRota(carro.getId(), periodo);
		try {

			String nomeArquivo = System.currentTimeMillis() + ".pdf";
			OfficePDFUtil.gerarPDF(rota, periodo, carro, nomeArquivo);

			String temp = System.getProperty("java.io.tmpdir");
			String caminho = temp + File.separator + nomeArquivo;
			InputStream stream = new FileInputStream(caminho);

			return FileDownload.getContentDoc(stream, nomeArquivo);
		} catch (DocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void handleReorder(DashboardReorderEvent event) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_INFO);
		message.setDetail(event.getWidgetId() + "");
		// message.setDetail(event.getItemIndex() +"");

		PegarEntregarEnun pee = event.getWidgetId().indexOf("p") >= 0 ? PegarEntregarEnun.PEGAR
				: PegarEntregarEnun.ENTREGAR;
		PerioddoEnum pe = null;

		String ids = event.getWidgetId().replace("p", "");
		ids = ids.replace("e", "");
		ids = ids.replace("E", "");
		ids = ids.replace("P", "");
		ids = ids.replace("q", "");
		Long idCarro = null;
		Long idCarroTroca = null;

		boolean isTroca = getValue(0, ids).toString().equalsIgnoreCase("2");
		idCarro = Long.parseLong((String) getValue(3, event.getWidgetId()));

		pe = PerioddoEnum.getPeriodo(Integer.valueOf((String) getValue(4, event.getWidgetId())));
		if (!isTroca) {
			Long idCrianca = null;
			EscolaEnum escola = null;
			try {
				if (Integer.parseInt(getValue(0, ids).toString()) == 1) {
					escola = EscolaEnum.values()[Integer.parseInt((String) getValue(2, ids))];
				} else {
					idCrianca = Long.parseLong((String) getValue(2, ids));
				}
			} catch (NumberFormatException nfe) {

			}

			if (event.getColumnIndex() == 1) {
				Aluno al = null;
				if (idCrianca != null) {
					al = alunoService.findById(idCrianca);
				}

				ObjetoRota obj = new ObjetoRota();
				obj.setAluno(al);
				if (al == null) {
					obj.setNome(event.getWidgetId());
				} else {
					obj.setNome(al.getNomeAluno());
				}
				obj.setPosicao(event.getItemIndex());
				obj.setIdCarro(idCarro);
				obj.setPeriodo(pe);
				obj.setPegarEntregar(pee);
				obj.setEscola(escola);
				Object idObject = getValue(5, event.getWidgetId());
				if (idObject != null) {
					Long id = Long.parseLong((String) idObject);
					obj.setId(id);
				}

				alunoService.saveAlunoRota(obj);
			} else {
				alunoService.removerObjetoRota(Long.parseLong((String) getValue(5, event.getWidgetId())));
			}

		} else {
			// TODO continuar
			idCarroTroca = Long.parseLong((String) getValue(2, event.getWidgetId()));
			if (event.getColumnIndex() == 1) {
				ObjetoRota obj = new ObjetoRota();
				obj.setNome("Troca");
				obj.setPosicao(event.getItemIndex());
				obj.setIdCarro(idCarro);
				obj.setPeriodo(pe);
				obj.setIdCarroAlvo(idCarroTroca);
				obj.setPegarEntregar(pee);

				if (pe.equals(PerioddoEnum.TARDE) || pe.equals(PerioddoEnum.TARDE)) {
					List<Aluno> alunosDaTroca = alunoService.findAlunoTrocaNoite(idCarro, idCarroTroca);
					StringBuilder sb = new StringBuilder();

					for (Aluno a : alunosDaTroca) {
						sb.append(a.getNomeAluno());
						sb.append(" - ");
					}

					obj.setDescricao(sb.toString());
				}

				alunoService.saveAlunoRota(obj);
			} else {
				alunoService.removerObjetoRota(Long.parseLong((String) getValue(5, event.getWidgetId())));
			}

		}

		addMessage(message);
	}

	private Object getValue(int i, String widgetId) {
		String aux = widgetId + "";

		for (int j = 0; j < i; j++) {
			aux = aux.replaceFirst(aux.substring(0, aux.indexOf("-") + 1), "");
		}

		int lastIndex = aux.indexOf("-");
		if (lastIndex < 0) {
			lastIndex = aux.length() - 1;
		}
		if (lastIndex < 0) {
			return null;
		}
		return aux.substring(0, lastIndex);
	}

	public void handleClose(CloseEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Panel Closed",
				"Closed panel id:'" + event.getComponent().getId() + "'");

		addMessage(message);
	}

	public void handleToggle(ToggleEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, event.getComponent().getId() + " toggled",
				"Status:" + event.getVisibility().name());

		addMessage(message);
	}

	private void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public DashboardBacker() {
	}

	private UIComponent createComponent(String componentType) {
		return FacesContext.getCurrentInstance().getApplication().createComponent(componentType);
	}

	private HashedMap<Carro, List<Aluno>> getAlunosTroca(PerioddoEnum periodo, Carro carro, Boolean pego) {
		HashedMap<Carro, List<Aluno>> carrosTrocas = new HashedMap<>();
		List<Carro> carrosQueFazemTrocaCom = new ArrayList<>();

		if (PerioddoEnum.INTEGRAL.equals(periodo)) {
			carrosQueFazemTrocaCom = alunoService.findCarrosTrocaMeioDia(carro, periodo, pego);
		} else {
			carrosQueFazemTrocaCom = alunoService.findCarrosTroca(carro, periodo, pego);
		}

		for (Carro carroTroca : carrosQueFazemTrocaCom) {
			if (periodo.equals(PerioddoEnum.TARDE)) {
				carrosTrocas.put(carroTroca, alunoService.findAlunosVoltam(carro, carroTroca, periodo));
			} else if (periodo.equals(PerioddoEnum.INTEGRAL)) {
				carrosTrocas.put(carroTroca, alunoService.findAlunosTrocaMeioDia(carro, carroTroca, periodo));
			} else {
				carrosTrocas.put(carroTroca, alunoService.findAlunosVao(carro, carroTroca, periodo, pego));
			}
		}

		return carrosTrocas;
	}

	private void popularDashBoard(Application application, FacesContext fc, DashboardModel model) {

		List<Aluno> alunos = new ArrayList<Aluno>();
		// MANHA
		if (periodo != null && periodo.equals(PerioddoEnum.MANHA)) {
			alunos.addAll(alunoService.findAlunoByCarroIDA(carro.getId(), periodo));
			alunos.addAll(alunoService.findAlunoByCarroIDA(carro.getId(), PerioddoEnum.INTEGRAL));
		}
		// TARDE
		if (periodo != null && periodo.equals(PerioddoEnum.TARDE)) {
			alunos.addAll(alunoService.findAlunoByCarroVolta(carro.getId(), periodo));
			alunos.addAll(alunoService.findAlunoByCarroVolta(carro.getId(), PerioddoEnum.INTEGRAL));
		}
		// MEIO DIA
		if (periodo != null && periodo.equals(PerioddoEnum.INTEGRAL)) {
			alunos.addAll(alunoService.findAlunoByCarroIDA(carro.getId(), PerioddoEnum.TARDE));
			alunos.addAll(alunoService.findAlunoByCarroVolta(carro.getId(), PerioddoEnum.MANHA));
		}

		List<ObjetoRota> objRotas = alunoService.findObjetosRota(carro.getId(), periodo);
		List<Aluno> alunosRota = getAlunos(objRotas);

		List<Aluno> todosAlunos = new ArrayList<>();
		todosAlunos.addAll(alunos);

		alunos.removeAll(alunosRota);

		HashedMap<EscolaEnum, List<Aluno>> escolas = new HashedMap<>();
		Map<EscolaEnum, PegarEntregarEnun> escolasAdicionadas = new HashMap<>();
		HashedMap<Carro, List<Aluno>> trocasCriancasPego = getAlunosTroca(periodo, carro, true);
		HashedMap<Carro, List<Aluno>> trocasCriancasPasso = getAlunosTroca(periodo, carro, false);
		DashboardColumn columnTarde = model.getColumn(0);
		// Alunos do periodo e carro que ainda nao estao na rota
		List<Aluno> alunosPegoPorOutroCarro = alunoService.findAlunoPegoOutroCarroTarde(periodo, carro);
		switch (periodo) {
		case MANHA:
			DashboardColumn column = model.getColumn(0);
			for (Aluno al : alunos) {
				// CriaPanelPegar
				Panel panel = criarPanel(application, fc, "p", al, carro.getId(), periodo);
				if (panel != null) {
					getDashboard().getChildren().add(panel);
					column.addWidget(panel.getId());
				}

			}

			List<Aluno> alunosParaEntregar = new ArrayList<>();
			alunosParaEntregar.addAll(todosAlunos);
			removerAlunosPassadorTroca(trocasCriancasPasso, alunosParaEntregar);

			populaListaEscolas(alunosParaEntregar, escolas);

			montaPanelLadoDireito(application, fc, model, objRotas, escolasAdicionadas);
			criarPanelTroca(application, fc, trocasCriancasPego, trocasCriancasPasso, columnTarde);
			// removeEscolasJaAdicionadas(escolas, escolasAdicionadas);
			// CriaPanelEntregar Escola

			populaListaEscolas(trocasCriancasPego, escolas);
			List<Panel> panels = criarPanelEntregarEscola(application, fc, "e", escolas, carro.getId(), periodo,
					escolasAdicionadas);
			for (Panel p : panels) {
				getDashboard().getChildren().add(p);
				column.addWidget(p.getId());

			}

			break;

		case TARDE:

			removerAlunosPegoPorOutroCarro(alunos, todosAlunos, trocasCriancasPego, alunosPegoPorOutroCarro);
			criaPanelAlunosPegar(application, fc, alunos, columnTarde);

			populaListaEscolas(todosAlunos, escolas);

			montaPanelLadoDireito(application, fc, model, objRotas, escolasAdicionadas);
			// removeEscolasJaAdicionadas(escolas, escolasAdicionadas);
			// CriaPanelEntregar Escola
			List<Panel> panelTarde = criarPanelEntregarEscola(application, fc, "p", escolas, carro.getId(), periodo,
					escolasAdicionadas);
			for (Panel p : panelTarde) {
				getDashboard().getChildren().add(p);
				columnTarde.addWidget(p.getId());
			}

			criarPanelTroca(application, fc, trocasCriancasPego, trocasCriancasPasso, columnTarde);

			break;

		case INTEGRAL:
			removerAlunosPegoPorOutroCarro(alunos, todosAlunos, trocasCriancasPego, alunosPegoPorOutroCarro);

			criaPanelAlunosPegar(application, fc, alunos, columnTarde);

			populaListaEscolas(todosAlunos, escolas);

			montaPanelLadoDireito(application, fc, model, objRotas, escolasAdicionadas);

			// removeEscolasJaAdicionadas(escolas, escolasAdicionadas);

			criarPanelEscolas(application, fc, escolas, columnTarde, escolasAdicionadas);

			criarPanelTroca(application, fc, trocasCriancasPego, trocasCriancasPasso, columnTarde);

			break;

		default:
			break;
		}

	}

	private void removerAlunosPassadorTroca(HashedMap<Carro, List<Aluno>> trocasCriancasPasso,
			List<Aluno> alunosParaEntregar) {
		for (Carro c : trocasCriancasPasso.keySet()) {
			alunosParaEntregar.removeAll(trocasCriancasPasso.get(c));
		}

	}

	private void criarPanelTroca(Application application, FacesContext fc, HashedMap<Carro, List<Aluno>> trocas,
			HashedMap<Carro, List<Aluno>> trocasPasso, DashboardColumn columnTarde) {
		
		for (Carro car : trocas.keySet()) {
			ObjetoRota trocaCarro = alunoService.findObjetosRota(carro.getId(), periodo,car.getId(),PegarEntregarEnun.PEGAR);
			
			if(trocaCarro == null){
				Panel trocaTarde = criarPanelTROCA(application, fc, "p", carro, trocas.get(car), periodo, car, true);
				
				if(trocaTarde != null){
					getDashboard().getChildren().add(trocaTarde);
					columnTarde.addWidget(trocaTarde.getId());	
				}				
			}
		}

		for (Carro car : trocasPasso.keySet()) {
			ObjetoRota trocaCarro = alunoService.findObjetosRota(carro.getId(), periodo,car.getId(),PegarEntregarEnun.ENTREGAR);
			if(trocaCarro == null){
				Panel  trocaTarde= criarPanelTROCA(application, fc, "e", carro, trocasPasso.get(car), periodo, car, false);
				if(trocaTarde != null){
					getDashboard().getChildren().add(trocaTarde);
					columnTarde.addWidget(trocaTarde.getId());	
				}				
			}
		}
	}

	private void criarPanelEscolas(Application application, FacesContext fc, HashedMap<EscolaEnum, List<Aluno>> escolas,
			DashboardColumn columnTarde, Map<EscolaEnum, PegarEntregarEnun> escolasJaAdicionadas) {
		// CriaPanelEntregar Escola
		List<Panel> panelMeioDia = criarPanelPegarEntregarEscola(application, fc, "p", escolas, carro.getId(), periodo,
				escolasJaAdicionadas);
		for (Panel p : panelMeioDia) {
			if (p != null) {
				getDashboard().getChildren().add(p);
				columnTarde.addWidget(p.getId());
			}

		}
		escolas.clear();
	}

	private void montaPanelLadoDireito(Application application, FacesContext fc, DashboardModel model,
			List<ObjetoRota> objRotas, Map<EscolaEnum, PegarEntregarEnun> escolasJaAdicionadas) {
		// MONTA O PANEL DO LADO DIREITO DA ROTA
		Collections.sort(objRotas);
		for (ObjetoRota objR : objRotas) {
			Panel panel3 = criarPanel(application, fc, objR.getPegarEntregar().getSigla(), objR, carro.getId(),
					periodo);
			if (panel3 != null) {
				getDashboard().getChildren().add(panel3);
				DashboardColumn column2 = model.getColumn(1);
				column2.addWidget(panel3.getId());
				if (objR.getEscola() != null) {
					escolasJaAdicionadas.put(objR.getEscola(), objR.getPegarEntregar());
				}
			}

		}
	}

	private void populaListaEscolas(List<Aluno> todosAlunos, HashedMap<EscolaEnum, List<Aluno>> escolas) {
		// Todos os alunos
		for (Aluno alun : todosAlunos) {
			if (escolas.containsKey(alun.getEscola())) {
				escolas.get(alun.getEscola()).add(alun);
			} else {
				List<Aluno> listaAlunos = new ArrayList<>();
				listaAlunos.add(alun);
				escolas.put(alun.getEscola(), listaAlunos);
			}

		}
	}

	private void populaListaEscolas(HashedMap<Carro, List<Aluno>> trocas, HashedMap<EscolaEnum, List<Aluno>> escolas) {
		// Todos os alunos
		for (Carro c : trocas.keySet()) {
			for (Aluno alun : trocas.get(c)) {
				if (escolas.containsKey(alun.getEscola())) {
					escolas.get(alun.getEscola()).add(alun);
				} else {
					List<Aluno> listaAlunos = new ArrayList<>();
					listaAlunos.add(alun);
					escolas.put(alun.getEscola(), listaAlunos);
				}
			}
		}
	}

	private void criaPanelAlunosPegar(Application application, FacesContext fc, List<Aluno> alunos,
			DashboardColumn columnTarde) {
		for (Aluno al : alunos) {
			// CriaPanelPegar
			Panel panel = criarPanel(application, fc, "e", al, carro.getId(), periodo);
			if (panel != null) {
				getDashboard().getChildren().add(panel);
				columnTarde.addWidget(panel.getId());
			}
		}
	}

	private void removerAlunosPegoPorOutroCarro(List<Aluno> alunos, List<Aluno> todosAlunos,
			HashedMap<Carro, List<Aluno>> trocas, List<Aluno> alunosPegoPorOutroCarro) {

		for (Carro aux : trocas.keySet()) {
			if(trocas.get(aux) != null && trocas.get(aux).size() >0){
				if(trocas.get(aux).get(0).getCarroLevaParaEscola() != null && trocas.get(aux).get(0).getCarroLevaParaEscola().equals(carro)){
					alunos.removeAll(trocas.get(aux));
				}else if (trocas.get(aux).get(0).getCarroPegaEscola() != null && trocas.get(aux).get(0).getCarroPegaEscola().equals(carro)){
					alunos.removeAll(trocas.get(aux));
				}
			}
		}
		todosAlunos.removeAll(alunosPegoPorOutroCarro);
	}

	private List<Panel> criarPanelPegarEntregarEscola(Application application, FacesContext fc, String modificadorID,
			HashedMap<EscolaEnum, List<Aluno>> escolas, Long idCarro, PerioddoEnum periodo,
			Map<EscolaEnum, PegarEntregarEnun> escolasJaAdicionadas) {

		List<Panel> panels = new ArrayList<>();

		for (EscolaEnum ee : escolas.keySet()) {

			if (ee != null) {
				List<Aluno> todosAlunosEscola = escolas.get(ee);
				List<Aluno> alunosVoltandoDaAula = new ArrayList<>();

				if (!(escolasJaAdicionadas.containsKey(ee)
						&& escolasJaAdicionadas.get(ee).equals(PegarEntregarEnun.PEGAR))) {
					for (Aluno a : todosAlunosEscola) {
						if (a.getPeriodo().equals(PerioddoEnum.MANHA)) {
							alunosVoltandoDaAula.add(a);
						}
					}

					Panel panelMANHA = (Panel) application.createComponent(fc, "org.primefaces.component.Panel",
							"org.primefaces.component.PanelRenderer");

					/*
					 * panelMANHA.setId("Escola" + "p" + ee.ordinal() + "-" +
					 * idCarro + "-" + PerioddoEnum.INTEGRAL.ordinal() + "-");
					 */
					panelMANHA.setId("q1" + "-" + "p" + "-"+ ee.ordinal() + "-" + idCarro + "-"
							+ PerioddoEnum.INTEGRAL.ordinal() + "-");

					panelMANHA.setHeader(ee.getName() + " " + escolas.get(ee).size());
					panelMANHA.setClosable(false);
					panelMANHA.setStyleClass("panelRotas");
					panelMANHA.setCollapsed(false);

					if (alunosVoltandoDaAula != null && !alunosVoltandoDaAula.isEmpty()) {
						StringBuilder texto = new StringBuilder();
						for (Aluno al : alunosVoltandoDaAula) {
							texto.append(al.getNomeAluno());
							texto.append("  - ");
						}
						HtmlOutputText text = new HtmlOutputText();
						text.setValue(texto);
						panelMANHA.getChildren().add(text);
						// panel.setFooter(al.getEscola().getName());
						panelMANHA.setToggleable(true);
						panels.add(panelMANHA);
					}
				}

				if (!(escolasJaAdicionadas.containsKey(ee)
						&& escolasJaAdicionadas.get(ee).equals(PegarEntregarEnun.ENTREGAR))) {
					Panel panelTarde = (Panel) application.createComponent(fc, "org.primefaces.component.Panel",
							"org.primefaces.component.PanelRenderer");

					/*
					 * panelTarde.setId("Escola" + "E" + ee.ordinal() + "-" +
					 * idCarro + "-" + PerioddoEnum.INTEGRAL + "-");
					 */
					panelTarde
							.setId("q1" + "-" + "E"+ "-" + ee.ordinal() + "-" + idCarro + "-" + PerioddoEnum.INTEGRAL + "-");

					panelTarde.setHeader(ee.getName() + " " + escolas.get(ee).size());
					panelTarde.setClosable(false);
					panelTarde.setStyleClass("panelRotas");
					panelTarde.setCollapsed(false);

					todosAlunosEscola.removeAll(alunosVoltandoDaAula);
					if (todosAlunosEscola != null && !todosAlunosEscola.isEmpty()) {
						StringBuilder texto = new StringBuilder();
						for (Aluno al : todosAlunosEscola) {
							texto.append(al.getNomeAluno());
							texto.append("  - ");
						}
						HtmlOutputText text = new HtmlOutputText();
						text.setValue(texto);
						panelTarde.getChildren().add(text);
						// panel.setFooter(al.getEscola().getName());
						panelTarde.setToggleable(true);
						panels.add(panelTarde);

					}
				}

			}

		}

		return panels;

	}

	private Panel criarPanelTROCA(Application application, FacesContext fc, String modificadorID, Carro carro,
			List<Aluno> alunos, PerioddoEnum periodo2, Carro car, Boolean pego) {
		if (alunos != null && alunos.size() > 0) {
			Panel panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel",
					"org.primefaces.component.PanelRenderer");

			/*
			 * panel.setId("TROCA" + modificadorID + "-" + carro.getId() + "-" +
			 * periodo2.ordinal() + "-" + car.getId() + "-");
			 */
			panel.setId("q2" + "-" + modificadorID + "-" + car.getId() + "-" + carro.getId() + "-" + periodo2.ordinal()
					+ "-");

			if (pego) {
				panel.setHeader("TROCA com " + car.getNome() + " Pegar");
			} else {
				panel.setHeader("TROCA com " + car.getNome() + " Passar ");
			}
			panel.setClosable(false);
			panel.setStyleClass("panelRotas");
			panel.setCollapsed(false);

			StringBuilder sb = new StringBuilder();
			for (Aluno al : alunos) {
				sb.append(al.getNomeAluno());
				sb.append(" - ");

			}
			HtmlOutputText text = new HtmlOutputText();
			text.setValue(sb.toString());
			panel.getChildren().add(text);

			panel.setToggleable(true);
			return panel;

		}else{
			return null;
		}
	}

	private List<Aluno> getAlunos(List<ObjetoRota> objRotas) {
		List<Aluno> ors = new ArrayList<>();
		try {
			for (ObjetoRota ob : objRotas) {
				Aluno al = ob.getAluno();
				if (al != null) {
					ors.add(al);
				}
			}
		} catch (Exception e) {
		}

		return ors;
	}

	private List<Panel> criarPanelEntregarEscola(Application application, FacesContext fc, String modificadorID,
			HashedMap<EscolaEnum, List<Aluno>> escolas, Long idCarro, PerioddoEnum periodo,
			Map<EscolaEnum, PegarEntregarEnun> escolasJaAdicionadas) {
		List<Panel> panels = new ArrayList<>();

		for (EscolaEnum ee : escolas.keySet()) {
			if (ee != null) {
				if (!(escolasJaAdicionadas.containsKey(ee)
						&& escolasJaAdicionadas.get(ee).equals(PegarEntregarEnun.ENTREGAR))) {
					Panel panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel",
							"org.primefaces.component.PanelRenderer");
					/*
					 * panel.setId("Escola" + modificadorID + ee.ordinal() + "-"
					 * + idCarro + "-" + periodo.ordinal() + "-");
					 */

					panel.setId("q1" + "-" + modificadorID + "-" + ee.ordinal() + "-" + idCarro + "-"
							+ periodo.ordinal() + "-");

					panel.setHeader(ee.getName() + " " + escolas.get(ee).size());
					panel.setClosable(false);
					panel.setStyleClass("panelRotas");
					panel.setCollapsed(false);

					if (escolas.get(ee) != null && !escolas.get(ee).isEmpty()) {
						StringBuilder texto = new StringBuilder();
						for (Aluno al : escolas.get(ee)) {
							texto.append(al.getNomeAluno());
							texto.append("  - ");
						}
						HtmlOutputText text = new HtmlOutputText();
						text.setValue(texto);
						panel.getChildren().add(text);
						// panel.setFooter(al.getEscola().getName());
					}

					panel.setToggleable(true);
					panels.add(panel);

				}
			}

		}

		return panels;
	}

	private Panel criarPanel(Application application, FacesContext fc, String modificadorID, ObjetoRota objRota,
			Long idCarro, PerioddoEnum periodo) {
		Panel panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel",
				"org.primefaces.component.PanelRenderer");
		String tipo = "0";
		Long id = 0L;
		if (objRota != null) {
			if (objRota.getIdCarroAlvo() != null) {
				tipo = "2";
				id = objRota.getIdCarroAlvo();
			} else if (objRota.getEscola() != null) {
				tipo = "1";
				id = Long.parseLong(String.valueOf(objRota.getEscola().ordinal()));
			} else {
				id = objRota.getAluno().getId();
			}

		}

		if (objRota != null && (objRota.getAluno() != null || objRota.getEscola() != null)) {
			String header = objRota.getAluno() != null ? objRota.getAluno().getNomeAluno() : objRota.getEscola().name();

			/*
			 * panel.setId(modificadorID + objRota.getId() + "-" + idCarro + "-"
			 * + periodo.ordinal() + "-");
			 */

			panel.setId("q" + tipo + "-" + modificadorID + "-" + id + "-" + idCarro + "-" + periodo.ordinal() + "-"
					+ objRota.getId() + "-");

			panel.setHeader(header + (modificadorID.equalsIgnoreCase("p") ? " - Pegar "
					: "" + (modificadorID.equalsIgnoreCase("e") ? " - Entregar " : "")));

			panel.setClosable(false);
			panel.setStyleClass("panelRotas");
			panel.setCollapsed(false);

			if (objRota.getAluno() != null && objRota.getAluno().getEscola() != null) {
				HtmlOutputText text = new HtmlOutputText();
				text.setValue(objRota.getAluno().getEscola().getName());
				panel.getChildren().add(text);
			} else if (objRota.getEscola() != null) {
				HtmlOutputText text = new HtmlOutputText();
				PegarEntregarEnun pee = modificadorID.indexOf("p") >= 0 ? PegarEntregarEnun.PEGAR
						: PegarEntregarEnun.ENTREGAR;
				List<Aluno> alunos = null;

				if (periodo.equals(PerioddoEnum.TARDE)) {
					alunos = alunoService.findAlunoPegaEscolaTarde(objRota.getEscola(), carro);
				} else if (periodo.equals(PerioddoEnum.MANHA)) {
					alunos = alunoService.findAlunoPegaEscolaManha(objRota.getEscola(), carro);
				} else {

					if (objRota.getPegarEntregar() != null
							&& objRota.getPegarEntregar().equals(PegarEntregarEnun.PEGAR)) {
						alunos = alunoService.findAlunoPegaEscolaMeioDia(objRota.getEscola(), carro);
					} else {
						alunos = alunoService.findAlunoLevaEscolaMeioDia(objRota.getEscola(), carro);
					}
				}

				StringBuilder sb = new StringBuilder();
				for (Aluno al : alunos) {
					sb.append(al.getNomeAluno());
					sb.append(" - ");
				}
				text.setValue(sb.toString());
				panel.getChildren().add(text);
			}

		} else {

			String header = "TROCA com " + turmaService.findById(objRota.getIdCarroAlvo()).getNome();
			/*
			 * panel.setId(modificadorID + objRota.getId() + "-" + idCarro + "-"
			 * + periodo.ordinal() + "-");
			 */
			panel.setId("q2" + "-" + modificadorID + "-" + objRota.getIdCarroAlvo() + "-" + idCarro + "-"
					+ periodo.ordinal() + "-" + objRota.getId() + "-");

			panel.setHeader(header + (modificadorID.equalsIgnoreCase("p") ? " - Pegar "
					: "" + (modificadorID.equalsIgnoreCase("e") ? " - Entregar " : "")));

			panel.setClosable(false);
			panel.setStyleClass("panelRotas");
			panel.setCollapsed(false);
			HtmlOutputText text = new HtmlOutputText();
			text.setValue(objRota.getDescricao());
			panel.getChildren().add(text);
		}

		panel.setToggleable(true);

		return panel;

	}

	private Panel criarPanel(Application application, FacesContext fc, String modificadorID, Aluno aluno, Long idCarro,
			PerioddoEnum periodo) {

		if (aluno != null && aluno.getPeriodo().equals(PerioddoEnum.TARDE) && periodo.equals(PerioddoEnum.INTEGRAL)) {
			modificadorID = "p";
		}

		String header = aluno != null ? aluno.getNomeAluno() : aluno.getEscola().name();
		Panel panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel",
				"org.primefaces.component.PanelRenderer");

		/*
		 * panel.setId(modificadorID + aluno.getId() + "-" + idCarro + "-" +
		 * periodo.ordinal() + "-");
		 */

		panel.setId("q0" + "-" + modificadorID + "-" + aluno.getId() + "-" + idCarro + "-" + periodo.ordinal() + "-");

		panel.setHeader(header + (modificadorID.equalsIgnoreCase("p") ? " - Pegar "
				: "" + (modificadorID.equalsIgnoreCase("e") ? " - Entregar " : "")));

		panel.setClosable(false);
		panel.setStyleClass("panelRotas");
		panel.setCollapsed(false);
		if (aluno != null && aluno.getEscola() != null) {
			HtmlOutputText text = new HtmlOutputText();
			text.setValue(aluno.getEscola().getName());
			panel.getChildren().add(text);
			// panel.setFooter(al.getEscola().getName());
		}

		panel.setToggleable(true);

		return panel;
	}

	public String editarRotaManha(Long idTurma) {
		carro = turmaService.findById(idTurma);
		Util.addAtributoSessao("turma", carro);
		init();
		return "cadastrar";
	}

	public String editarRotaMeioDia(Long idTurma) {
		carro = turmaService.findById(idTurma);
		Util.addAtributoSessao("turma", carro);
		init();
		return "cadastrar";
	}

	public String editarRotaTarde(Long idTurma) {
		carro = turmaService.findById(idTurma);
		Util.addAtributoSessao("turma", carro);
		init();
		return "cadastrar";
	}

	public List<Carro> getCarros() {
		if (getLoggedUser().getProfessor() != null) {
			return turmaService.findAll(getLoggedUser().getProfessor().getId());

		} else {
			return turmaService.findAll();
		}
	}

	public String voltar() {
		return "index";
	}

	public Dashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}
}