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
package org.escola.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.escola.enums.EscolaEnum;
import org.escola.enums.PerioddoEnum;
import org.escola.enums.Serie;

@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "id"))
public class Aluno implements Serializable {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column
    private int anoLetivo;
    
    @Column
    private Boolean removido;

    /**DADOS DO ALUNO*/
    @NotNull
    @Size(min = 1, max = 250)
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String nomeAluno;
    
    @OneToMany
    private List<AlunoCarro> alunosCarros;
    
    @OneToMany
    private List<AlunoAvaliacao> avaliacoes;
    
    @Column
    private String login;
    
    @Column
    private String codigo;

    @OneToOne
    private Member member;
    
    @Column
    private String senha;
    
    @NotNull
    private Serie serie;
    
    @Column
    private String endereco;
    
    @Column
    private String bairro;
    
    @Column
    private String cep;
    
    @Column
    private String cidade;
    
    @NotNull
    private PerioddoEnum periodo;
    
    @Column
    private Double anuidade;
    
    @Column
    private Integer numeroParcelas;
    
    @Column
    private String nomeResponsavel;
    
    @Column
    private String cpfResponsavel;
    
    @Column
    private double valorMensal;
    
    @Column
    private String telefone;
    
    @Column
    private Date dataMatricula;

    @Column
    private Date dataNascimento;
    
    @Column
    private EscolaEnum escola;
    
    @Column
    private boolean trocaIDA;
    @Column
    private boolean trocaVolta;
    
    @ManyToOne
    private Carro carroLevaParaEscola;
    
    @ManyToOne
    private Carro carroLevaParaEscolaTroca;
    
    @ManyToOne
    private Carro carroPegaEscola;
    
    @ManyToOne
    private Carro carroPegaEscolaTroca;
    
    @Column
    private int idaVolta;
    
    @Column
    private String horaPegar;
    
    @Column
    private String horaEntregar;
    
    
    /**DADOS DO PAI*/
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String nomePaiAluno;
    
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String nomeAvoPaternoPai;
    
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String nomeAvoHPaternoPai;
    
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String naturalidadePai;
    
    @Column
    private String cpfPai;
    
    @Column
    private String rgPai;
        
    @Column
    private String telefoneCelularPai;
    
    @Column
    private String telefoneResidencialPai;
    
    @Column
    private String emailPai;
    
    @Column
    private String empresaTrabalhaPai;
    
    @Column
    private String telefoneEmpresaTrabalhaPai;
    
    /**DADOS DA MAE*/
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String nomeMaeAluno;
    
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String nomeAvoPaternoMae;
    
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String nomeAvoHPaternoMae;
    
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String naturalidadeMae;
    
    @Column
    private String cpfMae;
    
    @Column
    private String rgMae;
    
    @Column
    private String telefoneCelularMae;
    
    @Column
    private String telefoneResidencialMae;
    
    @Column
    private String emailMae;
    
    @Column
    private String empresaTrabalhaMae;
    
    @Column
    private String telefoneEmpresaTrabalhaMae;
    
    /*DADOS DE CONTATOS PARA SAIDAS*/

    
    @Column
    private boolean ativo;
    
    @Column
    private String observacaoSecretaria;

    @Column
    private String observacaoMotorista;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Serie getSerie() {
		return serie;
	}
	
	public void setSerie(Serie serie) {
		this.serie = serie;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	
	
	public String getTelefoneEmpresaTrabalhaMae() {
		return telefoneEmpresaTrabalhaMae;
	}

	public void setTelefoneEmpresaTrabalhaMae(String telefoneEmpresaTrabalhaMae) {
		this.telefoneEmpresaTrabalhaMae = telefoneEmpresaTrabalhaMae;
	}

	public String getEmpresaTrabalhaMae() {
		return empresaTrabalhaMae;
	}

	public void setEmpresaTrabalhaMae(String empresaTrabalhaMae) {
		this.empresaTrabalhaMae = empresaTrabalhaMae;
	}

	public String getEmailMae() {
		return emailMae;
	}

	public void setEmailMae(String emailMae) {
		this.emailMae = emailMae;
	}

	public String getTelefoneResidencialMae() {
		return telefoneResidencialMae;
	}

	public void setTelefoneResidencialMae(String telefoneResidencialMae) {
		this.telefoneResidencialMae = telefoneResidencialMae;
	}

	public String getTelefoneCelularMae() {
		return telefoneCelularMae;
	}

	public void setTelefoneCelularMae(String telefoneCelularMae) {
		this.telefoneCelularMae = telefoneCelularMae;
	}

	public String getRgMae() {
		return rgMae;
	}

	public void setRgMae(String rgMae) {
		this.rgMae = rgMae;
	}

	public String getCpfMae() {
		return cpfMae;
	}

	public void setCpfMae(String cpfMae) {
		this.cpfMae = cpfMae;
	}

	public String getNaturalidadeMae() {
		return naturalidadeMae;
	}

	public void setNaturalidadeMae(String naturalidadeMae) {
		this.naturalidadeMae = naturalidadeMae;
	}

	public String getNomeAvoHPaternoMae() {
		return nomeAvoHPaternoMae;
	}

	public void setNomeAvoHPaternoMae(String nomeAvoHPaternoMae) {
		this.nomeAvoHPaternoMae = nomeAvoHPaternoMae;
	}

	public String getNomeAvoPaternoMae() {
		return nomeAvoPaternoMae;
	}

	public void setNomeAvoPaternoMae(String nomeAvoPaternoMae) {
		this.nomeAvoPaternoMae = nomeAvoPaternoMae;
	}

	public String getNomeMaeAluno() {
		return nomeMaeAluno;
	}

	public void setNomeMaeAluno(String nomeMaeAluno) {
		this.nomeMaeAluno = nomeMaeAluno;
	}

	public String getTelefoneEmpresaTrabalhaPai() {
		return telefoneEmpresaTrabalhaPai;
	}

	public void setTelefoneEmpresaTrabalhaPai(String telefoneEmpresaTrabalhaPai) {
		this.telefoneEmpresaTrabalhaPai = telefoneEmpresaTrabalhaPai;
	}

	public String getEmpresaTrabalhaPai() {
		return empresaTrabalhaPai;
	}

	public void setEmpresaTrabalhaPai(String empresaTrabalhaPai) {
		this.empresaTrabalhaPai = empresaTrabalhaPai;
	}

	public String getEmailPai() {
		return emailPai;
	}

	public void setEmailPai(String emailPai) {
		this.emailPai = emailPai;
	}

	public String getTelefoneResidencialPai() {
		return telefoneResidencialPai;
	}

	public void setTelefoneResidencialPai(String telefoneResidencialPai) {
		this.telefoneResidencialPai = telefoneResidencialPai;
	}

	public String getTelefoneCelularPai() {
		return telefoneCelularPai;
	}

	public void setTelefoneCelularPai(String telefoneCelularPai) {
		this.telefoneCelularPai = telefoneCelularPai;
	}

	public String getRgPai() {
		return rgPai;
	}

	public void setRgPai(String rgPai) {
		this.rgPai = rgPai;
	}

	public String getCpfPai() {
		return cpfPai;
	}

	public void setCpfPai(String cpfPai) {
		this.cpfPai = cpfPai;
	}

	public String getNaturalidadePai() {
		return naturalidadePai;
	}

	public void setNaturalidadePai(String naturalidadePai) {
		this.naturalidadePai = naturalidadePai;
	}

	public String getNomeAvoHPaternoPai() {
		return nomeAvoHPaternoPai;
	}

	public void setNomeAvoHPaternoPai(String nomeAvoHPaternoPai) {
		this.nomeAvoHPaternoPai = nomeAvoHPaternoPai;
	}

	public String getNomeAvoPaternoPai() {
		return nomeAvoPaternoPai;
	}

	public void setNomeAvoPaternoPai(String nomeAvoPaternoPai) {
		this.nomeAvoPaternoPai = nomeAvoPaternoPai;
	}

	public String getNomePaiAluno() {
		return nomePaiAluno;
	}

	public void setNomePaiAluno(String nomePaiAluno) {
		this.nomePaiAluno = nomePaiAluno;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getNomeAluno() {
		return nomeAluno;
	}

	public void setNomeAluno(String nomeAluno) {
		this.nomeAluno = nomeAluno;
	}

	public PerioddoEnum getPeriodo() {
		return periodo;
	}

	public void setPeriodo(PerioddoEnum periodo) {
		this.periodo = periodo;
	}

	public Double getAnuidade() {
		return anuidade;
	}

	public void setAnuidade(double anuidade) {
		this.anuidade = anuidade;
	}

	public Integer getNumeroParcelas() {
		return numeroParcelas;
	}

	public void setNumeroParcelas(Integer numeroParcelas) {
		this.numeroParcelas = numeroParcelas;
	}

	public double getValorMensal() {
		return valorMensal;
	}

	public void setValorMensal(double valorMensal) {
		this.valorMensal = valorMensal;
	}

	public Date getDataMatricula() {
		return dataMatricula;
	}

	public void setDataMatricula(Date dataMatricula) {
		this.dataMatricula = dataMatricula;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public List<AlunoAvaliacao> getAvaliacoes() {
		return avaliacoes;
	}

	public void setAvaliacoes(List<AlunoAvaliacao> avaliacoes) {
		this.avaliacoes = avaliacoes;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Aluno other = (Aluno) obj;
		if (nomeAluno == null) {
			if (other.nomeAluno != null)
				return false;
		} if (serie == null) {
			if (other.serie != null)
				return false;
		} else if (!periodo.equals(other.periodo))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		return true;

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

	public String getObservacaoSecretaria() {
		return observacaoSecretaria;
	}

	public void setObservacaoSecretaria(String observacaoSecretaria) {
		this.observacaoSecretaria = observacaoSecretaria;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public int getAnoLetivo() {
		return anoLetivo;
	}

	public void setAnoLetivo(int anoLetivo) {
		this.anoLetivo = anoLetivo;
	}

	public Boolean getRemovido() {
		return removido;
	}

	public void setRemovido(Boolean removido) {
		this.removido = removido;
	}

	public boolean isTrocaIDA() {
		return trocaIDA;
	}

	public void setTrocaIDA(boolean trocaIDA) {
		this.trocaIDA = trocaIDA;
	}

	public boolean isTrocaVolta() {
		return trocaVolta;
	}

	public void setTrocaVolta(boolean trocaVolta) {
		this.trocaVolta = trocaVolta;
	}

	public Carro getCarroLevaParaEscola() {
		return carroLevaParaEscola;
	}

	public void setCarroLevaParaEscola(Carro carroLevaParaEscola) {
		this.carroLevaParaEscola = carroLevaParaEscola;
	}

	public Carro getCarroLevaParaEscolaTroca() {
		return carroLevaParaEscolaTroca;
	}

	public void setCarroLevaParaEscolaTroca(Carro carroLevaParaEscolaTroca) {
		this.carroLevaParaEscolaTroca = carroLevaParaEscolaTroca;
	}

	public Carro getCarroPegaEscola() {
		return carroPegaEscola;
	}

	public void setCarroPegaEscola(Carro carroPegaEscola) {
		this.carroPegaEscola = carroPegaEscola;
	}

	public Carro getCarroPegaEscolaTroca() {
		return carroPegaEscolaTroca;
	}

	public void setCarroPegaEscolaTroca(Carro carroPegaEscolaTroca) {
		this.carroPegaEscolaTroca = carroPegaEscolaTroca;
	}

	public int getIdaVolta() {
		return idaVolta;
	}

	public void setIdaVolta(int idaVolta) {
		this.idaVolta = idaVolta;
	}

	public String getHoraPegar() {
		return horaPegar;
	}

	public void setHoraPegar(String horaPegar) {
		this.horaPegar = horaPegar;
	}

	public String getHoraEntregar() {
		return horaEntregar;
	}

	public void setHoraEntregar(String horaEntregar) {
		this.horaEntregar = horaEntregar;
	}

	public EscolaEnum getEscola() {
		return escola;
	}

	public void setEscola(EscolaEnum escola) {
		this.escola = escola;
	}
}
