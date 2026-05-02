package esprit.tn.junglepayments.controller;

import esprit.tn.junglepayments.DTO.EventResponse;
import esprit.tn.junglepayments.DTO.PaymentRequest;
import esprit.tn.junglepayments.entities.Payment;
import esprit.tn.junglepayments.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<Payment> createPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(toEntity(request)));
    }

    @PostMapping("/confirm-stripe")
    public ResponseEntity<Payment> confirmStripe(
            @RequestParam String paymentIntentId,
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.confirmStripePayment(paymentIntentId, toEntity(request)));
    }

    @GetMapping("/all")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id,
                                                 @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.updatePayment(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<Payment> updateStatus(@PathVariable Long id,
                                                @PathVariable String status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }

    @GetMapping("/event-details/{eventId}")
    public ResponseEntity<EventResponse> getEventDetails(@PathVariable Long eventId) {
        return ResponseEntity.ok(paymentService.getEventDetails(eventId));
    }

    private Payment toEntity(PaymentRequest req) {
        Payment payment = new Payment();
        payment.setEventId(req.getEventId());
        payment.setParticipantName(req.getParticipantName());
        payment.setParticipantEmail(req.getParticipantEmail());
        payment.setAmount(req.getAmount());
        payment.setOriginalAmount(req.getOriginalAmount());
        payment.setDiscountAmount(req.getDiscountAmount());
        payment.setPromoCode(req.getPromoCode());
        payment.setPaymentMethod(req.getPaymentMethod());
        if (req.getStatus() != null) payment.setStatus(req.getStatus());
        payment.setReference(req.getReference());
        payment.setParticipantId(req.getParticipantId());
        payment.setSessionId(req.getSessionId());
        payment.setParticipationStatus(req.getParticipationStatus());
        payment.setStripePaymentIntentId(req.getStripePaymentIntentId());
        return payment;
    }
}
