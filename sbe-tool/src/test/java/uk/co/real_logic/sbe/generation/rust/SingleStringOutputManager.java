package uk.co.real_logic.sbe.generation.rust;

import org.agrona.generation.OutputManager;

import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class SingleStringOutputManager implements OutputManager
{
    private final List<Map.Entry<String, StringWriter>> sections = new ArrayList<>();

    public Writer createOutput(final String name)
    {
        final StringWriter stringWriter = new StringWriter();
        sections.add(new AbstractMap.SimpleEntry<>(name, stringWriter));
        return stringWriter;
    }

    public String toString()
    {
        return sections.stream()
            .map((e) -> String.format("\n/// %s%n%s", e.getKey(), e.getValue().toString()))
            .collect(Collectors.joining());
    }

    public void clear()
    {
        sections.clear();
    }
}
