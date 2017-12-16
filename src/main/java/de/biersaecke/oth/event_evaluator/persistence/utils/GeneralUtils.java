package de.biersaecke.oth.event_evaluator.persistence.utils;

import de.biersaecke.oth.event_evaluator.persistence.entities.Benutzer;
import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import de.biersaecke.oth.event_evaluator.persistence.entities.Kalender;
import de.biersaecke.oth.event_evaluator.persistence.utils.IncorrectEntityConfigurationException.ConfigErrorEnum;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;

/**
 * Utilityklasse für die Entität Benutzer
 *
 * @author bim41337
 */
@SuppressWarnings( { "CallToPrintStackTrace" } )
public class GeneralUtils {

    public static final String EMPTY_STRING = "";

    /**
     * Prüft einen übergebenen Nutzer vor dem Persistieren auf Korrektheit
     *
     * @param neuerBenutzer Zu registrierender Benutzer
     * @param entityManager EntityManager aus dem Service für Abfragen
     * @return true, falls Benutzerobjekt valide
     * @throws IncorrectEntityConfigurationException Falls Benutzer fehlerhaft
     */
    public static boolean pruefeRegistrierung(Benutzer neuerBenutzer,
            EntityManager entityManager) throws IncorrectEntityConfigurationException {

        // Name und Passwort gesetzt?
        if (StringUtils.isAnyEmpty(neuerBenutzer.getName(), neuerBenutzer.
                getPasswort())) {
            throw new IncorrectEntityConfigurationException(ConfigErrorEnum.MISSING_USER_ARGUMENTS);
        }

        // User bereits vorhanden?
        TypedQuery<Benutzer> query = entityManager.
                createNamedQuery(Benutzer.NQ_PARAMS_MIT_NAME, Benutzer.class);
        query.setParameter("bName", neuerBenutzer.getName());
        if (!query.getResultList().isEmpty()) {
            throw new IncorrectEntityConfigurationException(ConfigErrorEnum.DUPLICATE_ENTITY);
        }

        return true;
    }

    /**
     * Prüft einen Kalendereintrag auf seine Validität.
     *
     * @param eintrag Zu prüfender Eintrag
     * @return true, falls Bewertung zulässig
     */
    public static boolean pruefeKalendereintrag(Eintrag eintrag) {

        Kalender evtKalender = eintrag.getKalender();
        if (evtKalender == null || evtKalender.getId() == null) {
            throw new RuntimeException("Eintrag ohne gültigen Kalender erhalten");
        }

        if (eintrag.getStart() == null || eintrag.getEnde() == null || StringUtils
                .isEmpty(eintrag.getTitel())) {
            throw new IncorrectEntityConfigurationException(ConfigErrorEnum.MISSING_EVENT_ARGUMENTS);
        }

        if (eintrag.getStart().compareTo(eintrag.getEnde()) > 0) {
            throw new IncorrectEntityConfigurationException(ConfigErrorEnum.INVALID_EVENT_START_END_CONFUSION);
        }

        return true;
    }

    /**
     * Generiert einen SHA-512 Hash aus dem übergebenen String<br>
     * <i>Übernommen von https://www.bennyn.de/programmierung/java/sha-512-verschlusselung-mit-java.html</i>
     *
     * @param text Zu hashender String
     * @return Hashwert für <code>text</code>
     */
    public static String getSHA512Hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(text.getBytes("UTF-8"), 0, text.length());

            return convertToHex(md.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException("Hash-Utils konnten nicht durchgeführt werden");
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;

            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }

        return buf.toString();
    }

}
