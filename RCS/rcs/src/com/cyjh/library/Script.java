package com.cyjh.library;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Script {
    private ArrayList<String> mContent = new ArrayList<String>();
    private String mProgram;

    public Script(String program) {
        mProgram = program;
    }

    public void put(String format, Object... objects) {
        mContent.add(Utilities.format(format, objects));
    }

    public String execute() {
        try {
            Utilities.log(this, mProgram);
            Process process = Runtime.getRuntime().exec(mProgram);
            OutputStream out = process.getOutputStream();
            for (String line : mContent) {
                Utilities.log(this, line);
                out.write(line.getBytes());
                out.write('\n');
                out.flush();
            }
            out = new ByteArrayOutputStream();
            Utilities.copy(process.getInputStream(), out);
            return out.toString();
        } catch (Exception e) {
            Utilities.log(Utilities.class, e);
            return null;
        }
    }

}
