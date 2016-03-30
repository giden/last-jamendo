package lastjam.utils;

import java.util.Collection;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.ui.Field;

import lastjam.backend.Band;

public class ValidatorUtils {
	 
    private ValidatorUtils() {}
     
    public static void installSingleValidatorBand(Field<?> field, String attribute) {
         
        Collection<Validator> validators = field.getValidators();
 
        if (validators == null || validators.isEmpty()) {
 
            field.addValidator(new BeanValidator(Band.class, attribute));
        }
    }
}