package de.biersaecke.oth.event_evaluator.persistence.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.joda.time.DateTime;

/**
 * Utitilyklasse für Zeiträume
 *
 * @author bim41337
 */
public class ZeitraumUtils {

    private static final DateFormat TAG_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
    private static final DateFormat TAG_ZEIT_FORMATTER = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public static String formatierenDatumMitZeit(Date datum) {
        return TAG_ZEIT_FORMATTER.format(datum);
    }

    public static String formatierenDatumNurTag(Date datum) {
        return TAG_FORMATTER.format(datum);
    }

    /**
     * Gibt einen Zeitraum (mit oder ohne Datum) formatiert mit Start- und Enddatum an
     *
     * @param zeitraumStart Startdatum
     * @param zeitraumEnde Enddatum
     * @param mitZeit Gibt an, ob die Zeit mitformatiert werden soll
     * @return Formatierter String des Zeitraums
     */
    public static String formatierenZeitraum(Date zeitraumStart,
            Date zeitraumEnde, boolean mitZeit) {
        String start = mitZeit ? formatierenDatumMitZeit(zeitraumStart) : formatierenDatumNurTag(zeitraumStart);
        String ende = mitZeit ? formatierenDatumMitZeit(zeitraumEnde) : formatierenDatumNurTag(zeitraumEnde);

        return String.format("%s - %s", start, ende);
    }

    /**
     * Prüft ob der Start eines Zeitraums seinem Ende oder gleich mit diesem ist
     *
     * @param zeitraum
     * @return <code>true</code>, falls Zeitraum gültig
     */
    public static boolean isGueltigerZeitraum(Zeitraum zeitraum) {
        return zeitraum.getStart().compareTo(zeitraum.getEnde()) <= 0;
    }

    /**
     * Gibt an, ob der Start eines Zeitraums in der Zukunft liegt.<br>
     * Sind Start und aktueller Timestamp exakt gleich, gilt das als Zukunft im
     * Sinne einer Auswertung.
     *
     * @param zeitraum Zu prüfender Zeitraum
     * @return <code>true</code>, falls zeitraum.start aktuell oder zukünftig
     */
    public static boolean isZukunft(Zeitraum zeitraum) {
        return zeitraum.getStart().compareTo(new Date()) >= 0;
    }

    /**
     * Erstellt einen Zeitraum mit standardisierten Zeiten<br>
     * Start: 00:00 Uhr<br>
     * Ende: 23:59 Uhr
     *
     * @param start Beginn des Zeitraums
     * @param ende Ende des Zeitraums
     * @return standardisierter Zeitraum
     */
    public static Zeitraum erstellenStandardZeitraum(Date start, Date ende) {
        Date startParsed = new DateTime(start).withTimeAtStartOfDay().toDate();
        Date endeParsed = new DateTime(ende).withTime(23, 59, 59, 0).toDate();

        Zeitraum zeitraum = new Zeitraum(startParsed, endeParsed);
        return zeitraum;
    }

}
