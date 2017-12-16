package de.biersaecke.oth.event_evaluator.persistence.utils;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Helperklasse für Zeiträume
 *
 * @author bim41337
 */
@Embeddable
public class Zeitraum implements Comparable<Zeitraum>, Serializable {
    
    private static final Long serialVersionUID = 0L;
    
    public static final Comparator<Zeitraum> COMPARATOR = (z1, z2) -> z1.
            compareTo(z2);
    
    @Getter
    @Setter
    private Date start;
    
    @Getter
    @Setter
    private Date ende;
    
    protected Zeitraum() {
    }
    
    public Zeitraum(Date datum) {
        this(datum, datum);
    }
    
    public Zeitraum(Date start, Date ende) {
        this.start = start;
        this.ende = ende;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.start);
        hash = 97 * hash + Objects.hashCode(this.ende);
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
        final Zeitraum other = (Zeitraum) obj;
        
        return this.compareTo(other) == 0;
    }
    
    @Override
    public int compareTo(Zeitraum o) {
        int startCompareValue = start.compareTo(o.getStart());
        
        if (startCompareValue < 0) {
            return -1;
        } else if (startCompareValue == 0) {
            return ende.compareTo(o.getEnde());
        } else {
            return 1;
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s", ZeitraumUtils.
                formatierenDatumMitZeit(start), ZeitraumUtils.
                formatierenDatumMitZeit(ende));
    }
    
}
