package lastjam.front;

import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

import lastjam.backend.Person;


public class RegisterForm implements Validator{
	
	private static final long serialVersionUID = 3623591129363627932L;
	
	public interface SavedHandler<T> {

		void onSave(T entity);
	}

	public interface ResetHandler<T> {

		void onReset();
	}


	private Button saveButton;
	private Button resetButton;

	private SavedHandler<Person> savedHandler;
	private ResetHandler<Person> resetHandler;

	BeanItem<Person> item = new BeanItem<Person>(new Person());
	FieldGroup group = new FieldGroup(item);

	private Window popup;

	TextField name;
	TextField password;
	TextField password2;
	Label message = new Label();


	public RegisterForm() {

		item = new BeanItem<Person>(new Person());
		group = new FieldGroup(item);

		name = (TextField) group.buildAndBind("Login", "username");
		password = (TextField) group.buildAndBind("Hasło", "password");
		password2 = new TextField();
		password2.setCaption("Powtórz hasło");

		name.addValidator(new BeanValidator(Person.class, "username"));
		password.addValidator(new BeanValidator(Person.class, "password"));
		password2.addValidator(this);
		password2.setImmediate(true);

		name.setNullRepresentation("");
		password.setNullRepresentation("");

		saveButton = new MButton(FontAwesome.SAVE, this::save);
		resetButton = new MButton(FontAwesome.BAN, this::reset);


	}

	@Override
	public void validate(Object value) throws InvalidValueException {
		if (!isValid(value))
			throw new InvalidValueException("Hasła nie pasują"); 		
	}

	public boolean isValid(Object value) {
		if(value.equals(password.getValue())) {
			return true;
		} else {
			return false;
		}
	} 

	protected Component createContent() {
		return new MVerticalLayout(
				new MFormLayout(
						name,
						password,
						password2
						).withWidth(""),
				new MHorizontalLayout(
						saveButton,
						resetButton
						),
				message
				).withWidth("");
	}

	public void openInModalPopup() {
		popup = new Window("Zarejestuj się", createContent());
		popup.setModal(true);
		UI.getCurrent().addWindow(popup);
		name.focus();
	}

	public void setSavedHandler(SavedHandler<Person> savedHandler) {
		this.savedHandler = savedHandler;
	}

	public void setResetHandler(ResetHandler<Person> resetHandler) {
		this.resetHandler = resetHandler;
	}

	protected void save(Button.ClickEvent e) {
		try {
			if (!name.isValid()) {
				message.setCaption(name.getErrorMessage().getFormattedHtmlMessage());
				message.setCaptionAsHtml(true);
				return;
			}
			if (!password.isValid()) {
				message.setCaption(password.getErrorMessage().getFormattedHtmlMessage());
				message.setCaptionAsHtml(true);
				return;
			}
			if (!password2.isValid()) {
				message.setCaption(password2.getErrorMessage().getFormattedHtmlMessage());
				message.setCaptionAsHtml(true);
				return;
			}
			
			group.commit();
		} catch (CommitException e1) {
			e1.printStackTrace();
		}
		savedHandler.onSave(item.getBean());
	}
	protected void reset(Button.ClickEvent e) {
		resetHandler.onReset();
	}
}
