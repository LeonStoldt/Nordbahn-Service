package de.cloud.fundamentals.nordbahnservice.rest;

import de.cloud.fundamentals.nordbahnservice.nordbahn.NordbahnConnector;
import dto.DataTransferObject;
import dto.Request;
import dto.Response;
import io.jsonwebtoken.JwtException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import security.JwsHelper;

import java.net.URI;
import java.util.List;

@RestController
public class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String NORDBAHN_SERVICE = "nordbahnService";
    private static final String HEADER_TOKEN_KEY = "token";

    private final NordbahnConnector connector;
    private final JwsHelper jwsHelper;

    public Controller(NordbahnConnector connector) {
        this.connector = connector;
        this.jwsHelper = new JwsHelper();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/", produces = JSON)
    public String getStatus() {
        return "NordbahnService is active.";
    }

    @PostMapping(value = "/api", consumes = JSON, produces = JSON)
    public void receiveRequest(@RequestHeader HttpHeaders header, @RequestBody Request dto) {
        Response response;
        List<String> tokenValue = header.get(HEADER_TOKEN_KEY);

        if (tokenValue != null && !tokenValue.isEmpty()) {
            String jws = tokenValue.get(0);

            if (jwsHelper.isValid(dto, jws)) {
                try {
                    String responseMessage = connector.getNordbahnMessage(dto.getMessage());
                    response = new Response(NORDBAHN_SERVICE, dto.getSource(), dto.getChatId(), responseMessage);
                    postReply(response, URI.create("http//localhost:8443/api")); //change later
                } catch (JwtException e) {
                    LOGGER.warn("invalid jws ({})", jws, e);
                }
            }
        }
    }

    private void postReply(Response dto, URI serviceUri) {
        try {
            CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build(); //change verifier later
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            RequestEntity<Response> responseDto = new RequestEntity<>(dto, getHeader(dto), HttpMethod.POST, serviceUri, Response.class);
            ResponseEntity<Response> response = new RestTemplate(requestFactory).exchange(responseDto, Response.class);

            if (isStatusCodeOk(response.getStatusCodeValue())) {
                LOGGER.info("sent response with status code 200");
            } else {
                LOGGER.warn("sending failed for response {}", dto.toMap());
                //retry?
            }
        } catch (Exception e) {
            LOGGER.warn("could not send message due to exception", e);
        }
    }

    private HttpHeaders getHeader(DataTransferObject dto) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.add(HEADER_TOKEN_KEY, jwsHelper.generateToken(dto));
        return header;
    }

    private boolean isStatusCodeOk(int statusCode) {
        return HttpStatus.valueOf(statusCode).equals(HttpStatus.OK);
    }

}
