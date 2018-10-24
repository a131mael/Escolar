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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.model.TabelaPrecos;
import org.escolar.service.TabelaPrecoService;
import org.primefaces.event.RowEditEvent;

@Named
@ViewScoped
public class TabelaPrecosController implements Serializable {

	/****/
	private static final long serialVersionUID = 1L;

	@Inject
	private TabelaPrecoService tabelaPrecoService;

	private List<TabelaPrecos> listaPrecos;

	@PostConstruct
	public void init() {
		listaPrecos = tabelaPrecoService.findAll();
	}

	public TabelaPrecoService getTabelaPrecoService() {
		return tabelaPrecoService;
	}

	public void setTabelaPrecoService(TabelaPrecoService tabelaPrecoService) {
		this.tabelaPrecoService = tabelaPrecoService;
	}

	public void onRowEdit(RowEditEvent event) {
		  TabelaPrecos tb = (TabelaPrecos) event.getObject();
		  tabelaPrecoService.save(tb);
		  
		  FacesMessage msg = new FacesMessage("Tabela Editada");
		  FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void onRowCancel(RowEditEvent event) {
		FacesMessage msg = new FacesMessage("Edição Cancelada");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void onAddNew() {
		TabelaPrecos tabelaPreco = new TabelaPrecos();
		tabelaPreco.setAno((short) 2019);
		getListaPrecos().add(tabelaPreco);
	}

	public List<TabelaPrecos> getListaPrecos() {
		return listaPrecos;
	}

	public List<TabelaPrecos> setListaPrecos(List<TabelaPrecos> listaPrecos) {
		this.listaPrecos = listaPrecos;
		return listaPrecos;
	}

}
