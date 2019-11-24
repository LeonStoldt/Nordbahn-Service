package de.cloud.fundamentals.nordbahnservice;

import de.cloud.fundamentals.nordbahnservice.nordbahn.Station;
import de.cloud.fundamentals.nordbahnservice.nordbahn.Train;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XmlParser {

    private static final String NORDBAHN_BASE_URL = "https://datnet-nbe.etc-consult.de/datnet-nbe/xml?bhf={station}&id_prod=DPN,Bus,DPN-G&format=xml&callback=?";
    private static final int ONE_HOUR_IN_MILLIS = 3600000;
    public static final String DEPARTURE = "abfahrt";
    public static final int TRAINLINE_INDEX = 2;
    public static final int DESTINATION_INDEX = 3;
    public static final int FORECAST_INDEX = 4;
    public static final int FORECAST_MINUTES_INDEX = 5;
    public static final int RAIL_INDEX = 6;
    public static final int CANCELLED_INDEX = 7;
    public static final int SHUTTLE_SERVICE_INDEX = 8;

    private DocumentBuilder builder;

    public XmlParser() throws ParserConfigurationException {
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    private String getUrl(String station) {
        return NORDBAHN_BASE_URL.replace("{station}", station);
    }

    public String readXml(String message) throws IOException, SAXException, ParseException {
        Document document = builder.parse(getXmlFromUrl(message));
        document.getDocumentElement().normalize();
        
        NodeList childNodes = document.getDocumentElement().getChildNodes();
        String station = childNodes.item(0).getTextContent();
        String time = childNodes.item(1).getTextContent();
        List<Train> trains = new ArrayList<>();
        
        for (int i = 2; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeName().equals(DEPARTURE)) {

                NodeList details = childNodes.item(i).getChildNodes();

                SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
                Date trainTime = parser.parse(details.item(0).getTextContent());
                Date timeNow = parser.parse(time);

                if (isTrainOfNextHour(trainTime, timeNow)) {
                    final String trainLine = details.item(TRAINLINE_INDEX).getTextContent();
                    final Station destination = Station
                            .valueOf(details.item(DESTINATION_INDEX)
                            .getTextContent()
                            .toUpperCase()
                            .replace(" ", "_")
                            .replace("-", "_"));
                    final String forecast = details.item(FORECAST_INDEX).getTextContent();
                    final int forecastMin = Integer.parseInt(details.item(FORECAST_MINUTES_INDEX).getTextContent());
                    final int rail = Integer.parseInt(details.item(RAIL_INDEX).getTextContent());
                    boolean cancelled = Boolean.parseBoolean(details.item(CANCELLED_INDEX).getTextContent());
                    boolean shuttleService = Boolean.parseBoolean(details.item(SHUTTLE_SERVICE_INDEX).getTextContent());

                    trains.add(new Train(trainTime, trainLine, destination, forecast, forecastMin, rail, cancelled, shuttleService));
                }
            }
        }

        StringBuilder data = new StringBuilder();
        trains.forEach(data::append);
        return "*Anfrage des Bahnhofs " + station + " um " + time + ":*\n" + data;
    }

    private boolean isTrainOfNextHour(Date trainTime, Date timeNow) {
        return trainTime.compareTo(new Date(timeNow.getTime() + ONE_HOUR_IN_MILLIS)) <= 0;
    }

    private InputStream getXmlFromUrl(String station) throws IOException {
        return new URL(getUrl(station)).openStream();
    }

}
