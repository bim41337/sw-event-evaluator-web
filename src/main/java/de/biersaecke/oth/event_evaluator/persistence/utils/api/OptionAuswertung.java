package de.biersaecke.oth.event_evaluator.persistence.utils.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * Qualifier-Annotation für AuswertungService
 *
 * @author bim41337
 */
@Qualifier
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER,
    ElementType.TYPE } )
public @interface OptionAuswertung {

}
