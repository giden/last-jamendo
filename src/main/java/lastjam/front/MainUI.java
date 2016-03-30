package lastjam.front;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import lastjam.backend.Band;
import lastjam.backend.BandRepository;
import lastjam.backend.Person;
import lastjam.backend.PersonRepository;
import lastjam.utils.JSONUtils;


@Theme("valo")
@SpringUI
public class MainUI extends UI {


	private static final String getArtist = "http://api.jamendo.com/v3.0/artists/?client_id=9d9f42e3&name=";
	private static final String getTracks = "http://api.jamendo.com/v3.0/albums/tracks/?client_id=9d9f42e3&artist_name=";


	private static final long serialVersionUID = 8300584888422968386L;

	@Autowired
	BandRepository repo;
	
	@Autowired
	PersonRepository personRepo;
	
	Navigator navigator;
	private Table list;
	private Item bean;

	private BeanItemContainer<Band> bands = new BeanItemContainer<>(
			Band.class);



	@Override
	protected void init(VaadinRequest vaadinRequest) {
		navigator = new Navigator(this, this);

		navigator.addView("", new LoginView());
		navigator.addView("main", new MainView());
		navigator.addView("band", new BandView());
		
		navigator.addViewChangeListener(new ViewChangeListener() {

			private static final long serialVersionUID = 868340624307696054L;

			@Override
            public boolean beforeViewChange(ViewChangeEvent event) {

                boolean isLoggedIn = getSession().getAttribute("user") != null;
                boolean isLoginView = event.getNewView() instanceof LoginView;

                if (!isLoggedIn && !isLoginView) {
                    navigator.navigateTo("");
                    return false;

                } else if (isLoggedIn && isLoginView) {
                    navigator.navigateTo("main");
                    return false;
                }

                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {

            }
        });

		setNavigator(navigator);
	}

	public class MainView extends CustomComponent implements View {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1429267200253165939L;
		private Button addNew;
		private Button edit;
		private Button delete;
		private Button process;
		private Button synchro;
		private Button logout;



		public MainView() {
			
			list = new Table();
			list.setContainerDataSource(bands);
			list.setVisibleColumns("name", "website", "formed");
			list.setColumnHeaders("nazwa", "adres WWW", "data powstania");
			list.setWidth(100, Unit.PERCENTAGE);
			list.setSelectable(true);
			list.setImmediate(true);
									
			addNew = new MButton(FontAwesome.PLUS, this::add);
			edit = new MButton(FontAwesome.PENCIL_SQUARE_O, this::edit);
			delete = new ConfirmButton(FontAwesome.TRASH_O,
					"Jesteś pewny?", this::remove);
			process = new MButton(FontAwesome.ARROW_RIGHT, this::process);
			synchro = new ConfirmButton(FontAwesome.FIREFOX, "Synchronizować z JamendoAPI?",this::synchroWithJamendo);
			
			logout = new Button("Wyloguj", this::logout);
			
			Panel panel = new Panel();

			MVerticalLayout vl = new MVerticalLayout(
					logout,
					new RichText().withMarkDownResource("/welcome.md"),
					new MHorizontalLayout(addNew, edit, delete, process, synchro),
					list
					).expand(list);
			
			vl.setComponentAlignment(logout, Alignment.TOP_RIGHT);
			
			panel.setSizeFull();
			panel.setContent(vl);
			list.addValueChangeListener(e -> adjustActionButtonState());
			list.addItemClickListener(e -> {
				if (e.isDoubleClick()) {
					bean = e.getItem();
					navigator.navigateTo("band");

				}
			});
			setCompositionRoot(panel);
		}

		private void adjustActionButtonState() {
			boolean hasSelection = list.getValue() != null;
			edit.setEnabled(hasSelection);
			delete.setEnabled(hasSelection);
			process.setEnabled(hasSelection);
			synchro.setEnabled(hasSelection);
		}

		private void listEntities() {
			bands.removeAllItems();
			bands.addAll(repo.findByPerson((Person)getSession().getAttribute("user")));
			bands.sort(new Object[]{"name", "formed"}, new boolean[]{true, false});
		}

		public void add(Button.ClickEvent e) {
			edit(new Band());
		}

		public void edit(Button.ClickEvent e) {
			edit((Band)list.getValue());
		}

		protected void edit(final Band band) {
			BandForm bandForm = new BandForm(band, (Person)getSession().getAttribute("user"));
			bandForm.openInModalPopup();
			bandForm.setSavedHandler(this::saveEntry);
			bandForm.setResetHandler(this::resetEntry);
		}

		public void saveEntry(Band band){
			repo.save(band);
			listEntities();
			closeWindow();
		}

		public void resetEntry(Band band){
			listEntities();
			closeWindow();
		}


		protected void closeWindow() {
			getWindows().stream().forEach(w -> removeWindow(w));
		}

		public void remove(Button.ClickEvent e) {
			Person person = ((Band)list.getValue()).getPerson();
			person.removeBand((Band)list.getValue());
			
			personRepo.save(person);
			
			list.setValue(null);
			listEntities();
		}

		public void process(Button.ClickEvent e) {
			navigator.navigateTo("band");
		}

		public void synchroWithJamendo(Button.ClickEvent e) {
			String json = JSONUtils.downloadFileFromInternet(
					getArtist+((Band)list.getValue()).getName());


			JSONObject jsob = new JSONObject(json);

			JSONArray array = jsob.getJSONArray("results");

			if(array.isNull(0)){
				VerticalLayout vl = new VerticalLayout();

				Label label = new Label("Nie udało się. Czy podałeś właściwy zespół?");
				Button button = new Button("zamknij");
				button.addClickListener(e2 -> closeWindow());

				label.setHeight("7em");

				vl.addComponent(label);
				vl.addComponent(button);

				vl.setComponentAlignment(button, Alignment.BOTTOM_RIGHT);
				vl.setSpacing(true);
				vl.setMargin(true);

				Window popup = new Window("Edit entry", vl);
				popup.setModal(true);
				popup.setHeight("14em");
				popup.setWidth("25em");

				UI.getCurrent().addWindow(popup);
				return;
			}

			Band band = (Band) list.getValue();
			band.setWebsite(array.getJSONObject(0).getString("website"));
			try {
				band.setFormed(new SimpleDateFormat("yyyy-MM-dd").parse(array.getJSONObject(0).getString("joindate")));
			} catch (JSONException | ParseException e1) {
				e1.printStackTrace();
			}

			repo.save(band);
			listEntities();


		}
		
		public void logout(Button.ClickEvent e) {
			getSession().setAttribute("user", null);

            // Refresh this view, should redirect to login view
            navigator.navigateTo("");

		}


		@Override
		public void enter(ViewChangeEvent viewChangeEvent) {
			listEntities();
		}
	}

	public class BandView extends CustomComponent implements View {

		/**
		 * @throws IOException 
		 * @throws MalformedURLException 
		 * 
		 */
		
		Panel panel = new Panel();

		Label nameLabel;
		Link link = new Link();
		VerticalLayout layout;
		HorizontalLayout hl;

		Button button = new Button("Go to Main View",
				(Button.ClickListener) event -> {
					navigator.navigateTo("main");
				});

		private static final long serialVersionUID = 7239676187675446035L;

		public BandView() {
			setSizeFull();
			}

		@Override
		public void enter(ViewChangeEvent viewChangeEvent) {
			layout = new VerticalLayout();
			hl = new HorizontalLayout();
			hl.setWidth("100%");

			
			panel = new Panel();
			
			panel.setContent(layout);
			panel.setSizeFull();
			panel.getContent().setSizeUndefined();
			panel.getContent().setWidth("100%");
			setCompositionRoot(panel);
			
			String json = null;
			if(list.getValue()==null){
				if(bean.getItemProperty("website").getValue()!=null)
					link.setResource(new ExternalResource((String)bean.getItemProperty("website").getValue()));
				link.setCaption((String)bean.getItemProperty("name").getValue());
				nameLabel = new Label("<h1>"+(String)bean.getItemProperty("name").getValue()+"</h1>", ContentMode.HTML);

				if(bean.getItemProperty("name").getValue()!=null){
					json = JSONUtils.downloadFileFromInternet(
							getTracks+bean.getItemProperty("name").getValue());

				}
			}
			else{
				if(((Band)list.getValue()).getWebsite()!=null) 
					link.setResource(new ExternalResource(((Band)list.getValue()).getWebsite()));
				link.setCaption(((Band)list.getValue()).getName());
				nameLabel = new Label("<h1>"+((Band)list.getValue()).getName()+"</h1>", ContentMode.HTML);


				json = JSONUtils.downloadFileFromInternet(
						getTracks+((Band)list.getValue()).getName());

			}
			
			layout.addComponent(nameLabel);
			layout.addComponent(link);
			layout.addComponent(hl);


			
			if(json!=null){
				JSONObject jsob = new JSONObject(json);

				JSONArray array = jsob.getJSONArray("results");

				for(int j=0;j<array.length();j++){
					
					if(j%4==0){
						hl = new HorizontalLayout();
						hl.setWidth("100%");
						layout.addComponent(hl);
					}
					
					JSONArray arrayTracks = array.getJSONObject(j).getJSONArray("tracks");

					VerticalLayout albumVL = new VerticalLayout();
					albumVL.addComponent(new Label((j+1)+". "+array.getJSONObject(j).getString("name")));
					

					String audioName;
					String audioURL;
					
					for(int i=0;i<arrayTracks.length();i++){
						audioName = arrayTracks.getJSONObject(i).getString("name");
						audioURL = arrayTracks.getJSONObject(i).getString("audiodownload");

						albumVL.addComponent(new Link(audioName,new ExternalResource(audioURL)));
						albumVL.setMargin(true);

					}hl.addComponent(albumVL);
					
					
				}
			}
			layout.addComponent(button);
			layout.setComponentAlignment(button, Alignment.BOTTOM_CENTER);
		}



	}
	public class LoginView extends CustomComponent implements View, Button.ClickListener {

		private static final long serialVersionUID = 5468062162019822012L;
		
		private final TextField user;

		private final PasswordField password;

		private final Button loginButton;
		private final Button registerButton;
		
		Label message = new Label();

		public LoginView() {
			setSizeFull();

			user = new TextField("Użytkownik:");
			user.setWidth("300px");
			user.setRequired(true);
			user.setInputPrompt("name@sample.com");
			user.addValidator(new EmailValidator("Musi być email"));

			password = new PasswordField("Hasło:");
			password.setWidth("300px");
			password.setRequired(true);
			password.setValue("");
			password.setNullRepresentation("");

			loginButton = new Button("Login", this);
			registerButton = new Button("Register");
			registerButton.addClickListener(e -> edit());

			HorizontalLayout hl = new HorizontalLayout(loginButton, registerButton);
			hl.setSpacing(true);
			VerticalLayout fields = new VerticalLayout(user, password, hl, message);
			fields.setCaption("Zaloguj się");
			fields.setSpacing(true);
			fields.setMargin(new MarginInfo(true, true, true, false));
			fields.setSizeUndefined();

			VerticalLayout viewLayout = new VerticalLayout(fields);
			viewLayout.setSizeFull();
			viewLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
			viewLayout.setStyleName(Reindeer.LAYOUT_BLUE);
			setCompositionRoot(viewLayout);
		}
		
		protected void edit() {
			RegisterForm registerForm = new RegisterForm();
			registerForm.openInModalPopup();
			registerForm.setSavedHandler(this::save);
			registerForm.setResetHandler(this::reset);
		}

		public void save(Person p){
			personRepo.save(p);
			closeWindow();
		}

		public void reset(){
			closeWindow();
		}
		
		protected void closeWindow() {
			getWindows().stream().forEach(w -> removeWindow(w));
		}

		@Override
		public void enter(ViewChangeEvent event) {
			user.focus();
		}

		@Override
		public void buttonClick(ClickEvent event) {

			if (!user.isValid()) {
				message.setCaption(user.getErrorMessage().getFormattedHtmlMessage());
				message.setCaptionAsHtml(true);
				return;
			}
			if (!password.isValid()) {
				message.setCaption(password.getErrorMessage().getFormattedHtmlMessage());
				message.setCaptionAsHtml(true);
				return;
			}

			String username = user.getValue();
			String password = this.password.getValue();


			Person user = personRepo.findByUsername(username);
			

			if (user!=null && password.equals(user.getPassword())) {

				getSession().setAttribute("user", user);

				navigator.navigateTo("main");

			} else {
				message.setCaption("Brak usera lub złe hasło");
				this.password.setValue(null);
				this.password.focus();

			}
		}
	}


}
