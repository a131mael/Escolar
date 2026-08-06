package org.escola.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.model.Frete;
import org.escolar.model.PassageiroViagem;
import org.escolar.service.FreteService;
import org.escolar.service.PassageiroViagemService;
import org.escola.validator.CPFValidator;

@Named
@ViewScoped
public class ViagemPublicController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private FreteService freteService;

    @Inject
    private PassageiroViagemService passageiroService;

    private Frete viagem;
    private PassageiroViagem novoPassageiro = new PassageiroViagem();
    private boolean cadastrado = false;
    private String tokenParam;

    @PostConstruct
    public void init() {
        tokenParam = FacesContext.getCurrentInstance()
            .getExternalContext().getRequestParameterMap().get("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            viagem = freteService.findByTokenPublico(tokenParam);
        }
    }

    public void cadastrarOutro() {
        cadastrado = false;
    }

    public void cadastrar() {
        if (viagem == null) {
            addErro("Viagem não encontrada.");
            return;
        }
        String cpfLimpo = novoPassageiro.getCpf().replaceAll("[^0-9]", "");
        if (!CPFValidator.isCPF(cpfLimpo)) {
            addErro("CPF inválido. Verifique os números digitados.");
            return;
        }
        if (passageiroService.cpfJaCadastrado(viagem.getId(), cpfLimpo)) {
            addErro("Este CPF já está cadastrado nesta viagem.");
            return;
        }
        novoPassageiro.setCpf(cpfLimpo);
        novoPassageiro.setFrete(viagem);
        passageiroService.save(novoPassageiro);
        cadastrado = true;
        novoPassageiro = new PassageiroViagem();
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public Frete getViagem() { return viagem; }
    public PassageiroViagem getNovoPassageiro() { return novoPassageiro; }
    public void setNovoPassageiro(PassageiroViagem novoPassageiro) { this.novoPassageiro = novoPassageiro; }
    public boolean isCadastrado() { return cadastrado; }
    public void setCadastrado(boolean cadastrado) { this.cadastrado = cadastrado; }
    public String getTokenParam() { return tokenParam; }
    public void setTokenParam(String tokenParam) { this.tokenParam = tokenParam; }
}
