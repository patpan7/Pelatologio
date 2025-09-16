package org.easytech.pelatologio.models;

public class ErganiRegistration {
    public final String program;
    public final String years;
    public final String email;
    public final String entrance;

    public ErganiRegistration(String program, String years, String email, String entrance) {
        this.program = program;
        this.years = years;
        this.email = email;
        this.entrance = entrance;
    }

    public String getProgram() { return program; }
    public String getYears() { return years; }
    public String getEmail() { return email; }
    public String getEntrance() { return entrance; }
}
