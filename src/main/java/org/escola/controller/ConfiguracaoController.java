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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escola.util.FileDownload;
import org.escolar.enums.StatusBoletoEnum;
import org.escolar.model.Aluno;
import org.escolar.model.Boleto;
import org.escolar.model.Configuracao;
import org.escolar.model.ContratoAluno;
import org.escolar.rotinasAutomaticas.CNAB240;
import org.escolar.service.AlunoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.util.CompactadorZip;
import org.escolar.util.FileUtils;
import org.escolar.util.ImpressoesUtils;
import org.escolar.util.Util;
import org.escolar.util.Verificador;
import org.primefaces.model.StreamedContent;

@Named
@ViewScoped
public class ConfiguracaoController implements Serializable {

	/****/
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private Configuracao configuracao;

	private int mesGerarCNAB;
	private int mesGerarCNABCancelamento;
	
	@Inject
	private ConfiguracaoService configuracaoService;

	@Inject
	private AlunoService alunoService;

	@Inject
	private CNAB240 cnab240;

	
	@PostConstruct
	private void init() {
		List<Configuracao> confs = configuracaoService.findAll();

		if (confs == null || confs.isEmpty()) {
			configuracao = new Configuracao();
		} else {
			configuracao = confs.get(0);
		}
	}
	
	public StreamedContent gerarCNABCancelamentoDoMES(int mes) {
		try {
			Calendar calendario = Calendar.getInstance();

			StringBuilder sb = new StringBuilder();
			sb.append(calendario.get(Calendar.YEAR));
			sb.append(calendario.get(Calendar.MONTH));
			
			sb.append(calendario.get(Calendar.DAY_OF_MONTH));

			List<Boleto> boletos = configuracaoService.findBoletosCanceladosMes(mesGerarCNABCancelamento, configuracao.getAnoRematricula());

			String caminhoFinalPasta = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + File.separator + sb+ File.separator;
			CompactadorZip.createDir(caminhoFinalPasta);

			for (Boleto b : boletos) {
				gerarCNB240Cancelamento(b, caminhoFinalPasta);
			}

			String arquivoSaida = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + File.separator + sb + "CNAB240.zip";
			CompactadorZip.compactarParaZip(arquivoSaida, caminhoFinalPasta);

			InputStream stream2 = new FileInputStream(arquivoSaida);
			return FileDownload.getContentDoc(stream2, "escolarCNABSdoMes" +sb+".zip");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public StreamedContent gerarCNABDoMES(int mes) {
		try {
			Calendar calendario = Calendar.getInstance();

			StringBuilder sb = new StringBuilder();
			sb.append(calendario.get(Calendar.YEAR));
			sb.append(calendario.get(Calendar.MONTH));
			sb.append(calendario.get(Calendar.DAY_OF_MONTH));

			List<Boleto> boletos = configuracaoService.findBoletosMes(mesGerarCNAB, configuracao.getAnoRematricula());

			String caminhoFinalPasta = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + File.separator + sb;
			CompactadorZip.createDir(caminhoFinalPasta);

			for (Boleto b : boletos) {
				ContratoAluno ca = configuracaoService.findContrato(b.getId()); 
				
				if(ca.getCpfResponsavel() != null && !ca.getCpfResponsavel().equalsIgnoreCase("")){
					if(ca.getNomeResponsavel() != null && !ca.getNomeResponsavel().equalsIgnoreCase("")){
						if(ca.getEndereco() != null && !ca.getEndereco().equalsIgnoreCase("")){
							if(ca.getCep() == null || ca.getCep().equalsIgnoreCase("")){
								ca.setCep("88132700");	
							}
							if(ca.getBairro() == null || ca.getBairro().equalsIgnoreCase("")){
								ca.setBairro("Bela Vista");
							}
							if(ca.getCidade() == null || ca.getCidade().equalsIgnoreCase("")){
								ca.setCidade("Palhoca");
							}
							
							InputStream stream = gerarCNB240(ca, mesGerarCNAB, caminhoFinalPasta);
							FileUtils.inputStreamToFile(stream, b.getNossoNumero()+"");
						}
					}
				}
				configuracaoService.mudarStatusParaCNABEnviado(b);
			}

			String arquivoSaida = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + File.separator + sb + "CNAB240.zip";
			CompactadorZip.compactarParaZip(arquivoSaida, caminhoFinalPasta);

			InputStream stream2 = new FileInputStream(arquivoSaida);
			return FileDownload.getContentDoc(stream2, "escolarCNABSdoMes" +sb+".zip");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public InputStream gerarCNB240(ContratoAluno contrato, int mes, String caminhoArquivo) {
		try {
			String sequencialArquivo = configuracaoService.getSequencialArquivo() + "";

			InputStream stream = FileUtils.gerarCNB240(sequencialArquivo, contrato, mes, caminhoArquivo);
			configuracaoService.incrementaSequencialArquivoCNAB();

			return stream;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public StreamedContent gerarProtestos() {
		try {
			Calendar calendario = Calendar.getInstance();
			System.out.println("X1");
			StringBuilder sb = new StringBuilder();
			sb.append(calendario.get(Calendar.YEAR));
			sb.append(calendario.get(Calendar.MONTH));
			sb.append(calendario.get(Calendar.DAY_OF_MONTH));
			sb.append(System.currentTimeMillis());

			List<ContratoAluno> contratos = configuracaoService.findContratosProtestados(false);

			System.out.println("X2 :" + contratos.size());
			String caminhoFinalPasta = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + File.separator + sb;
			CompactadorZip.createDir(caminhoFinalPasta);
			System.out.println("X3 :" + caminhoFinalPasta);
			for (ContratoAluno ca : contratos) {
				
				if(ca.getCpfResponsavel() != null && !ca.getCpfResponsavel().equalsIgnoreCase("")){
					if(ca.getNomeResponsavel() != null && !ca.getNomeResponsavel().equalsIgnoreCase("")){
						if(ca.getEndereco() != null && !ca.getEndereco().equalsIgnoreCase("")){
							if(ca.getCep() == null || ca.getCep().equalsIgnoreCase("")){
								ca.setCep("88132700");	
							}
							if(ca.getBairro() == null || ca.getBairro().equalsIgnoreCase("")){
								ca.setBairro("Bela Vista");
							}
							if(ca.getCidade() == null || ca.getCidade().equalsIgnoreCase("")){
								ca.setCidade("Palhoca");
							}
							System.out.println("X4 :");
							gerarArquivoProtesto(ca,sb.toString());
							
						}
					}
				}
				configuracaoService.mudarStatusParaProtestoEnviadoPorEmail(ca);
			}
			System.out.println("X7 :");
			String arquivoSaida = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + File.separator + sb + "escolarPROTESTOS.zip";
			CompactadorZip.compactarParaZip(arquivoSaida, caminhoFinalPasta);
			System.out.println("X8 :+ arquivoSaida");
			InputStream stream2 = new FileInputStream(arquivoSaida);
			return FileDownload.getContentDoc(stream2, "escolarPROTESTOS" +sb+".zip");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void gerarArquivoProtesto(ContratoAluno ca, String nomeArquivo) {
		//String nomeArquivo = "";
		if (ca != null && ca.getId() != null) {
			nomeArquivo += File.separator ;
			nomeArquivo += ca.getId() + "Z";
			String modeloArq = "modelo_protesto.docx";
			try {
				switch (ca.getBoletos().size()) {
				case 12:
					modeloArq = "modelo_protesto12.docx";
					break;
				case 11:
					modeloArq = "modelo_protesto11.docx";
					break;
				case 10:
					modeloArq = "modelo_protesto10.docx";
					break;
				case 9:
					modeloArq = "modelo_protesto9.docx";
					break;
				case 8:
					modeloArq = "modelo_protesto8.docx";
					break;
				case 7:
					modeloArq = "modelo_protesto7.docx";
					break;
				case 6:
					modeloArq = "modelo_protesto6.docx";
					break;
				case 5:
					modeloArq = "modelo_protesto5.docx";
					break;
				case 4:
					modeloArq = "modelo_protesto4.docx";
					break;

				case 3:
					modeloArq = "modelo_protesto3.docx";
					break;

				case 2:
					modeloArq = "modelo_protesto2.docx";
					break;

				case 1:
					modeloArq = "modelo_protesto1.docx";
					break;

				default:
					break;
				}
				System.out.println("X5 :");
				ImpressoesUtils.gerarArquivoFisico(modeloArq, montarArquivoProtesto(ca), nomeArquivo);
				System.out.println("X6 :" + nomeArquivo);
			} catch (IOException e) {
				e.printStackTrace();
			}
			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "modelo_protesto.docx";
		}

		/*String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + "\\" + nomeArquivo;
		InputStream stream = null;
		try {
			stream = new FileInputStream(caminho);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}
	
	public HashMap<String, String> montarArquivoProtesto(ContratoAluno contratoAluno) {
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.DATE_FIELD, new Locale("pt", "BR"));
		String dataExtenso = formatador.format(new Date());

		HashMap<String, String> trocas = new HashMap<>();
		trocas.put("nomeresponsavel", contratoAluno.getNomeResponsavel());
		trocas.put("nomemae", contratoAluno.getNomeMaeResponsavel());
		trocas.put("cpfresponsavel", contratoAluno.getCpfResponsavel());
		trocas.put("rgresponsavel", contratoAluno.getRgResponsavel());
		trocas.put("enderecoresponsavel", contratoAluno.getEndereco() + " - " + contratoAluno.getBairro() + " - "
				+ contratoAluno.getCidade() + " - " + contratoAluno.getCep() + " - " + "Santa Catarina");
		trocas.put("numerocontrato", contratoAluno.getNumero());
		trocas.put("datacontrato", formatador.format(contratoAluno.getDataCriacaoContrato()));
		trocas.put("valortotalcont",
				((contratoAluno.getNumeroParcelas() * contratoAluno.getValorMensal()) + "").replace(".", ","));
		trocas.put("numeroParcelas", contratoAluno.getNumeroParcelas() + "");
		trocas.put("valorparcela", (contratoAluno.getValorMensal() + "").replace(".", ","));
		trocas.put("datahoje", dataExtenso);

		double totalAberto = 0;
		for (int i = contratoAluno.getBoletos().size(); i > 0; i--) {
			if (i == 12) {
				trocas.put("dtvenctwo", formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("vloritwo", contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("vlcoritow", verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsdoze", situacao);
			} else if (i == 11) {
				trocas.put("dtvencelev", formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrigonze", contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("vlcorriele", verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsonze", situacao);
			} else if (i == 10) {
				trocas.put("dtvencten", formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("vloriten", contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("vlcoriten", verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsdez", situacao);
			} 
			if (i == 9) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsnove", situacao);
			}
			if (i == 8) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsoito", situacao);
			}
			if (i == 7) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stssete", situacao);
			}
			if (i == 6) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsseis", situacao);
			}
			if (i == 5) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stscnco", situacao);
			}
			if (i == 4) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsquatro", situacao);
			}
			if (i == 3) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("ststres", situacao);
			}
			if (i == 2) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsdois", situacao);
			}
			if (i == 1) {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.CANCELADO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.A_VENCER)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.VENCE_HOJE)
						) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsum", situacao);
			}

		}
		trocas.put("totalemaberto", (totalAberto + "0").replace(".", ","));

		return trocas;
	}
	
	public String verificaValorFinalBoleto(Boleto boleto) {
		return Util.formatarDouble2Decimais(Verificador.getValorFinal(boleto));
	}
	
	public void gerarCNB240Cancelamento(Boleto b, String caminhoArquivo) {
		try {
			cnab240.gerarBaixaBoletosCancelados(b, caminhoArquivo);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String salvar() {
		configuracaoService.save(configuracao);
		return "index";
	}

	public void alterAnoRematricula() {

	}

	public Configuracao getConfiguracao() {
		return configuracao;
	}

	public void setConfiguracao(Configuracao configuracao) {
		this.configuracao = configuracao;
	}

	public int getMesGerarCNAB() {
		return mesGerarCNAB;
	}

	public void setMesGerarCNAB(int mesGerarCNAB) {
		this.mesGerarCNAB = mesGerarCNAB;
	}

	public int getMesGerarCNABCancelamento() {
		return mesGerarCNABCancelamento;
	}

	public void setMesGerarCNABCancelamento(int mesGerarCNABCancelamento) {
		this.mesGerarCNABCancelamento = mesGerarCNABCancelamento;
	}

}
