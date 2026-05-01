package esprit.tn.junglepayments;

import esprit.tn.junglepayments.DTO.PromoCodeValidationRequest;
import esprit.tn.junglepayments.DTO.PromoCodeValidationResponse;
import esprit.tn.junglepayments.entities.PromoCode;
import esprit.tn.junglepayments.entities.PromoCodeType;
import esprit.tn.junglepayments.repositories.EventFeignClient;
import esprit.tn.junglepayments.repositories.PromoCodeRepository;
import esprit.tn.junglepayments.repositories.UserFeignClient;
import esprit.tn.junglepayments.services.PromoCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du service PromoCode avec base H2 réelle.
 * Le repository PromoCodeRepository n'est PAS mocké.
 * Seuls les Feign clients (services externes) sont mockés.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PromoCodeServiceIntegrationTest {

    @Autowired
    private PromoCodeService promoCodeService;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @MockBean
    private EventFeignClient eventFeignClient;

    @MockBean
    private UserFeignClient userFeignClient;

    @BeforeEach
    void nettoyerBase() {
        promoCodeRepository.deleteAll();
    }

    private PromoCode buildPromoCode(String code, PromoCodeType type, Double value) {
        PromoCode promo = new PromoCode();
        promo.setCode(code);
        promo.setType(type);
        promo.setValue(value);
        promo.setMaxUses(10);
        promo.setUsesCount(0);
        promo.setExpirationDate(LocalDate.now().plusMonths(1));
        return promo;
    }

    // ── CODE VALIDE ─────────────────────────────────────────────────────────

    @Test
    void testCodePourcentage_calculReductionCorrect() {
        promoCodeRepository.save(buildPromoCode("PROMO20", PromoCodeType.PERCENTAGE, 20.0));

        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("PROMO20");
        req.setAmount(100.0);

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isTrue();
        assertThat(resp.getDiscountAmount()).isEqualTo(20.0);
        assertThat(resp.getFinalAmount()).isEqualTo(80.0);
    }

    @Test
    void testCodeFixe_calculReductionCorrect() {
        PromoCode promo = buildPromoCode("REMISE15", PromoCodeType.AMOUNT, 15.0);
        promoCodeRepository.save(promo);

        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("REMISE15");
        req.setAmount(100.0);

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isTrue();
        assertThat(resp.getDiscountAmount()).isEqualTo(15.0);
        assertThat(resp.getFinalAmount()).isEqualTo(85.0);
    }

    @Test
    void testCodeInsensibleCasse_validationReussit() {
        promoCodeRepository.save(buildPromoCode("SUMMER10", PromoCodeType.PERCENTAGE, 10.0));

        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("summer10");
        req.setAmount(200.0);

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isTrue();
    }

    // ── CODE EXPIRÉ ─────────────────────────────────────────────────────────

    @Test
    void testCodeExpire_retourneInvalide() {
        PromoCode promo = buildPromoCode("EXPIREDCODE", PromoCodeType.PERCENTAGE, 10.0);
        promo.setExpirationDate(LocalDate.now().minusDays(1)); // expiré hier
        promoCodeRepository.save(promo);

        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("EXPIREDCODE");
        req.setAmount(100.0);

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isFalse();
        assertThat(resp.getMessage()).containsIgnoringCase("expiré");
    }

    // ── QUOTA ÉPUISÉ ────────────────────────────────────────────────────────

    @Test
    void testCodeUsageMaxAtteint_retourneInvalide() {
        PromoCode promo = buildPromoCode("FULLUSED", PromoCodeType.PERCENTAGE, 10.0);
        promo.setMaxUses(3);
        promo.setUsesCount(3); // quota épuisé
        promoCodeRepository.save(promo);

        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("FULLUSED");
        req.setAmount(100.0);

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isFalse();
    }

    // ── CODE INEXISTANT ─────────────────────────────────────────────────────

    @Test
    void testCodeInexistant_retourneInvalide() {
        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("FANTOME99");
        req.setAmount(50.0);

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isFalse();
    }

    @Test
    void testCodeVide_retourneInvalide() {
        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("");
        req.setAmount(50.0);

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isFalse();
    }

    // ── MONTANT MINIMUM ─────────────────────────────────────────────────────

    @Test
    void testMontantMinimumNonAtteint_retourneInvalide() {
        PromoCode promo = buildPromoCode("BIGORDER", PromoCodeType.PERCENTAGE, 15.0);
        promo.setMinAmount(200.0);
        promoCodeRepository.save(promo);

        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("BIGORDER");
        req.setAmount(50.0); // < 200

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isFalse();
        assertThat(resp.getMessage()).containsIgnoringCase("minimum");
    }

    // ── INCREMENT USES ──────────────────────────────────────────────────────

    @Test
    void testIncrementUses_compteurAugmente() {
        promoCodeRepository.save(buildPromoCode("INCTEST", PromoCodeType.AMOUNT, 5.0));

        promoCodeService.incrementUses("INCTEST");

        PromoCode updated = promoCodeRepository.findByCodeIgnoreCase("INCTEST").orElseThrow();
        assertThat(updated.getUsesCount()).isEqualTo(1);
    }

    // ── CODE LIÉ À UN ÉVÉNEMENT ────────────────────────────────────────────

    @Test
    void testCodeLieAUnEvent_validePourCetEvent() {
        PromoCode promo = buildPromoCode("EVENT5OFF", PromoCodeType.PERCENTAGE, 5.0);
        promo.setEventId(42L);
        promoCodeRepository.save(promo);

        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("EVENT5OFF");
        req.setEventId(42L);
        req.setAmount(100.0);

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isTrue();
    }

    // ── RÉDUCTION NE DÉPASSE PAS LE MONTANT ────────────────────────────────

    @Test
    void testReductionFixeSuperieureAuMontant_totalNePasNegatif() {
        PromoCode promo = buildPromoCode("BIG50", PromoCodeType.AMOUNT, 50.0);
        promo.setExpirationDate(null); // pas d'expiration
        promoCodeRepository.save(promo);

        PromoCodeValidationRequest req = new PromoCodeValidationRequest();
        req.setPromoCode("BIG50");
        req.setAmount(30.0); // réduction > montant

        PromoCodeValidationResponse resp = promoCodeService.validatePromoCode(req);

        assertThat(resp.isValid()).isTrue();
        assertThat(resp.getFinalAmount()).isGreaterThanOrEqualTo(0.0);
    }
}
