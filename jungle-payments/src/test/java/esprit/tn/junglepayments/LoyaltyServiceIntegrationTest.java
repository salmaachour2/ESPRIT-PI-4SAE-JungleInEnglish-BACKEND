package esprit.tn.junglepayments;

import esprit.tn.junglepayments.entities.*;
import esprit.tn.junglepayments.repositories.EventFeignClient;
import esprit.tn.junglepayments.repositories.LoyaltyAccountRepository;
import esprit.tn.junglepayments.repositories.PromoCodeRepository;
import esprit.tn.junglepayments.repositories.UserFeignClient;
import esprit.tn.junglepayments.services.LoyaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du service Loyalty avec base H2 réelle.
 * Les repositories LoyaltyAccount et PromoCode ne sont PAS mockés.
 * Seuls les Feign clients (microservices externes) sont mockés.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class LoyaltyServiceIntegrationTest {

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @MockBean
    private EventFeignClient eventFeignClient;

    @MockBean
    private UserFeignClient userFeignClient;

    @BeforeEach
    void nettoyerBase() {
        loyaltyAccountRepository.deleteAll();
        promoCodeRepository.deleteAll();
    }

    private Payment buildPayment(String email, Double amount) {
        Payment p = new Payment();
        p.setParticipantEmail(email);
        p.setParticipantName("Test User");
        p.setParticipantId(1L);
        p.setEventId(10L);
        p.setAmount(amount);
        p.setStatus(PaymentStatus.PAID);
        return p;
    }

    // ── CRÉATION DE COMPTE ──────────────────────────────────────────────────

    @Test
    void testCreditPoints_creerNouveauCompte() {
        Payment payment = buildPayment("alice@test.com", 50.0);

        loyaltyService.creditPoints(payment);

        LoyaltyAccount account = loyaltyService.getAccount("alice@test.com");
        assertThat(account).isNotNull();
        assertThat(account.getTotalPoints()).isEqualTo(1);
        assertThat(account.getTotalSpent()).isEqualTo(50.0);
        assertThat(account.getTier()).isEqualTo(LoyaltyTier.BRONZE);
    }

    // ── CUMUL DES POINTS ────────────────────────────────────────────────────

    @Test
    void testCreditPoints_cumulerPlusieursEvents() {
        String email = "bob@test.com";

        loyaltyService.creditPoints(buildPayment(email, 30.0));
        loyaltyService.creditPoints(buildPayment(email, 40.0));

        LoyaltyAccount account = loyaltyService.getAccount(email);
        assertThat(account.getTotalPoints()).isEqualTo(2);
        assertThat(account.getTotalSpent()).isEqualTo(70.0);
    }

    // ── PROGRESSION DES TIERS ───────────────────────────────────────────────

    @Test
    void testTier_BRONZE_avecMoinsde3Events() {
        String email = "bronze@test.com";
        loyaltyService.creditPoints(buildPayment(email, 20.0));
        loyaltyService.creditPoints(buildPayment(email, 20.0));

        LoyaltyAccount account = loyaltyService.getAccount(email);
        assertThat(account.getTier()).isEqualTo(LoyaltyTier.BRONZE);
    }

    @Test
    void testTier_SILVER_avec3Events() {
        String email = "silver@test.com";
        for (int i = 0; i < 3; i++) {
            loyaltyService.creditPoints(buildPayment(email, 50.0));
        }

        LoyaltyAccount account = loyaltyService.getAccount(email);
        assertThat(account.getTier()).isEqualTo(LoyaltyTier.SILVER);
    }

    @Test
    void testTier_GOLD_avec6Events() {
        String email = "gold@test.com";
        for (int i = 0; i < 6; i++) {
            loyaltyService.creditPoints(buildPayment(email, 100.0));
        }

        LoyaltyAccount account = loyaltyService.getAccount(email);
        assertThat(account.getTier()).isEqualTo(LoyaltyTier.GOLD);
    }

    // ── JALONS & CODES PROMO FIDÉLITÉ ───────────────────────────────────────

    @Test
    void testJalon2Events_genereCodePromo10Pourcent() {
        String email = "jalon2@test.com";
        loyaltyService.creditPoints(buildPayment(email, 60.0));
        loyaltyService.creditPoints(buildPayment(email, 60.0));

        List<PromoCode> codes = loyaltyService.getLoyaltyCodesByUser(email);
        assertThat(codes).hasSize(1);
        assertThat(codes.get(0).getValue()).isEqualTo(10.0);
        assertThat(codes.get(0).getType()).isEqualTo(PromoCodeType.PERCENTAGE);
    }

    @Test
    void testJalon4Events_genereCodePromo30Pourcent() {
        String email = "jalon4@test.com";
        for (int i = 0; i < 4; i++) {
            loyaltyService.creditPoints(buildPayment(email, 40.0));
        }

        List<PromoCode> codes = loyaltyService.getLoyaltyCodesByUser(email);
        // 2 codes : jalon 2 + jalon 4
        assertThat(codes).hasSize(2);
        boolean has30 = codes.stream().anyMatch(c -> c.getValue() == 30.0);
        assertThat(has30).isTrue();
    }

    @Test
    void testJalonsNonDupliques_codeGeneréUneSeuleFois() {
        String email = "noduplicate@test.com";
        for (int i = 0; i < 4; i++) {
            loyaltyService.creditPoints(buildPayment(email, 50.0));
        }
        // 2 paiements supplémentaires : les jalons ne doivent pas régénérer
        loyaltyService.creditPoints(buildPayment(email, 50.0));
        loyaltyService.creditPoints(buildPayment(email, 50.0));

        List<PromoCode> codes = loyaltyService.getLoyaltyCodesByUser(email);
        long codes10 = codes.stream().filter(c -> c.getValue() == 10.0).count();
        long codes30 = codes.stream().filter(c -> c.getValue() == 30.0).count();
        assertThat(codes10).isEqualTo(1); // généré une seule fois
        assertThat(codes30).isEqualTo(1); // généré une seule fois
    }

    // ── GET ACCOUNT ─────────────────────────────────────────────────────────

    @Test
    void testGetAccount_compteInexistant_retourneNull() {
        LoyaltyAccount account = loyaltyService.getAccount("inconnu@test.com");
        assertThat(account).isNull();
    }

    // ── NULL SAFETY ─────────────────────────────────────────────────────────

    @Test
    void testCreditPoints_emailNull_sansException() {
        Payment payment = new Payment();
        payment.setParticipantEmail(null);
        // ne doit pas lever d'exception
        loyaltyService.creditPoints(payment);

        assertThat(loyaltyAccountRepository.findAll()).isEmpty();
    }

    // ── CODES DE FIDÉLITÉ GLOBAUX ───────────────────────────────────────────

    @Test
    void testGetAllLoyaltyCodes_retourneTousLesCodesPersonnels() {
        String email1 = "user1@test.com";
        String email2 = "user2@test.com";
        for (int i = 0; i < 2; i++) {
            loyaltyService.creditPoints(buildPayment(email1, 50.0));
        }
        for (int i = 0; i < 2; i++) {
            loyaltyService.creditPoints(buildPayment(email2, 50.0));
        }

        List<PromoCode> allCodes = loyaltyService.getAllLoyaltyCodes();
        assertThat(allCodes).hasSizeGreaterThanOrEqualTo(2);
        allCodes.forEach(c -> assertThat(c.getOwnerEmail()).isNotNull());
    }
}
