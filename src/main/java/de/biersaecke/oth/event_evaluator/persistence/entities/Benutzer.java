package de.biersaecke.oth.event_evaluator.persistence.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entität der Kalenderbenutzer
 *
 * @author bim41337
 */
@Entity
@Table( name = "BENUTZER" )
@NamedQueries( {
    @NamedQuery( name = Benutzer.NQ_PARAMS_MIT_NAME,
            query = "Select b From Benutzer b Where b.name = :bName" )
    ,
    @NamedQuery( name = Benutzer.NQ_PARAMS_LOGIN,
            query = "Select b From Benutzer b Where b.name = :bName And b.passwort = :bPw" )
} )
public class Benutzer extends AbstractEntity {

    private static final long serialVersionUID = 0L;

    private static final String NQ_NAME_PREFIX = "Benutzer.";
    public static final String NQ_PARAMS_MIT_NAME = NQ_NAME_PREFIX + "benutzerMitName";
    public static final String NQ_PARAMS_LOGIN = NQ_NAME_PREFIX + "login";

    @Column( nullable = false, unique = true )
    @Getter
    @Setter
    private String name;

    @Column( nullable = false )
    @Getter
    @Setter
    private String passwort;

    @Column( nullable = false )
    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true,
            mappedBy = Kalender.PROP_BENUTZER )
    private Set<Kalender> kalender = new HashSet<>();

    public Benutzer() {
    }

    public Benutzer(String name, String passwort) {
        this.name = name;
        this.passwort = passwort;
    }

    public Set<Kalender> getKalender() {
        return Collections.unmodifiableSet(kalender);
    }

    public void hinzufuegenKalender(Kalender kalender) {
        this.kalender.add(kalender);
    }

    public void entfernenKalender(Kalender kalender) {
        this.kalender.remove(kalender);
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d)", name, getId());
    }

}
