package de.biersaecke.oth.event_evaluator.persistence.entities;

import de.biersaecke.oth.event_evaluator.persistence.utils.ZeitraumUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

/**
 * Entität der Kalendereinträge
 *
 * @author bim41337
 */
@Entity
@Table( name = "EINTRAG" )
@Inheritance( strategy = InheritanceType.JOINED )
@DiscriminatorColumn( name = "EINTRAG_TYP" )
@DiscriminatorValue( "E" )
@NamedQueries( {
    @NamedQuery( name = Eintrag.NQ_PARAMS_EINTRAEGE_ORT_ZEITRAUM_OEFFENTLICH,
            query = "SELECT evt FROM Eintrag evt WHERE lower(evt.ort) like lower(concat('%', :oName, '%')) AND (evt.start <= :zEnde AND evt.ende >= :zStart) AND evt.oeffentlich = TRUE" )
    ,
    @NamedQuery( name = Eintrag.NQ_PARAMS_EINTRAEGE_ZEITRAUM_OEFFENTLICH,
            query = "SELECT evt FROM Eintrag evt WHERE ((evt.start <= :zEnde AND evt.ende >= :zStart) AND evt.oeffentlich = TRUE)" )
    ,
    @NamedQuery( name = Eintrag.NQ_PARAMS_EINTRAEGE_BEGRIFF_ZEITRAUM_OEFFENTLICH,
            query = "SELECT evt FROM Eintrag evt WHERE lower(evt.titel) like lower(concat('%', :pBegriff, '%')) AND (evt.start <= :zEnde AND evt.ende >= :zStart) AND evt.oeffentlich = TRUE" )
    ,
    @NamedQuery( name = Eintrag.NQ_PARAMS_EINTRAEGE_ZEITRAUM_USER,
            query = "SELECT evt FROM Eintrag evt WHERE (evt.start <= :zEnde AND evt.ende >= :zStart) AND (evt.oeffentlich = TRUE OR evt.kalender.benutzer = :user)" )
    ,
    @NamedQuery( name = Eintrag.NQ_PARAMS_EINTRAEGE_BEGRIFF_ZEITRAUM_USER,
            query = "SELECT evt FROM Eintrag evt WHERE lower(evt.titel) like lower(concat('%', :pBegriff, '%')) AND (evt.start <= :zEnde AND evt.ende >= :zStart) AND (evt.oeffentlich = TRUE OR evt.kalender.benutzer = :user)" )
    ,
    @NamedQuery( name = Eintrag.NQ_PARAMS_SUCHE_USER,
            query = "SELECT evt FROM Eintrag evt WHERE ((evt.titel LIKE :term) OR (evt.details LIKE :term)) AND (evt.oeffentlich = TRUE OR evt.kalender.benutzer = :user)" )
    ,
    @NamedQuery( name = Eintrag.NQ_PARAMS_EINTRAEGE_ZU_KALENDER,
            query = "SELECT evt FROM Eintrag evt WHERE evt.kalender = :pKalender" )
    ,
    @NamedQuery( name = Eintrag.NQ_PARAMS_EINTRAEGE_ZU_KALENDER_ZEITRAUM,
            query = "SELECT evt FROM Eintrag evt WHERE evt.kalender = :pKalender AND (evt.start <= :zEnde AND evt.ende >= :zStart)" )
} )
public class Eintrag extends AbstractEntity implements Comparable<Eintrag> {

    private static final Long serialVersionUID = 0L;
    private static final DateFormat FORMATER = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    private static final String NQ_NAME_PREFIX = "Eintrag.";
    public static final String NQ_PARAMS_EINTRAEGE_ORT_ZEITRAUM_OEFFENTLICH = NQ_NAME_PREFIX + "eintraegeFuerOrtParamZeitraumParamOeffentlich";
    public static final String NQ_PARAMS_EINTRAEGE_BEGRIFF_ZEITRAUM_OEFFENTLICH = NQ_NAME_PREFIX + "eintraegeFuerBegriffParamZeitraumParamOeffentlich";
    public static final String NQ_PARAMS_EINTRAEGE_ZEITRAUM_OEFFENTLICH = NQ_NAME_PREFIX + "eintraegeFuerZeitraumParamOeffentlich";
    public static final String NQ_PARAMS_EINTRAEGE_ZEITRAUM_USER = NQ_NAME_PREFIX + "eintraegeFuerZeitraumParamUserParam";
    public static final String NQ_PARAMS_EINTRAEGE_BEGRIFF_ZEITRAUM_USER = NQ_NAME_PREFIX + "eintraegeFuerBegriffParamZeitraumParamUserParam";
    public static final String NQ_PARAMS_EINTRAEGE_ZU_KALENDER = NQ_NAME_PREFIX + "eintraegeZuKalenderParam";
    public static final String NQ_PARAMS_EINTRAEGE_ZU_KALENDER_ZEITRAUM = NQ_NAME_PREFIX + "eintraegeZuKalenderZeitraum";
    public static final String NQ_PARAMS_SUCHE_USER = NQ_NAME_PREFIX + "sucheEintraegeParamUser";

    public static final String PROP_SCHLAGWORTE = "schlagworte";

    @Getter
    @Setter
    private String titel;

    @Column( nullable = false )
    @Temporal( TemporalType.TIMESTAMP )
    @Getter
    @Setter
    private Date start;

    @Column( nullable = false )
    @Temporal( TemporalType.TIMESTAMP )
    @Getter
    @Setter
    private Date ende;

    @Column( length = 10000 )
    @Lob
    @Getter
    @Setter
    private String details;

    @Getter
    @Setter
    private String ort;

    @Getter
    @Setter
    private Boolean oeffentlich;

    @Embedded
    @Getter
    @Setter
    private Bewertung bewertung;

    @ManyToOne( optional = false )
    @Getter
    @Setter
    private Kalender kalender;

    @ManyToMany( cascade = CascadeType.PERSIST, fetch = FetchType.EAGER )
    @JoinTable( name = "TAGGING", joinColumns
            = @JoinColumn( name = "EINTRAG" ),
            inverseJoinColumns = @JoinColumn( name = "TAG" ) )
    Set<Schlagwort> schlagworte = new HashSet<>();

    public Eintrag() {
    }

    /**
     * Konstruktor für den einfachsten Fall eines Eintrages mit Titel und Datum
     *
     * @param titel
     * @param start
     */
    public Eintrag(String titel, Date start) {
        this(titel, start, start, null, "", false);
    }

    public Eintrag(String titel, Date start, Date ende, String details,
            String ort, Boolean oeffentlich) {
        this.titel = titel;
        this.start = start;
        this.ende = ende;
        this.details = details;
        this.ort = ort;
        this.oeffentlich = oeffentlich;
    }

    public Set<Schlagwort> getSchlagworte() {
        return Collections.unmodifiableSet(schlagworte);
    }

    public void hinzufuegenSchlagwort(Schlagwort schlagwort) {
        schlagworte.add(schlagwort);
    }

    public void entfernenSchlagwort(Schlagwort schlagwort) {
        schlagworte.remove(schlagwort);
    }

    public void clearSchlagworte() {
        schlagworte.clear();
    }

    public void initialisierenZeitraum() {
        start = new DateTime().withTimeAtStartOfDay().toDate();
        ende = new DateTime().toDate();
    }

    public String getZeitraumFormated() {
        return ZeitraumUtils.formatierenZeitraum(start, ende, true);
    }

    @Override
    public int compareTo(Eintrag other) {
        return getStart().compareTo(other.getStart());
    }

    @Override
    public String toString() {
        return String.
                format("%s (%s -> %s)\nDetails: %s\n%s", titel, FORMATER.
                        format(start), FORMATER.format(ende), details, getBewertung() != null ? getBewertung() : "Bewertung: ---");
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.titel);
        hash = 67 * hash + Objects.hashCode(this.start);
        hash = 67 * hash + Objects.hashCode(this.ende);
        hash = 67 * hash + Objects.hashCode(this.details);
        hash = 67 * hash + Objects.hashCode(this.oeffentlich);
        hash = 67 * hash + Objects.hashCode(this.kalender);
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

        final Eintrag other = (Eintrag) obj;
        if (!Objects.equals(this.titel, other.titel)) {
            return false;
        }
        if (!Objects.equals(this.details, other.details)) {
            return false;
        }
        if (!Objects.equals(this.start, other.start)) {
            return false;
        }
        if (!Objects.equals(this.ende, other.ende)) {
            return false;
        }
        if (!Objects.equals(this.oeffentlich, other.oeffentlich)) {
            return false;
        }
        if (!Objects.equals(this.kalender, other.kalender)) {
            return false;
        }

        return true;
    }

}
