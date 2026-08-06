package org.escola.controller;

import br.com.aaf.base.base.Constantes;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import org.escolar.model.Abastecimento;
import org.escolar.model.Configuracao;
import org.escolar.service.AbastecimentoService;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.SicoobBoletoService;

@Named
@ViewScoped
public class AbastecimentoController implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String RUDIPEL_CHAVE_PIX = "75415075000132";
    private static final String RUDIPEL_TIPO_PIX  = "CNPJ";
    private static final String ARIEL_WHATSAPP    = "48999664943";

    @Inject private AbastecimentoService abastecimentoService;
    @Inject private ConfiguracaoService configuracaoService;
    @Inject private SicoobBoletoService sicoobBoletoService;

    private List<Abastecimento> abastecimentos;

    private static final int LIMITE_DIARIO_ABASTECIMENTOS = 3;

    // form fields
    private Date dataAbastecimento = new Date();
    private BigDecimal litros = new BigDecimal("1000");
    private BigDecimal valorPorLitro;
    private String senhaConfirmacao;

    // filtro
    private int mesFiltro;
    private int anoFiltro;

    @PostConstruct
    public void init() {
        carregarLista();
        if (valorPorLitro == null) {
            BigDecimal ultimo = abastecimentoService.getUltimoValorPorLitro();
            if (ultimo != null) valorPorLitro = ultimo;
        }
        Calendar c = Calendar.getInstance();
        if (mesFiltro == 0) mesFiltro = c.get(Calendar.MONTH) + 1;
        if (anoFiltro == 0) anoFiltro  = c.get(Calendar.YEAR);
    }

    private void carregarLista() {
        abastecimentos = abastecimentoService.findAll();
    }

    public String novoAbastecimento() {
        dataAbastecimento = new Date();
        return "novo?faces-redirect=true";
    }

    public String voltar() {
        return "index?faces-redirect=true";
    }

    public void realizarAbastecimento() {
        if (litros == null || litros.compareTo(BigDecimal.ZERO) <= 0) {
            addError("Informe a quantidade de litros."); return;
        }
        if (valorPorLitro == null || valorPorLitro.compareTo(BigDecimal.ZERO) <= 0) {
            addError("Informe o valor por litro."); return;
        }

        Configuracao conf = configuracaoService.getConfiguracao();

        // Validar senha de autorização
        String senhaEsperada = conf.getPixSenha();
        if (senhaConfirmacao == null || senhaConfirmacao.trim().isEmpty()) {
            addError("Informe a senha de autorização PIX."); return;
        }
        if (!senhaConfirmacao.equals(senhaEsperada)) {
            addError("Senha de autorização incorreta."); return;
        }

        BigDecimal valorTotal = litros.multiply(valorPorLitro).setScale(2, RoundingMode.HALF_UP);

        // Validar limite de valor — abastecimento tem limite 1,5x o limite geral
        double limiteValor = (conf.getPixValorMaximo() != null ? conf.getPixValorMaximo() : 50000.0) * 1.5;
        if (valorTotal.doubleValue() > limiteValor) {
            addError("Valor total R$ " + valorTotal + " excede o limite máximo de R$ "
                    + String.format("%.2f", limiteValor) + "."); return;
        }

        // Validar limite diário
        long abastecimentosHoje = abastecimentoService.contarAbastecimentosHoje();
        if (abastecimentosHoje >= LIMITE_DIARIO_ABASTECIMENTOS) {
            addError("Limite de " + LIMITE_DIARIO_ABASTECIMENTOS
                    + " abastecimentos por dia atingido. Tente amanhã."); return;
        }
        Abastecimento a = new Abastecimento();
        a.setData(dataAbastecimento != null ? dataAbastecimento : new Date());
        a.setLitros(litros);
        a.setValorPorLitro(valorPorLitro);
        a.setValorTotal(valorTotal);
        a.setStatusPix("PENDENTE");
        abastecimentoService.save(a);

        try {
            String descricao = String.format("Abastecimento %.0f litros Favo de Mel", litros);
            String[] resultado = sicoobBoletoService.pagarViaPix(
                    conf, RUDIPEL_CHAVE_PIX, RUDIPEL_TIPO_PIX,
                    valorTotal.doubleValue(), descricao);
            a.setEndToEndId(resultado[0]);
            a.setNomeDestinatario(resultado[1]);
            a.setStatusPix("PAGO");
        } catch (Exception e) {
            a.setStatusPix("ERRO");
            a.setErroPix(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            System.err.println("[ABASTECIMENTO] Erro PIX: " + e.getMessage());
            abastecimentoService.save(a);
            addError("Erro no PIX: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
            return;
        }

        // salva PAGO imediatamente antes de tentar o PDF
        try {
            abastecimentoService.save(a);
        } catch (Exception e) {
            System.err.println("[ABASTECIMENTO] Falha ao salvar PAGO: " + e.getMessage());
        }

        try {
            byte[] pdf = abastecimentoService.gerarComprovantePdf(a);
            a.setComprovantePdf(pdf);
            try {
                abastecimentoService.save(a);
            } catch (Exception e) {
                System.err.println("[ABASTECIMENTO] Falha ao salvar PDF: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[ABASTECIMENTO] Falha ao gerar PDF: " + e.getMessage());
        }

        try {
            enviarWhatsApp(a);
            a.setWhatsappEnviado(true);
            abastecimentoService.save(a);
        } catch (Exception ignored) { }

        Locale br = new Locale("pt", "BR");
        NumberFormat nf = NumberFormat.getInstance(br);
        nf.setMinimumFractionDigits(2); nf.setMaximumFractionDigits(2);
        addInfo("Abastecimento realizado! PIX de R$ " + nf.format(valorTotal)
                + " enviado para " + (a.getNomeDestinatario() != null ? a.getNomeDestinatario() : "Rudipel")
                + ". EndToEnd: " + a.getEndToEndId());

        try {
            FacesContext.getCurrentInstance().getExternalContext()
                .redirect("index.jsf");
        } catch (Exception ignored) { }
    }

    private void enviarWhatsApp(Abastecimento a) throws Exception {
        Locale br = new Locale("pt", "BR");
        NumberFormat nf = NumberFormat.getInstance(br);
        nf.setMinimumFractionDigits(2); nf.setMaximumFractionDigits(2);
        NumberFormat nfL = NumberFormat.getInstance(br);
        nfL.setMinimumFractionDigits(0); nfL.setMaximumFractionDigits(2);

        String dataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", br).format(a.getData());
        String texto = "Abastecimento realizado! ✅\n"
                + nfL.format(a.getLitros()) + " litros — R$ " + nf.format(a.getValorTotal()) + "\n"
                + "Preco/litro: R$ " + nf.format(a.getValorPorLitro()) + "\n"
                + "Data: " + dataHora + "\n"
                + "EndToEnd: " + a.getEndToEndId();

        String watiUrl = Constantes.URL;
        String token   = Constantes.TOKEN;
        String phone   = "55" + ARIEL_WHATSAPP.replaceAll("\\D", "");

        URL urlText = new URL(watiUrl + "/api/v1/sendSessionMessage/" + phone
                + "?whatsappNumber=" + phone);
        HttpURLConnection connText = (HttpURLConnection) urlText.openConnection();
        connText.setRequestMethod("POST");
        connText.setRequestProperty("Authorization", token);
        connText.setRequestProperty("Content-Type", "application/json");
        connText.setDoOutput(true);
        String jsonBody = "{\"messageText\":\"" + texto.replace("\"", "'").replace("\n", "\\n") + "\"}";
        try (OutputStream os = connText.getOutputStream()) {
            os.write(jsonBody.getBytes("UTF-8"));
        }
        connText.getResponseCode();

        if (a.getComprovantePdf() != null) {
            String boundary = "----Boundary" + System.currentTimeMillis();
            URL urlFile = new URL(watiUrl + "/api/v1/sendFile/" + phone + "?whatsappNumber=" + phone);
            HttpURLConnection connFile = (HttpURLConnection) urlFile.openConnection();
            connFile.setRequestMethod("POST");
            connFile.setRequestProperty("Authorization", token);
            connFile.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connFile.setDoOutput(true);
            String filename = "comprovante_abastecimento_"
                    + new SimpleDateFormat("ddMMyyyy_HHmm").format(a.getData()) + ".pdf";
            try (OutputStream os = connFile.getOutputStream()) {
                String part = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n"
                        + "Content-Type: application/pdf\r\n\r\n";
                os.write(part.getBytes("UTF-8"));
                os.write(a.getComprovantePdf());
                os.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));
            }
            connFile.getResponseCode();
        }
    }

    public void baixarComprovante(Abastecimento a) {
        if (a.getComprovantePdf() == null) {
            // gera e salva antes de baixar
            try {
                byte[] pdf = abastecimentoService.gerarComprovantePdf(a);
                a.setComprovantePdf(pdf);
                abastecimentoService.save(a);
            } catch (Exception e) {
                addError("Erro ao gerar comprovante: " + e.getMessage());
                return;
            }
        }
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
            String filename = "comprovante_abastecimento_"
                    + new SimpleDateFormat("ddMMyyyy_HHmm").format(a.getData()) + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(a.getComprovantePdf().length);
            response.getOutputStream().write(a.getComprovantePdf());
            response.getOutputStream().flush();
            fc.responseComplete();
        } catch (Exception e) {
            addError("Erro ao baixar: " + e.getMessage());
        }
    }

    // --- filtros e totais ---

    public List<Abastecimento> getAbastecimentosFiltrados() {
        if (abastecimentos == null) return new ArrayList<>();
        return abastecimentos.stream().filter(a -> {
            if (a.getData() == null) return false;
            Calendar c = Calendar.getInstance();
            c.setTime(a.getData());
            boolean mesOk  = mesFiltro == 0 || (c.get(Calendar.MONTH) + 1) == mesFiltro;
            boolean anoOk  = anoFiltro == 0 || c.get(Calendar.YEAR) == anoFiltro;
            return mesOk && anoOk;
        }).collect(Collectors.toList());
    }

    public BigDecimal getTotalLitrosFiltrado() {
        return getAbastecimentosFiltrados().stream()
                .filter(a -> "PAGO".equals(a.getStatusPix()) && a.getLitros() != null)
                .map(Abastecimento::getLitros)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalValorFiltrado() {
        return getAbastecimentosFiltrados().stream()
                .filter(a -> "PAGO".equals(a.getStatusPix()) && a.getValorTotal() != null)
                .map(Abastecimento::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Integer> getAnosDisponiveis() {
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        List<Integer> anos = new ArrayList<>();
        for (int i = anoAtual; i >= anoAtual - 3; i--) anos.add(i);
        return anos;
    }

    public BigDecimal getValorTotal() {
        if (litros == null || valorPorLitro == null) return BigDecimal.ZERO;
        return litros.multiply(valorPorLitro).setScale(2, RoundingMode.HALF_UP);
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Abastecimento", msg));
    }
    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    public List<Abastecimento> getAbastecimentos() { return abastecimentos; }
    public Date getDataAbastecimento() { return dataAbastecimento; }
    public void setDataAbastecimento(Date d) { this.dataAbastecimento = d; }
    public BigDecimal getLitros() { return litros; }
    public void setLitros(BigDecimal l) { this.litros = l; }
    public BigDecimal getValorPorLitro() { return valorPorLitro; }
    public void setValorPorLitro(BigDecimal v) { this.valorPorLitro = v; }
    public String getSenhaConfirmacao() { return senhaConfirmacao; }
    public void setSenhaConfirmacao(String s) { this.senhaConfirmacao = s; }
    public int getMesFiltro() { return mesFiltro; }
    public void setMesFiltro(int m) { this.mesFiltro = m; }
    public int getAnoFiltro() { return anoFiltro; }
    public void setAnoFiltro(int a) { this.anoFiltro = a; }
}
