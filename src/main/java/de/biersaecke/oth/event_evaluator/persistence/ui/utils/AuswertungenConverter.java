package de.biersaecke.oth.event_evaluator.persistence.ui.utils;

import de.biersaecke.oth.event_evaluator.persistence.entities.Auswertung;
import de.biersaecke.oth.event_evaluator.persistence.services.AuswertungService;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralUtils;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Converter für Dropdown-Auswahl von Auswertungen
 *
 * @author bim41337
 */
@FacesConverter( "de.biersaecke.oth.event_evaluator.persistence.ui.utils.AuswertungenConverter" )
public class AuswertungenConverter implements Converter {

    @Setter
    private AuswertungService auswertungService;

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String idString) {
        if (StringUtils.isEmpty(idString)) {
            return null;
        }

        Auswertung auswertung;
        try {
            auswertung = auswertungService.holenAuswertung(Long
                    .parseLong(idString));
        } catch (RuntimeException ex) {
            return null;
        }

        return auswertung;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic,
            Object auswertungObj) {
        if (auswertungObj == null || !Auswertung.class.equals(auswertungObj
                .getClass())) {
            return GeneralUtils.EMPTY_STRING;
        }

        return ((Auswertung) auswertungObj).getId().toString();
    }

}
