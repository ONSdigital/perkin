package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.SurveyParser;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class Audit {

    private Map<String, AtomicLong> counters;
    private List<String> messages;

    public Audit() {
        counters = new HashMap<>();
        messages = new ArrayList<>();
    }

    public void increment(String key) {
        increment(key, 1);
    }

    public void increment(String key, int size) {
        String message = createMessage(key);
        increment(key, size, message);
    }

    public void increment(String key, Exception e) {
        String message = createMessage(key) + getExceptionMessage(e);
        increment(key, 1, message);
    }

    protected String getExceptionMessage(Exception e) {
        return " " + e.getClass().getName() + " " + e.getMessage() + getCause(e);
    }

    private String getCause(Exception e) {
        String cause = "";
        if (e.getCause() != null) {
            cause = " caused by " + e.getCause().getClass().getName() + " " + e.getCause().getMessage();
        }

        return cause;
    }

    private void increment(String key, int size, String message) {
        addMessage(message);

        AtomicLong current = counters.get(key);
        if (current == null) {
            counters.put(key, new AtomicLong(1));
        } else {
            current.getAndAdd(size);
        }
    }

    private void addMessage(String message) {
        log.info("AUDIT|" + message);
        messages.add(message);
    }

    private String createMessage(String key) {
        return new SimpleDateFormat(SurveyParser.ISO8601).format(new Date()) + " " + key;
    }

    public List<String> getMessages() {
        return Lists.reverse(messages);
    }
}
