package esprit.tn.jungleevents;

import esprit.tn.jungleevents.DTO.EventRequest;
import esprit.tn.jungleevents.entities.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EventRequestTest {

    @Test
    void testGettersAndSetters() {
        EventRequest req = new EventRequest();

        req.setTitle("Test Event");
        req.setDescription("Description longue pour le test unitaire");
        req.setCategory("Tech");
        req.setLocation("Tunis");
        req.setMeetLink("https://meet.google.com/abc");
        req.setFormat("ONLINE");
        req.setStatus(EventStatus.PUBLISHED);
        req.setPrice(50.0);
        req.setMaxParticipants(100);

        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 2);
        req.setStartDate(start);
        req.setEndDate(end);
        req.setLatitude(36.8);
        req.setLongitude(10.2);

        assertEquals("Test Event", req.getTitle());
        assertEquals("Description longue pour le test unitaire", req.getDescription());
        assertEquals("Tech", req.getCategory());
        assertEquals("Tunis", req.getLocation());
        assertEquals("https://meet.google.com/abc", req.getMeetLink());
        assertEquals("ONLINE", req.getFormat());
        assertEquals(EventStatus.PUBLISHED, req.getStatus());
        assertEquals(50.0, req.getPrice());
        assertEquals(100, req.getMaxParticipants());
        assertEquals(start, req.getStartDate());
        assertEquals(end, req.getEndDate());
        assertEquals(36.8, req.getLatitude());
        assertEquals(10.2, req.getLongitude());
    }

    @Test
    void testNullValues() {
        EventRequest req = new EventRequest();

        assertNull(req.getTitle());
        assertNull(req.getDescription());
        assertNull(req.getCategory());
        assertNull(req.getLocation());
        assertNull(req.getMeetLink());
        assertNull(req.getFormat());
        assertNull(req.getStatus());
        assertNull(req.getPrice());
        assertNull(req.getMaxParticipants());
        assertNull(req.getStartDate());
        assertNull(req.getEndDate());
        assertNull(req.getLatitude());
        assertNull(req.getLongitude());
    }
}
