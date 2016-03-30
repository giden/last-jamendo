package lastjam.front;

import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import lastjam.backend.Band;
import lastjam.backend.Person;
import lastjam.utils.ValidatorUtils;


public class BandForm{


	public interface SavedHandler<T> {

        void onSave(T entity);
    }

    public interface ResetHandler<T> {

        void onReset(T entity);
    }
    
    Person person;
	
    private Button saveButton;
    private Button resetButton;
    
    private SavedHandler<Band> savedHandler;
    private ResetHandler<Band> resetHandler;

    BeanItem<Band> item = new BeanItem<Band>(new Band());
    FieldGroup group = new FieldGroup(item);

    private Window popup;
	
	TextField name;
    TextField website;
    DateField formed;
	
	public BandForm(Band band, Person person) {
		
		this.person = person;
		
	    item = new BeanItem<Band>(band);
	    group = new FieldGroup(item);
	    
	    name = (TextField) group.buildAndBind("Name", "name");
	    website = (TextField) group.buildAndBind("Website", "website");
	    formed = (DateField) group.buildAndBind("Formed", "formed");
				
		name.addBlurListener(new AddBandValidatorBlurListener(name, "name"));
		website.addBlurListener(new AddBandValidatorBlurListener(website, "website"));
		formed.addBlurListener(new AddBandValidatorBlurListener(formed, "formed"));

		name.setNullRepresentation("");
		website.setNullRepresentation("");

		saveButton = new MButton(FontAwesome.SAVE, this::save);
	    resetButton = new MButton(FontAwesome.BAN, this::reset);

		
	}


	protected Component createContent() {
		return new MVerticalLayout(
                new MFormLayout(
                        name,
                        website,
                        formed
                ).withWidth(""),
                new MHorizontalLayout(
                        saveButton,
                        resetButton
                )
        ).withWidth("");
	}
	
	public void openInModalPopup() {
        popup = new Window("Edit entry", createContent());
        popup.setModal(true);
        UI.getCurrent().addWindow(popup);
    }
	
    public void setSavedHandler(SavedHandler<Band> savedHandler) {
        this.savedHandler = savedHandler;
    }

    public void setResetHandler(ResetHandler<Band> resetHandler) {
        this.resetHandler = resetHandler;
    }
    
    protected void save(Button.ClickEvent e) {
    	try {
    		ValidatorUtils.installSingleValidatorBand(name, "name");
    		ValidatorUtils.installSingleValidatorBand(website, "website");
    		ValidatorUtils.installSingleValidatorBand(formed, "formed");
    		
			group.commit();
		} catch (CommitException e1) {
			e1.printStackTrace();
		}
    	
    	Band band = item.getBean();
    	band.setPerson(person);
        savedHandler.onSave(band);
    }
    protected void reset(Button.ClickEvent e) {
        resetHandler.onReset(null);
    }




}
