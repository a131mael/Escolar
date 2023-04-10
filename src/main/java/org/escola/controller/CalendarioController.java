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
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.enums.FormaPagamentoEnum;
import org.escolar.model.Carro;
import org.escolar.model.CarroFrete;
import org.escolar.model.Configuracao;
import org.escolar.model.Contratante;
import org.escolar.model.Evento;
import org.escolar.model.Frete;
import org.escolar.service.AlunoService;
import org.escolar.service.EventoService;
import org.escolar.service.FreteService;
import org.escolar.util.UtilFinalizarAnoLetivo;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

@Named
@ViewScoped
public class CalendarioController implements Serializable {

	/****/
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private ScheduleModel eventModel;
	private ScheduleEvent event = new DefaultScheduleEvent();

	@Inject
	private AlunoService alunoService;

	@Inject
	private EventoService eventoService;

	@Inject
	private org.escolar.service.ConfiguracaoService configuracaoService;

	@Inject
	private FreteService freteService;

	@Produces
	@Named
	private Evento evento;

	@Produces
	@Named
	private Frete frete;

	private Configuracao conf;

	@Inject
	private UtilFinalizarAnoLetivo mudarDatas;

	private List<Carro> carrosSelecionados;

	@PostConstruct
	private void init() {
		setConf(configuracaoService.getConfiguracao());
		mudarDatas.mudaDataDosEventosParaAnoLetivoAtual();
		evento = new Evento();
		frete = new Frete();
		Contratante c = new Contratante();
		frete.setContratante(c);
		setEventModel(new DefaultScheduleModel());
		montarAgenda();
		/*
		 * getEventModel().addEvent(new DefaultScheduleEvent("Champions League Match",
		 * previousDay8Pm(), previousDay11Pm())); getEventModel().addEvent(new
		 * DefaultScheduleEvent("Birthday Party", today1Pm(), today6Pm()));
		 * getEventModel().addEvent(new DefaultScheduleEvent("Breakfast at Tiffanys",
		 * nextDay9Am(), nextDay11Am())); getEventModel().addEvent(new
		 * DefaultScheduleEvent("Plant the new garden stuff", theDayAfter3Pm(),
		 * fourDaysLater3pm()));
		 */
	}

	private List<Frete> getEventos() {
		List<Frete> eventos = freteService.findAll();
		return eventos;
	}

	private void montarAgenda() {
		Calendar date = Calendar.getInstance();
		Calendar dateFim = Calendar.getInstance();

		getEventModel().clear();
		for (Frete evento : getEventos()) {
			if (evento.getHorarioLocalOrigem() != null && evento.getHorarioParaRetorno() != null) {

				date.setTime(evento.getHorarioLocalOrigem());
				// date.add(Calendar.HOUR, 8);

				dateFim.setTime(evento.getHorarioParaRetorno());
				// dateFim.add(Calendar.HOUR, 12);

				String titulo = "#" + evento.getId() + "# - " + evento.getLocalDestino() + " - "
						+ evento.getContratante().getNome();

				if (evento.getValor() != null && evento.getValor().equals(evento.getValorPago())) {
					titulo += " - PAGO";
				} else {
					titulo += " - ABERTO";
				}
				ScheduleEvent ev = new DefaultScheduleEvent(titulo, date.getTime(), dateFim.getTime());

				getEventModel().addEvent(ev);
			}
		}
	}

	private List<Carro> montarCarrosSelecionados(List<CarroFrete> carrosf) {
		List<Carro> carros = new ArrayList<Carro>();
		if (carrosf != null) {
			for (CarroFrete cf : carrosf) {
				carros.add(cf.getCarro());
			}
		}
		return carros;
	}

	public void buscarContratanteCPF() {
		if(frete.getContratante().getCPF_CNPJ() != null && !frete.getContratante().getCPF_CNPJ().equalsIgnoreCase("")) {
			Contratante contratante = freteService.findContratanteByCPF(frete.getContratante().getCPF_CNPJ());
			if (contratante != null) {
				frete.setContratante(contratante);
			}
		}
		RequestContext.getCurrentInstance().update("eventDialog");
	}

	public void salvarContratante() {
		Contratante c = freteService.saveContratante(frete.getContratante());
		frete.setContratante(c);
		RequestContext.getCurrentInstance().update("eventDialog");
	}

	public void salvarFrete() {
		List<CarroFrete> carros = new ArrayList<>();

		if (carrosSelecionados != null) {
			for (Carro carro : carrosSelecionados) {
				CarroFrete cf = new CarroFrete();
				cf.setCarro(carro);
				carros.add(cf);
			}
		}
		frete.setCarroFrete(carros);
		
		
		freteService.save(frete);
		frete = new Frete();
		Contratante c = new Contratante();
		frete.setContratante(c);
		montarAgenda();

		carrosSelecionados = null;
	}
	
	public double getTotalEmCaixa() {
		Double total = conf.getValordinheiroEmcaixa(); 
		total -= freteService.getTotalPagoMotorista();
		total -= freteService.getTotalGastoDinheiro(FormaPagamentoEnum.DINHEIRO);
		total += freteService.getTotalRecebidoFormaPagamento(FormaPagamentoEnum.DINHEIRO);
		return total;
	}
	
	public double getValorRecebidoDinheiro() {
		Double valor = 0d;
		if(getEventModel().getEventCount() >0) {
			Date date = (Date) getEventModel().getEvents().get(0).getStartDate();
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			valor = freteService.getTotalRecebidoMesFormaPagamento(c.get(Calendar.MONTH)+1, FormaPagamentoEnum.DINHEIRO);
		}
		return valor;
	}

	
	public double getValorRecebidoMes() {
		Double valor = 0d;
		if(getEventModel().getEventCount() >0) {
			Date date = (Date) getEventModel().getEvents().get(0).getStartDate();
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			valor = freteService.getTotalRecebidoMes(c.get(Calendar.MONTH)+1);
		}
		return valor;
	}
	
	public double getValorPagoMotoristaMes() {
		Double valor = 0d;
		if(getEventModel().getEventCount() >0) {
			Date date = (Date) getEventModel().getEvents().get(0).getStartDate();
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			valor = freteService.getTotalPagoMotoristaMes(c.get(Calendar.MONTH)+1);
		}
		return valor;
	}

	public String salvar() {
		eventoService.save(evento);
		return "index";
	}

	public String voltar() {
		return "index";
	}

	public String editar(Long idprof) {
		// event = alunoService.findById(idprof);
		// Util.addAtributoSessao("aluno", aluno);
		return "cadastrar";
	}

	public String remover(Long idTurma) {
		alunoService.remover(idTurma);
		return "index";
	}

	public String adicionarNovo() {
		return "cadastrar";
	}

	public String cadastrarNovo() {

		return "eventoAluno";
	}

	public Date getRandomDate(Date base) {
		Calendar date = Calendar.getInstance();
		date.setTime(base);
		date.add(Calendar.DATE, ((int) (Math.random() * 30)) + 1); // set random day of month

		return date.getTime();
	}

	public Date getInitialDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR), Calendar.FEBRUARY, calendar.get(Calendar.DATE), 0, 0, 0);

		return calendar.getTime();
	}

	private Calendar today() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);

		return calendar;
	}

	private Date previousDay8Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) - 1);
		t.set(Calendar.HOUR, 8);

		return t.getTime();
	}

	private Date previousDay11Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) - 1);
		t.set(Calendar.HOUR, 11);

		return t.getTime();
	}

	private Date today1Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.HOUR, 1);

		return t.getTime();
	}

	private Date theDayAfter3Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.DATE, t.get(Calendar.DATE) + 2);
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.HOUR, 3);

		return t.getTime();
	}

	private Date today6Pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.HOUR, 6);

		return t.getTime();
	}

	private Date nextDay9Am() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) + 1);
		t.set(Calendar.HOUR, 9);

		return t.getTime();
	}

	private Date nextDay11Am() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.AM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) + 1);
		t.set(Calendar.HOUR, 11);

		return t.getTime();
	}

	private Date fourDaysLater3pm() {
		Calendar t = (Calendar) today().clone();
		t.set(Calendar.AM_PM, Calendar.PM);
		t.set(Calendar.DATE, t.get(Calendar.DATE) + 4);
		t.set(Calendar.HOUR, 3);

		return t.getTime();
	}

	public ScheduleEvent getEvent() {
		return event;
	}

	public void setEvent(ScheduleEvent event) {
		this.event = event;
	}

	public void addEvent(ActionEvent actionEvent) {
		if (event.getId() == null)
			getEventModel().addEvent(event);
		else
			getEventModel().updateEvent(event);

		event = new DefaultScheduleEvent();
	}

	public void onEventSelect(SelectEvent selectEvent) {
		event = (ScheduleEvent) selectEvent.getObject();
		String titulo = event.getTitle();
		String idFrete = titulo.substring(1, titulo.lastIndexOf("#"));

		frete = freteService.findById((Long.parseLong(idFrete)));
		carrosSelecionados = montarCarrosSelecionados(frete.getCarroFrete());

	}

	public void onDateSelect(SelectEvent selectEvent) {

		frete = new Frete();
		Contratante c = new Contratante();
		frete.setContratante(c);
		carrosSelecionados = new ArrayList<Carro>();
		
		Calendar date = Calendar.getInstance();
		Calendar dateFim = Calendar.getInstance();
		date.setTime((Date) selectEvent.getObject());
		date.add(Calendar.HOUR, 8);

		dateFim.setTime((Date) selectEvent.getObject());
		dateFim.add(Calendar.HOUR, 12);

		frete.setHorarioLocalOrigem(date.getTime());
		frete.setHorarioParaRetorno(dateFim.getTime());
	}

	public void onEventMove(ScheduleEntryMoveEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved",
				"Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());

		addMessage(message);
	}

	public void onEventResize(ScheduleEntryResizeEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized",
				"Day delta:" + event.getDayDelta() + ", Minute delta:" + event.getMinuteDelta());

		addMessage(message);
	}

	private void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public ScheduleModel getEventModel() {
		return eventModel;
	}

	public void setEventModel(ScheduleModel eventModel) {
		this.eventModel = eventModel;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public FreteService getFreteService() {
		return freteService;
	}

	public void setFreteService(FreteService freteService) {
		this.freteService = freteService;
	}

	public Frete getFrete() {
		return frete;
	}

	public void setFrete(Frete frete) {
		this.frete = frete;
	}

	public Configuracao getConf() {
		return conf;
	}

	public void setConf(Configuracao conf) {
		this.conf = conf;
	}

	public List<Carro> getCarrosSelecionados() {
		return carrosSelecionados;
	}

	public void setCarrosSelecionados(List<Carro> carrosSelecionados) {
		this.carrosSelecionados = carrosSelecionados;
	}

}
