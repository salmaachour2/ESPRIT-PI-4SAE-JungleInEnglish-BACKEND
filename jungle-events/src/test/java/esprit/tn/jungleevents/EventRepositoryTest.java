package esprit.tn.jungleevents;

import esprit.tn.jungleevents.entities.Event;
import esprit.tn.jungleevents.entities.EventStatus;
import esprit.tn.jungleevents.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests directs avec base H2 en mémoire — aucun mock.
 * @DataJpaTest charge uniquement la couche JPA (pas Eureka, pas Feign).
 */
@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    private Event buildEvent(String title) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription("Description suffisamment longue pour passer la validation Bean");
        e.setCategory("Technologie");
        e.setLocation("Tunis");
        e.setFormat("Présentiel");
        e.setPrice(50.0);
        e.setMaxParticipants(100);
        e.setStartDate(LocalDate.of(2025, 6, 1));
        e.setEndDate(LocalDate.of(2025, 6, 2));
        e.setStatus(EventStatus.PUBLISHED);
        return e;
    }

    @BeforeEach
    void cleanup() {
        eventRepository.deleteAll();
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Test
    void testSauvegarderEvent_retourneEventAvecId() {
        Event saved = eventRepository.save(buildEvent("Spring Boot Conference"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Spring Boot Conference");
        assertThat(saved.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    // ── READ ALL ────────────────────────────────────────────────────────────

    @Test
    void testFindAll_retourneTousLesEvents() {
        eventRepository.save(buildEvent("Event A"));
        eventRepository.save(buildEvent("Event B"));
        eventRepository.save(buildEvent("Event C"));

        List<Event> events = eventRepository.findAll();

        assertThat(events).hasSize(3);
    }

    // ── READ BY ID ──────────────────────────────────────────────────────────

    @Test
    void testFindById_eventExistant_retourneEvent() {
        Event saved = eventRepository.save(buildEvent("DevOps Day"));

        Optional<Event> found = eventRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("DevOps Day");
    }

    @Test
    void testFindById_eventInexistant_retourneEmpty() {
        Optional<Event> found = eventRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────

    @Test
    void testModifierEvent_titreChange() {
        Event saved = eventRepository.save(buildEvent("Ancien Titre"));

        saved.setTitle("Nouveau Titre");
        Event updated = eventRepository.save(saved);

        assertThat(updated.getTitle()).isEqualTo("Nouveau Titre");
        assertThat(updated.getId()).isEqualTo(saved.getId());
    }

    @Test
    void testModifierStatut_DRAFT_versCANCELLED() {
        Event saved = eventRepository.save(buildEvent("Event Test Statut"));
        saved.setStatus(EventStatus.CANCELLED);
        eventRepository.save(saved);

        Event reloaded = eventRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.CANCELLED);
    }

    // ── DELETE ──────────────────────────────────────────────────────────────

    @Test
    void testSupprimerEvent_plusPresentEnBase() {
        Event saved = eventRepository.save(buildEvent("A supprimer"));
        Long id = saved.getId();

        eventRepository.deleteById(id);

        assertThat(eventRepository.findById(id)).isEmpty();
    }

    @Test
    void testSupprimerTousLesEvents_baseVide() {
        eventRepository.save(buildEvent("Evt1"));
        eventRepository.save(buildEvent("Evt2"));

        eventRepository.deleteAll();

        assertThat(eventRepository.findAll()).isEmpty();
    }

    // ── PRICE ───────────────────────────────────────────────────────────────

    @Test
    void testEventGratuit_prixZero() {
        Event gratuit = buildEvent("Atelier Gratuit");
        gratuit.setPrice(0.0);
        Event saved = eventRepository.save(gratuit);

        assertThat(saved.getPrice()).isEqualTo(0.0);
    }
}
