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
package org.escola.controller.devedor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Base64;
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
import javax.persistence.Column;

import org.aaf.escolar.RetornoEnvioContratoDTO;
import org.escola.util.FileDownload;
import org.escolar.enums.PerioddoEnum;
import org.escolar.enums.StatusBoletoEnum;
import org.escolar.model.Aluno;
import org.escolar.model.Boleto;
import org.escolar.model.Configuracao;
import org.escolar.model.ContratoAluno;
import org.escolar.model.Devedor;
import org.escolar.service.AlunoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.DevedorService;
import org.escolar.service.TurmaService;
import org.escolar.util.ImpressoesUtils;
import org.escolar.util.Util;
import org.escolar.util.Verificador;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;

import br.com.aaf.base.base.ConstantesEscolaApi;
import br.com.aaf.base.comunicadores.EnviadorJson;
import br.com.aaf.base.whats.model.Parametro;

@Named
@ViewScoped
public class DevedorController implements Serializable {

	/****/
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private Devedor devedor;

	@Produces
	@Named
	private Aluno alunoDevedor;

	@Produces
	@Named
	private List<Devedor> devedores;

	@Inject
	private DevedorService devedorService;

	@Inject
	private AlunoService alunoService;

	@Inject
	private TurmaService carroService;

	@Inject
	private ConfiguracaoService configuracaoService;

	private LazyDataModel<Aluno> lazyListDataModelAtrasados;

	private LazyDataModel<Aluno> lazyListDataModelAtrasadosContactados;

	private LazyDataModel<Aluno> lazyListDataModel;

	private LazyDataModel<ContratoAluno> lazyListDataModel2;

	private LazyDataModel<ContratoAluno> lazyListDataModelProtestoWebService;
	
	private LazyDataModel<ContratoAluno> lazyListDataModelProtestoConfirmadoWebService;

	private LazyDataModel<ContratoAluno> lazyListDataModel3;

	private LazyDataModel<Aluno> lazyListDataModelAnoLetivo;

	private LazyDataModel<Aluno> lazyListDataModelAnoPassado;

	private LazyDataModel<Aluno> lazyListDataModelAnteriores;

	private Date dataInicio;

	private Date dataFim;

	private Configuracao configuracao;

	private Double valorTotal = 0D;

	@Column
	private String observacaoAtrasado;

	private long total = 0;
	@Produces
	@Named
	private ContratoAluno contratoS;

	private UploadedFile file;
	private byte[] bts = null;

	@PostConstruct
	private void init() {
		if (getDevedor() == null) {
			Object obj = Util.getAtributoSessao("devedor");
			if (obj != null) {
				setDevedor((Devedor) obj);
				Double total = 0D;
				for (Boleto b : devedor.getBoletos()) {
					// +=b.getValor();
				}
				getDevedor().setValorTotal(total);
			} else {
				Boleto b = new Boleto();
				List<Boleto> bs = new ArrayList<>();
				bs.add(b);
				devedor = new Devedor();
				devedor.setBoletos(bs);
				devedor.setValorTotal(0D);
			}
		}
		configuracao = configuracaoService.getConfiguracao();
	}

	public StreamedContent imprimirDevedores() throws IOException, DocumentException {
		/*
		 * String nomeArquivo = "devedores.pdf"; String caminho =
		 * FacesContext.getCurrentInstance().getExternalContext().getRealPath( "/") +
		 * "\\"+nomeArquivo; Map<String, Object> filtros = new LinkedHashMap();
		 * filtros.put("removido", false); List<Aluno> devedores =
		 * devedorService.find(0, 2000, "nomeResponsavel", "asc", filtros);
		 * 
		 * LinkedHashSet<Aluno> aux = new LinkedHashSet(); aux.addAll(devedores);
		 * 
		 * List<Aluno> aux2 = new ArrayList<>(); aux2.addAll(aux);
		 * OfficePDFUtil.criaPDFDevedores(aux2, caminho);
		 * 
		 * InputStream stream = new FileInputStream(caminho); return
		 * FileDownload.getContentDoc(stream, nomeArquivo);
		 */
		return null;
	}

	public void atualizarStatus() {
		@SuppressWarnings("unchecked")
		List<ContratoAluno> data = (List<ContratoAluno>) lazyListDataModelProtestoWebService.getWrappedData();
		for (ContratoAluno contrato : data) {
			try {
				byte[] decode = getStatus(contrato).getBytes();
				contrato.setComentarioWebService(decode);
				alunoService.saveComentarioContrato(contrato);	
			}catch (Exception e) {
			}
			
		}

	}

	public void enviarCartorio() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		String endpoint = ConstantesEscolaApi.URL_CARTORIO + ConstantesEscolaApi.ProtestoCartorio;
		// String endpoint =
		// "http://localhost:1414/integracao-0.0.1-SNAPSHOT/api/enviarContratoProtesto"
		// ;

		Parametro p1 = new Parametro("idContrato", getContratoS().getNumero());
		List<Parametro> parametros = new ArrayList<>();
		parametros.add(p1);
		String retornoJson = EnviadorJson.get2(endpoint, null, parametros);
		RetornoEnvioContratoDTO retorno = new RetornoEnvioContratoDTO();
		try {
			retorno = mapper.readValue(retornoJson, RetornoEnvioContratoDTO.class);
			System.out.println(retornoJson);

			if (retorno.getCodigo() != null
					&& (retorno.getCodigo().contains("201") || retorno.getCodigo().contains("200"))) {

				// contratoS.setComentario(contratoS.getComentario() + "/n" +
				// retorno.getResponse()[0].getResposta().getMensagem());
				alunoService.enviarCDL(contratoS);
				byte[] decode = retorno.getResponse()[0].getResposta().getMensagem().getBytes();
				contratoS.setComentarioWebService(decode);
				alunoService.saveComentarioContrato(contratoS);

			} else {
				// contratoS.setComentario(contratoS.getComentario() + "/n" +
				// retorno.getResponse()[0].getResposta().getMensagem());

				byte[] decode = retorno.getResponse()[0].getResposta().getMensagem().getBytes();
				contratoS.setComentarioWebService(decode);
				alunoService.saveComentarioContrato(contratoS);
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void enviarProtestoCartorio(ContratoAluno contrato) {

	}

	public void adicionarBoleto() {
		Boleto ultimo = new Boleto();
		if (devedor.getBoletos().size() > 0) {
			ultimo = devedor.getBoletos().get(devedor.getBoletos().size() - 1);
		}
		Boleto boleto = new Boleto();
		/*
		 * boleto.setNumeroContrato(ultimo.getNumeroContrato());
		 * boleto.setDataGeracao(ultimo.getDataGeracao());
		 * boleto.setValor(ultimo.getValor());
		 */
		devedor.getBoletos().add(boleto);
	}

	public void buscar() {

	}

	public LazyDataModel<Aluno> getLazyDataModelAtrasados() {
		if (lazyListDataModelAtrasados == null) {

			lazyListDataModelAtrasados = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return getDevedorService().findById(Long.valueOf(rowKey));
				}

				@Override
				public Long getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("enviadoParaCobrancaCDL")) {
						filtros.put("enviadoParaCobrancaCDL",
								filtros.get("enviadoParaCobrancaCDL").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("contratoTerminado")) {
						filtros.put("contratoTerminado",
								filtros.get("contratoTerminado").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("enviadoSPC")) {
						filtros.put("enviadoSPC",
								filtros.get("enviadoSPC").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = getDevedorService().findAtrasados(dataInicio, dataFim, orderByParam, orderParam,
							first, pageSize, where);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelAtrasados.setRowCount((int) getDevedorService().countAtrasados(null));
						return ol;
					}

					this.setRowCount((int) getDevedorService().countAtrasados(null));
					return null;

				}
			};
			lazyListDataModelAtrasados.setRowCount((int) getDevedorService().countAtrasados(null));

		}

		return lazyListDataModelAtrasados;

	}

	public LazyDataModel<Aluno> getLazyDataModelAtrasadosContactados() {
		if (lazyListDataModelAtrasadosContactados == null) {

			lazyListDataModelAtrasadosContactados = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return getDevedorService().findById(Long.valueOf(rowKey));
				}

				@Override
				public Long getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("enviadoParaCobrancaCDL")) {
						filtros.put("enviadoParaCobrancaCDL",
								filtros.get("enviadoParaCobrancaCDL").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("contratoTerminado")) {
						filtros.put("contratoTerminado",
								filtros.get("contratoTerminado").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("enviadoSPC")) {
						filtros.put("enviadoSPC",
								filtros.get("enviadoSPC").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = getDevedorService().findAtrasadosContactado(dataInicio, dataFim, orderByParam,
							orderParam, first, pageSize, where);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelAtrasadosContactados
								.setRowCount((int) getDevedorService().countAtrasados(null));
						return ol;
					}

					this.setRowCount((int) getDevedorService().countAtrasados(null));
					return null;

				}
			};
			lazyListDataModelAtrasadosContactados.setRowCount((int) getDevedorService().countAtrasados(null));

		}

		return lazyListDataModelAtrasadosContactados;

	}

	private Double sumAll(List<Aluno> alunos) {
		Double total = 0D;
		for (Aluno a : alunos) {

			total += a.getContratoVigente().getValorMensal();
		}
		return total;
	}

	public LazyDataModel<Aluno> getLazyDataModel() {
		if (lazyListDataModel == null) {

			lazyListDataModel = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return getDevedorService().findById(Long.valueOf(rowKey));
				}

				@Override
				public Long getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("enviadoParaCobrancaCDL")) {
						filtros.put("enviadoParaCobrancaCDL",
								filtros.get("enviadoParaCobrancaCDL").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("contratoTerminado")) {
						filtros.put("contratoTerminado",
								filtros.get("contratoTerminado").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("enviadoSPC")) {
						filtros.put("enviadoSPC",
								filtros.get("enviadoSPC").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = getDevedorService().findDevedor(dataInicio, dataFim);

					if (ol != null && ol.size() > 0) {
						lazyListDataModel.setRowCount((int) getDevedorService().count(filtros));
						return ol;
					}

					this.setRowCount((int) getDevedorService().count(filtros));
					return null;

				}
			};
			lazyListDataModel.setRowCount((int) getDevedorService().count(null));

		}

		return lazyListDataModel;

	}

	public void onCellEdit(CellEditEvent event) {
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();
		System.out.println(newValue);

	}

	public void onRowEdit(RowEditEvent event) {
		System.out.println("a");
	}

	public void saveComentario(ContratoAluno ca) {
		devedorService.saveComentario(ca);
	}

	public LazyDataModel<Aluno> getLazyDataModelAnoLetivo() {
		if (lazyListDataModelAnoLetivo == null) {

			lazyListDataModelAnoLetivo = new LazyDataModel<Aluno>() {

				@Override
				public Aluno getRowData(String rowKey) {
					return getDevedorService().findById(Long.valueOf(rowKey));
				}

				@Override
				public Long getRowKey(Aluno al) {
					return al.getId();
				}

				@Override
				public List<Aluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("enviadoParaCobrancaCDL")) {
						filtros.put("enviadoParaCobrancaCDL",
								filtros.get("enviadoParaCobrancaCDL").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("contratoTerminado")) {
						filtros.put("contratoTerminado",
								filtros.get("contratoTerminado").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("enviadoSPC")) {
						filtros.put("enviadoSPC",
								filtros.get("enviadoSPC").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<Aluno> ol = getDevedorService().findAnoLetivo(first, pageSize, orderByParam, orderParam,
							filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelAnoLetivo.setRowCount((int) getDevedorService().count(filtros));
						return ol;
					}

					this.setRowCount((int) getDevedorService().count(filtros));
					return null;

				}
			};
			lazyListDataModelAnoLetivo.setRowCount((int) getDevedorService().count(null));

		}

		return lazyListDataModelAnoLetivo;

	}

	public LazyDataModel<ContratoAluno> getLazyDataModel2() {
		if (lazyListDataModel2 == null) {

			lazyListDataModel2 = new LazyDataModel<ContratoAluno>() {

				@Override
				public ContratoAluno getRowData(String rowKey) {
					return getDevedorService().findByIdContratoAluno(Long.valueOf(rowKey));
				}

				@Override
				public Long getRowKey(ContratoAluno al) {
					return al.getId();
				}

				@Override
				public List<ContratoAluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("enviadoParaCobrancaCDL")) {
						filtros.put("enviadoParaCobrancaCDL",
								filtros.get("enviadoParaCobrancaCDL").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("contratoTerminado")) {
						filtros.put("contratoTerminado",
								filtros.get("contratoTerminado").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("enviadoSPC")) {
						filtros.put("enviadoSPC",
								filtros.get("enviadoSPC").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}
					filtros.put("protestado", Boolean.TRUE);
					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<ContratoAluno> ol = getDevedorService().findProtesto(first, pageSize, orderByParam, orderParam,
							filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModel2.setRowCount(ol.size() + pageSize + 1);
						return ol;
					}

					this.setRowCount(ol.size() + pageSize + 1);
					return null;

				}
			};
			lazyListDataModel2.setRowCount((int) getDevedorService().countContratoAluno(null));

		}

		return lazyListDataModel2;

	}

	public LazyDataModel<ContratoAluno> getLazyListDataModelProtestoWebService() {
		if (lazyListDataModelProtestoWebService == null) {

			lazyListDataModelProtestoWebService = new LazyDataModel<ContratoAluno>() {

				@Override
				public ContratoAluno getRowData(String rowKey) {
					return getDevedorService().findByIdContratoAluno(Long.valueOf(rowKey));
				}

				@Override
				public Long getRowKey(ContratoAluno al) {
					return al.getId();
				}

				@Override
				public List<ContratoAluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("enviadoParaCobrancaCDL")) {
						filtros.put("enviadoParaCobrancaCDL",
								filtros.get("enviadoParaCobrancaCDL").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("contratoTerminado")) {
						filtros.put("contratoTerminado",
								filtros.get("contratoTerminado").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("enviadoSPC")) {
						filtros.put("enviadoSPC",
								filtros.get("enviadoSPC").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}
					filtros.put("protestado", Boolean.TRUE);
					filtros.put("enviadoParaCobrancaCDL", Boolean.TRUE);
					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<ContratoAluno> ol = getDevedorService().findProtesto(first, pageSize, orderByParam, orderParam,
							filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelProtestoWebService.setRowCount(ol.size() + pageSize + 1);
						return ol;
					}

					if (ol == null) {
						return null;
					}
					this.setRowCount(ol.size() + pageSize + 1);
					return null;

				}
			};
			lazyListDataModelProtestoWebService.setRowCount((int) getDevedorService().countContratoAluno(null));

		}

		return lazyListDataModelProtestoWebService;

	}
	
	public LazyDataModel<ContratoAluno> getLazyListDataModelProtestoConfirmadoWebService() {
		if (lazyListDataModelProtestoConfirmadoWebService == null) {

			lazyListDataModelProtestoConfirmadoWebService = new LazyDataModel<ContratoAluno>() {

				@Override
				public ContratoAluno getRowData(String rowKey) {
					return getDevedorService().findByIdContratoAluno(Long.valueOf(rowKey));
				}

				@Override
				public Long getRowKey(ContratoAluno al) {
					return al.getId();
				}

				@Override
				public List<ContratoAluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("enviadoParaCobrancaCDL")) {
						filtros.put("enviadoParaCobrancaCDL",
								filtros.get("enviadoParaCobrancaCDL").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("contratoTerminado")) {
						filtros.put("contratoTerminado",
								filtros.get("contratoTerminado").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("enviadoSPC")) {
						filtros.put("enviadoSPC",
								filtros.get("enviadoSPC").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}
					filtros.put("protestado", Boolean.TRUE);
					filtros.put("enviadoParaCobrancaCDL", Boolean.TRUE);
					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<ContratoAluno> ol = getDevedorService().findProtestoConfirmado(first, pageSize, orderByParam, orderParam,
							filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModelProtestoConfirmadoWebService.setRowCount(ol.size() + pageSize + 1);
						return ol;
					}

					if (ol == null) {
						return null;
					}
					this.setRowCount(ol.size() + pageSize + 1);
					return null;

				}
			};
			lazyListDataModelProtestoConfirmadoWebService.setRowCount((int) getDevedorService().countContratoAluno(null));

		}

		return lazyListDataModelProtestoConfirmadoWebService;

	}

	public void handleFileUpload(FileUploadEvent event) throws IOException {
		setFile(event.getFile());
		setBts(getFile().getContents());
		contratoS.setContratoScaneado(getBase64FromByte(getBts()));
		System.out.println(getBase64FromByte(getBts()));

		alunoService.saveArquivoContrato(contratoS);
	}

	public String getBase64FromByte(byte[] bytes) throws IOException {
		if (bytes != null) {
			byte[] encoded = Base64.getEncoder().encode(bytes);
			String encodedString = new String(encoded);

			return encodedString;

		} else {
			return "ainda nao foi enviado arquivo";
		}

	}

	public String getStatus(ContratoAluno contrato) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
			String endpoint = ConstantesEscolaApi.URL_CARTORIO + ConstantesEscolaApi.STATATUS_CONTRATO;
			
		// String endpoint ="http://localhost:1414/integracao-0.0.1-SNAPSHOT/api/recuperarStatus"			 ;


			Parametro p1 = new Parametro("idContrato", contrato.getNumero());
			List<Parametro> parametros = new ArrayList<>();
			parametros.add(p1);
			String retornoJson = EnviadorJson.get2(endpoint, null, parametros);
			if (retornoJson.equalsIgnoreCase("")) {
				return "Sem informaçoes";
			}
			RetornoEnvioContratoDTO retorno = new RetornoEnvioContratoDTO();

			retorno = mapper.readValue(retornoJson, RetornoEnvioContratoDTO.class);

			return retorno.getCodigo() + " - " + retorno.getMensagem();

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Sem informaçoes";
	}

	public String getMessage(byte[] bytes) throws IOException {

		if (bytes != null) {

			return new String(bytes);

		} else {
			return "ainda nao foi enviado arquivo";
		}

	}

	public LazyDataModel<ContratoAluno> getLazyDataModel3() {
		if (lazyListDataModel3 == null) {

			lazyListDataModel3 = new LazyDataModel<ContratoAluno>() {

				@Override
				public ContratoAluno getRowData(String rowKey) {
					return getDevedorService().findByIdContratoAluno(Long.valueOf(rowKey));
				}

				@Override
				public Long getRowKey(ContratoAluno al) {
					return al.getId();
				}

				@Override
				public List<ContratoAluno> load(int first, int pageSize, String order, SortOrder so,
						Map<String, Object> where) {

					Map<String, Object> filtros = new HashMap<String, Object>();

					filtros.putAll(where);
					if (filtros.containsKey("periodo")) {
						filtros.put("periodo", filtros.get("periodo").equals("MANHA") ? PerioddoEnum.MANHA
								: filtros.get("periodo").equals("TARDE") ? PerioddoEnum.TARDE : PerioddoEnum.INTEGRAL);
					}

					if (filtros.containsKey("enviadoParaCobrancaCDL")) {
						filtros.put("enviadoParaCobrancaCDL",
								filtros.get("enviadoParaCobrancaCDL").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("contratoTerminado")) {
						filtros.put("contratoTerminado",
								filtros.get("contratoTerminado").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}

					if (filtros.containsKey("enviadoSPC")) {
						filtros.put("enviadoSPC",
								filtros.get("enviadoSPC").equals("Não") ? Boolean.FALSE : Boolean.TRUE);
					}
					filtros.put("protestado", Boolean.TRUE);
					String orderByParam = (order != null) ? order : "id";
					String orderParam = ("ASCENDING".equals(so.name())) ? "asc" : "desc";

					List<ContratoAluno> ol = getDevedorService().findProtestoEnviado(first, pageSize, orderByParam,
							orderParam, filtros);

					if (ol != null && ol.size() > 0) {
						lazyListDataModel3.setRowCount(ol.size() + pageSize + 1);
						return ol;
					}

					this.setRowCount(ol.size() + pageSize + 1);
					return null;

				}
			};
			lazyListDataModel3.setRowCount((int) getDevedorService().countContratoAluno(null));

		}

		return lazyListDataModel3;

	}

	public boolean isAlunoSelecionado() {
		return devedor.getId() != null ? true : false;
	}

	public String voltar() {
		return "index";
	}

	public String marcarLinha2(Aluno a) {
		String cor = "";

		if (a == null) {
			return "";
		}

		if (a.getRemovido() != null && a.getRemovido()) {
			cor = "marcarLinhaVermelho";
		} else {
			cor = "marcarLinha";
		}

		/*
		 * cor = "marcarLinhaVermelho"; cor = "marcarLinhaVerde"; cor =
		 * "marcarLinhaAmarelo"; cor = "marcarLinha"
		 */
		return cor;
	}

	public boolean temcontato(String contato) {
		if (contato != null && !contato.equalsIgnoreCase("")) {
			return true;
		}
		return false;
	}

	public String salvar() {
		// devedorService.save(devedor);
		Util.removeAtributoSessao("devedor");
		return "index";
	}

	public String editar(Long idprof) {
		// devedor = devedorService.findById(idprof);
		Util.addAtributoSessao("devedor", devedor);
		return "cadastrar";
	}

	public String visualizar(Long idprof) {
		alunoDevedor = alunoService.findById(idprof);
		Util.addAtributoSessao("aluno", alunoDevedor);
		return "cadastrar";
	}

	public void contactado(Aluno aluno) {
		aluno.setDataContato(new Date());
		aluno.setContactado(true);
		aluno.setQuantidadeContatos(alunoDevedor.getQuantidadeContatos() + 1);
		alunoService.saveContactado(alunoDevedor);

	}

	public Double getTotal(Aluno devedor) {
		Double total = 0D;
		if (devedor != null) {
			if (devedor.getContratoVigente() != null && !devedor.getContratoVigente().getBoletos().isEmpty()) {
				for (Boleto b : devedor.getContratoVigente().getBoletos()) {
					if (b.getAtrasado() != null && b.getAtrasado()) {
						total += Verificador.getValorFinal(b);
					}
				}
			}
		}

		return total;
	}

	public Double getTotal(ContratoAluno ca) {
		Double total = 0D;
		for (Boleto b : ca.getBoletos()) {
			if (b.getAtrasado() != null && b.getAtrasado()) {
				total += Verificador.getValorFinal(b);
			}
		}

		return total;
	}

	public String editar() {
		return editar(devedor.getId());
	}

	public String adicionarNovo() {
		return "cadastrar";
	}

	public String remover(Long idTurma) {
		getDevedorService().remover(idTurma);
		return "index";
	}

	public String remover(Boleto boleto) {
		devedor.getBoletos().remove(boleto);
		return "";
	}

	public double valorTotal(Aluno aluno, ContratoAluno contrato) {
		if (contrato != null && contrato.getNumeroParcelas() != null) {
			return contrato.getValorMensal() * contrato.getNumeroParcelas();
		} else {
			return 0;
		}
	}

	public StreamedContent gerarArquivoProtesto() {
		return gerarArquivoProtesto(contratoS);
	}

	public StreamedContent gerarArquivoProtesto(ContratoAluno ca) {
		enviarProtestoCartorio(ca);

		String nomeArquivo = "";
		if (ca != null && ca.getId() != null) {

			nomeArquivo = ca.getId() + "Z";
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
				ImpressoesUtils.gerarArquivoFisico(modeloArq, montarArquivoProtesto(ca), nomeArquivo);

			} catch (IOException e) {
				e.printStackTrace();
			}
			nomeArquivo += ".doc";
		} else {
			nomeArquivo = "modelo_protesto.docx";
		}

		String caminho = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/") + File.separator
				+ nomeArquivo;
		InputStream stream = null;
		try {
			stream = new FileInputStream(caminho);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return FileDownload.getContentDoc(stream, nomeArquivo);

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
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
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
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
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
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsdez", situacao);
			} else {
				trocas.put("dataVenc" + i, formatador.format(contratoAluno.getBoletos().get(i - 1).getVencimento()));
				trocas.put("valorOrig" + i, contratoAluno.getBoletos().get(i - 1).getValorNominal() + "");
				trocas.put("valorCorri" + i, verificaValorFinalBoleto(contratoAluno.getBoletos().get(i - 1)));

			}

			if (i == 9) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsnove", situacao);
			}
			if (i == 8) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsoito", situacao);
			}
			if (i == 7) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stssete", situacao);
			}
			if (i == 6) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsseis", situacao);
			}
			if (i == 5) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stscnco", situacao);
			}
			if (i == 4) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsquatro", situacao);
			}
			if (i == 3) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("ststres", situacao);
			}
			if (i == 2) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
					situacao = "QUITADO";
				} else {
					totalAberto += Verificador.getValorFinal(contratoAluno.getBoletos().get(i - 1));
				}
				trocas.put("stsdois", situacao);
			}
			if (i == 1) {
				String situacao = "ABERTO";
				if (Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1)).equals(StatusBoletoEnum.PAGO)
						|| Verificador.getStatusEnum(contratoAluno.getBoletos().get(i - 1))
								.equals(StatusBoletoEnum.CANCELADO)) {
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

	public String marcarLinha(ContratoAluno a) {
		String cor = "";
		if (a != null) {
			if (a.getPodeProtestarFinal() != null && a.getPodeProtestarFinal()) {
				cor = "marcarLinhaVerde";
				if (a.getEnviadoProtestoDefinitivo() != null && a.getEnviadoProtestoDefinitivo()) {
					cor = "marcarLinhaVermelho";
				}
			} else {
				if (a.getEnviadoProtestoDefinitivo() != null && a.getEnviadoProtestoDefinitivo()) {
					cor = "";
				}

				if (a.getEnviadoParaCobrancaCDL() != null && a.getEnviadoParaCobrancaCDL()) {
					if (a.getEnviadoSPC() != null && a.getEnviadoSPC()) {
						cor = "marcarLinhaVerde";
					}
				}

				// TODO VER
				// boolean estaNaTurma = alunoService.estaEmUmaTUrma(a.getId());
				// if (!estaNaTurma) {
				// cor = "marcarLinha";
				// }
			}
		}

		return cor;
	}

	public void protestar(Aluno al) {
		devedorService.enviarParaProtesto(al);
	}

	public void enviarProtesto(ContratoAluno ca) {
		devedorService.enviarParaProtesto(ca);
	}

	public void enviarPodeEnviarProtestoFina(ContratoAluno ca) {
		devedorService.enviarPodeEnviarProtestoFina(ca);
	}

	public void enviadoProtestoDefinitivo(ContratoAluno ca) {
		devedorService.enviadoProtestoDefinitivo(ca);
	}

	public void enviarPodeEnviarProtestoFina() {
		devedorService.enviarPodeEnviarProtestoFina(contratoS);
	}

	public void enviadoProtestoDefinitivo() {
		devedorService.enviadoProtestoDefinitivo(contratoS);
	}

	public boolean podeEnviar() {
		if (contratoS == null) {
			return false;
		}
		return !Util.nullOrTrue(contratoS.getPodeProtestarFinal());
	}

	public boolean podeEnviado() {
		if (contratoS == null) {
			return false;
		}
		return Util.nullOrTrue(contratoS.getPodeProtestarFinal())
				&& !Util.nullOrTrue(contratoS.getEnviadoProtestoDefinitivo());
	}

	public void saveObservavao(Aluno aluno) {
		devedorService.saveObservacao(aluno);
	}

	private Float maior(Float float1, Float float2) {
		return float1 > float2 ? float1 : float2;
	}

	public String verificaValorFinalBoleto(Boleto boleto) {
		return Util.formatarDouble2Decimais(Verificador.getValorFinal(boleto));
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

	public Devedor getDevedor() {
		return devedor;
	}

	public void setDevedor(Devedor devedor) {
		this.devedor = devedor;
	}

	public List<Devedor> getDevedores() {
		return devedores;
	}

	public void setDevedores(List<Devedor> devedores) {
		this.devedores = devedores;
	}

	public DevedorService getDevedorService() {
		return devedorService;
	}

	public void setDevedorService(DevedorService devedorService) {
		this.devedorService = devedorService;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public boolean isContratoSelecionado() {
		if (getContratoS() == null) {
			return false;
		}
		return getContratoS().getId() != null ? true : false;
	}

	public ContratoAluno getContratoS() {
		return contratoS;
	}

	public void setContratoS(ContratoAluno contratoS) {
		this.contratoS = contratoS;
	}

	public Aluno getAlunoDevedor() {
		return alunoDevedor;
	}

	public void setAlunoDevedor(Aluno alunoDevedor) {
		this.alunoDevedor = alunoDevedor;
	}

	public String getObservacaoAtrasado() {
		return observacaoAtrasado;
	}

	public void setObservacaoAtrasado(String observacaoAtrasado) {
		this.observacaoAtrasado = observacaoAtrasado;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public byte[] getBts() {
		return bts;
	}

	public void setBts(byte[] bts) {
		this.bts = bts;
	}

}
