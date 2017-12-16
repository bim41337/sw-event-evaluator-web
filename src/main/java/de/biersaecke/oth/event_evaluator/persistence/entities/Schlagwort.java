package de.biersaecke.oth.event_evaluator.persistence.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entität für Schlagworte (Tags) von Einträgen
 *
 * @author bim41337
 */
@Entity
@Table( name = "SCHLAGWORT" )
@NamedQueries( {
    @NamedQuery( name = Schlagwort.NQ_GEBUNDENE_TAGS,
            query = "SELECT tag FROM Schlagwort tag WHERE tag.eintraege IS NOT EMPTY" )
    ,
    @NamedQuery( name = Schlagwort.NQ_UNGEBUNDENE_TAGS,
            query = "SELECT tag FROM Schlagwort tag WHERE tag.eintraege IS EMPTY" )
    ,
    @NamedQuery( name = Schlagwort.NQ_SCHLAGWORTE_MIT_TEXT_PARAM,
            query = "SELECT tag FROM Schlagwort tag WHERE tag.titel LIKE :pTag" )
} )
public class Schlagwort extends AbstractEntity {

    private static final Long serialVersionUID = 0L;

    private static final String NQ_NAME_PREFIX = "Schlagwort.";
    public static final String NQ_GEBUNDENE_TAGS = NQ_NAME_PREFIX + "alleGebundenen";
    public static final String NQ_UNGEBUNDENE_TAGS = NQ_NAME_PREFIX + "alleUngebundenen";
    public static final String NQ_SCHLAGWORTE_MIT_TEXT_PARAM = NQ_NAME_PREFIX + "SchlagworteMitText";

    @Column( nullable = false, unique = true )
    @Getter
    @Setter
    private String titel;

    @ManyToMany( mappedBy = Eintrag.PROP_SCHLAGWORTE )
    private Set<Eintrag> eintraege = new HashSet<>();

    public Schlagwort() {
    }

    public Schlagwort(String titel) {
        this.titel = titel.toLowerCase();
    }

    public Set<Eintrag> getEintraege() {
        return Collections.unmodifiableSet(eintraege);
    }

    public void hinzufuegenEintrag(Eintrag e) {
        eintraege.add(e);
    }

    public void entfernenEintrag(Eintrag e) {
        eintraege.remove(e);
    }

    @Override
    public String toString() {
        return titel;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.titel);
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
        final Schlagwort other = (Schlagwort) obj;
        if (!Objects.equals(this.titel, other.titel)) {
            return false;
        }
        return true;
    }

}
