package uk.co.real_logic.sbe.xml;

/**
 * Time unit of an SBE timestamp type.
 */
public enum TimeUnit
{
    NANOSECOND("nanosecond", java.util.concurrent.TimeUnit.NANOSECONDS),
    MICROSECOND("microsecond", java.util.concurrent.TimeUnit.MICROSECONDS),
    MILLISECOND("millisecond", java.util.concurrent.TimeUnit.MILLISECONDS),
    SECOND("second", java.util.concurrent.TimeUnit.SECONDS);

    private final String sbeName;
    private final java.util.concurrent.TimeUnit jucTimeUnit;

    TimeUnit(final String sbeName, final java.util.concurrent.TimeUnit jucTimeUnit)
    {
        this.sbeName = sbeName;
        this.jucTimeUnit = jucTimeUnit;
    }

    /**
     * Get the SBE name for the time unit.
     *
     * @return the SBE name for the time unit.
     */
    public String sbeName()
    {
        return sbeName;
    }

    /**
     * Get the corresponding {@link java.util.concurrent.TimeUnit} enum for the SBE time unit.
     *
     * @return the corresponding {@link java.util.concurrent.TimeUnit} enum for the SBE time unit.
     */
    public java.util.concurrent.TimeUnit jucTimeUnit()
    {
        return jucTimeUnit;
    }

    /**
     * Get the TimeUnit for the given SBE name for a TimeUnit.
     *
     * @param sbeName for the TimeUnit.
     * @return the TimeUnit corresponding to the SBE name.
     */
    public static TimeUnit get(final String sbeName)
    {
        for (final TimeUnit timeUnit : values())
        {
            if (sbeName.equalsIgnoreCase(timeUnit.sbeName))
            {
                return timeUnit;
            }
        }

        throw new IllegalArgumentException("Unknown SBE name: " + sbeName);
    }
}
