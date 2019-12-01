package de.cloud.fundamentals.nordbahnservice.nordbahn;

import de.cloud.fundamentals.nordbahnservice.userfeedback.I18n;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Train {

    private static final I18n USER_FEEDBACK = new I18n();
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
        String result = USER_FEEDBACK.format("message.train", dateFormat.format(time), trainLine, destination.getOfficialName());

        if (cancelled) {
            result += (shuttleService)
                    ? USER_FEEDBACK.get("message.shuttle-service.true")
                    : USER_FEEDBACK.get("message.shuttle-service.false");
        } else {
            boolean delayed = forecastMin > 5;
            result += (delayed)
                    ? USER_FEEDBACK.format("message.forecast.delayed.true", forecastMin)
                    : USER_FEEDBACK.format("message.forecast.delayed.false", forecastMin);

            if (rail < MAX_RAIL_NUMBER) result += USER_FEEDBACK.format("message.rail", rail);

            if (delayed) {
                String newArrivalTime = dateFormat.format(new Date(time.getTime() + forecastMin * ONE_MINUTE_IN_MILLIS));
                result += USER_FEEDBACK.format("message.delayed.new-time", newArrivalTime);
            }

        }
        return result + "\n";
    }
}
