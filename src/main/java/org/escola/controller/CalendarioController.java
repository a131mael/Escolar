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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
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
import javax.servlet.http.HttpServletRequest;

import org.escolar.enums.FormaPagamentoEnum;
import org.escolar.model.Carro;
import org.escolar.model.CarroFrete;
import org.escolar.model.Configuracao;
import org.escolar.model.Contratante;
import org.escolar.model.Evento;
import org.escolar.model.Frete;
import org.escolar.model.PassageiroViagem;
import org.escolar.service.AlunoService;
import org.escolar.service.CTeOSService;
import org.escolar.service.EventoService;
import org.escolar.service.FreteService;
import org.escolar.service.ScmobiService;
import org.escolar.util.MunicipioIBGEUtil;
import org.escolar.util.UtilFinalizarAnoLetivo;
import org.json.JSONObject;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.primefaces.model.StreamedContent;

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

	@Inject
	private CTeOSService cteOSService;

	@Inject
	private ScmobiService scmobiService;

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

	private String idParam;

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
	}

	public void preencherContratantePadrao() {
		Contratante contratante = freteService.findContratanteByCPF("45552550978");
		if (contratante == null) {
			contratante = new Contratante();
			contratante.setCPF_CNPJ("45552550978");
		}
		contratante.setNome("Aldevino Norberto Fidencio");
		contratante.setEmail("divinofidencio@gmail.com");
		contratante.setLogradouro("Manoel Joaquim de Souza");
		contratante.setNumeroEndereco("97");
		contratante.setMunicipio("Palhoça");
		contratante.setUf("SC");
		contratante.setCep("88132710");
		frete.setContratante(contratante);
	}

	public String salvarFreteEVoltar() {
		salvarFrete();
		return "index?faces-redirect=true";
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

		frete.setCodMunOrigem(MunicipioIBGEUtil.buscarCodigoMunicipio(frete.getLocalOrigem(), "SC"));
		frete.setCodMunDestino(MunicipioIBGEUtil.buscarCodigoMunicipio(frete.getLocalDestino(), "SC"));

		Contratante contratante = frete.getContratante();
		if (contratante.getMunicipio() != null && !contratante.getMunicipio().isEmpty()) {
			contratante.setCodMunicipio(MunicipioIBGEUtil.buscarCodigoMunicipio(contratante.getMunicipio(), contratante.getUf()));
		}

		frete = freteService.save(frete);
		montarAgenda();
		addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Viagem salva com sucesso!", null));
	}

	public void gerarXmlCte() {
		salvarFrete();
		try {
			cteOSService.prepararNumeracao(frete, conf);
			String xml = cteOSService.gerarXmlPreview(frete, conf);
			frete.setXmlCteOS(xml);
			frete.setStatusCte("RASCUNHO");
			frete = freteService.save(frete);
			configuracaoService.save(conf);
			addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
					"XML do CT-e OS gerado (rascunho, ainda sem assinatura digital).", null));
		} catch (Exception e) {
			e.printStackTrace();
			addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar XML do CT-e OS: " + e.getMessage(), null));
		}
	}

	public void emitirCte() {
		salvarFrete();
		try {
			cteOSService.emitirCteOS(frete, conf);
			frete = freteService.save(frete);
			configuracaoService.save(conf);

			if ("AUTORIZADO".equals(frete.getStatusCte())) {
				addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
						"CT-e OS autorizado! Protocolo: " + frete.getProtocoloCte(), null));
			} else {
				addMessage(new FacesMessage(FacesMessage.SEVERITY_WARN,
						"CT-e OS nao autorizado: " + frete.getMotivoCte(), null));
			}
		} catch (Exception e) {
			e.printStackTrace();
			addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao emitir CT-e OS: " + e.getMessage(), null));
		}
	}

	private String justificativaCancelamento;

	public String getJustificativaCancelamento() { return justificativaCancelamento; }
	public void setJustificativaCancelamento(String justificativaCancelamento) { this.justificativaCancelamento = justificativaCancelamento; }

	/** Envia o evento de cancelamento (110111) do CT-e OS autorizado para a SEFAZ. */
	public void cancelarCte() {
		if (justificativaCancelamento == null || justificativaCancelamento.trim().length() < 15) {
			addMessage(new FacesMessage(FacesMessage.SEVERITY_WARN,
					"A justificativa do cancelamento deve ter no minimo 15 caracteres.", null));
			return;
		}
		try {
			cteOSService.cancelarCteOS(frete, conf, justificativaCancelamento.trim());
			frete = freteService.save(frete);

			if ("CANCELADO".equals(frete.getStatusCte())) {
				addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,
						"CT-e OS cancelado! Protocolo do cancelamento: " + frete.getProtocoloCancelamentoCte(), null));
				justificativaCancelamento = null;
			} else {
				addMessage(new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Cancelamento nao homologado: " + frete.getMotivoCte(), null));
			}
		} catch (Exception e) {
			e.printStackTrace();
			addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao cancelar CT-e OS: " + e.getMessage(), null));
		}
	}

	/** Dispara a geracao assincrona da Licenca de Fretamento Eventual no scMOBI (microsservico Python/Selenium). */
	public void gerarLicencaScmobi() {
		salvarFrete();

		if (frete.getNumeroLicencaScmobi() != null) {
			addMessage(new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Licença scMOBI já gerada para este CT-e (Nº " + frete.getNumeroLicencaScmobi()
							+ "). O scMOBI não permite gerar outra licença para a mesma chave de CT-e.", null));
			return;
		}

		StringBuilder erros = new StringBuilder();
		if (frete.getQuilometragem() == null || frete.getQuilometragem() <= 0) {
			erros.append("Quilometragem da viagem deve ser maior que zero. ");
		}
		if (frete.getChaveCte() == null || frete.getChaveCte().trim().isEmpty()) {
			erros.append("Chave de acesso do CT-e nao informada (gere o CT-e OS primeiro). ");
		}
		if (frete.getCarroFrete() == null || frete.getCarroFrete().isEmpty()) {
			erros.append("Nenhum veiculo selecionado. ");
		}
		if (frete.getMotorista() == null || frete.getMotorista().getCnhNumero() == null
				|| frete.getMotorista().getCnhNumero().trim().isEmpty()) {
			erros.append("Motorista sem CNH cadastrada. ");
		}
		if (frete.getContratante() == null || frete.getContratante().getCPF_CNPJ() == null
				|| frete.getContratante().getCPF_CNPJ().trim().isEmpty()) {
			erros.append("Contratante sem CPF/CNPJ. ");
		}
		if (frete.getPassageiros() == null || frete.getPassageiros().isEmpty()) {
			erros.append("Nenhum passageiro confirmado para esta viagem. ");
		}
		if (frete.getLocalOrigem() == null || frete.getLocalDestino() == null) {
			erros.append("Origem/Destino nao informados. ");
		}
		if (frete.getHorarioLocalOrigem() == null) {
			erros.append("Data/hora de saida nao informada. ");
		}

		if (erros.length() > 0) {
			addMessage(new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Não foi possível gerar a licença: " + erros.toString().trim(), null));
			return;
		}

		try {
			String jobId = scmobiService.solicitarLicenca(frete, conf);
			frete.setJobIdScmobi(jobId);
			frete.setStatusLicencaScmobi("PROCESSANDO");
			frete.setErroLicencaScmobi(null);
			frete = freteService.save(frete);
			addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Geração da licença scMOBI iniciada...", null));
		} catch (Exception e) {
			e.printStackTrace();
			addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao solicitar licença scMOBI: " + e.getMessage(), null));
		}
	}

	/** Listener do &lt;p:poll&gt; da aba scMOBI - consulta o scmobi-automation-service e atualiza o status/PDFs no banco.
	 *  (A RotinaAutomatica deveria fazer essa verificacao em segundo plano, mas o seu timer nao esta disparando;
	 *  enquanto isso, o proprio poll da tela assume essa checagem.) */
	public void atualizarStatusLicencaScmobi() {
		if (frete.getId() == null) {
			return;
		}
		Frete atualizado = freteService.findById(frete.getId());

		if ("PROCESSANDO".equals(atualizado.getStatusLicencaScmobi()) && atualizado.getJobIdScmobi() != null) {
			try {
				JSONObject status = scmobiService.consultarStatus(atualizado.getJobIdScmobi());
				String situacao = status.getString("status");

				if ("concluido".equals(situacao)) {
					byte[] licenca = scmobiService.baixarLicencaPdf(atualizado.getJobIdScmobi());
					byte[] passageiros = scmobiService.baixarPassageirosPdf(atualizado.getJobIdScmobi());
					String dataGeracao = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
					freteService.atualizarLicencaScmobiConcluida(atualizado.getId(), status.optInt("numeroContrato"),
							licenca, passageiros, dataGeracao);
					atualizado = freteService.findById(frete.getId());
				} else if ("erro".equals(situacao)) {
					String erro = status.optString("erro", "Erro desconhecido no scmobi-automation-service");
					freteService.atualizarLicencaScmobiErro(atualizado.getId(), erro);
					atualizado = freteService.findById(frete.getId());
				}
				// "processando" -> nada a fazer, tenta novamente no proximo poll

			} catch (IOException e) {
				if (e.getMessage() != null && e.getMessage().contains("HTTP 404")) {
					// scmobi-automation-service foi reiniciado e perdeu o job em memoria - nao ha como recuperar, gerar de novo.
					freteService.atualizarLicencaScmobiErro(atualizado.getId(),
							"O serviço de geração da licença foi reiniciado e perdeu este pedido. Gere a licença novamente.");
					atualizado = freteService.findById(frete.getId());
				} else {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		frete.setStatusLicencaScmobi(atualizado.getStatusLicencaScmobi());
		frete.setNumeroLicencaScmobi(atualizado.getNumeroLicencaScmobi());
		frete.setErroLicencaScmobi(atualizado.getErroLicencaScmobi());
		frete.setDataGeracaoLicencaScmobi(atualizado.getDataGeracaoLicencaScmobi());
		frete.setLicencaScmobiPdf(atualizado.getLicencaScmobiPdf());
		frete.setPassageirosScmobiPdf(atualizado.getPassageirosScmobiPdf());
	}

	/** Rotulo amigavel do status da licenca scMOBI, exibido na aba scMOBI. */
	public String getStatusLicencaScmobiLabel() {
		String status = frete.getStatusLicencaScmobi();
		if (status == null) {
			return "Não gerada";
		}
		switch (status) {
			case "PROCESSANDO": return "Processando...";
			case "CONCLUIDO": return "Concluída";
			case "ERRO": return "Erro";
			default: return status;
		}
	}

	public String getChaveCteFormatada() {
		String chave = frete.getChaveCte();
		if (chave == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chave.length(); i += 4) {
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(chave.substring(i, Math.min(i + 4, chave.length())));
		}
		return sb.toString();
	}

	/** URL do QR Code (infCTeSupl/qrCodCTe) extraida do XML assinado, para impressao do DACTE OS. */
	public String getQrCodCteUrl() {
		return extrairTagXml("qrCodCTe");
	}

	/** Descricao do servico (infServico/xDescServ) extraida do XML assinado. */
	public String getDescServ() {
		String desc = extrairTagXml("xDescServ");
		return desc == null ? "TRANSPORTE DE PASSAGEIROS" : desc;
	}

	/** TAF ou Numero de Registro Estadual (ja no formato enviado a SEFAZ), para o quadro do modal rodoviario. */
	public String getNroRegEstadualImpressao() {
		String nro = extrairTagXml("TAF");
		return nro != null ? nro : extrairTagXml("NroRegEstadual");
	}

	/** Rotulo do TAF/Nro de Registro Estadual, conforme qual dos dois foi enviado. */
	public String getNroRegEstadualLabel() {
		return extrairTagXml("TAF") != null ? "Termo de Autorizacao de Fretamento (TAF)" : "Nro do Registro Estadual";
	}

	/** Rotulo do tpServ (tabela de Tipo de Servico do CT-e OS) extraido do XML assinado. */
	public String getTpServLabel() {
		String tpServ = extrairTagXml("tpServ");
		if (tpServ == null) {
			return "Transporte de Passageiros";
		}
		switch (tpServ) {
			case "0": return "Transporte de Pessoas";
			case "1": return "Transporte de Valores";
			case "2": return "Excesso de Bagagem";
			case "3": return "Locação de Veículos";
			case "4": return "Transporte de Pessoas com Encomendas";
			case "5": return "Outros";
			case "6": return "Transporte de Pessoas";
			default: return tpServ;
		}
	}

	public String getTpFretamentoLabel() {
		return "2".equals(frete.getTpFretamento())
				? "2 - CONTÍNUO (INTERMUNICIPAL/INTERESTADUAL)"
				: "1 - EVENTUAL";
	}

	public String getCnpjFormatado() {
		return formatarCnpj(conf.getCnpj());
	}

	public String getCfopENaturezaOperacao() {
		return conf.getCfop() + " - " + conf.getNaturezaOperacao();
	}

	/** Numero do CT-e no padrao "000.000.001" usado no DACTE OS. */
	public String getNumeroCteFormatado() {
		if (frete.getNumeroCte() == null) {
			return null;
		}
		String n = String.format("%09d", frete.getNumeroCte());
		return n.substring(0, 3) + "." + n.substring(3, 6) + "." + n.substring(6, 9);
	}

	public String getDhEmiFormatada() {
		return formatarDataIso(extrairTagXml("dhEmi"));
	}

	public String getDataAutorizacaoCteFormatada() {
		return formatarDataIso(frete.getDataAutorizacaoCte());
	}

	public String getClassificacaoTributaria() {
		return (conf.getAliquotaIcms() == null || conf.getAliquotaIcms() == 0d)
				? "ICMS Simples Nacional" : "ICMS Normal";
	}

	public String getBaseCalculoIcms() {
		boolean simples = conf.getAliquotaIcms() == null || conf.getAliquotaIcms() == 0d;
		double base = simples ? 0d : (frete.getValor() == null ? 0d : frete.getValor());
		return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%,.2f", base);
	}

	public String getAliquotaIcmsFormatada() {
		double aliquota = conf.getAliquotaIcms() == null ? 0d : conf.getAliquotaIcms();
		return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%,.2f", aliquota);
	}

	public String getValorIcms() {
		boolean simples = conf.getAliquotaIcms() == null || conf.getAliquotaIcms() == 0d;
		double valor = 0d;
		if (!simples) {
			double base = frete.getValor() == null ? 0d : frete.getValor();
			valor = base * conf.getAliquotaIcms() / 100d;
		}
		return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%,.2f", valor);
	}

	/** Extrai o conteudo de uma tag simples (sem atributos/filhos) do XML assinado do CT-e OS. */
	private String extrairTagXml(String tag) {
		String xml = frete.getXmlCteOS();
		if (xml == null) {
			return null;
		}
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("<" + tag + ">(.*?)</" + tag + ">").matcher(xml);
		return m.find() ? m.group(1).replace("&amp;", "&") : null;
	}

	private String formatarCnpj(String cnpj) {
		if (cnpj == null) {
			return null;
		}
		String d = cnpj.replaceAll("[^0-9]", "");
		if (d.length() != 14) {
			return cnpj;
		}
		return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8) + "/" + d.substring(8, 12) + "-" + d.substring(12, 14);
	}

	private String formatarDataIso(String dataIso) {
		if (dataIso == null) {
			return null;
		}
		try {
			java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(dataIso);
			return odt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		} catch (Exception e) {
			return dataIso;
		}
	}

	public StreamedContent getXmlCteDownload() {
		if (frete.getXmlCteOS() == null) {
			return null;
		}
		String nomeArquivo = "CTeOS-" + frete.getNumeroCte() + "-" + frete.getSerieCte() + ".xml";
		return new DefaultStreamedContent(
				new ByteArrayInputStream(frete.getXmlCteOS().getBytes(StandardCharsets.UTF_8)),
				"application/xml", nomeArquivo);
	}

	public StreamedContent getPassageirosCsvDownload() {
		StringBuilder sb = new StringBuilder();
		for (PassageiroViagem p : frete.getPassageiros()) {
			sb.append(p.getNome()).append(",").append(p.getCpf()).append("\r\n");
		}
		String nomeArquivo = "Passageiros-" + frete.getId() + ".csv";
		return new DefaultStreamedContent(
				new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)),
				"text/csv", nomeArquivo);
	}

	public StreamedContent getLicencaScmobiDownload() {
		if (frete.getLicencaScmobiPdf() == null) {
			return null;
		}
		String nomeArquivo = "Licenca-scMOBI-" + frete.getNumeroLicencaScmobi() + ".pdf";
		return new DefaultStreamedContent(new ByteArrayInputStream(frete.getLicencaScmobiPdf()), "application/pdf", nomeArquivo);
	}

	public StreamedContent getPassageirosScmobiDownload() {
		if (frete.getPassageirosScmobiPdf() == null) {
			return null;
		}
		String nomeArquivo = "Passageiros-scMOBI-" + frete.getNumeroLicencaScmobi() + ".pdf";
		return new DefaultStreamedContent(new ByteArrayInputStream(frete.getPassageirosScmobiPdf()), "application/pdf", nomeArquivo);
	}

	public List<Frete> getFretes() {
		return freteService.findAll();
	}

	public boolean isPago(Frete f) {
		return f.getValor() != null && f.getValor().equals(f.getValorPago());
	}

	public void novoFrete() {
		frete = new Frete();
		frete.setTpFretamento("1");
		Contratante c = new Contratante();
		frete.setContratante(c);
		carrosSelecionados = new ArrayList<Carro>();

		Calendar date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR_OF_DAY, 8);
		date.set(Calendar.MINUTE, 0);

		Calendar dateFim = (Calendar) date.clone();
		dateFim.add(Calendar.HOUR, 4);

		frete.setHorarioLocalOrigem(date.getTime());
		frete.setHorarioParaRetorno(dateFim.getTime());
	}

	public void editarFrete(Long id) {
		frete = freteService.findById(id);
		garantirTokenPublico();
		carrosSelecionados = montarCarrosSelecionados(frete.getCarroFrete());
	}

	public void carregarViagem() {
		if (idParam != null && !idParam.isEmpty()) {
			try {
				editarFrete(Long.parseLong(idParam));
				return;
			} catch (NumberFormatException e) {
				// idParam invalido, segue para nova viagem
			}
		}
		novoFrete();
	}

	public String getIdParam() {
		return idParam;
	}

	public void setIdParam(String idParam) {
		this.idParam = idParam;
	}

	private void garantirTokenPublico() {
		if (frete.getTokenPublico() == null || frete.getTokenPublico().isEmpty()) {
			frete.gerarToken();
			frete = freteService.save(frete);
		}
	}

	public void removerFrete(Long id) {
		freteService.remover(id);
		montarAgenda();
		addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Viagem removida com sucesso!", null));
	}

	private static final String DOMINIO_LINK_VIAGEM = "https://viagem.tefamel.com.br";

	public String getLinkViagem() {
		return getLinkViagem(frete);
	}

	public String getLinkViagem(Frete f) {
		if (f == null || f.getTokenPublico() == null || f.getTokenPublico().isEmpty()) {
			return null;
		}
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest();
		return DOMINIO_LINK_VIAGEM + request.getContextPath() + "/public/viagem.xhtml?token=" + f.getTokenPublico();
	}

	public void setLinkViagem(String linkViagem) {
		// somente leitura, exibido para copiar o link público da viagem
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

	public void onEventSelect(SelectEvent selectEvent) throws IOException {
		event = (ScheduleEvent) selectEvent.getObject();
		String titulo = event.getTitle();
		String idFrete = titulo.substring(1, titulo.lastIndexOf("#"));

		redirecionarParaEdicao(idFrete);
	}

	public void onDateSelect(SelectEvent selectEvent) throws IOException {
		redirecionarParaEdicao(null);
	}

	private void redirecionarParaEdicao(String idFrete) throws IOException {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest();
		String url = request.getContextPath() + "/page/viagem/editarViagem.xhtml";
		if (idFrete != null) {
			url += "?id=" + idFrete;
		}
		FacesContext.getCurrentInstance().getExternalContext().redirect(url);
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
