package lastjam.front;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Field;

import lastjam.utils.ValidatorUtils;

public class AddBandValidatorBlurListener implements BlurListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4855585206361488677L;
	private Field<?> field;
    private String attribute;
 
    public AddBandValidatorBlurListener(Field<?> field, String attribute) {
 
        this.field = field;
        this.attribute = attribute;
    }
	
	@Override
	public void blur(BlurEvent event) {
        ValidatorUtils.installSingleValidatorBand(field, attribute);
	}

}
