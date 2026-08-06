package org.escola.controller.professor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

import org.escola.auth.AuthController;
import org.escolar.model.Aluno;
import org.escolar.model.Carro;
import org.escolar.model.Configuracao;
import org.escolar.model.Funcionario;
import org.escolar.model.Member;
import org.escolar.model.PagamentoFuncionario;
import org.escolar.service.AlunoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.PagamentoFuncionarioService;
import org.escolar.service.ProfessorService;
import org.escolar.service.PixWhitelistService;
import org.escolar.service.SicoobBoletoService;
import org.escolar.util.Util;

import br.com.aaf.base.base.EnviadorWhats;
import br.com.aaf.base.whats.model.Parametro;

@Named
@ViewScoped
public class ProfessorController extends AuthController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final double PIX_LIMITE_PADRAO = 5000.0;
	private static final long SETE_DIAS_MS = 7L * 24 * 60 * 60 * 1000;
	private static final long PIX_LIMITE_DIARIO = 11L;

	@Produces
	@Named
	private Funcionario professor;

	@Produces
	@Named
	private List<Funcionario> professores;

	@Inject
	private ProfessorService professorService;

	@Inject
	private AlunoService alunoService;

	@Inject
	private SicoobBoletoService sicoobBoletoService;

	@Inject
	private PagamentoFuncionarioService pagamentoFuncionarioService;

	@Inject
	private ConfiguracaoService configuracaoService;

	@Inject
	private PixWhitelistService pixWhitelistService;

	// --- estado do dialog de pagamento ---
	private Funcionario funcionarioParaPagar;
	private double valorPagamento;
	private String senhaConfirmacao;
	private List<PagamentoFuncionario> historicoPagamentos;

	@PostConstruct
	private void init() {
		if (professor == null) {
			Object objectSessao = Util.getAtributoSessao("professor");
			if (objectSessao != null) {
				professor = (Funcionario) objectSessao;
				Util.removeAtributoSessao("professor");
			} else {
				Member m = new Member();
				professor = new Funcionario();
				professor.setMember(m);
			}
		}
	}

	public List<Funcionario> getProfessores() {
		return professorService.findAll();
	}

	public List<Aluno> getAlunosDoProfessor() {
		List<Carro> turmasDosProfessor = professorService.findTurmaByProfessor(getLoggedUser().getId());
		return alunoService.findAlunoTurmaBytTurma(turmasDosProfessor);
	}

	public String salvar() {
		professor.setInicio(null);
		professor.setAtivo(true);
		professorService.save(professor);
		return "index";
	}

	public String voltar() {
		return "index";
	}

	public String editar(Long idprof) {
		professor = professorService.findById(idprof);
		Util.addAtributoSessao("professor", professor);
		return "cadastrar";
	}

	public String remover(Long idTurma) {
		professorService.remover(idTurma);
		return "index";
	}

	public String adicionarNovo() {
		return "cadastrar";
	}

	public String cadastrarNovo() {
		return "exibir";
	}

	// --- Pagamento Pix ---

	public void iniciarPagamento(Funcionario f) {
		String bloqueio = verificarBloqueios(f);
		if (bloqueio != null) {
			addError(bloqueio);
			return;
		}
		funcionarioParaPagar = f;
		senhaConfirmacao = "";
		historicoPagamentos = pagamentoFuncionarioService.findByFuncionario(f.getId());

		// pré-preenche com o salário cadastrado
		try {
			valorPagamento = Double.parseDouble(
					f.getSalario().replace(",", ".").replaceAll("[^0-9.]", ""));
		} catch (Exception ignored) {
			valorPagamento = 0;
		}

		org.primefaces.context.RequestContext.getCurrentInstance()
				.execute("PF('dlgPagamento').show()");
	}

	public void confirmarPagamento() {
		if (funcionarioParaPagar == null) return;

		Configuracao conf = configuracaoService.getConfiguracao();
		String erro = validarPagamento(conf);
		if (erro != null) {
			addError(erro);
			return;
		}

		PagamentoFuncionario registro = new PagamentoFuncionario();
		registro.setFuncionario(funcionarioParaPagar);
		registro.setDataPagamento(new Date());
		registro.setValor(valorPagamento);
		registro.setChavePix(funcionarioParaPagar.getChavePix());
		registro.setTipoChavePix(funcionarioParaPagar.getTipoChavePix());
		registro.setUsuarioAutorizou(getLoggedUser() != null ? getLoggedUser().getLogin() : "sistema");

		try {
			String descricao = "Salario " + funcionarioParaPagar.getNome()
					+ " " + new SimpleDateFormat("MM/yyyy").format(new Date());
			String[] resultado = sicoobBoletoService.pagarViaPix(conf,
					funcionarioParaPagar.getChavePix(),
					funcionarioParaPagar.getTipoChavePix(),
					valorPagamento, descricao);

			registro.setEndToEndId(resultado[0]);
			registro.setNomeProprietarioSicoob(resultado[1]);
			registro.setStatus("SUCESSO");
			pagamentoFuncionarioService.salvar(registro);
			pagamentoFuncionarioService.atualizarUltimoPagamento(funcionarioParaPagar, new Date());

			enviarWhatsAppFuncionario(funcionarioParaPagar, valorPagamento, resultado[0]);

			String nomeConfirmado = resultado[1] != null && !resultado[1].isEmpty()
					? " — destinatário confirmado: " + resultado[1] : "";
			addInfo("Pagamento de R$ " + String.format("%.2f", valorPagamento)
					+ " enviado com sucesso via Pix!" + nomeConfirmado
					+ " EndToEnd: " + resultado[0]);
			funcionarioParaPagar = null;

		} catch (Exception e) {
			registro.setStatus("ERRO");
			registro.setObservacao(e.getMessage());
			pagamentoFuncionarioService.salvar(registro);
			addError("Erro ao enviar Pix: " + e.getMessage());
		}
	}

	private String verificarBloqueios(Funcionario f) {
		if (f.getChavePix() == null || f.getChavePix().trim().isEmpty())
			return "Chave Pix não cadastrada para " + f.getNome() + ". Edite o cadastro primeiro.";

		Date agora = new Date();
		if (f.getDataCadastro() != null && (agora.getTime() - f.getDataCadastro().getTime()) < SETE_DIAS_MS)
			return "Funcionário cadastrado há menos de 7 dias. Pagamento bloqueado por segurança.";

		if (f.getDataEdicaoChavePix() != null && (agora.getTime() - f.getDataEdicaoChavePix().getTime()) < SETE_DIAS_MS)
			return "Chave Pix alterada recentemente. Pagamento bloqueado por 7 dias após alteração.";

		if (pagamentoFuncionarioService.temPagamentoUltimos7Dias(f.getId()))
			return "Já existe um pagamento registrado nos últimos 7 dias para " + f.getNome() + ".";

		if (pagamentoFuncionarioService.contarPagamentosHoje() >= PIX_LIMITE_DIARIO)
			return "Limite diário de " + PIX_LIMITE_DIARIO + " pagamentos via Pix atingido. Tente novamente amanhã.";

		return null;
	}

	private String validarPagamento(Configuracao conf) {
		if (senhaConfirmacao == null || senhaConfirmacao.isEmpty())
			return "Informe a senha de autorização.";

		String senhaEsperada = conf.getPixSenha();
		if (senhaEsperada == null || !senhaEsperada.equals(senhaConfirmacao))
			return "Senha de autorização incorreta.";

		if (valorPagamento <= 0)
			return "Informe um valor válido maior que zero.";

		double limite = conf.getPixValorMaximo() != null ? conf.getPixValorMaximo() : PIX_LIMITE_PADRAO;
		boolean isento = funcionarioParaPagar.getChavePix() != null
				&& pixWhitelistService.isChaveIsenta(funcionarioParaPagar.getChavePix());
		if (!isento && valorPagamento > limite)
			return "Valor excede o limite máximo permitido de R$ " + String.format("%.2f", limite) + ".";

		try {
			double salario = Double.parseDouble(
					funcionarioParaPagar.getSalario().replace(",", ".").replaceAll("[^0-9.]", ""));
			if (valorPagamento > salario)
				return "Valor excede o salário cadastrado de R$ " + String.format("%.2f", salario) + ".";
		} catch (Exception ignored) { /* salario nao numerico, ignora validacao */ }

		return null;
	}

	private void enviarWhatsAppFuncionario(Funcionario f, double valor, String endToEndId) {
		try {
			String telefone = f.getTelefone2() != null ? f.getTelefone2() : f.getTelefone1();
			if (telefone == null || telefone.trim().isEmpty()) return;
			List<Parametro> params = new ArrayList<>();
			params.add(new Parametro("1", f.getNome()));
			params.add(new Parametro("2", String.format("%.2f", valor)));
			params.add(new Parametro("3", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));
			params.add(new Parametro("4", endToEndId));
			EnviadorWhats.enviarWhats("pagamento_salario", telefone, params);
		} catch (Exception ignored) { /* WhatsApp é best-effort */ }
	}

	public List<PagamentoFuncionario> getHistoricoPagamentos() {
		return historicoPagamentos;
	}

	public Funcionario getFuncionarioParaPagar() { return funcionarioParaPagar; }
	public void setFuncionarioParaPagar(Funcionario f) { this.funcionarioParaPagar = f; }

	public double getValorPagamento() { return valorPagamento; }
	public void setValorPagamento(double valorPagamento) { this.valorPagamento = valorPagamento; }

	public String getSenhaConfirmacao() { return senhaConfirmacao; }
	public void setSenhaConfirmacao(String senhaConfirmacao) { this.senhaConfirmacao = senhaConfirmacao; }

	private void addError(String msg) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
	}

	private void addInfo(String msg) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
	}
}
