package esprit.tn.jungleevents;

import esprit.tn.jungleevents.entities.Event;
import esprit.tn.jungleevents.entities.EventStatus;
import esprit.tn.jungleevents.repositories.EventRepository;
import esprit.tn.jungleevents.repositories.PaymentFeignClient;
import esprit.tn.jungleevents.repositories.UserFeignClient;
import esprit.tn.jungleevents.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests d'intégration du service Event avec base H2 réelle.
 * La couche JPA/base n'est PAS mockée.
 * Seuls les Feign clients (microservices externes) sont mockés.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EventServiceIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    // Feign vers microservices externes — on les mock pour ne pas avoir besoin
    // que jungle-payments et user-microservice tournent pendant les tests
    @MockBean
    private PaymentFeignClient paymentFeignClient;

    @MockBean
    private UserFeignClient userFeignClient;

    @BeforeEach
    void nettoyerBase() {
        eventRepository.deleteAll();
    }

    private Event buildEvent(String title) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription("Description complète de l'événement pour test d'intégration");
        e.setCategory("Technologie");
        e.setLocation("Tunis");
        e.setFormat("Présentiel");
        e.setPrice(75.0);
        e.setMaxParticipants(50);
        e.setStartDate(LocalDate.of(2025, 7, 10));
        e.setEndDate(LocalDate.of(2025, 7, 11));
        return e;
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Test
    void testCreerEvent_statutParDefautPUBLISHED() {
        Event event = buildEvent("Java Summit");
        // status = null → le service doit mettre PUBLISHED

        Event saved = eventService.createEvent(event);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void testCreerEvent_statutFourniConserve() {
        Event event = buildEvent("Workshop DevOps");
        event.setStatus(EventStatus.DRAFT);

        Event saved = eventService.createEvent(event);

        assertThat(saved.getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void testCreerEvent_persisteEnBase() {
        eventService.createEvent(buildEvent("Cloud Native Day"));
        eventService.createEvent(buildEvent("Angular Workshop"));

        assertThat(eventRepository.findAll()).hasSize(2);
    }

    // ── READ ────────────────────────────────────────────────────────────────

    @Test
    void testGetAllEvents_listeComplète() {
        eventService.createEvent(buildEvent("Event 1"));
        eventService.createEvent(buildEvent("Event 2"));
        eventService.createEvent(buildEvent("Event 3"));

        List<Event> events = eventService.getAllEvents();

        assertThat(events).hasSize(3);
    }

    @Test
    void testGetEventById_retourneCorrectement() {
        Event saved = eventService.createEvent(buildEvent("Kubernetes Conf"));

        Event found = eventService.getEventById(saved.getId());

        assertThat(found.getTitle()).isEqualTo("Kubernetes Conf");
        assertThat(found.getPrice()).isEqualTo(75.0);
    }

    @Test
    void testGetEventById_idInexistant_leveException() {
        assertThatThrownBy(() -> eventService.getEventById(9999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────

    @Test
    void testUpdateEvent_modifieLesChamps() {
        Event saved = eventService.createEvent(buildEvent("Ancien Titre"));

        Event modifications = buildEvent("Nouveau Titre");
        modifications.setPrice(120.0);
        modifications.setLocation("Sousse");
        modifications.setStatus(EventStatus.PUBLISHED);

        Event updated = eventService.updateEvent(saved.getId(), modifications);

        assertThat(updated.getTitle()).isEqualTo("Nouveau Titre");
        assertThat(updated.getPrice()).isEqualTo(120.0);
        assertThat(updated.getLocation()).isEqualTo("Sousse");
    }

    @Test
    void testUpdateEvent_idInexistant_leveException() {
        assertThatThrownBy(() -> eventService.updateEvent(9999L, buildEvent("X")))
                .isInstanceOf(RuntimeException.class);
    }

    // ── DELETE ──────────────────────────────────────────────────────────────

    @Test
    void testDeleteEvent_supprimeDeLaBase() {
        Event saved = eventService.createEvent(buildEvent("A supprimer"));
        Long id = saved.getId();

        eventService.deleteEvent(id);

        assertThat(eventRepository.findById(id)).isEmpty();
    }

    @Test
    void testDeleteEvent_baseVideApresSuppressionTotale() {
        eventService.createEvent(buildEvent("Evt1"));
        eventService.createEvent(buildEvent("Evt2"));

        List<Event> tous = eventService.getAllEvents();
        tous.forEach(e -> eventService.deleteEvent(e.getId()));

        assertThat(eventService.getAllEvents()).isEmpty();
    }
}
