package de.cloud.fundamentals.nordbahnservice.nordbahn;

public enum Station {
    HAMBURG_HBF("AH"), HBF("AH"),
    HAMBURG_DAMMTOR("ADF"), DAMMTOR("ADF"),
    HAMBURG_ALTONA("AA"), ALTONA("AA"),
    PINNEBERG("AP"), PB("AP"),
    PRISDORF("APD"),
    TORNESCH("ATM"),
    ELMSHORN("AEL"),
    HERZHORN("AHZH"),
    GLÜCKSTADT("AGST"), GLUECKSTADT("AGST"),
    KREMPE("AKM"),
    KREMPERHEIDE("AKHD"),
    ITZEHOE("AIZ"),
    HORST("AHOT"),
    DAUENHOF("ADH"),
    WRIST("AWST");

    private String abbr;

    Station(String abbr) {
        this.abbr = abbr;
    }

    public String abbr() {
        return abbr;
    }

    public String normalize() {
        return toString().substring(0, 1) + toString().substring(1).toLowerCase().replace("_", " ");
    }
}
