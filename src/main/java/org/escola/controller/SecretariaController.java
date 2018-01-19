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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.escola.model.Custo;
import org.escola.service.CustoService;
import org.escola.util.Constant;
import org.escola.util.Util;
import org.escola.util.UtilFinalizarAnoLetivo;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@Model
@ViewScoped
public class SecretariaController {

	@Produces
	@Named
	private Custo custo;

	@Inject
	private CustoService custoService;

	private LazyDataModel<Custo> lazyListDataModel;
	private LazyDataModel<Custo> lazyListDataModelJan;
	private LazyDataModel<Custo> lazyListDataModelFev;
	private LazyDataModel<Custo> lazyListDataModelMar;
	private LazyDataModel<Custo> lazyListDataModelAbr;
	private LazyDataModel<Custo> lazyListDataModelMai;
	private LazyDataModel<Custo> lazyListDataModelJun;
	private LazyDataModel<Custo> lazyListDataModelJul;
	private LazyDataModel<Custo> lazyListDataModelAgo;
	private LazyDataModel<Custo> lazyListDataModelSet;
	private LazyDataModel<Custo> lazyListDataModelOut;
	private LazyDataModel<Custo> lazyListDataModelNov;
	private LazyDataModel<Custo> lazyListDataModelDez;

	private DashboardModel dashboardModelManha;
	private DashboardModel dashboardModelMeioDia;
	private DashboardModel dashboardModelTarde;

	@PostConstruct
	private void init() {
		if (custo == null) {
			custo = new Custo();
		}

		setDashboardModelManha(new DefaultDashboardModel());
		setDashboardModelMeioDia(new DefaultDashboardModel());
		setDashboardModelTarde(new DefaultDashboardModel());

		DashboardColumn column1 = new DefaultDashboardColumn();
		DashboardColumn column2 = new DefaultDashboardColumn();
		DashboardColumn column3 = new DefaultDashboardColumn();

		column1.addWidget("avaliable");
		column1.addWidget("used");


		dashboardModelManha.addColumn(column1);
		dashboardModelManha.addColumn(column2);
		dashboardModelManha.addColumn(column3);

	}
	
	public void handleReorder(DashboardReorderEvent event) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_INFO);
		message.setSummary("Reordered: " + event.getWidgetId());
		message.setDetail("Item index: " + event.getItemIndex() + ", Column index: " + event.getColumnIndex()
				+ ", Sender index: " + event.getSenderColumnIndex());

		addMessage(message);
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

	public String adicionar(Custo custo) {
		custoService.save(custo);
		return "index";
	}

	public String adicionarNovo() {
		return "cadastrar";
	}

	public String editar(Long idprof) {
		custo = custoService.findById(idprof);
		Util.addAtributoSessao("custo", custo);
		return "cadastrar";
	}

	public LazyDataModel<Custo> getLazyDataModelJan() {
		if (lazyListDataModelJan == null) {

			lazyListDataModelJan = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.JANUARY);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.JANUARY);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelJan.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelJan.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelJan;

	}

	public LazyDataModel<Custo> getLazyDataModelFev() {
		if (lazyListDataModelFev == null) {

			lazyListDataModelFev = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.FEBRUARY);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.FEBRUARY);
					c2.set(Calendar.DAY_OF_MONTH, 29);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelFev.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelFev.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelFev;

	}

	public LazyDataModel<Custo> getLazyDataModelMar() {
		if (lazyListDataModelMar == null) {

			lazyListDataModelMar = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.MARCH);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.MARCH);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelMar.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelMar.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelMar;

	}

	public LazyDataModel<Custo> getLazyDataModelJun() {
		if (lazyListDataModelJun == null) {

			lazyListDataModelJun = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.JUNE);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.JUNE);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelJun.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelJun.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelJun;

	}

	public LazyDataModel<Custo> getLazyDataModelJul() {
		if (lazyListDataModelJul == null) {

			lazyListDataModelJul = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.JULY);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.JULY);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelJul.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelJul.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelJul;

	}

	public LazyDataModel<Custo> getLazyDataModelAgo() {
		if (lazyListDataModelAgo == null) {

			lazyListDataModelAgo = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.AUGUST);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.AUGUST);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelAgo.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelAgo.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelAgo;

	}

	public LazyDataModel<Custo> getLazyDataModelSet() {
		if (lazyListDataModelSet == null) {

			lazyListDataModelSet = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.SEPTEMBER);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.SEPTEMBER);
					c2.set(Calendar.DAY_OF_MONTH, 30);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelSet.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelSet.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelSet;

	}

	public LazyDataModel<Custo> getLazyDataModelOut() {
		if (lazyListDataModelOut == null) {

			lazyListDataModelOut = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.OCTOBER);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.OCTOBER);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelOut.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelOut.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelOut;

	}

	public LazyDataModel<Custo> getLazyDataModelNov() {
		if (lazyListDataModelNov == null) {

			lazyListDataModelNov = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.NOVEMBER);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.NOVEMBER);
					c2.set(Calendar.DAY_OF_MONTH, 30);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelNov.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelNov.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelNov;

	}

	public LazyDataModel<Custo> getLazyDataModelDez() {
		if (lazyListDataModelDez == null) {

			lazyListDataModelDez = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.DECEMBER);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.DECEMBER);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelDez.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelDez.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelDez;

	}

	public LazyDataModel<Custo> getLazyDataModelMai() {
		if (lazyListDataModelMai == null) {

			lazyListDataModelMai = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.MAY);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.MAY);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelMai.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelMai.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelMai;

	}

	public LazyDataModel<Custo> getLazyDataModelAbr() {
		if (lazyListDataModelAbr == null) {

			lazyListDataModelAbr = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					List<Date> datasEntre = new ArrayList<>();
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.MONTH, Calendar.APRIL);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c.getTime());

					Calendar c2 = Calendar.getInstance();
					c2.set(Calendar.HOUR_OF_DAY, 23);
					c2.set(Calendar.MINUTE, 59);
					c2.set(Calendar.SECOND, 59);
					c2.set(Calendar.MILLISECOND, 999);
					c2.set(Calendar.MONTH, Calendar.APRIL);
					c2.set(Calendar.DAY_OF_MONTH, 31);
					c2.set(Calendar.YEAR, Constant.anoLetivoAtual);
					datasEntre.add(c2.getTime());

					filtros.put("dateBetween", datasEntre);

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelAbr.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelAbr.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModelAbr;

	}

	public LazyDataModel<Custo> getLazyDataModel() {
		if (lazyListDataModel == null) {

			lazyListDataModel = new LazyDataModel<Custo>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Custo getRowData(String rowKey) {
					return custoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Custo al) {
					return al.getId();
				}

				@Override
				public List<Custo> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Custo> ol = custoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModel.setRowCount((int) custoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) custoService.count(filtros));
					return null;

				}
			};
			lazyListDataModel.setRowCount((int) custoService.count(null));

		}

		return lazyListDataModel;

	}
	
	public String remover(Long idcusto){
		custoService.remover(idcusto);
		return "index";
	}

	public String linkAlunos() {
		return "listagemAlunos";
	}

	public String linkProfessores() {
		return "listagemProfessores";
	}

	public String linkCalendario() {
		return "listagemCalendario";
	}

	public String linkTurmas() {
		return "listagemTurmas";
	}

	public DashboardModel getDashboardModelManha() {
		return dashboardModelManha;
	}

	public void setDashboardModelManha(DashboardModel dashboardModelManha) {
		this.dashboardModelManha = dashboardModelManha;
	}

	public DashboardModel getDashboardModelMeioDia() {
		return dashboardModelMeioDia;
	}

	public void setDashboardModelMeioDia(DashboardModel dashboardModelMeioDia) {
		this.dashboardModelMeioDia = dashboardModelMeioDia;
	}

	public DashboardModel getDashboardModelTarde() {
		return dashboardModelTarde;
	}

	public void setDashboardModelTarde(DashboardModel dashboardModelTarde) {
		this.dashboardModelTarde = dashboardModelTarde;
	}

}
