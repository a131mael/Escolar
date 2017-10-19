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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.aaf.financeiro.util.CNAB240_SICOOB;
import org.escola.controller.OfficeDOCUtil;
import org.escola.controller.OfficePDFUtil;
import org.escola.enums.BimestreEnum;
import org.escola.enums.DisciplinaEnum;
import org.escola.enums.PerioddoEnum;
import org.escola.enums.Serie;
import org.escola.model.Aluno;
import org.escola.model.AlunoAvaliacao;
import org.escola.model.Custo;
import org.escola.service.AlunoService;
import org.escola.service.AvaliacaoService;
import org.escola.util.Constant;
import org.escola.util.CurrencyWriter;
import org.escola.util.FileDownload;
import org.escola.util.ImpressoesUtils;
import org.escola.util.Util;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

@Named
@ViewScoped
public class AlunoController implements Serializable {

	/****/
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private Aluno aluno;

	private boolean irmao1;

	private boolean irmao2;

	private boolean irmao3;

	private boolean irmao4;

	@Produces
	@Named
	private List<Aluno> alunos;

	@Inject
	private AlunoService alunoService;

	@Inject
	private AvaliacaoService avaliacaoService;

	private OfficeDOCUtil officeDOCUtil;
	CurrencyWriter cw;

	@Named
	private BimestreEnum bimestreSel;
	@Named
	private DisciplinaEnum disciplinaSel;

	private LazyDataModel<Aluno> lazyListDataModel;

	private LazyDataModel<Aluno> lazyListDataModelExAlunos;

	private LazyDataModel<Aluno> lazyListDataModelUltimoAnoLetivo;

	public LazyDataModel<Aluno> getLazyDataModel() {
		if (lazyListDataModel == null) {

			lazyListDataModel = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return alunoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.put("anoLetivo", Constant.anoLetivoAtual);

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

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = alunoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModel.setRowCount((int) alunoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) alunoService.count(filtros));
					return null;

				}
			};
			lazyListDataModel.setRowCount((int) alunoService.count(null));

		}

		return lazyListDataModel;

	}

	public double valorTotal(Aluno aluno) {
		if (aluno != null && aluno.getNumeroParcelas() != null) {
			return aluno.getValorMensal() * aluno.getNumeroParcelas();
		} else {
			return 0;
		}
	}

	public LazyDataModel<Aluno> getLazyDataModelExAlunos() {
		if (lazyListDataModelExAlunos == null) {

			lazyListDataModelExAlunos = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return alunoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					filtros.put("removido", true);
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

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = alunoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelExAlunos.setRowCount((int) alunoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) alunoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelExAlunos.setRowCount((int) alunoService.count(null));

		}

		return lazyListDataModelExAlunos;

	}

	public LazyDataModel<Aluno> getLazyDataModelUltimoAnoLetivo() {
		if (lazyListDataModelUltimoAnoLetivo == null) {

			lazyListDataModelUltimoAnoLetivo = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return alunoService.findById(Long.valueOf(rowKey));
				}

				@Override
				public Object getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);

					filtros.put("anoLetivo", Constant.anoLetivoAtual - 1);

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

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = alunoService.find(first, pageSize, orderByParam, orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelUltimoAnoLetivo.setRowCount((int) alunoService.count(filtros));
						return ol;
					}

					this.setRowCount((int) alunoService.count(filtros));
					return null;

				}
			};
			lazyListDataModelUltimoAnoLetivo.setRowCount((int) alunoService.count(null));

		}

		return lazyListDataModelUltimoAnoLetivo;

	}

	@PostConstruct
	private void init() {
		if (aluno == null) {
			Object obj = Util.getAtributoSessao("aluno");
			if (obj != null) {
				aluno = (Aluno) obj;
				if (aluno.getIrmao1() != null) {
					irmao1 = true;
				}
				if (aluno.getIrmao2() != null) {
					irmao2 = true;
				}
				if (aluno.getIrmao3() != null) {
					irmao3 = true;
				}
				if (aluno.getIrmao4() != null) {
					irmao4 = true;
				}
			} else {
				aluno = new Aluno();
			}
		}

		officeDOCUtil = new OfficeDOCUtil();
		cw = new CurrencyWriter();
	}

	public boolean estaEmUmaTurma(long idAluno) {
		boolean estaNaTurma = alunoService.estaEmUmaTUrma(idAluno);
		return estaNaTurma;
	}

	private Float maior(Float float1, Float float2) {
		return float1 > float2 ? float1 : float2;
	}

	private String mostraNotas(Float nota) {
		if (nota == null || nota == 0) {
			return "";
		} else {
			return String.valueOf(Math.round(nota * 100) / 100);
		}
	}

	private Float media(Float... notas) {
		int qtade = 0;
		float sum = 0;
		for (Float f : notas) {
			sum += f;
			qtade++;
		}
		return qtade > 0 ? sum / qtade : 0;
	}

	public float getNota(DisciplinaEnum disciplina, BimestreEnum bimestre) {
		return alunoService.getNota(aluno.getId(), disciplina, bimestre, false);
	}

	public List<Custo> getHistoricoAluno(Aluno aluno) {
		return alunoService.getHistoricoAluno(aluno);
	}

	public boolean renderDisciplina(int ordinal) {
		if (getDisciplinaSel() == null) {
			return true;
		}

		return ordinal == getDisciplinaSel().ordinal();
	}

	public void saveAvaliacaoAluno(AlunoAvaliacao alav) {
		avaliacaoService.saveAlunoAvaliacao(alav);
	}

	public void saveAvaliacaoAluno(Long idAluAv, Float nota) {
		avaliacaoService.saveAlunoAvaliacao(idAluAv, nota);
	}

	public HashMap<String, String> montarAtestadoNegativoDebito(Aluno aluno) {
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
		String dataExtenso = formatador.format(new Date());

		HashMap<String, String> trocas = new HashMap<>();
		trocas.put("adonainomealuno", aluno.getNomeAluno());
		trocas.put("adonaiturma", aluno.getSerie().getName());
		trocas.put("adonaiperiodo", aluno.getPeriodo().getName());
		trocas.put("adonaidata", dataExtenso);
		trocas.put("adonaicpfresponsavel", aluno.getCpfResponsavel());
		trocas.put("adonainomeresponsavel", aluno.getNomeResponsavel());

		return trocas;
	}

	public HashMap<String, String> montarAtestadoVaga(Aluno aluno) {
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
		String dataExtenso = formatador.format(new Date());

		HashMap<String, String> trocas = new HashMap<>();
		trocas.put("adonainomealuno", aluno.getNomeAluno());
		trocas.put("adonaiturma", aluno.getSerie().getName());
		trocas.put("adonaiperiodo", aluno.getPeriodo().getName());
		trocas.put("adonaidata", dataExtenso);

		return trocas;
	}

	public HashMap<String, String> montarContrato(Aluno aluno) {
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
		String dataExtenso = formatador.format(new Date());
		Calendar dataLim = Calendar.getInstance();
		dataLim.add(Calendar.MONTH, 1);
		String dataLimiteExtenso = formatador.format(dataLim.getTime());

		DateFormat formatadorAno = DateFormat.getDateInstance(DateFormat.YEAR_FIELD, new Locale("pt", "BR"));
		String ano = formatadorAno.format(new Date());

		// int anoInt = Integer.parseInt(ano);

		HashMap<String, String> trocas = new HashMap<>();
		trocas.put("#ANOCONTRATO", "2018"); // TODO colocar o ano atual +1
		trocas.put("#CONTRATANTECID", "Palhoça"); // TODO COLOCAR CIDADE DO
													// Contratado
		trocas.put("#DATAEXTENSO", dataExtenso);

		trocas.put("#CONTRATANTENOME", aluno.getNomeResponsavel());
		trocas.put("#CONTRATANTERG", aluno.getRgResponsavel());
		trocas.put("#CONTRATANTECPF", aluno.getCpfResponsavel());
		trocas.put("#CONTRATANTERUA", aluno.getEndereco() + ", " + aluno.getBairro());

		String nomeAluno = aluno.getNomeAluno();
		if (aluno.getIrmao1() != null) {
			nomeAluno += ", " + aluno.getIrmao1().getNomeAluno();
		}
		if (aluno.getIrmao2() != null) {
			nomeAluno += ", " + aluno.getIrmao2().getNomeAluno();
		}
		if (aluno.getIrmao3() != null) {
			nomeAluno += ", " + aluno.getIrmao3().getNomeAluno();
		}
		if (aluno.getIrmao4() != null) {
			nomeAluno += ", " + aluno.getIrmao4().getNomeAluno();
		}

		trocas.put("#TRANSPORTADONOME", nomeAluno);
		trocas.put("#TRANSPORTADORUA", aluno.getEndereco() + ", " + aluno.getBairro());
		trocas.put("#TRANSPORTADOESCOLA", aluno.getEscola().getName());

		String periodo1 = "";
		if (aluno.getPeriodo().equals(PerioddoEnum.INTEGRAL) || aluno.getPeriodo().equals(PerioddoEnum.MANHA)) {
			periodo1 = "06:30";
		} else {
			periodo1 = "11:30";
		}
		String periodo2 = "";
		if (aluno.getPeriodo().equals(PerioddoEnum.INTEGRAL) || aluno.getPeriodo().equals(PerioddoEnum.TARDE)) {
			periodo2 = "19:30";
		} else {
			periodo2 = "13:30";
		}

		trocas.put("#DADOSGERAISHORARIO1", periodo1);
		trocas.put("#DADOSGERAISHORARIO2", periodo2);
		trocas.put("#DADOSGERAISMES1", getMesInicioPagamento(aluno));
		trocas.put("#DADOSGERAISMES2", "Dezembro");
		trocas.put("#DADOSGERAISPARCELAS", aluno.getNumeroParcelas() + "");
		// BigDecimal valorTotal = (new
		// BigDecimal(contrato.getValorTotal())).multiply(((new
		// BigDecimal(contrato.getParcelas()))));
		// trocas.put("#DADOSGERAISTOTAL", valorTotal.toString());
		trocas.put("#DADOSGERAISTOTAL", String.valueOf(valorTotal(aluno))); // TODO
																			// ver
		// contrato.setValorTotal(contrato.getValorTotal().replace(",", "."));
		trocas.put("#DADOSGERAISTOTALEXTENSO", cw.write(new BigDecimal(valorTotal(aluno))));
		trocas.put("#DADOSGERAISQTADEPARCELAS", aluno.getNumeroParcelas() + "");
		trocas.put("#DADOSGERAISEXTENSOPARCELA", cw.write(new BigDecimal(aluno.getValorMensal())));
		trocas.put("#DADOSGERAISPARCELA", aluno.getValorMensal() + "");/// valor
																		/// da
																		/// parcela

		String idaEVolta = "CLAUSULA 6ª – O CONTRATANTE compromete-se a deixar o TRANSPORTADO pronto e aguardando pelo CONTRATADO no endereço e hora combinada, ou seja, na rua  #CONTRATANTERUA   as #DADOSGERAISHORARIO1,  não tolerando qualquer tipo de atraso ou mudança de endereço.";
		String ida = "CLAUSULA 6ª - O CONTRATADO SO SE RESPONSABILIZARA PELO TRANSPORTE DE IDA PARA A ESCOLA, O TRANSPORTE DE VOLTA DA ESCOLA È DE RESPONSABILIDADE DO CONTRATANTE.";
		String volta = "CLAUSULA 6ªB – O CONTRATADO SO SE RESPONSABILIZARA PELO TRANSPORTE DE VOLTA DA ESCOLA, O TRANSPORTE DE IDA PARA A ESCOLA È DE RESPONSABILIDADE DO CONTRATANTE.";

		switch (aluno.getIdaVolta()) {
		case 1:
			trocas.put("#TIPOCONTRATO", idaEVolta);
			break;

		case 2:
			trocas.put("#TIPOCONTRATO", ida);
			break;

		case 3:
			trocas.put("#TIPOCONTRATO", volta);
			break;

		default:
			break;
		}

		return trocas;
	}

	private String getMesInicioPagamento(Aluno aluno2) {
		String mes = "Janeiro";
		switch (aluno.getNumeroParcelas()) {
		case 12:
			break;

		case 11:
			mes = "Fevereiro";
			break;

		case 10:
			mes = "Março";
			break;

		case 9:
			mes = "Abril";
			break;

		case 8:
			mes = "Maio";
			break;

		case 7:
			mes = "Junho";
			break;

		case 6:
			mes = "Julho";
			break;

		case 5:
			mes = "Agosto";
			break;

		case 4:
			mes = "Setembro";
			break;

		case 3:
			mes = "Outubro";
			break;

		case 2:
			mes = "Novembro";
			break;

		case 1:
			mes = "Dezembro";
			break;

		default:
			break;
		}
		return mes;
	}

	public void onRowSelect(SelectEvent event) {
		Aluno al = (Aluno) event.getObject();
		FacesMessage msg = new FacesMessage("Car Selected");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void onRowUnselect(UnselectEvent event) {
		Aluno al = (Aluno) event.getObject();
		FacesMessage msg = new FacesMessage("Car Unselected");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public boolean isAlunoSelecionado() {
		return aluno.getId() != null ? true : false;
	}

	public StreamedContent imprimirNegativoDebito(Aluno aluno) throws IOException {
		String nomeArquivo = "";
		if (aluno != null && aluno.getId() != null) {
			nomeArquivo = aluno.getId() + "f";
			ImpressoesUtils.imprimirInformacoesAluno(aluno, "modeloNegativoDebito2017.docx",
					montarAtestadoNegativoDebito(aluno), nomeArquivo);

			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "modeloNegativoDebito2017.docx";
		}

		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nomeArquivo;
		InputStream stream = new FileInputStream(caminho);
		return FileDownload.getContentDoc(stream, nomeArquivo);
	}

	public StreamedContent imprimirNegativoDebito() throws IOException {
		return imprimirNegativoDebito(aluno);
	}

	public StreamedContent imprimirContrato(Aluno aluno) throws IOException {
		String nomeArquivo = "";
		if (aluno != null && aluno.getId() != null) {
			nomeArquivo = aluno.getId() + "g";
			ImpressoesUtils.imprimirInformacoesAluno(aluno, "MODELO1-2.doc", montarContrato(aluno), nomeArquivo);
			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "MODELO1-1.doc";
		}

		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nomeArquivo;
		InputStream stream = new FileInputStream(caminho);
		return FileDownload.getContentDoc(stream, nomeArquivo);
	}

	public StreamedContent imprimirContrato() throws IOException {
		return imprimirContrato(aluno);
	}

	public StreamedContent imprimirAtestadoVaga(Aluno aluno) throws IOException {
		String nomeArquivo = "";
		if (aluno != null && aluno.getId() != null) {
			nomeArquivo = aluno.getId() + "h";
			ImpressoesUtils.imprimirInformacoesAluno(aluno, "modeloAtestadoVaga2017.docx", montarAtestadoVaga(aluno),
					nomeArquivo);
			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "modeloAtestadoVaga2017.docx";
		}

		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nomeArquivo;
		InputStream stream = new FileInputStream(caminho);
		return FileDownload.getContentDoc(stream, nomeArquivo);
	}

	public StreamedContent imprimirAtestadoVaga() throws IOException {
		return imprimirAtestadoVaga(aluno);
	}

	public List<Aluno> getAlunos() {

		return alunoService.findAll();
	}

	public String salvar() {
		alunoService.save(aluno);
		Util.removeAtributoSessao("aluno");
		return "index";
	}

	public String voltar() {
		return "index";
	}

	public String editar(Long idprof) {
		aluno = alunoService.findById(idprof);
		Util.addAtributoSessao("aluno", aluno);
		return "cadastrar";
	}

	public String visualizar(Aluno aluno) {
		aluno = alunoService.findById(aluno.getId());
		Util.addAtributoSessao("aluno", aluno);

		return "cadastrar";
	}

	public String editar() {
		return editar(aluno.getId());
	}

	public String remover(Long idTurma) {
		alunoService.remover(idTurma);
		return "index";
	}

	public String restaurar(Long idTurma) {
		alunoService.restaurar(idTurma);
		return "index";
	}

	public String adicionarNovo() {
		return "cadastrar";
	}

	public void adicionarIrmao() {
		if (!isIrmao1()) {
			setIrmao1(true);
			aluno.setIrmao1(new Aluno());
		} else if (!isIrmao2()) {
			setIrmao2(true);
			aluno.setIrmao2(new Aluno());
		} else if (!isIrmao3()) {
			setIrmao3(true);
			aluno.setIrmao3(new Aluno());
		} else if (!isIrmao4()) {
			setIrmao4(true);
			aluno.setIrmao4(new Aluno());
		}
	}

	public void removerIrmao() {
		if (isIrmao4()) {
			setIrmao4(false);
		} else if (isIrmao3()) {
			setIrmao3(false);
		} else if (isIrmao2()) {
			setIrmao2(false);
		} else if (isIrmao1()) {
			setIrmao1(false);
		}
	}

	public void removerHistorico(long idHistorico) {
		alunoService.removerHistorico(idHistorico);
	}

	public String cadastrarNovo() {

		return "exibirAluno";
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public DisciplinaEnum getDisciplinaSel() {
		return disciplinaSel;
	}

	public void setDisciplinaSel(DisciplinaEnum disciplinaSel) {
		this.disciplinaSel = disciplinaSel;
	}

	public BimestreEnum getBimestreSel() {
		return bimestreSel;
	}

	public void setBimestreSel(BimestreEnum bimestreSel) {
		this.bimestreSel = bimestreSel;
	}

	public boolean isIrmao1() {
		return irmao1;
	}

	public void setIrmao1(boolean irmao1) {
		this.irmao1 = irmao1;
	}

	public boolean isIrmao2() {
		return irmao2;
	}

	public void setIrmao2(boolean irmao2) {
		this.irmao2 = irmao2;
	}

	public boolean isIrmao3() {
		return irmao3;
	}

	public void setIrmao3(boolean irmao3) {
		this.irmao3 = irmao3;
	}

	public boolean isIrmao4() {
		return irmao4;
	}

	public void setIrmao4(boolean irmao4) {
		this.irmao4 = irmao4;
	}

	public StreamedContent gerarBoleto() {
		try {
			CNAB240_SICOOB cnab = new CNAB240_SICOOB();
			String nomeArquivo = "boleto.pdf";
			
			String numeroDoBoleto = "10";
			Date dataVencimento = new Date();
			String valorBoleto = "200";
			
			String nomePagador = aluno.getNomeResponsavel();
			String ruaPagador = aluno.getEndereco();
			String cepPagador = aluno.getCep(); 
			String cidadePagador = aluno.getCidade(); 
			String ufPagador = "SC"; 
			String cpfPagador = aluno.getCpfResponsavel();
			String bairroPagador = aluno.getBairro();
			
			
			byte[] pdf = cnab.getBoletoPDF(numeroDoBoleto,valorBoleto,nomePagador,ruaPagador,cepPagador,cidadePagador,bairroPagador,ufPagador,cpfPagador,dataVencimento);
			
			OfficePDFUtil.geraPDF(nomeArquivo, pdf);

			String temp = System.getProperty("java.io.tmpdir");
			String caminho = temp + File.separator + nomeArquivo;
			
			InputStream stream;
			stream = new FileInputStream(caminho);
			return FileDownload.getContentDoc(stream, nomeArquivo);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
