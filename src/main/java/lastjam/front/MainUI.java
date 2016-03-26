package lastjam.front;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Item;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;

import lastjam.backend.Band;
import lastjam.backend.BandRepository;
import lastjam.utils.JSONUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;


    @Theme("valo")
    @SpringUI
    public class MainUI extends UI {


    	private static final String getArtist = "http://api.jamendo.com/v3.0/artists/?client_id=9d9f42e3&name=";
    	
    	
		private static final long serialVersionUID = 8300584888422968386L;

	@Autowired
    BandRepository repo;

    Navigator navigator;
	private MTable<Band> list;
	private Item bean;



    @Override
    protected void init(VaadinRequest vaadinRequest) {
        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        navigator.addView("", new MainView());
        navigator.addView("band", new BandView());

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



        public MainView() {

            list = new MTable<>(Band.class)
                    .withProperties("id", "name", "website", "formed")
                    .withColumnHeaders("id", "name", "website", "formed")
                    .setSortableProperties("name")
                    .withFullWidth();
            addNew = new MButton(FontAwesome.PLUS, this::add);
            edit = new MButton(FontAwesome.PENCIL_SQUARE_O, this::edit);
            delete = new ConfirmButton(FontAwesome.TRASH_O,
                    "Jesteś pewny?", this::remove);
            process = new MButton(FontAwesome.ARROW_RIGHT, this::process);
            synchro = new ConfirmButton(FontAwesome.FIREFOX, "Synchronizować z JamendoAPI?",this::synchroWithJamendo);

            Panel panel = new Panel();

            MVerticalLayout vl = new MVerticalLayout(
                            new RichText().withMarkDownResource("/welcome.md"),
                            new MHorizontalLayout(addNew, edit, delete, process, synchro),
                            list
                    ).expand(list);
            panel.setSizeFull();
            panel.setContent(vl);
            listEntities();
            list.addMValueChangeListener(e -> adjustActionButtonState());
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
            list.setBeans(repo.findAll());
        }

        public void add(Button.ClickEvent e) {
            edit(new Band());
        }

		public void edit(Button.ClickEvent e) {
            edit(list.getValue());
        }

        protected void edit(final Band band) {
			BandForm bandForm = new BandForm(band);
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
            repo.delete(list.getValue());
            list.setValue(null);
            listEntities();
        }

        public void process(Button.ClickEvent e) {
            navigator.navigateTo("band");
        }
        
        public void synchroWithJamendo(Button.ClickEvent e) {
            String json = JSONUtils.downloadFileFromInternet(
						getArtist+list.getValue().getName());
			

            JSONObject jsob = new JSONObject(json);

            JSONArray array = jsob.getJSONArray("results");
            
            if(array.isNull(0)){
            	VerticalLayout vl = new VerticalLayout();
            	
            	Label label = new Label("Nie udało się. Czy podałeś właściwy zespół?");
            	Button button = new Button("zamknij");
            	button.addClickListener(e2 -> closeWindow());
            	
            	label.setHeight("10em");
            			
            	vl.addComponent(label);
            	vl.addComponent(button);
            	
            	vl.setComponentAlignment(button, Alignment.BOTTOM_RIGHT);
            	
            	
            	Window popup = new Window("Edit entry", vl);
                popup.setModal(true);
                popup.setHeight("15em");
                popup.setWidth("25em");

                UI.getCurrent().addWindow(popup);
                return;
            }
            
            Band band = list.getValue();
            band.setWebsite(array.getJSONObject(0).getString("website"));
            try {
				band.setFormed(new SimpleDateFormat("yyyy-MM-dd").parse(array.getJSONObject(0).getString("joindate")));
			} catch (JSONException | ParseException e1) {
				e1.printStackTrace();
			}
            
            repo.save(band);
            listEntities();
            

        	}


        @Override
        public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

        }
    }

    public class BandView extends VerticalLayout implements View {

        /**
         * @throws IOException 
         * @throws MalformedURLException 
		 * 
		 */
    	
    	Label label = new Label();
    	Link link = new Link();
		private static final long serialVersionUID = 7239676187675446035L;

		public BandView() {
            setSizeFull();

           Button button = new Button("Go to Main View",
                    (Button.ClickListener) event -> {
                        navigator.navigateTo("");
                    });
            addComponent(label);
            addComponent(link);

            addComponent(button);
            setComponentAlignment(button, Alignment.BOTTOM_LEFT);

        }

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        	if(list.getValue()==null){
        		if(bean.getItemProperty("website")!=null)
        		link.setResource(new ExternalResource((String)bean.getItemProperty("website").getValue()));
        		link.setCaption((String)bean.getItemProperty("name").getValue());
        	}
        	else{
        		if(list.getValue().getWebsite()!=null) 
        			link.setResource(new ExternalResource(list.getValue().getWebsite()));
        		link.setCaption(list.getValue().getName());
        	}
        }



    }


}
