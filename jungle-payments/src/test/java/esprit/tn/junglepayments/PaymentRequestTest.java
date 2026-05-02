package esprit.tn.junglepayments;

import esprit.tn.junglepayments.DTO.PaymentRequest;
import esprit.tn.junglepayments.entities.PaymentMethod;
import esprit.tn.junglepayments.entities.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

    @Test
    void testGettersAndSetters() {
        PaymentRequest req = new PaymentRequest();

        req.setEventId(10L);
        req.setParticipantName("Salma Achour");
        req.setParticipantEmail("salma@test.com");
        req.setAmount(150.0);
        req.setOriginalAmount(200.0);
        req.setDiscountAmount(50.0);
        req.setPromoCode("PROMO10");
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setStatus(PaymentStatus.PENDING);
        req.setReference("PAY-2026-001");
        req.setParticipantId(5L);
        req.setSessionId(3L);
        req.setParticipationStatus("REGISTERED");
        req.setStripePaymentIntentId("pi_test_123");

        assertEquals(10L, req.getEventId());
        assertEquals("Salma Achour", req.getParticipantName());
        assertEquals("salma@test.com", req.getParticipantEmail());
        assertEquals(150.0, req.getAmount());
        assertEquals(200.0, req.getOriginalAmount());
        assertEquals(50.0, req.getDiscountAmount());
        assertEquals("PROMO10", req.getPromoCode());
        assertEquals(PaymentMethod.CREDIT_CARD, req.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, req.getStatus());
        assertEquals("PAY-2026-001", req.getReference());
        assertEquals(5L, req.getParticipantId());
        assertEquals(3L, req.getSessionId());
        assertEquals("REGISTERED", req.getParticipationStatus());
        assertEquals("pi_test_123", req.getStripePaymentIntentId());
    }

    @Test
    void testNullValues() {
        PaymentRequest req = new PaymentRequest();

        assertNull(req.getEventId());
        assertNull(req.getParticipantName());
        assertNull(req.getParticipantEmail());
        assertNull(req.getAmount());
        assertNull(req.getOriginalAmount());
        assertNull(req.getDiscountAmount());
        assertNull(req.getPromoCode());
        assertNull(req.getPaymentMethod());
        assertNull(req.getStatus());
        assertNull(req.getReference());
        assertNull(req.getParticipantId());
        assertNull(req.getSessionId());
        assertNull(req.getParticipationStatus());
        assertNull(req.getStripePaymentIntentId());
    }
}
