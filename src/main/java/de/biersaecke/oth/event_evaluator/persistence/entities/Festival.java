package de.biersaecke.oth.event_evaluator.persistence.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Abgeleitete Entität für externe Festivals
 *
 * @author bim41337
 */
@Entity
@Table( name = "FESTIVAL" )
@DiscriminatorValue( "F" )
public class Festival extends Eintrag {

    private static final Long serialVersionUID = 0L;

    @ElementCollection( fetch = FetchType.EAGER )
    @Getter
    @Setter
    private Set<String> bands = new HashSet<>();

    @ElementCollection( fetch = FetchType.EAGER )
    @Getter
    @Setter
    private Set<String> genres = new HashSet<>();

    public Festival() {
    }

    public Festival(String titel, Date start, Date ende, String details,
            String ort, Boolean oeffentlich) {
        super(titel, start, ende, details, ort, oeffentlich);
    }

    public String getBandsString() {
        return bands.isEmpty() ? "---" : StringUtils.join(bands, ", ");
    }

    public String getGenresString() {
        return genres.isEmpty() ? "---" : StringUtils.join(genres, ", ");
    }

    @Override
    public Set<Schlagwort> getSchlagworte() {
        Set<String> festivalSchlagworte = new HashSet<>();
        festivalSchlagworte.addAll(bands);
        festivalSchlagworte.addAll(genres);

        return festivalSchlagworte.stream().map(sw -> new Schlagwort(sw))
                .collect(Collectors.toCollection(HashSet<Schlagwort>::new));
    }

}
