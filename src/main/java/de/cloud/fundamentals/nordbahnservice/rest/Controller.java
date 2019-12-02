package de.cloud.fundamentals.nordbahnservice.rest;

import de.cloud.fundamentals.nordbahnservice.nordbahn.NordbahnConnector;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final NordbahnConnector connector;

    public Controller(NordbahnConnector connector) {
        this.connector = connector;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/", produces = JSON)
    public String getStatus() {
        return "NordbahnService is active.";
    }

    @PostMapping(value = "/api", produces = JSON, consumes = JSON)
    public ResponseEntity<String> receiveRequest(@RequestBody String message) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(connector.getNordbahnMessage(message));
    }
}
