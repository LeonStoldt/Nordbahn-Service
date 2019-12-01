package de.cloud.fundamentals.nordbahnservice.nordbahn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Train {

    private static final int ONE_MINUTE_IN_MILLIS = 60000;
    private static final int MAX_RAIL_NUMBER = 14;

    private final Date time;
    private final String trainLine;
    private final Station destination;
    private final String forecast;
    private final int forecastMin;
    private final int rail;
    private final boolean cancelled;
    private final boolean shuttleService;

    public Train(Date time, String trainLine, Station destination, String forecast, int forecastMin, int rail, boolean cancelled, boolean shuttleService) {
        this.time = time;
        this.trainLine = trainLine;
        this.destination = destination;
        this.forecast = forecast;
        this.forecastMin = forecastMin;
        this.rail = rail;
        this.cancelled = cancelled;
        this.shuttleService = shuttleService;
    }

    public Date getTime() {
        return time;
    }

    public String getTrainLine() {
        return trainLine;
    }

    public Station getDestination() {
        return destination;
    }

    public String getForecast() {
        return forecast;
    }

    public int getForecastMin() {
        return forecastMin;
    }

    public int getRail() {
        return rail;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isShuttleService() {
        return shuttleService;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String result = "\n*" + dateFormat.format(time) + "* - " + trainLine + " nach " + destination.getOfficialName();

        if (cancelled) {
            result += (shuttleService)
                    ? ("\n*ACHTUNG:* Zug fällt aus. Schienenersatzverkehr ist eingerichtet.")
                    : ("\n*ACHTUNG:* Zug fällt aus. Es ist kein Schienenersatzverkehr eingerichtet.");
        } else {
            boolean delayed = forecastMin > 5;
            result += (delayed)
                    ? (" (*+" + forecastMin + "min*)\n")
                    : (" (+" + forecastMin + "min)\n");

            if (rail < MAX_RAIL_NUMBER) result += "fährt von Gleis: " + rail;

            if (delayed)
                result += " | voraussichtlich " + dateFormat.format(new Date(time.getTime() + forecastMin * ONE_MINUTE_IN_MILLIS));
        }
        return result + "\n";
    }
}
