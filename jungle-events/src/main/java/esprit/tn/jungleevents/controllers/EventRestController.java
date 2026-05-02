package esprit.tn.jungleevents.controllers;

import esprit.tn.jungleevents.DTO.EventRequest;
import esprit.tn.jungleevents.DTO.EventResponse;
import esprit.tn.jungleevents.entities.Event;
import esprit.tn.jungleevents.services.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    @Autowired
    private EventService eventService;

    @PostMapping(value = "/addEvent", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addEvent(
            @Valid @ModelAttribute EventRequest request,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws Exception {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            return ResponseEntity.badRequest().body("La date de fin doit être après la date de début");
        }

        Event event = toEntity(request);

        if (file != null && !file.isEmpty()) {
            String fileName = saveFile(file);
            event.setMediaFileName(fileName);
        }

        return ResponseEntity.ok(eventService.createEvent(event));
    }

    @GetMapping("/getAllEvents")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("getEventById/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PutMapping(value = "updateEventById/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute EventRequest request,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws Exception {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }

        Event event = toEntity(request);

        if (file != null && !file.isEmpty()) {
            String fileName = saveFile(file);
            event.setMediaFileName(fileName);
        }

        return ResponseEntity.ok(eventService.updateEvent(id, event));
    }

    @DeleteMapping("deleteEventById/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<EventResponse> getEventForPayment(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        EventResponse dto = new EventResponse();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setPrice(event.getPrice());
        dto.setLocation(event.getLocation());
        dto.setStatus(event.getStatus() != null ? event.getStatus().name() : null);
        return ResponseEntity.ok(dto);
    }

    private Event toEntity(EventRequest req) {
        Event event = new Event();
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setCategory(req.getCategory());
        event.setLocation(req.getLocation());
        event.setMeetLink(req.getMeetLink());
        event.setFormat(req.getFormat());
        event.setStatus(req.getStatus());
        event.setPrice(req.getPrice());
        event.setMaxParticipants(req.getMaxParticipants());
        event.setStartDate(req.getStartDate());
        event.setEndDate(req.getEndDate());
        event.setLatitude(req.getLatitude());
        event.setLongitude(req.getLongitude());
        return event;
    }

    private String saveFile(MultipartFile file) throws Exception {
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        file.transferTo(new File(uploadDir + fileName));
        return fileName;
    }
}
