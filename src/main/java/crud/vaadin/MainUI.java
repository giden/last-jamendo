package crud.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;

import crud.backend.Band;
import crud.backend.BandRepository;

import java.io.InputStream;
import java.net.URL;
import org.json.JSONArray;
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


    /**
     * 
     * 
     * 
     * 
     */
		private static final long serialVersionUID = 8300584888422968386L;

	@Autowired
    BandRepository repo;

    Navigator navigator;
	private MTable<Band> list;


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        navigator.addView("", new MainView());
        navigator.addView("band", new BandView());

        setNavigator(navigator);
    }

    @SpringView(name = "")
    public class MainView extends CustomComponent implements View {

        /**
		 * 
		 */
		private static final long serialVersionUID = -1429267200253165939L;
        private Button addNew;
        private Button edit;
        private Button delete;
        private Button process;



        public MainView() {

            list = new MTable<>(Band.class)
                    .withProperties("id", "name", "website", "formed")
                    .withColumnHeaders("id", "name", "website", "formed")
                    .setSortableProperties("name")
                    .withFullWidth();
            addNew = new MButton(FontAwesome.PLUS, this::add);
            edit = new MButton(FontAwesome.PENCIL_SQUARE_O, this::edit);
            delete = new ConfirmButton(FontAwesome.TRASH_O,
                    "Are you sure you want to delete the entry?", this::remove);
            process = new MButton(FontAwesome.ARROW_RIGHT, this::process);

            Panel panel = new Panel();

            MVerticalLayout vl = new MVerticalLayout(
                            new RichText().withMarkDownResource("/welcome.md"),
                            new MHorizontalLayout(addNew, edit, delete, process),
                            list
                    ).expand(list);
            panel.setSizeFull();
            panel.setContent(vl);
            listEntities();
            list.addMValueChangeListener(e -> adjustActionButtonState());
            setCompositionRoot(panel);
        }

        private void adjustActionButtonState() {
            boolean hasSelection = list.getValue() != null;
            edit.setEnabled(hasSelection);
            delete.setEnabled(hasSelection);
            process.setEnabled(hasSelection);
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

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

        }
    }

    @SpringView(name = "band")
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
            link.setResource(new ExternalResource(getBandWWW(list.getValue().getName())));
            link.setCaption(list.getValue().getName());
        }

        public String getBandWWW(String name) {
            String json = downloadFileFromInternet("http://api.jamendo.com/v3.0/artists/?client_id=9d9f42e3&name="+name);

            JSONObject jsob = new JSONObject(json);

            JSONArray array = jsob.getJSONArray("results");
            
            Band band = list.getValue();
            band.setWebsite(array.getJSONObject(0).getString("website"));
            repo.save(band);
            
            return band.getWebsite();

        }
        private String downloadFileFromInternet(String url)
        {
            if(url == null || url.isEmpty() == true)
                throw new IllegalArgumentException("url is empty/null");
            StringBuilder sb = new StringBuilder();
            InputStream inStream = null;
            try
            {
                url = urlEncode(url);
                URL link = new URL(url);
                inStream = link.openStream();
                int i;
                int total = 0;
                byte[] buffer = new byte[8 * 1024];
                while((i=inStream.read(buffer)) != -1)
                {
                    if(total >= (1024 * 1024))
                    {
                        return "";
                    }
                    total += i;
                    sb.append(new String(buffer,0,i));
                }
            }
            catch(Exception e )
            {
                e.printStackTrace();
                return null;
            }catch(OutOfMemoryError e)
            {
                e.printStackTrace();
                return null;
            }
            return sb.toString();
        }

        private String urlEncode(String url)
        {
            url = url.replace("[","");
            url = url.replace("]","");
            url = url.replaceAll(" ","%20");
            return url;
        }

    }


}
