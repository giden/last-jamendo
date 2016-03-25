package crud.vaadin;

import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;

import crud.backend.Band;

public class BandForm extends AbstractForm<Band>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	TextField name = new MTextField("Name");
    TextField website = new MTextField("Website");
    DateField formed = new DateField("Formed");
	
	public BandForm(Band band) {
		setSizeUndefined();
		setEntity(band);
	}

	@Override
	protected Component createContent() {
		return new MVerticalLayout(
                new MFormLayout(
                        name,
                        website,
                        formed
                ).withWidth(""),
                getToolbar()
        ).withWidth("");
	}

}
