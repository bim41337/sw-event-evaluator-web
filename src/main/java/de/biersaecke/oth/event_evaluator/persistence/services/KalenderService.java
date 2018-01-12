package de.biersaecke.oth.event_evaluator.persistence.services;

import de.biersaecke.oth.event_evaluator.persistence.entities.Auswertung;
import de.biersaecke.oth.event_evaluator.persistence.entities.Benutzer;
import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import de.biersaecke.oth.event_evaluator.persistence.entities.Festival;
import de.biersaecke.oth.event_evaluator.persistence.entities.Kalender;
import de.biersaecke.oth.event_evaluator.persistence.entities.Schlagwort;
import de.biersaecke.oth.event_evaluator.persistence.services.api.KalenderIF;
import de.biersaecke.oth.event_evaluator.persistence.utils.IncorrectEntityConfigurationException;
import de.biersaecke.oth.event_evaluator.persistence.utils.IncorrectEntityConfigurationException.ConfigErrorEnum;
import de.biersaecke.oth.event_evaluator.persistence.utils.Zeitraum;
import de.biersaecke.oth.event_evaluator.persistence.utils.ZeitraumUtils;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionKalender;
import de.oth.joa44741.swprojektjohn.services.BandEntity;
import de.oth.joa44741.swprojektjohn.services.FestivalEntity;
import de.oth.joa44741.swprojektjohn.services.FestivalService;
import de.oth.joa44741.swprojektjohn.services.FestivalServiceImpl;
import de.oth.joa44741.swprojektjohn.services.FestivalWithDetailsDto;
import de.oth.joa44741.swprojektjohn.services.LineupDateEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.xml.ws.WebServiceRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

/**
 * Service für Kalendereinträge
 *
 * @author bim41337
 */
@RequestScoped
@WebService
public class KalenderService implements KalenderIF {

    @WebServiceRef( wsdlLocation = "WEB-INF/wsdl/im-lamport_8080/FestivalPlaner-John/FestivalService.wsdl" )
    private FestivalService service;
    private FestivalServiceImpl port;

    @PersistenceContext( unitName = "eventevalPU" )
    private EntityManager entityManager;

    @Inject
    @OptionKalender
    private Logger logger;

    /*
     * ### Öffentliche Methoden ###
     */
    @Override
    @Transactional
    public List<Eintrag> holenOeffentlicheEintraegeFuerTag(Date datum,
            String ort) {
        Zeitraum abfrageZeitraum = ZeitraumUtils
                .erstellenStandardZeitraum(datum, datum);

        TypedQuery<Eintrag> query = entityManager.
                createNamedQuery(Eintrag.NQ_PARAMS_EINTRAEGE_ORT_ZEITRAUM_OEFFENTLICH, Eintrag.class);
        query.setParameter("zStart", abfrageZeitraum.getStart());
        query.setParameter("zEnde", abfrageZeitraum.getEnde());
        query.setParameter("oName", ort);

        List<Eintrag> oeffentlicheEintraege = query.getResultList();
        Collections.sort(oeffentlicheEintraege);

        logger.info(String.
                format("KalenderService liefert %d Einträge für %s (%s)", oeffentlicheEintraege
                        .size(), ort, ZeitraumUtils
                                .formatierenDatumNurTag(datum)));
        return Collections.unmodifiableList(oeffentlicheEintraege);
    }

    /*
     * ### Servicemethoden ###
     */
    @Transactional
    @WebMethod( exclude = true )
    public Eintrag erzeugenEintrag(Eintrag eintrag) {
        Kalender evtKalender = entityManager.find(Kalender.class, eintrag.
                getKalender().getId());
        evtKalender.hinzufuegenEintrag(eintrag);
        Set<Schlagwort> neueSchlagworte = new HashSet<>(eintrag.getSchlagworte());
        eintrag.clearSchlagworte();
        entityManager.persist(eintrag);
        verarbeitenEintragSchlagworte(eintrag, neueSchlagworte, true);

        logger.info("Neuer Eintrag: " + eintrag);
        return eintrag;
    }

    @Transactional
    @WebMethod( exclude = true )
    public Eintrag aendernEintrag(Eintrag eintrag) {
        Set<Schlagwort> neueSchlagworte = new HashSet<>(eintrag.getSchlagworte());
        eintrag.clearSchlagworte();
        eintrag = entityManager.merge(eintrag);
        verarbeitenEintragSchlagworte(eintrag, neueSchlagworte, false);

        return eintrag;
    }

    private void verarbeitenEintragSchlagworte(Eintrag eintrag,
            Set<Schlagwort> neueSchlagworte,
            boolean neuerEintrag) {

        if (!neuerEintrag) {
            Eintrag eintragEntity = entityManager.find(Eintrag.class, eintrag
                    .getId());
            // Alle Verbindungen lösen ...
            Set<Schlagwort> schlagworte = new HashSet<>(eintragEntity
                    .getSchlagworte());
            schlagworte.forEach(sw -> {
                sw.entfernenEintrag(eintragEntity);
                eintragEntity.entfernenSchlagwort(sw);
            });
        }

        // ... und (ggf.) neu herstellen
        TypedQuery<Schlagwort> query;
        List<Schlagwort> tmpResults;
        Schlagwort tmpSwEntity;
        for (Schlagwort sw : neueSchlagworte) {
            query = entityManager
                    .createNamedQuery(Schlagwort.NQ_SCHLAGWORTE_MIT_TEXT_PARAM, Schlagwort.class);
            query.setParameter("pTag", sw.getTitel());
            tmpResults = query.getResultList();
            if (tmpResults.size() == 1) {
                tmpSwEntity = tmpResults.get(0);
            } else if (tmpResults.isEmpty()) {
                entityManager.persist(sw);
                tmpSwEntity = sw;
            } else {
                logger.error(String
                        .format("Fehler Schlagworte ändern: eId: %d\nTags: %s", eintrag
                                .getId(), neueSchlagworte.toString()));
                throw new RuntimeException("Unerwartete Schlagwort-Konfiguration");
            }
            tmpSwEntity.hinzufuegenEintrag(eintrag);
            eintrag.hinzufuegenSchlagwort(tmpSwEntity);
        }
    }

    @Transactional
    @WebMethod( exclude = true )
    public void loeschenEintrag(Long eintragId) {
        Eintrag eintragEntity = entityManager.find(Eintrag.class, eintragId);

        TypedQuery<Auswertung> countQuery = entityManager
                .createNamedQuery(Auswertung.NQ_PARAMS_AUSWERTUNG_EINTRAEGE, Auswertung.class);
        countQuery.setParameter("pEintrag", eintragEntity);
        List<Auswertung> eintragAuswertungen = countQuery.getResultList();
        if (eintragAuswertungen.size() > 0) {
            eintragAuswertungen.forEach((auswertung) -> {
                auswertung.entfernenPosten(eintragEntity);
            });
        }

        eintragEntity.getKalender().entfernenEintrag(eintragEntity);
        logger.warn("Lösche Eintrag: " + eintragEntity);
        entityManager.remove(eintragEntity);
    }

    @Transactional
    @WebMethod( exclude = true )
    public List<Eintrag> holenEintraegeZuKalender(long kalenderId) {
        Kalender kalenderEntity = entityManager.find(Kalender.class, kalenderId);
        TypedQuery<Eintrag> query = entityManager
                .createNamedQuery(Eintrag.NQ_PARAMS_EINTRAEGE_ZU_KALENDER, Eintrag.class);
        query.setParameter("pKalender", kalenderEntity);

        return query.getResultList();
    }

    @Transactional
    @WebMethod( exclude = true )
    public List<Eintrag> holenEintraegeZuKalenderInZeitraum(long kalenderId,
            Zeitraum zeitraum) {
        Kalender kalenderEntity = entityManager.find(Kalender.class, kalenderId);
        TypedQuery<Eintrag> query = entityManager
                .createNamedQuery(Eintrag.NQ_PARAMS_EINTRAEGE_ZU_KALENDER_ZEITRAUM, Eintrag.class);
        query.setParameter("pKalender", kalenderEntity);
        query.setParameter("zStart", zeitraum.getStart());
        query.setParameter("zEnde", zeitraum.getEnde());

        return query.getResultList();
    }

    @Transactional
    @WebMethod( exclude = true )
    public List<Eintrag> suchenOeffentlicheEintraege(String begriff,
            Zeitraum zeitraum) {
        TypedQuery<Eintrag> eintragQuery;

        if (StringUtils.isEmpty(begriff)) {
            eintragQuery = entityManager.
                    createNamedQuery(Eintrag.NQ_PARAMS_EINTRAEGE_ZEITRAUM_OEFFENTLICH, Eintrag.class);
        } else {
            eintragQuery = entityManager
                    .createNamedQuery(Eintrag.NQ_PARAMS_EINTRAEGE_BEGRIFF_ZEITRAUM_OEFFENTLICH, Eintrag.class);
            eintragQuery.setParameter("pBegriff", begriff);
        }

        eintragQuery.setParameter("zStart", zeitraum.getStart());
        eintragQuery.setParameter("zEnde", zeitraum.getEnde());

        return eintragQuery.getResultList();
    }

    @Transactional
    @WebMethod( exclude = true )
    public List<Eintrag> suchenEintraegeFuerUser(String begriff,
            Zeitraum zeitraum, Benutzer benutzer) {
        TypedQuery<Eintrag> eintragQuery;

        if (StringUtils.isEmpty(begriff)) {
            eintragQuery = entityManager.
                    createNamedQuery(Eintrag.NQ_PARAMS_EINTRAEGE_ZEITRAUM_USER, Eintrag.class);
        } else {
            eintragQuery = entityManager
                    .createNamedQuery(Eintrag.NQ_PARAMS_EINTRAEGE_BEGRIFF_ZEITRAUM_USER, Eintrag.class);
            eintragQuery.setParameter("pBegriff", begriff);
        }

        eintragQuery.setParameter("zStart", zeitraum.getStart());
        eintragQuery.setParameter("zEnde", zeitraum.getEnde());
        eintragQuery.setParameter("user", benutzer);

        return eintragQuery.getResultList();
    }

    @Transactional
    @WebMethod( exclude = true )
    public Set<Schlagwort> holenSchlagworteZuEintrag(long eintragId) {
        Eintrag eintrag = entityManager.find(Eintrag.class, eintragId);

        return eintrag.getSchlagworte();
    }

    @Transactional
    @WebMethod( exclude = true )
    public List<Festival> holenFreigegebeneFestivals() {
        List<FestivalEntity> festivals;
        try { // Call Web Service Operation
            port = service.getFestivalPort();
            festivals = port.findFreigegebeneFestivals();
        } catch (Exception ex) {
            logger.error("Fehler beim Festival-Abruf: " + ex.getMessage());
            festivals = Collections.emptyList();
        }

        List<Festival> festivalsIntern = new ArrayList<>();
        if (!festivals.isEmpty()) {
            festivalsIntern = mappenFestivalEntities(festivals);
        }

        return festivalsIntern;
    }

    private List<Festival> mappenFestivalEntities(List<FestivalEntity> festivals) {
        List<Festival> festivalsIntern = new ArrayList<>();

        for (FestivalEntity fe : festivals) {
            Festival tmpFestival = new Festival(
                    fe.getName(),
                    fe.getDatumVon().toGregorianCalendar().getTime(),
                    fe.getDatumBis().toGregorianCalendar().getTime(),
                    fe.getWebseite(),
                    fe.getLocation().getName(),
                    true
            );

            try { // Call Web Service Operation
                FestivalWithDetailsDto festivalDetails = port
                        .retrieveFestivalDtoByIdIncludingDetails(fe.getId());
                Set<String> genres = new HashSet<>();
                for (LineupDateEntity lde : festivalDetails.getLineupDates()) {
                    BandEntity band = lde.getBand();
                    tmpFestival.getBands().add(band.getName());
                    genres.addAll(band.getGenres().stream().map(ge -> ge.name())
                            .collect(Collectors.toSet()));
                }
                tmpFestival.getGenres().addAll(genres);
            } catch (Exception ex) {
                logger.error("Fehler beim Detailfestival-Abruf: " + ex
                        .getMessage());
                logger.info("Bands und Genres leer gesetzt.");
                tmpFestival.setBands(Collections.emptySet());
                tmpFestival.setGenres(Collections.emptySet());
            }

            festivalsIntern.add(tmpFestival);
        }

        return festivalsIntern;
    }

    /*
     * ### KALENDER ###
     */
    @Transactional
    @WebMethod( exclude = true )
    public Kalender erzeugenKalender(Kalender kalender) {
        TypedQuery<Kalender> query = entityManager.
                createQuery("Select k From Kalender k Where k.bezeichnung = :kName And k.benutzer = :kUser", Kalender.class);
        query.setParameter("kName", kalender.getBezeichnung());
        query.setParameter("kUser", kalender.getBenutzer());

        if (!query.getResultList().isEmpty()) {
            throw new IncorrectEntityConfigurationException(ConfigErrorEnum.DUPLICATE_ENTITY);
        }

        Benutzer kalenderBenutzer = entityManager.find(Benutzer.class, kalender.
                getBenutzer().getId());
        if (kalenderBenutzer == null) {
            throw new IncorrectEntityConfigurationException(ConfigErrorEnum.MISSING_USER);
        }

        kalender.setBenutzer(kalenderBenutzer);
        kalenderBenutzer.hinzufuegenKalender(kalender);
        entityManager.persist(kalender);

        logger.info("Neuer Kalender: " + kalender);
        return kalender;
    }

    @Transactional
    @WebMethod( exclude = true )
    public List<Kalender> holenKalenderFuerBenutzer(Benutzer benutzer) {
        TypedQuery<Kalender> query = entityManager.
                createNamedQuery(Kalender.NQ_KALENDER_BENUTZER, Kalender.class);
        query.setParameter("user", benutzer);
        return query.getResultList();
    }

    @Transactional
    @WebMethod( exclude = true )
    public Kalender aendernKalender(Kalender kalender) {

        if (StringUtils.isEmpty(kalender.getBezeichnung())) {
            throw new IncorrectEntityConfigurationException(ConfigErrorEnum.MISSING_CALENDAR_ARGUMENTS);
        }

        Kalender kalenderEntity = entityManager.find(Kalender.class, kalender.
                getId());
        kalenderEntity.setBezeichnung(kalender.getBezeichnung());
        kalenderEntity.setBeschreibung(kalender.getBeschreibung());

        return kalenderEntity;
    }

    @Transactional
    @WebMethod( exclude = true )
    public void loeschenKalender(long kalenderId) {
        Kalender kalenderEntity = entityManager.find(Kalender.class, kalenderId);

        if (kalenderEntity == null) {
            throw new RuntimeException("Kalender zum Löschen wurde nicht gefunden");
        }
        if (kalenderEntity.getBenutzer().getKalender().size() == 1) {
            throw new RuntimeException("Der letzte Kalender eines Benutzers kann nicht gelöscht werden");
        }

        logger.warn("Lösche Kalender: " + kalenderEntity);
        if (kalenderEntity.getEintraege().size() > 0) {
            List<Eintrag> tmpEintraegeList = new ArrayList<>(kalenderEntity
                    .getEintraege());
            tmpEintraegeList.stream().mapToLong(Eintrag::getId)
                    .forEach(this::loeschenEintrag);
        }
        kalenderEntity.getBenutzer().entfernenKalender(kalenderEntity);
        entityManager.remove(kalenderEntity);
    }

}
