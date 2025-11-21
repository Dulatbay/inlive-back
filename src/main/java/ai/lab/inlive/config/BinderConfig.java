package ai.lab.inlive.config;

import ai.lab.inlive.entities.enums.DictionaryKey;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class BinderConfig {

    @InitBinder
    public void init(WebDataBinder binder) {
        binder.registerCustomEditor(List.class, "keys", new CustomCollectionEditor(List.class) {
            @Override
            protected Object convertElement(Object element) {
                if (element == null) return null;
                String raw = String.valueOf(element).trim();
                raw = raw.replaceAll("[\\[\\]\"]", "");
                return Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> DictionaryKey.valueOf(s.toUpperCase()))
                        .toList();
            }
        });
    }
}
