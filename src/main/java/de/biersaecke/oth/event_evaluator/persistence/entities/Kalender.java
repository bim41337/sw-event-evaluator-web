package de.biersaecke.oth.event_evaluator.persistence.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Entität der Kalender
 *
 * @author bim41337
 */
@Entity
@Table( name = "KALENDER", uniqueConstraints = {
    @UniqueConstraint( columnNames = { Kalender.FIELD_BENUTZER_FK,
        Kalender.PROP_BEZEICHNUNG }, name = "UQ_USER_KALENDER" )
} )
@NamedQueries( {
    @NamedQuery( name = Kalender.NQ_KALENDER_BENUTZER,
            query = "SELECT k FROM Kalender k WHERE k.benutzer = :user" )
} )
public class Kalender extends AbstractEntity {

    private static final Long serialVersionUID = 0L;

    public static final String NQ_PREFIX_NAME = "Kalender.";
    public static final String NQ_KALENDER_BENUTZER = NQ_PREFIX_NAME + "kalenderFuerBenutzer";

    public static final String DEFAULT_NAME = "Mein Kalender";
    public static final String FIELD_BENUTZER_FK = "BENUTZER_FK";

    public static final String PROP_BENUTZER = "benutzer";
    public static final String PROP_BEZEICHNUNG = "bezeichnung";
    public static final String PROP_EINTRAEGE = "eintraege";

    @Column( nullable = false )
    @Getter
    @Setter
    private String bezeichnung;

    @Column( length = 10000 )
    @Lob
    @Getter
    @Setter
    private String beschreibung;

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true )
    private List<Eintrag> eintraege = new ArrayList<>();

    @ManyToOne( optional = false )
    @JoinColumn( name = FIELD_BENUTZER_FK, foreignKey = @ForeignKey(
             name = "FK_KALENDER_BENUTZER" ) )
    @Getter
    @Setter
    private Benutzer benutzer;

    public Kalender() {
    }

    public Kalender(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public List<Eintrag> getEintraege() {
        return Collections.unmodifiableList(eintraege);
    }

    public void hinzufuegenEintrag(Eintrag eintrag) {
        eintraege.add(eintrag);
        eintrag.setKalender(this);
    }

    public void entfernenEintrag(Eintrag eintrag) {
        eintraege.remove(eintrag);
    }

    @Override
    public String toString() {
        return String.format("Kalender \"%s\" (%s)", bezeichnung, benutzer);
    }

}
