package esprit.tn.junglepayments;

import esprit.tn.junglepayments.entities.Payment;
import esprit.tn.junglepayments.entities.PaymentMethod;
import esprit.tn.junglepayments.entities.PaymentStatus;
import esprit.tn.junglepayments.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests directs du repository Payment avec base H2 en mémoire — aucun mock.
 * @DataJpaTest charge uniquement la couche JPA (pas Eureka, pas Feign, pas Stripe).
 */
@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void nettoyerBase() {
        paymentRepository.deleteAll();
    }

    private Payment buildPayment(String email, Long eventId) {
        Payment p = new Payment();
        p.setParticipantEmail(email);
        p.setParticipantName("Prénom Nom");
        p.setEventId(eventId);
        p.setAmount(80.0);
        p.setStatus(PaymentStatus.PENDING);
        p.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        p.setReference("PAY-TEST-001");
        return p;
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    @Test
    void testSauvegarderPaiement_retourneIdGenere() {
        Payment saved = paymentRepository.save(buildPayment("user@test.com", 1L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getParticipantEmail()).isEqualTo("user@test.com");
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    // ── READ ────────────────────────────────────────────────────────────────

    @Test
    void testFindById_paiementExistant() {
        Payment saved = paymentRepository.save(buildPayment("alice@test.com", 5L));

        Optional<Payment> found = paymentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualTo(80.0);
    }

    @Test
    void testFindById_paiementInexistant_retourneEmpty() {
        Optional<Payment> found = paymentRepository.findById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll_retourneTousLesPaiements() {
        paymentRepository.save(buildPayment("u1@test.com", 1L));
        paymentRepository.save(buildPayment("u2@test.com", 2L));
        paymentRepository.save(buildPayment("u3@test.com", 3L));

        List<Payment> all = paymentRepository.findAll();

        assertThat(all).hasSize(3);
    }

    // ── REQUÊTES PERSONNALISÉES ─────────────────────────────────────────────

    @Test
    void testFindByEventId_retournePaiementsDeLEvent() {
        paymentRepository.save(buildPayment("a@test.com", 10L));
        paymentRepository.save(buildPayment("b@test.com", 10L));
        paymentRepository.save(buildPayment("c@test.com", 99L));

        List<Payment> paiementsEvent10 = paymentRepository.findByEventId(10L);

        assertThat(paiementsEvent10).hasSize(2);
        paiementsEvent10.forEach(p -> assertThat(p.getEventId()).isEqualTo(10L));
    }

    @Test
    void testFindByParticipantEmail_retournePaiementsDuUser() {
        paymentRepository.save(buildPayment("salma@test.com", 1L));
        paymentRepository.save(buildPayment("salma@test.com", 2L));
        paymentRepository.save(buildPayment("other@test.com", 3L));

        List<Payment> paiementsSalma = paymentRepository.findByParticipantEmail("salma@test.com");

        assertThat(paiementsSalma).hasSize(2);
    }

    @Test
    void testFindByParticipantEmail_emailInconnu_retourneListeVide() {
        paymentRepository.save(buildPayment("someone@test.com", 1L));

        List<Payment> result = paymentRepository.findByParticipantEmail("nobody@test.com");

        assertThat(result).isEmpty();
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────

    @Test
    void testModifierStatut_PENDINGversPAID() {
        Payment saved = paymentRepository.save(buildPayment("pay@test.com", 5L));
        saved.setStatus(PaymentStatus.PAID);
        paymentRepository.save(saved);

        Payment reloaded = paymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    // ── DELETE ──────────────────────────────────────────────────────────────

    @Test
    void testSupprimerPaiement_plusPresentEnBase() {
        Payment saved = paymentRepository.save(buildPayment("del@test.com", 1L));
        Long id = saved.getId();

        paymentRepository.deleteById(id);

        assertThat(paymentRepository.findById(id)).isEmpty();
    }

    // ── STATUTS ─────────────────────────────────────────────────────────────

    @Test
    void testPaiementAvecStatutFAILED_persiste() {
        Payment p = buildPayment("fail@test.com", 7L);
        p.setStatus(PaymentStatus.FAILED);
        Payment saved = paymentRepository.save(p);

        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void testPaiementVirement_methodeConservee() {
        Payment p = buildPayment("vir@test.com", 2L);
        p.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        Payment saved = paymentRepository.save(p);

        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
    }
}
