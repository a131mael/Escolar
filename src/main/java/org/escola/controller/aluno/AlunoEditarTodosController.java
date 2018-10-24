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

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.model.Aluno;
import org.escolar.service.AlunoService;

@Named
@ViewScoped
public class AlunoEditarTodosController implements Serializable {

	/****/
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private Aluno alunoEditarTodos;
		
	@Inject
	private AlunoService alunoService;
	
	private int index = 0;
	
	public Aluno getAlunoEditarTodos() {
		return alunoEditarTodos;
	}

	public void setAlunoEditarTodos(Aluno alunoEditarTodos) {
		this.alunoEditarTodos = alunoEditarTodos;
	}

	@PostConstruct
	private void ini() {
		//alunoEditarTodos = alunoService.findAlunoSemEndereco();
		alunoEditarTodos = alunoService.findAlunoBairro(index);
		index ++;
	}
	
	public void proximo() {
		salvar();
		alunoEditarTodos = alunoService.findAlunoBairro(index);
		index ++;
	}
	
	public void salvar() {
		alunoService.saveAlunoEndereco(alunoEditarTodos);
		//Util.removeAtributoSessao("aluno");
		/*if (getLoggedUser().getTipoMembro().equals(TipoMembro.FINANCEIRO)) {
			return "indexFinanceiro";
		}
		return "index";*/
	}

	
}
