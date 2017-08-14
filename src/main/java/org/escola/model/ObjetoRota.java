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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.escola.enums.EscolaEnum;
import org.escola.enums.PegarEntregarEnun;
import org.escola.enums.PerioddoEnum;

@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "id"))
public class ObjetoRota implements Serializable , Comparable<ObjetoRota>{

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Size(min = 1, max = 250)
    private String nome;
    
    @ManyToOne
    private Aluno aluno;
    
    private EscolaEnum escola;
    
    private Long idCarro;
    
    private Long idCarroAlvo;
    
    private PerioddoEnum periodo;
    
    private int quantidadeAlunos;
    
    @Column(columnDefinition="TEXT")
    private String descricao;
    
    @Column
    private PegarEntregarEnun pegarEntregar;
    
    @Column
    private int posicao;
    
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		ObjetoRota other = (ObjetoRota) obj;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} 
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		return true;

	}
	
	@Override
	public String toString() {
		return nome;
	}

	public EscolaEnum getEscola() {
		return escola;
	}

	public void setEscola(EscolaEnum escola) {
		this.escola = escola;
	}

	public int getQuantidadeAlunos() {
		return quantidadeAlunos;
	}

	public void setQuantidadeAlunos(int quantidadeAlunos) {
		this.quantidadeAlunos = quantidadeAlunos;
	}

	public int getPosicao() {
		return posicao;
	}

	public void setPosicao(int posicao) {
		this.posicao = posicao;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public PegarEntregarEnun getPegarEntregar() {
		return pegarEntregar;
	}

	public void setPegarEntregar(PegarEntregarEnun pegarEntregar) {
		this.pegarEntregar = pegarEntregar;
	}

	@Override
	public int compareTo(ObjetoRota o) {
		if(posicao < o.posicao){
			return -1;
		}
		if(posicao > o.posicao){
			return 1;
		}
		
		return 0;
	}

	public Long getIdCarro() {
		return idCarro;
	}

	public void setIdCarro(Long idCarro) {
		this.idCarro = idCarro;
	}

	public PerioddoEnum getPeriodo() {
		return periodo;
	}

	public void setPeriodo(PerioddoEnum periodo) {
		this.periodo = periodo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Long getIdCarroAlvo() {
		return idCarroAlvo;
	}

	public void setIdCarroAlvo(Long idCarroAlvo) {
		this.idCarroAlvo = idCarroAlvo;
	}
		
}
