package de.biersaecke.oth.event_evaluator.persistence.utils;

import lombok.Getter;

/**
 * Exception zum Anzeigen einer fehlerhaft konfigurierten Entity.<br>
 * Betrifft beispielsweise fehlende Eigenschaften oder doppelte Einträge
 *
 * @author bim41337
 */
public class IncorrectEntityConfigurationException extends RuntimeException {

    @Getter
    private final ConfigErrorEnum error;

    public IncorrectEntityConfigurationException(ConfigErrorEnum err) {
        super(err.getMessage());
        error = err;
    }

    /**
     * Enum zur Verwaltung von Fehlercodes und -nachrichten für Entities
     */
    public static enum ConfigErrorEnum {

        INVALID_RATING("Angegebene Bewertung liegt außerhalb des gültigen Bereichs [-1, 1]"),
        MISSING_USER_ARGUMENTS("Benutzer fehlt Name und / oder Passwort"),
        MISSING_CALENDAR_ARGUMENTS("Pflichtfelder des Kalenders sind nicht gefüllt"),
        MISSING_EVENT_ARGUMENTS("Pflichtfelder des Eintrags sind nicht gefüllt"),
        INVALID_EVENT_START_END_CONFUSION("Startdatum des Eintrags liegt nach dem Enddatum"),
        DUPLICATE_ENTITY("Eine Entität mit diesen Eigenschaften existiert bereits"),
        MISSING_USER(
                "Der Benutzer konnte nicht gefunden werden"),;

        @Getter
        private final String message;

        private ConfigErrorEnum(String msg) {
            message = msg;
        }

    }

}
