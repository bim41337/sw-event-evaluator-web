package de.biersaecke.oth.event_evaluator.persistence.entities;

import de.biersaecke.oth.event_evaluator.persistence.utils.IncorrectEntityConfigurationException;
import de.biersaecke.oth.event_evaluator.persistence.utils.ZeitraumUtils;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 * Embeddable Bewertung für Einträge
 *
 * @author bim41337
 */
@Embeddable
public class Bewertung implements Serializable {

    private static final Long serialVersionUID = 0L;

    @Getter
    private Double wert;

    @Temporal( TemporalType.TIMESTAMP )
    @Getter
    @Setter
    private Date datum;

    @Getter
    @Setter
    private String notiz;

    public Bewertung() {
        datum = new Date();
    }

    public Bewertung(Double wert, Date datum, String notiz) {
        pruefeWert(wert);
        this.wert = wert;
        this.datum = datum;
        this.notiz = notiz;
    }

    public void setWert(Double wert) {
        pruefeWert(wert);
        this.wert = wert;
    }

    private void pruefeWert(Double neuerWert) throws IncorrectEntityConfigurationException {
        if (neuerWert != null) {
            if (neuerWert.compareTo(-1.) < 0 || neuerWert.compareTo(1.) > 0) {
                throw new IncorrectEntityConfigurationException(IncorrectEntityConfigurationException.ConfigErrorEnum.INVALID_RATING);
            }
        }
    }

    public String getDatumFormated() {
        return ZeitraumUtils.formatierenDatumNurTag(datum);
    }

    @Override
    public String toString() {
        return String.format("Bewertung vom %s: %.2f (%s)", ZeitraumUtils.
                formatierenDatumMitZeit(datum), wert, notiz);
    }

}
