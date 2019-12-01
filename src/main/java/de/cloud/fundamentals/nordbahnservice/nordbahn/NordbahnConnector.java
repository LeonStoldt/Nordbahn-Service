package de.cloud.fundamentals.nordbahnservice.nordbahn;

import de.cloud.fundamentals.nordbahnservice.XmlParser;
import de.cloud.fundamentals.nordbahnservice.userfeedback.I18n;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;

@Service
public class NordbahnConnector {

    private static final I18n USER_FEEDBACK = new I18n();

    public String getNordbahnMessage(String messageText) {
        String response;
        try {
            Station station = Station.ofMessage(messageText);
            response = new XmlParser().readXml(station);
        } catch (IllegalArgumentException | XMLStreamException e) {
            response = USER_FEEDBACK.get("message.default");
        }
        return response;
    }
}
