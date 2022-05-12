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
package org.escola.controller.extrato;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.escolar.model.extrato.ItemExtrato;
import org.escolar.service.ConfiguracaoService;
import org.escolar.service.ExtratoBancarioService;

import br.com.aaf.base.constantes.ConstantesEXTRATO;
import br.com.aaf.base.importacao.extrato.ExtratoTiposEntradaEnum;
import br.com.aaf.base.importacao.extrato.ExtratoTiposEntradaSaidaEnum;
import br.com.aaf.base.util.OfficeUtil;

@Named
@ViewScoped
public class ExtratoController implements Serializable {

	@Inject
	private ConfiguracaoService configuracaoService;
	
	@Inject
	private ExtratoBancarioService extratoBancarioService;
	
	@PostConstruct
	public void init() {
	}

		

	public static void main(String[] args) {
		ExtratoController extrato = new ExtratoController();
	//	extrato.lerExtrato("C:\\Extrato");
	}

}
