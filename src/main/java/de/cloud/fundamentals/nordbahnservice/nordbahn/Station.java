package de.cloud.fundamentals.nordbahnservice.nordbahn;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

public enum Station {
    // first name should be official name
    AH("hamburg hauptbahnhof", "hamburg hbf", "hbf"),
    ADF("hamburg dammtor", "dammtor"),
    AA("hamburg altona", "altona"),
    AP("pinneberg", "pb"),
    APD("prisdorf"),
    ATM("tornesch"),
    AEL("elmshorn", "elmo"),
    AHZH("herzhorn"),
    AGST("glückstadt", "glueckstadt"),
    AKM("krempe"),
    AKHD("kremperheide"),
    AIZ("itzehoe"),
    AHOT("horst"),
    ADH("dauenhof"),
    AWST("wrist");

    private List<String> keywords;

    Station(@NotNull String... keywords) {
        this.keywords = Arrays.asList(keywords);
    }

    public List<String> keywords() {
        return keywords;
    }

    public static Station ofMessage(String message) {
        return Arrays
                .stream(Station.values())
                .filter(station -> station.keywords().stream().anyMatch(message::contains))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getOfficialName() {
        return keywords()
                .stream()
                .findFirst()
                .map(this::normalizeDesignation)
                .orElse("");
    }

    private String normalizeDesignation(String designation) {
        return Arrays
                .stream(designation.split("\\s+"))
                .map(this::normalizeWord)
                .reduce((word1, word2) -> String.join(" ", word1, word2))
                .orElse("");
    }

    private String normalizeWord(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
