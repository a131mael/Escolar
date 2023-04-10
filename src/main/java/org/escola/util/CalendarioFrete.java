package org.escola.util;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;

import org.escolar.model.Frete;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.ScheduleEvent;

public class CalendarioFrete  extends SelectEvent implements ScheduleEvent {

	private String id;
	
	private Frete data;
	
	private String title;
	
	private Date startDate;
	
	private Date endDate;
	
	private String description;
	
	
	public CalendarioFrete(UIComponent component, Behavior behavior, Object object) {
		super(component, behavior, object);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public boolean isAllDay() {
		return false;
	}

	@Override
	public String getStyleClass() {
		return null;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
