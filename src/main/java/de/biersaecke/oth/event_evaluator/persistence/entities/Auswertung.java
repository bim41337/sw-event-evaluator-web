package de.biersaecke.oth.event_evaluator.persistence.entities;

import de.biersaecke.oth.event_evaluator.persistence.utils.Zeitraum;
import de.biersaecke.oth.event_evaluator.persistence.utils.ZeitraumUtils;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.DoubleStream;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * Entität für Auswertungen von Einträgen
 *
 * @author bim41337
 */
@Entity
@Table( name = "AUSWERTUNG" )
@NamedQueries( {
    @NamedQuery( name = Auswertung.NQ_PARAMS_AUSWERTUNG_NUTZER,
            query = "SELECT a FROM Auswertung a WHERE a.benutzer = :user" )
    ,
    @NamedQuery( name = Auswertung.NQ_PARAMS_AUSWERTUNG_EINTRAEGE,
            query = "SELECT COUNT(a.id) FROM Auswertung a WHERE :pEintrag MEMBER OF a.posten" )
} )
public class Auswertung extends AbstractEntity {

    private static final Long serialVersionUID = 0L;

    public static final String NQ_NAME_PREFIX = "Auswertung.";
    public static final String NQ_PARAMS_AUSWERTUNG_NUTZER = NQ_NAME_PREFIX + "auswertungenFuerBenutzer";
    public static final String NQ_PARAMS_AUSWERTUNG_EINTRAEGE = NQ_NAME_PREFIX + "auswertungenEintraege";

    @Embedded
    @Column( nullable = false )
    @Getter
    @Setter
    private Zeitraum zeitraum;

    @Transient
    private transient Double durchschnitt;

    @ManyToMany
    @JoinTable( name = "AUSWERTUNGSPOSTEN", joinColumns
            = @JoinColumn( name = "AUSWERTUNG" ),
            inverseJoinColumns = @JoinColumn( name = "EINTRAG" ) )
    private Set<Eintrag> posten = new HashSet<>();

    @ManyToOne( optional = false )
    @Getter
    @Setter
    private Benutzer benutzer;

    public Auswertung() {
    }

    public Auswertung(Zeitraum zeitraum) {
        this.zeitraum = zeitraum;
    }

    public Auswertung(Date zeitraumStart, Date zeitraumEnde) {
        this(new Zeitraum(zeitraumStart, zeitraumEnde));
    }

    public void errechneDurchschnitt() {
        DoubleStream ds = posten.stream().filter(e -> e.getBewertung() != null).
                mapToDouble(e -> e.getBewertung().getWert());

        durchschnitt = ds.average().orElse(0.0);
    }

    public Double getDurchschnitt() {
        return durchschnitt;
    }

    public Set<Eintrag> getPosten() {
        return Collections.unmodifiableSet(posten);
    }

    public void hinzufuegenPosten(Eintrag eintrag) {
        posten.add(eintrag);
    }

    public void entfernenPosten(Eintrag eintrag) {
        posten.remove(eintrag);
    }

    public String getZeitraumFormated() {
        return ZeitraumUtils
                .formatierenZeitraum(zeitraum.getStart(), zeitraum.getEnde(), false);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)",
                ZeitraumUtils.formatierenZeitraum(
                        zeitraum.getStart(),
                        zeitraum.getEnde(), false),
                ZeitraumUtils.formatierenDatumMitZeit(getCreationDate())
        );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.zeitraum);
        hash = 23 * hash + Objects.hashCode(this.posten);
        hash = 23 * hash + Objects.hashCode(this.benutzer);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Auswertung other = (Auswertung) obj;
        if (!Objects.equals(this.zeitraum, other.zeitraum)) {
            return false;
        }
        if (!Objects.equals(getCreationDate(), other.getCreationDate())) {
            return false;
        }
        if (!Objects.equals(this.benutzer, other.benutzer)) {
            return false;
        }

        return true;
    }

}
