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
package org.escola.controller.aluno;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.escolar.model.Aluno;
import org.escolar.model.ContratoAluno;
import org.primefaces.event.FlowEvent;

@Named
@ViewScoped
public class PedidoMatriculaController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private ContratoAluno contratoAlun;
	
	@PostConstruct
	public void init() {
		contratoAlun = new ContratoAluno();
		Aluno a = new Aluno();
		contratoAlun.setAluno(a);
		System.out.println("aaaaaaaaaaaaa");
	}

	public String enviar() {
		contratoAlun.getAluno().getNomeAluno();
		return "enviadoComSucesso";
	}

	public String onFlowProcess(FlowEvent event) {
		return event.getNewStep();
	}

	public ContratoAluno getContratoAlun() {
		return contratoAlun;
	}

	public void setContratoAlun(ContratoAluno contratoAlun) {
		this.contratoAlun = contratoAlun;
	}

}
