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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.enums.PerioddoEnum;
import org.escolar.enums.Serie;
import org.escolar.enums.TipoMobilidadeEnum;
import org.escolar.model.Aluno;
import org.escolar.model.Carro;
import org.escolar.service.AlunoService;
import org.escolar.service.RelatorioService;

@Named
@ViewScoped
public class RelatorioController implements Serializable{

	/****/
	private static final long serialVersionUID = 1L;
		
	@Inject
    private RelatorioService relatorioService;
	
	@Inject
    private AlunoService alunoService;

	
	@PostConstruct
	private void init() {
	}
	
	private Serie maternal;
	private Serie jardimI;
	private Serie jardimII;
	private Serie pre;
	private Serie primeiro;
	private Serie segundo;
	private Serie terceiro;
	private Serie quarto;
	private Serie quinto;
	
	private PerioddoEnum manha;
	private PerioddoEnum tarde;
	private PerioddoEnum integral;
	
	public int quantidadeIrmaos(Aluno aluno){
		int quatidade = 1;
		if(aluno.getIrmao1()!= null){
			quatidade ++;
		}
		if(aluno.getIrmao2()!= null){
			quatidade ++;
		}
		if(aluno.getIrmao3()!= null){
			quatidade ++;
		}
		
		return quatidade;
	}
	
	public int quantidadeCarrosIda(Aluno aluno){
		int carros = 1;
		if(aluno.getIdaVolta()==2){ //Soh volta
			return 0;
		}else{
			if(aluno.isTrocaIDA()){
				carros++;
			}
			if((aluno.isTrocaIDA2() != null && aluno.isTrocaIDA2())){
				carros++;
			}
			if(aluno.isTrocaIDA3() != null && aluno.isTrocaIDA3()){
				carros++;
			}
			
		}
		
		return carros;
	}
	
	public int quantidadeCarrosVolta(Aluno aluno){
		int carros = 1;
		if(aluno.getIdaVolta()==1){ //Soh Vai
			return 0;
		}else{
			if(aluno.isTrocaVolta()){
				carros++;
			}
			if(aluno.isTrocaVolta2() != null && aluno.isTrocaVolta2()){
				carros++;
			}
			if(aluno.isTrocaVolta3() != null && aluno.isTrocaVolta3()){
				carros++;
			}
			
		}
		
		return carros;
	}

	public boolean leva(Aluno aluno, Carro carro){
		if (aluno.getCarroLevaParaEscola() != null && aluno.getCarroLevaParaEscola().equals(carro)){
			return true;
		}else if(aluno.getCarroLevaParaEscolaTroca() != null &&  aluno.getCarroLevaParaEscolaTroca().equals(carro)){
			return true;
		}else if(aluno.getCarroLevaParaEscolaTroca2() != null &&  aluno.getCarroLevaParaEscolaTroca2().equals(carro)){
			return true;
		}else if(aluno.getCarroLevaParaEscolaTroca3() != null &&  aluno.getCarroLevaParaEscolaTroca3().equals(carro)){
			return true;
		}
		return false;
	}
	
	public boolean pega(Aluno aluno, Carro carro){
		if (aluno.getCarroPegaEscola() != null && aluno.getCarroPegaEscola().equals(carro)){
			return true;
		}else if(aluno.getCarroPegaEscolaTroca() != null && aluno.getCarroPegaEscolaTroca().equals(carro)){
			return true;
		}else if(aluno.getCarroPegaEscolaTroca2() != null && aluno.getCarroPegaEscolaTroca2().equals(carro)){
			return true;
		}else if(aluno.getCarroPegaEscolaTroca3() != null && aluno.getCarroPegaEscolaTroca3().equals(carro)){
			return true;
		}
		return false;
	}
	
	public double getTotalAlunos(){
		Map<String, Object> filtros = new HashMap<>();
		return relatorioService.countAlunos(filtros);
	}

	public double getTotalAlunosManha(){
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("periodo", PerioddoEnum.MANHA);
		return relatorioService.countAlunos(filtros);
	}
	
	public double getTotalAlunosTarde(){
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("periodo", PerioddoEnum.TARDE);
		return relatorioService.countAlunos(filtros);
	}
	
	public long getTotalAlunosIntegral(){
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("periodo", PerioddoEnum.INTEGRAL);
		return relatorioService.count(filtros);
	}

	public double getTotalAlunos(Serie serie, PerioddoEnum periodo){
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("serie", getSerie(serie));
		filtros.put("periodo", getPeriodo(periodo));
		return relatorioService.countAlunos(filtros);
	}
	
	public int getTotalAlunos(Carro carro){
		int quantidade = 0;
		List<Aluno> todosAlunos = alunoService.findAll();
		for(Aluno aluno : todosAlunos){
			boolean pega = pega(aluno, carro);
			boolean leva = leva(aluno, carro);
			
			if(pega || leva){
				quantidade++;
			}
		}
		
		return quantidade;
	}
	
	public double getValorTotal2(Carro carro){
		double totalCriancasSoLeva = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.SO_VAI);
		double totalCriancasSoLevaComTrocaIda = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.SO_VAI_TROCA_IDA)/2;

		double totalCriancasSoPega = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.SO_VOLTA);
		double totalCriancasSoPegaComTroca = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.SO_VOLTA_TROCA_VOLTA)/2;
		
		double totalvaiVoltaComOutroCarro = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.VAI_VOLTA_IDA_CARRO_DIFERENTE);
		double totalvaiVoltaComOutroCarroComtrocaIda = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.VAI_VOLTA_IDA_COM_TROCA_IDA);
		double totalvaiVoltaComOutroCarroComtrocaIdaeVolta = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.VAI_VOLTA_IDA_COM_TROCA_IDA_E_COM_TROCA_VOLTA);
		double totalvaiVoltaComOutroCarroComtrocaVolta = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.VAI_VOLTA_IDA_COM_TROCA_VOLTA);
		
		double totalvaivoltaVaicomOutroCarro = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.VAI_VOLTA_VOLTA_CARRO_DIFERENTE);
		double totalvaivoltaVaicomOutrocarroTrocaIda = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.VAI_VOLTA_VOLTA_COM_TROCA_IDA);
		double totalvaivoltaVaicomOutrocarroTrocaIdaENaVolta = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.VAI_VOLTA_VOLTA_COM_TROCA_IDA_E_COM_TROCA_VOLTA);
		double totalvaivoltaVaicomOutrocarroTrocaNaVolta = relatorioService.getValorTotalMensalidade(carro,TipoMobilidadeEnum.VAI_VOLTA_VOLTA_COM_TROCA_VOLTA);
		
		//TROCA na IDA e TROCA na VOLTA carros iguais
		return totalCriancasSoLeva + totalCriancasSoLevaComTrocaIda+totalCriancasSoPega
				+totalCriancasSoPegaComTroca+totalvaiVoltaComOutroCarro
				+totalvaiVoltaComOutroCarroComtrocaIda
				+totalvaiVoltaComOutroCarroComtrocaIdaeVolta
				+totalvaiVoltaComOutroCarroComtrocaVolta
				+totalvaivoltaVaicomOutroCarro
				+totalvaivoltaVaicomOutrocarroTrocaIda
				+totalvaivoltaVaicomOutrocarroTrocaIdaENaVolta
				+totalvaivoltaVaicomOutrocarroTrocaNaVolta;
	}
	
	public double getValorTotal(Carro carro){
		Double valorTotal = 0D;
		List<Aluno> todosAlunos = alunoService.findAll();
		for(Aluno aluno : todosAlunos){
			if(aluno.getRemovido() != null && aluno.getRemovido() != null){
				boolean pega = pega(aluno, carro);
				boolean leva = leva(aluno, carro);
				
				int qdadeIda = quantidadeCarrosIda(aluno); 
				int qdadeVolta = quantidadeCarrosVolta(aluno);

				double valorCrianca = 0;
				if(pega || leva){
					if(qdadeVolta==0){ //soh vai
						if(leva){
							valorCrianca = aluno.getContratoVigente().getValorMensal()/qdadeIda;
						}	
					}else if(qdadeIda==0){ //soh volta
							if(pega){
								valorCrianca =(aluno.getContratoVigente().getValorMensal()-20)/qdadeVolta;
							}	
					}else{ // vai e volta
						if(pega){
							valorCrianca = ((aluno.getContratoVigente().getValorMensal()-20)/qdadeVolta)/2;
						}	
						if(leva){
							valorCrianca += ((aluno.getContratoVigente().getValorMensal()-20)/qdadeIda)/2;
						}	
					}
					int quantidadeIrmaos = quantidadeIrmaos(aluno);
					if(quantidadeIrmaos>1){
						valorTotal += (valorCrianca/quantidadeIrmaos);
						
					}else{
						valorTotal += valorCrianca;
					}
				}
	
			}
						
		}
		return valorTotal;
	}
	
	
	
	public double getValorTotalMensalidade(Carro carro){
		Map<String, Object> filtros = new HashMap<>();
		filtros.put("carroLevaParaEscola", carro);
		filtros.put("carroLevaParaEscolaTroca", carro);
		filtros.put("carroPegaEscola", carro);
		filtros.put("carroPegaEscolaTroca", carro);
		return relatorioService.getValor(filtros);
	}
	
	public double getValorTotalMensalidade(){
		Map<String, Object> filtros = new HashMap<>();
		return relatorioService.getValor(filtros);
	}
	
	//TODO nao pergunte =) converter para o server funcionar
	private Serie getSerie(Serie serie){
		return serie;
	}
	//TODO nao pergunte =) converter para o server funcionar
	private PerioddoEnum getPeriodo(PerioddoEnum periodo){
		return periodo;
	}
	public Serie getMaternal() {
		return Serie.MATERNAL;
	}

	public void setMaternal(Serie maternal) {
		this.maternal = maternal;
	}

	public Serie getJardimI() {
		return Serie.JARDIM_I;
	}

	public void setJardimI(Serie jardimI) {
		this.jardimI = jardimI;
	}

	public Serie getJardimII() {
		return Serie.JARDIM_II;
	}

	public void setJardimII(Serie jardimII) {
		this.jardimII = jardimII;
	}

	public Serie getPre() {
		return Serie.PRE;
	}

	public void setPre(Serie pre) {
		this.pre = pre;
	}

	public Serie getPrimeiro() {
		return Serie.PRIMEIRO_ANO;
	}

	public void setPrimeiro(Serie primeiro) {
		this.primeiro = primeiro;
	}

	public Serie getSegundo() {
		return Serie.SEGUNDO_ANO;
	}

	public void setSegundo(Serie segundo) {
		this.segundo = segundo;
	}

	public Serie getTerceiro() {
		return Serie.TERCEIRO_ANO;
	}

	public void setTerceiro(Serie terceiro) {
		this.terceiro = terceiro;
	}

	public Serie getQuarto() {
		return Serie.QUARTO_ANO;
	}

	public void setQuarto(Serie quarto) {
		this.quarto = quarto;
	}

	public Serie getQuinto() {
		return Serie.QUINTO_ANO;
	}

	public void setQuinto(Serie quinto) {
		this.quinto = quinto;
	}

	public PerioddoEnum getManha() {
		return PerioddoEnum.MANHA;
	}

	public void setManha(PerioddoEnum manha) {
		this.manha = manha;
	}

	public PerioddoEnum getTarde() {
		return PerioddoEnum.TARDE;
	}

	public void setTarde(PerioddoEnum tarde) {
		this.tarde = tarde;
	}

	public PerioddoEnum getIntegral() {
		return PerioddoEnum.INTEGRAL;
	}

	public void setIntegral(PerioddoEnum integral) {
		this.integral = integral;
	}
	
	
}
