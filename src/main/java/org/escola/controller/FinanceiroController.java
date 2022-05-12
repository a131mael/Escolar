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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.enums.PerioddoEnum;
import org.escolar.enums.Serie;
import org.escolar.enums.StatusBoletoEnum;
import org.escolar.model.Aluno;
import org.escolar.model.Boleto;
import org.escolar.model.Configuracao;
import org.escolar.model.Funcionario;
import org.escolar.model.Member;
import org.escolar.model.extrato.ItemExtrato;
import org.escolar.service.AlunoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.ExtratoBancarioService;
import org.escolar.service.FinanceiroService;
import org.escolar.util.Formatador;
import org.escolar.util.Util;
import org.escolar.util.Verificador;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@Named
@ViewScoped
public class FinanceiroController implements Serializable{

	/****/
	private static final long serialVersionUID = 1L;

	@Inject
	private ConfiguracaoService configuracaoService;
	
	@Inject
	private FinanceiroService financeiroService;
	
	@Inject
	private AlunoService alunoService;
	
	@Inject
	private ExtratoBancarioService extratoBancarioService;

	private LazyDataModel<Boleto> lazyListDataModel;
	
	private LazyDataModel<ItemExtrato> lazyListDataModelExtrato;
		
	private List<Aluno> alunosEcontrados;
	
	private String nomeAluno;
	
	private String nomeResponsavel;
	
	private String cpfResponsavel;

	private String nDocumento;

	@Named
	private Aluno alunoBaixaSelecionado;
	
	@Named
	private ItemExtrato itemExtrato;
	
	@Produces
	@Named
	private ItemExtrato extratoBancario;
	
	private Integer anoSelecionado;
	
	private Integer mesSelecionado;
	
	private Configuracao configuracao;

	@PostConstruct
	private void init() {
		setConfiguracao(configuracaoService.getConfiguracao());
		
		Object obj2 = Util.getAtributoSessao("anoSelecionado");
		if (obj2 != null) {
			anoSelecionado = (Integer) obj2;
		}else{
			anoSelecionado = getConfiguracao().getAnoLetivo();
		}
		
		Object obj3 = Util.getAtributoSessao("mesSelecionado");
		if (obj3 != null) {
			setMesSelecionado((Integer) obj3);
		}else{
			setMesSelecionado(getMesAtual());
		}
		
		if(extratoBancario == null){
			Object objectSessao = Util.getAtributoSessao("extratoBancario");
			if(objectSessao != null){
				extratoBancario = (ItemExtrato) objectSessao;
				Util.removeAtributoSessao("extratoBancario");
			}else{
				extratoBancario = new ItemExtrato();
			}
		}
	}

	private Integer getMesAtual() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.MONTH);
	}
	
	public void dividirExtrato(){
		ItemExtrato filho = new ItemExtrato();
		List<ItemExtrato> filhos = extratoBancario.getItensFilhos();
		if(filhos != null && filhos.size()>0){
		
			String codigo = filhos.get(filhos.size()-1).getCodigoEntrada();
			String finalCodigo = codigo.substring(codigo.length()-2);
			int proximo = Integer.parseInt(finalCodigo);
			proximo++;
			filho.setCodigoEntrada(extratoBancario.getCodigoEntrada()+"0"+proximo);
		}else{
			filho.setCodigoEntrada(extratoBancario.getCodigoEntrada()+"01");
		}
		filho.setAno(extratoBancario.getAno());
		filho.setMes(extratoBancario.getMes());
		filho.setDataEvento(extratoBancario.getDataEvento());
		filho.setPai(false);
		filho.setTipoEntrada(extratoBancario.getTipoEntrada());
		filho.setTipoEntradaSaida(extratoBancario.getTipoEntradaSaida());
		filho.setValor(0d);
		extratoBancario.setPai(true);
		extratoBancario.addFilho(filho);
	}
	
	public String pintarPai(ItemExtrato item){
		if(item.isPai()){
			return "backgroundVerde";
		}
		return "";
	}
	
	public String editarExtrato(Long id){
		extratoBancario = financeiroService.findItemExtratoByID(id);
		Util.addAtributoSessao("extratoBancario", extratoBancario);
		return "editarExtrato";
	}
	
	public void removerExtrato(){
		extratoBancarioService.removerExtrato(extratoBancario);
	}
	
	public String editarExtrato() {
		return editarExtrato(extratoBancario.getId()); 
	}
	
	public String adicionarExtrato(){
		return "editarExtrato";
	}
	
	public String saveExtrato(ItemExtrato item){
		extratoBancarioService.save(item);
		return "indexExtrato";
	}
	
	public String saveExtrato() {
		extratoBancarioService.save(extratoBancario);
		return "indexExtrato";
	}

	public LazyDataModel<ItemExtrato> getLazyDataModelExtrato() {
		if (lazyListDataModelExtrato == null) {

			lazyListDataModelExtrato = new LazyDataModel<ItemExtrato>() {

				@Override
				public ItemExtrato getRowData(String rowKey) {
					return financeiroService.findItemExtratoByID(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(ItemExtrato al) {
					return al.getId();
				}

				@Override
				public List<ItemExtrato> load(int first, int pageSize, String order, SortOrder so,	Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					filtros.put("removido", false);
					filtros.put("mes", mesSelecionado+1);
					filtros.put("ano", anoSelecionado);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<ItemExtrato> ol = financeiroService.findItemExtrato(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelExtrato.setRowCount((int) financeiroService.countExtratosMes(filtros));
						return ol;
					}
					return null;
				}
			};

			Map<String, Object> filtros = new HashMap<String, Object>();

			filtros.put("removido", false);
			filtros.put("mes", mesSelecionado+1);
			filtros.put("ano", anoSelecionado);

			lazyListDataModelExtrato.setRowCount((int) financeiroService.countExtratosMes(filtros));

		}

		return lazyListDataModelExtrato;

	}
	
	public void buscarAluno(String nome, String nomeResponsavel,String cpf, String numeroDocumento){
		alunosEcontrados = alunoService.findAluno(nome,nomeResponsavel,cpf,numeroDocumento);	
	}
	
	public void buscarAluno(){
		buscarAluno(nomeAluno, nomeResponsavel, cpfResponsavel,nDocumento);
	}
	
	public List<Boleto> getBoletos(int mes){
		return financeiroService.getBoletoMes(mes);
	}

	public void baixarBoleto(Boleto boleto){
		Double valorFinal = Verificador.getValorFinal(boleto);
		boleto.setValorPago(valorFinal);
		boleto.setDataPagamento(new Date());
		boleto.setBaixaManual(true);
		financeiroService.save(boleto);
	}
	
	public String marcarLinha(Aluno a) {
		String cor = "";
		if(a == null){
			return "";
		}
		
		if(a.getRemovido() != null && a.getRemovido()){
			cor = "marcarLinhaVermelho";
		}else{
			cor = "marcarLinha";
		}
		
		/*cor = "marcarLinhaVermelho";
		cor = "marcarLinhaVerde";
		cor = "marcarLinhaAmarelo";
		cor = "marcarLinha"
		*/
		return cor;
	}
	
	public String marcarLinhaExtrato(ItemExtrato a) {
		String cor = "";
		if(a == null){
			return "";
		}
		
		if(a.getAtualizado() != null && a.getAtualizado()){
			cor = "marcarLinhaVerde";
		}else{
			cor = "marcarLinha";
		}
		
		/*cor = "marcarLinhaVermelho";
		cor = "marcarLinhaVerde";
		cor = "marcarLinhaAmarelo";
		cor = "marcarLinha"
		*/
		return cor;
	}
	
	
	public boolean isBoletoPago(Boleto boleto){
		return Verificador.getStatusEnum(boleto).equals(StatusBoletoEnum.PAGO) || isRemovido(boleto);
	}
	
	public boolean isRemovido(Boleto boleto){
		if(boleto.getCancelado() != null && boleto.getCancelado()){
			return true;
		}
		return false;
	}
	
	public String formatarData(Date data){
		return Formatador.formataData(data);	
	}
	
	public String verificaValorFinalBoleto(Boleto boleto){
		return Util.formatarDouble2Decimais(Verificador.getValorFinal(boleto)) ;	
	}
	
	public String verificaJurosMultaFinalBoleto(Boleto boleto){
		return Util.formatarDouble2Decimais(Verificador.getJurosMulta(boleto)) ;	
	}
	
	public LazyDataModel<Boleto> getLazyDataModel(int mes) {
		if (lazyListDataModel == null) {

			lazyListDataModel = new LazyDataModel<Boleto>() {

				@Override
				public Boleto getRowData(String rowKey) {
					return financeiroService.findBoletoByID(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Boleto al) {
					return al.getId();
				}

				@Override
				public List<Boleto> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					filtros.put("removido", false);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("serie")) {
						if (filtros.get("serie").equals(Serie.JARDIM_I.toString())) {
							filtros.put("serie", Serie.JARDIM_I);
						} else if (filtros.get("serie").equals(Serie.JARDIM_II.toString())) {
							filtros.put("serie", Serie.JARDIM_II);
						} else if (filtros.get("serie").equals(Serie.MATERNAL.toString())) {
							filtros.put("serie", Serie.MATERNAL);
						} else if (filtros.get("serie").equals(Serie.PRE.toString())) {
							filtros.put("serie", Serie.PRE);
						} else if (filtros.get("serie").equals(Serie.PRIMEIRO_ANO.toString())) {
							filtros.put("serie", Serie.PRIMEIRO_ANO);
						} else if (filtros.get("serie").equals(Serie.SEGUNDO_ANO.toString())) {
							filtros.put("serie", Serie.SEGUNDO_ANO);
						} else if (filtros.get("serie").equals(Serie.TERCEIRO_ANO.toString())) {
							filtros.put("serie", Serie.TERCEIRO_ANO);
						} else if (filtros.get("serie").equals(Serie.QUARTO_ANO.toString())) {
							filtros.put("serie", Serie.QUARTO_ANO);
						} else if (filtros.get("serie").equals(Serie.QUINTO_ANO.toString())) {
							filtros.put("serie", Serie.QUINTO_ANO);
						}

					}
					
					filtros.put("removido", false);

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Boleto> ol = financeiroService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModel.setRowCount(ol.size());
						return ol;
					}

					this.setRowCount((int) financeiroService.count(filtros));
					return null;

				}
			};
			lazyListDataModel.setRowCount((int) financeiroService.count(null));

		}

		return lazyListDataModel;

	}
	
	public String verAluno(long id) {
			Aluno aluno = alunoService.findById(id);
			Util.addAtributoSessao("aluno", aluno);
		return "veraluno";
	}
	
	public double getPrevisto(int mes){
		return financeiroService.getPrevisto(mes);
	}
	
	public double getPago(int mes){
		return financeiroService.getPago(mes);
	}

	public double getDiferenca(int mes){
		return getPrevisto(mes) - getPago(mes);
	}


	public List<Aluno> getAlunosEcontrados() {
		return alunosEcontrados;
	}

	public void setAlunosEcontrados(List<Aluno> alunosEcontrados) {
		this.alunosEcontrados = alunosEcontrados;
	}

	public Aluno getAlunoBaixaSelecionado() {
		return alunoBaixaSelecionado;
	}

	public void setAlunoBaixaSelecionado(Aluno alunoBaixaSelecionado) {
		this.alunoBaixaSelecionado = alunoBaixaSelecionado;
	}

	public String getNomeAluno() {
		return nomeAluno;
	}

	public void setNomeAluno(String nomeAluno) {
		this.nomeAluno = nomeAluno;
	}

	public String getNomeResponsavel() {
		return nomeResponsavel;
	}

	public void setNomeResponsavel(String nomeResponsavel) {
		this.nomeResponsavel = nomeResponsavel;
	}

	public String getCpfResponsavel() {
		return cpfResponsavel;
	}

	public void setCpfResponsavel(String cpfResponsavel) {
		this.cpfResponsavel = cpfResponsavel;
	}

	public String getnDocumento() {
		return nDocumento;
	}

	public void setnDocumento(String nDocumento) {
		this.nDocumento = nDocumento;
	}

	public ItemExtrato getItemExtrato() {
		return itemExtrato;
	}

	public void setItemExtrato(ItemExtrato itemExtrato) {
		this.itemExtrato = itemExtrato;
	}

	public Integer getAnoSelecionado() {
		return anoSelecionado;
	}

	public void setAnoSelecionado(Integer anoSelecionado) {
		Util.addAtributoSessao("anoSelecionado", anoSelecionado);
		this.anoSelecionado = anoSelecionado;
	}

	public Integer getMesSelecionado() {
		return mesSelecionado;
	}

	public void setMesSelecionado(Integer mesSelecionado) {
		Util.addAtributoSessao("mesSelecionado", mesSelecionado);
		this.mesSelecionado = mesSelecionado;
	}

	public Configuracao getConfiguracao() {
		return configuracao;
	}

	public void setConfiguracao(Configuracao configuracao) {
		this.configuracao = configuracao;
	}

	public ItemExtrato getExtratoBancario() {
		return extratoBancario;
	}

	public void setExtratoBancario(ItemExtrato extratoBancario) {
		this.extratoBancario = extratoBancario;
	}
	
}
