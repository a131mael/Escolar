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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.model.Aluno;
import org.escolar.model.ContratoAluno;
import org.escolar.service.AlunoService;
import org.escolar.service.ConfiguracaoService;
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

	@Inject
	private AlunoService alunoservice;

	@Inject
	private ConfiguracaoService configuracaoService;

	@PostConstruct
	public void init() {
		contratoAlun = new ContratoAluno();
		Aluno a = new Aluno();
		contratoAlun.setAluno(a);
	}

	public String enviar() {

		StringBuilder sb = new StringBuilder();
		sb.append(contratoAlun.getEndereco());
		sb.append(", N");
		sb.append(contratoAlun.getNumero());
		sb.append(",");
		sb.append(contratoAlun.getComplemento());

		contratoAlun.getAluno().setCadastroTemporario(true);
		contratoAlun.getAluno().setAtivo(true);
		contratoAlun.getAluno().setCodigo(alunoservice.getProximoCodigo() + "");
		contratoAlun.getAluno().setCpfResponsavel(contratoAlun.getCpfResponsavel());
		contratoAlun.getAluno().setNomeResponsavel(contratoAlun.getNomeResponsavel());
		contratoAlun.getAluno().setRemovido(false);
		contratoAlun.getAluno().setEnderecoAluno(contratoAlun.getAluno().getEndereco());
		Aluno alper = alunoservice.saveAluno(contratoAlun.getAluno(), true);

		contratoAlun.setAluno(alper);
		contratoAlun.setAno((short) configuracaoService.getConfiguracao().getAnoLetivo());
		contratoAlun.setEndereco(sb.toString());
		
		adicionarNovoContrato(alper,contratoAlun);
		

		//alunoservice.saveContrato(contratoAlun);
		init();
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

	public void adicionarNovoContrato(Aluno aluno, ContratoAluno novoContrato) {

		novoContrato.setAno((short) configuracaoService.getConfiguracao().getAnoRematricula());
		novoContrato.setCnabEnviado(false);
		novoContrato.setCancelado(false);
		novoContrato.setDataCancelamento(null);
		novoContrato.setBoletos(null);
		novoContrato.setContratoTerminado(false);
		novoContrato.setDataCriacaoContrato(new Date());
		novoContrato.setEnviadoSPC(false);
		novoContrato.setEnviadoParaCobrancaCDL(false);

		String ano = String.valueOf(novoContrato.getAno());
		String finalANo = ano.substring(ano.length() - 2, ano.length());
		String numeroUltimoContrato = "01";
		if (aluno.getContratos() != null) {
			for (ContratoAluno contratt : aluno.getContratos()) {
				if (contratt.getNumero() != null && !contratt.getNumero().equalsIgnoreCase("")) {
					if (contratt.getAno() == novoContrato.getAno()) {
						String numeroContratt = contratt.getNumero();
						numeroContratt = numeroContratt.substring(numeroContratt.length() - 2, numeroContratt.length());
						if (Integer.parseInt(numeroContratt) > Integer.parseInt(numeroUltimoContrato)) {
							numeroUltimoContrato = numeroContratt;
						}
					}
				}
				int numeroNovo = Integer.parseInt(numeroUltimoContrato);
				numeroNovo++;
				numeroUltimoContrato = String.valueOf(numeroNovo);
			}
		}

		String numero = finalANo + aluno.getCodigo() + "0" + numeroUltimoContrato;
		novoContrato.setNumero(numero);

		aluno = alunoservice.adicionarContrato(aluno, novoContrato);
	}

}
