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
package org.escola.controller.turma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escola.auth.AuthController;
import org.escolar.enums.BimestreEnum;
import org.escolar.enums.DisciplinaEnum;
import org.escolar.enums.PerioddoEnum;
import org.escolar.enums.Serie;
import org.escolar.enums.TipoMembro;
import org.escolar.model.Aluno;
import org.escolar.model.AlunoAvaliacao;
import org.escolar.model.Avaliacao;
import org.escolar.model.Carro;
import org.escolar.model.Funcionario;
import org.escolar.service.AlunoService;
import org.escolar.service.AvaliacaoService;
import org.escolar.service.ProfessorService;
import org.escolar.service.TurmaService;
import org.escolar.util.Util;
import org.primefaces.event.DragDropEvent;
import org.primefaces.model.DualListModel;

@Named
@ViewScoped
public class TurmaController extends AuthController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Produces
	@Named
	private Carro turma;

	@Inject
	private AvaliacaoService avaliacaoService;

	private float valorSelecionado;

	private int totalAlunos = 0;
	
	@Named
	private DisciplinaEnum disciplinaSelecionada;

	@Named
	private BimestreEnum bimestreSelecionado;

	@Inject
	private TurmaService turmaService;

	@Inject
	private ProfessorService professorService;

	@Inject
	private AlunoService alunoService;
	
	//private List<AlunoAvaliacao> alunoAvaliacaoPortugues;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoPortugues;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoIngles;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoMatematica;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoHistoria;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoEDFisica;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoGeografia;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoCiencias;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoFormacaoCrista;
	private Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoArtes;

	private DualListModel<Funcionario> professores;

	private DualListModel<Aluno> alunos;
	/* private DualListModel<Professor> professores; */
	
	private List<AlunoAvaliacao> alunosAvaliacao;

	@PostConstruct
	private void init() {
		// TODO SOMENTE CARREGAR OS PICKLISTA pra quem tem permissao de ver
		if (turma == null) {
			Object objectSessao = Util.getAtributoSessao("turma");
			if (objectSessao != null) {
				turma = (Carro) objectSessao;
				Util.removeAtributoSessao("turma");
			} else {
				turma = new Carro();
			}
		}

		montarPickListProfessor();
	}
	
	public List<Carro> getAlunosDisponiveis() {
		
		//Carro
		//Periodo
		if (getLoggedUser().getProfessor() != null) {
			return turmaService.findAll(getLoggedUser().getProfessor().getId());

		} else {
			return turmaService.findAll();
		}
	}

	 public void onCarDrop(DragDropEvent ddEvent) {
	        Carro car = ((Carro) ddEvent.getData());
	  
	   //     droppedCars.add(car);
	    //    cars.remove(car);
	    }
	
		private void montarPickListProfessor() {
		/** MONTANDO O PICKLIST */
		List<Funcionario> todosProfessores = professorService.findAll();
		List<Funcionario> professoresDisponiveis = new ArrayList<>();
		List<Funcionario> professoresSelecionados = getProfessoresSelecionados();

		professoresDisponiveis.addAll(todosProfessores);
		professoresDisponiveis.removeAll(professoresSelecionados);

		professores = new DualListModel<Funcionario>(professoresDisponiveis, professoresSelecionados);
	}

	public int getTotalAlunos(){
		return this.totalAlunos;
	}
	
	public void verificarTodosAlunosTemAvaliacao(Long idTurma){
		List<Aluno> alunosTurma = alunoService.findAlunoTurmaBytTurma(idTurma);
		if(alunosTurma != null && !alunosTurma.isEmpty()){
			List<Avaliacao> avaliacoesTurma = avaliacaoService.find(alunosTurma.get(0).getSerie(), null);
			for(Aluno aluno :alunosTurma){
				for(Avaliacao avaliacao :avaliacoesTurma){
					List<AlunoAvaliacao> alav = avaliacaoService.findAlunoAvaliacaoby(aluno.getId(), avaliacao.getId(), null, null, null); 
					if(alav== null || alav.isEmpty()){
						avaliacaoService.createAlunoAvaliacao(aluno,avaliacao);
					}
				}
			}	
		}
		
	}
	
	private List<Funcionario> getProfessoresSelecionados() {

		return turma.getId() != null ? professorService.findFuncionarioCarroBytTurma(turma.getId())
				: new ArrayList<Funcionario>();
	}

	private List<Aluno> getAlunosSelecionados() {

		return turma.getId() != null ? alunoService.findAlunoTurmaBytTurma(turma.getId()) : new ArrayList<Aluno>();
	}
	
	public float getNota(Aluno aluno, DisciplinaEnum disciplina, BimestreEnum  bimestre){
		return alunoService.getNota(aluno.getId(), disciplina, bimestre, false);
	}

	public List<Carro> getTurmas() {
		if (getLoggedUser().getProfessor() != null) {
			return turmaService.findAll(getLoggedUser().getProfessor().getId());

		} else {
			return turmaService.findAll();
		}
	}

	public String salvar() {
		turma = turmaService.save(turma);
		saveProfessorTurma();
		verificarTodosAlunosTemAvaliacao(turma.getId());

		return "index";
	}

	public String editar(Long idTurma) {
		turma = turmaService.findById(idTurma);
		Util.addAtributoSessao("turma", turma);
		return "cadastrar";
	}

	
	public String editarRotaManha(Long idTurma) {
		turma = turmaService.findById(idTurma);
		Util.addAtributoSessao("carro", turma);
		Util.addAtributoSessao("periodo", PerioddoEnum.MANHA);
		return "cadastrar";
	}
	
	public String editarRotaMeioDia(Long idTurma) {
		turma = turmaService.findById(idTurma);
		Util.addAtributoSessao("carro", turma);
		Util.addAtributoSessao("periodo", PerioddoEnum.INTEGRAL);
		return "cadastrar";
	}
	
	public String editarRotaTarde(Long idTurma) {
		turma = turmaService.findById(idTurma);
		Util.addAtributoSessao("carro", turma);
		Util.addAtributoSessao("periodo", PerioddoEnum.TARDE);
		return "cadastrar";
	}
	
	
	public String voltar() {
		return "index";
	}

	public String remover(Long idTurma) {
		turmaService.remove(idTurma);
		return "index";
	}

	private void saveProfessorTurma() {
		professorService.saveProfessorTurma(professores.getTarget(), turma);
	}

	public String adicionarNovo() {
		return "cadastrar";
	}

	public String cadastrarNovo() {

		return "exibir";
	}

	public DualListModel<Funcionario> getProfessores() {
		return professores;
	}

	public void setProfessores(DualListModel<Funcionario> professores) {
		this.professores = professores;
	}

	public DualListModel<Aluno> getAlunos() {
		return alunos;
	}

	public void setAlunos(DualListModel<Aluno> alunos) {
		this.alunos = alunos;
	}

	public DisciplinaEnum getDisciplinaSelecionada() {
		return disciplinaSelecionada;
	}

	public void setDisciplinaSelecionada(DisciplinaEnum disciplinaSelecionada) {
		this.disciplinaSelecionada = disciplinaSelecionada;
	}

	public BimestreEnum getBimestreSelecionado() {
		return bimestreSelecionado;
	}

	public void setBimestreSelecionado(BimestreEnum bimestreSelecionado) {
		this.bimestreSelecionado = bimestreSelecionado;
	}

	public float getValorSelecionado() {
		return valorSelecionado;
	}

	public void setValorSelecionado(float valorSelecionado) {
		this.valorSelecionado = valorSelecionado;
	}

	public void test(Long id, float nota) {
		System.out.println(id);
		System.out.println(nota);
	}

	public void saveAvaliacaoAluno(AlunoAvaliacao alav) {
		avaliacaoService.saveAlunoAvaliacao(alav);
	}

	public void saveAvaliacaoAluno(Long idAluAv, Float nota) {
		avaliacaoService.saveAlunoAvaliacao(idAluAv, nota);
	}

	public List<AlunoAvaliacao> getAlunoAvaliacoes(Long idAluno, Long idAvaliacao, DisciplinaEnum disciplina,
			BimestreEnum bimestre, Serie serie) {

		setAlunosAvaliacao(avaliacaoService.findAlunoAvaliacaoby(idAluno, idAvaliacao, disciplina, bimestre, serie));
		return getAlunosAvaliacao();
	}

	public List<AlunoAvaliacao> getAlunosAvaliacao() {
		return alunosAvaliacao;
	}

	public void setAlunosAvaliacao(List<AlunoAvaliacao> alunosAvaliacao) {
		this.alunosAvaliacao = alunosAvaliacao;
	}

	public boolean renderDisciplina(int ordinal){
		if(getLoggedUser() != null && !getLoggedUser().getTipoMembro().equals(TipoMembro.MOTORISTA) ){
			return false;
		}
		
		if(disciplinaSelecionada == null){
			return false;
		}		
		return ordinal== disciplinaSelecionada.ordinal();
	}
	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoPortugues() {
		return alunoAvaliacaoPortugues;
	}
	
	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoIngles() {
		return alunoAvaliacaoIngles;
	}
	
	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoEdFisica() {
		return alunoAvaliacaoEDFisica;
	}
	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoGeografia() {
		return alunoAvaliacaoGeografia;
	}
	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoHistoria() {
		return alunoAvaliacaoHistoria;
	}
	
	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoMatematica() {
		return alunoAvaliacaoMatematica;
	}
	
	public List<AlunoAvaliacao> getAlunosAvaliacao(Aluno al){
		return alunoAvaliacaoPortugues.get(al);
	}

	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoCiencias() {
		return alunoAvaliacaoCiencias;
	}

	public void setAlunoAvaliacaoCiencias(Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoCiencias) {
		this.alunoAvaliacaoCiencias = alunoAvaliacaoCiencias;
	}

	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoFormacaoCrista() {
		return alunoAvaliacaoFormacaoCrista;
	}

	public void setAlunoAvaliacaoFormacaoCrista(Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoFormacaoCrista) {
		this.alunoAvaliacaoFormacaoCrista = alunoAvaliacaoFormacaoCrista;
	}

	public Map<Aluno,List<AlunoAvaliacao>> getAlunoAvaliacaoArtes() {
		return alunoAvaliacaoArtes;
	}

	public void setAlunoAvaliacaoArtes(Map<Aluno,List<AlunoAvaliacao>> alunoAvaliacaoArtes) {
		this.alunoAvaliacaoArtes = alunoAvaliacaoArtes;
	}
	public BimestreEnum getPrimeiroBimestre(){
		return BimestreEnum.PRIMEIRO_BIMESTRE;
	}
	
	public BimestreEnum getSegundoBimestre(){
		return BimestreEnum.SEGUNDO_BIMESTRE;
	}
	
	public BimestreEnum getTerceiroBimestre(){
		return BimestreEnum.TERCEIRO_BIMESTRE;
	}
	
	public BimestreEnum getQuartoBimestre(){
		return BimestreEnum.QUARTO_BIMESTRE;
	}
	
	public DisciplinaEnum getPortugues(){
		return DisciplinaEnum.PORTUGUES;
	}
	
	public DisciplinaEnum getMatematica(){
		return DisciplinaEnum.MATEMATICA;
	}
	
	public DisciplinaEnum getHistoria(){
		return DisciplinaEnum.HISTORIA;
	}
	
	public DisciplinaEnum getIngles(){
		return DisciplinaEnum.INGLES;
	}
	
	public DisciplinaEnum getEDFisica(){
		return DisciplinaEnum.EDUCACAO_FISICA;
	}
	
	public DisciplinaEnum getGeografia(){
		return DisciplinaEnum.GEOGRAFIA;
	}
	
	public DisciplinaEnum getCiencias(){
		return DisciplinaEnum.CIENCIAS;
	}
	
	public DisciplinaEnum getFormacaoCrista(){
		return DisciplinaEnum.FORMACAO_CRISTA;
	}
	
	public DisciplinaEnum getArtes(){
		return DisciplinaEnum.ARTES;
	}

	public void setTotalAlunos(int totalAlunos) {
		this.totalAlunos = totalAlunos;
	}

}
