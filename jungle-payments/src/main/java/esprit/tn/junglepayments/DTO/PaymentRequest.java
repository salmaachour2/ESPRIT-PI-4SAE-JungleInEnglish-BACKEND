package esprit.tn.junglepayments.DTO;

import esprit.tn.junglepayments.entities.PaymentMethod;
import esprit.tn.junglepayments.entities.PaymentStatus;

public class PaymentRequest {

    private Long eventId;
    private String participantName;
    private String participantEmail;
    private Double amount;
    private Double originalAmount;
    private Double discountAmount;
    private String promoCode;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String reference;
    private Long participantId;
    private Long sessionId;
    private String participationStatus;
    private String stripePaymentIntentId;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public String getParticipantEmail() { return participantEmail; }
    public void setParticipantEmail(String participantEmail) { this.participantEmail = participantEmail; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Double getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(Double originalAmount) { this.originalAmount = originalAmount; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getParticipationStatus() { return participationStatus; }
    public void setParticipationStatus(String participationStatus) { this.participationStatus = participationStatus; }
    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }
}
