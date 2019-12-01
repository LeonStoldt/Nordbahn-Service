package de.cloud.fundamentals.nordbahnservice;

import de.cloud.fundamentals.nordbahnservice.nordbahn.Station;
import de.cloud.fundamentals.nordbahnservice.nordbahn.Train;
import de.cloud.fundamentals.nordbahnservice.userfeedback.I18n;
import org.javatuples.Pair;

import javax.validation.constraints.NotNull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * example response of website:
 * <auskunft>
 *      <name>Pinneberg</name>
 *      <stand>11:08</stand>
 *      <abfahrt>
 *          <zeit>11:25</zeit>
 *          <zug>75520</zug>
 *          <linie>RB 61</linie>
 *          <ziel id="AIZ">Itzehoe</ziel>
 *          <prognose>pünktlich</prognose>
 *          <prognosemin>0</prognosemin>
 *          <gleis>3</gleis>
 *          <ausfall>false</ausfall>
 *          <sev>false</sev>
 *      </abfahrt>
 *      [...]
 * <auskunft>
 */
public class XmlParser {

    private static final I18n USER_FEEDBACK = new I18n();
    private static final String NORDBAHN_URL_STATION_KEY = "{station}";
    private static final String NORDBAHN_BASE_URL = "https://datnet-nbe.etc-consult.de/datnet-nbe/xml?bhf=" + NORDBAHN_URL_STATION_KEY + "&id_prod=DPN,Bus,DPN-G&format=xml&callback=?";
    private static final int ONE_HOUR_IN_MILLIS = 3600000;
    public static final String INQUIRY_TAG = "auskunft";
    public static final String STATION_NAME_TAG = "name";
    public static final String TIME_NOW_TAG = "stand";
    public static final String DEPARTURE_TAG = "abfahrt";
    public static final String TRAIN_TIME_TAG = "zeit";
    public static final String TRAIN_LINE_TAG = "linie";
    public static final String TRAIN_DESTINATION_TAG = "ziel";
    public static final String TRAIN_FORECAST_TAG = "prognose";
    public static final String TRAIN_FORECAST_MIN_TAG = "prognosemin";
    public static final String TRAIN_RAIL_TAG = "gleis";
    public static final String TRAIN_CANCELLED_TAG = "ausfall";
    public static final String TRAIN_SHUTTLE_SERVICE_TAG = "sev";

    private final XMLInputFactory factory;
    private final SimpleDateFormat parser;

    public XmlParser() {
        this.parser = new SimpleDateFormat("HH:mm");
        this.factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    }

    public String readXml(Station station) throws XMLStreamException {
        XMLStreamReader xmlReader = null;
        try {
            xmlReader = factory.createXMLStreamReader(getXmlFromUrl(station));
            return readDocument(xmlReader);
        } catch (IOException | ParseException e) {
            throw new XMLStreamException("failed parsing with exception:", e);
        } finally {
            if (xmlReader != null) xmlReader.close();
        }
    }

    public String readDocument(XMLStreamReader xmlReader) throws ParseException, XMLStreamException {
        String stationName = "";
        Date time = null;
        List<Train> trains = new ArrayList<>();
        StringBuilder data = new StringBuilder();

        while (xmlReader.hasNext()) {
            int next = xmlReader.next();
            if (next == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals(INQUIRY_TAG)) {
                Pair<Metadata, List<Train>> document = readContent(xmlReader);
                stationName = document.getValue0().getStation();
                time = document.getValue0().getTimeNow();
                trains = document.getValue1();
            }
        }
        trains.forEach(data::append);

        if (!(stationName.equals("") || time == null)) {
            return USER_FEEDBACK.format("message.response", stationName, parser.format(time), data);
        }
        throw new XMLStreamException("invalid parsing values");
    }

    private static class Metadata {

        private final String station;
        private final Date timeNow;

        public Metadata(String station, Date timeNow) {
            this.station = station;
            this.timeNow = timeNow;
        }

        public String getStation() {
            return station;
        }

        public Date getTimeNow() {
            return timeNow;
        }
    }

    private Pair<Metadata, List<Train>> readContent(XMLStreamReader reader) throws XMLStreamException, ParseException {
        List<Train> trains = new ArrayList<>();
        String station = "";
        Date timeNow = Date.from(Instant.now());

        while (reader.hasNext()) {
            int next = reader.next();
            if (next == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case STATION_NAME_TAG:
                        station = reader.getElementText();
                        break;
                    case TIME_NOW_TAG:
                        timeNow = parser.parse(reader.getElementText());
                        break;
                    case DEPARTURE_TAG:
                        Optional<Train> optionalTrain = readTrain(reader, timeNow);
                        optionalTrain.ifPresent(trains::add);
                        break;
                    default:
                        break;
                }
            } else if (next == XMLStreamConstants.END_ELEMENT) {
                return new Pair<>(new Metadata(station, timeNow), trains);
            }
        }
        throw new XMLStreamException("Premature end of file");
    }

    private Optional<Train> readTrain(XMLStreamReader reader, @NotNull Date timeNow) throws XMLStreamException, ParseException {
        Date trainTme = null;
        String trainLine = null;
        Station destination = null;
        String forecast = null;
        int forecastMin = -1;
        int rail = -1;
        boolean cancelled = false;
        boolean shuttleService = false;
        boolean isTrainOfNextHour = false;

        while (reader.hasNext()) {

            int next = reader.next();
            if (next == XMLStreamConstants.START_ELEMENT) {

                if (trainTme == null && reader.getLocalName().equals(TRAIN_TIME_TAG)) {
                    trainTme = parser.parse(reader.getElementText());
                    isTrainOfNextHour = isTrainOfNextHour(trainTme, timeNow);

                } else if (trainTme != null && isTrainOfNextHour) {
                    switch (reader.getLocalName()) {
                        case TRAIN_LINE_TAG:
                            trainLine = reader.getElementText();
                            break;
                        case TRAIN_DESTINATION_TAG:
                            destination = Station.ofMessage(reader.getElementText().toLowerCase());
                            break;
                        case TRAIN_FORECAST_TAG:
                            forecast = reader.getElementText();
                            break;
                        case TRAIN_FORECAST_MIN_TAG:
                            forecastMin = Integer.parseInt(reader.getElementText());
                            break;
                        case TRAIN_RAIL_TAG:
                            rail = Integer.parseInt(reader.getElementText());
                            break;
                        case TRAIN_CANCELLED_TAG:
                            cancelled = Boolean.parseBoolean(reader.getElementText());
                            break;
                        case TRAIN_SHUTTLE_SERVICE_TAG:
                            shuttleService = Boolean.parseBoolean(reader.getElementText());
                            break;
                        default:
                            break;
                    }
                }
            } else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(DEPARTURE_TAG)) {
                return isTrainOfNextHour
                        ? Optional.of(new Train(trainTme, trainLine, destination, forecast, forecastMin, rail, cancelled, shuttleService))
                        : Optional.empty();
            }
        }
        throw new XMLStreamException("Premature end of file");
    }

    private boolean isTrainOfNextHour(@NotNull Date trainTime, @NotNull Date timeNow) {
        return trainTime.compareTo(new Date(timeNow.getTime() + ONE_HOUR_IN_MILLIS)) <= 0;
    }

    private InputStream getXmlFromUrl(Station station) throws IOException {
        return new URL(getUrl(station)).openStream();
    }

    private String getUrl(Station station) {
        return NORDBAHN_BASE_URL.replace(NORDBAHN_URL_STATION_KEY, station.toString());
    }
}
