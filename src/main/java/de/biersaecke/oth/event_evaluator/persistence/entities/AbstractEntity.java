package de.biersaecke.oth.event_evaluator.persistence.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstrakte Entitätsklasse zur Implementierung von Standards
 *
 * @author bim41337
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 0L;

    @Id
    @Column( name = "id" )
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Getter
    @Setter
    private Long id;

    @Column( nullable = false )
    @Temporal( TemporalType.TIMESTAMP )
    @Getter
    @Setter
    private Date creationDate;

    public AbstractEntity() {
        this.creationDate = new Date();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final AbstractEntity other = (AbstractEntity) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.biersaecke.oth.event_evaluator.persistence.entities.AbstractEntity[ id=" + id + " ]";
    }

}
