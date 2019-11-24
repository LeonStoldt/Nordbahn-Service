package de.cloud.fundamentals.nordbahnservice.nordbahn;

import de.cloud.fundamentals.nordbahnservice.XmlParser;
import org.springframework.stereotype.Service;
import userfeedback.I18n;

@Service
public class NordbahnConnector {

    private static final I18n USER_FEEDBACK = new I18n("messages");

    public String getNordbahnMessage(String messageText) {
        String message = USER_FEEDBACK.get("message.default");
        String[] words = messageText.split("\\s+");
        int position = 0;
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("nb")) { // should be linked with commands in telegram connector
                position = i;
            }
        }
        Station destination;

        StringBuilder searchString = new StringBuilder();
        int maxPosition = Math.min(position + 5, words.length);
        for (int j = position + 1; j < maxPosition; j++) {
            try {
                if (j != position + 1) {
                    searchString
                            .append("_")
                            .append(words[j].toUpperCase());
                } else {
                    searchString.append(words[j].toUpperCase());
                }
                destination = Station.valueOf(searchString.toString());
                message = getNordbahnInformation(destination);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    private String getNordbahnInformation(Station station) {
        String data = "";
        try {
            XmlParser parser = new XmlParser();
            data = parser.readXml(station.abbr());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
