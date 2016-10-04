package com.cyjh.library;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Command {

    private String mName;
    private Map<String, String> mParameters = new HashMap<String, String>();

    public Command(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public Command set(String key, Object value) {
        mParameters.put(key, String.valueOf(value));
        return this;
    }

    public String get(String key) {
        return mParameters.get(key);
    }

    public Set<String> keys() {
        return mParameters.keySet();
    }

    public String toString() {
        if (mParameters.isEmpty()) {
            return mName;
        }
        StringBuilder builder = new StringBuilder(mName);
        for (Entry<String, String> entry : mParameters.entrySet()) {
            builder.append(' ').append(entry.getKey()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }

    public static Command parse(String line) {
        String[] array = line.trim().split("\\s+");
        Command command = new Command(array[0]);
        for (int i = 1; i < array.length; i++) {
            int index = array[i].indexOf('=');
            if (index > 0) {
                command.set(array[i].substring(0, index), array[i].substring(index + 1));
            }
        }
        return command;
    }

}
