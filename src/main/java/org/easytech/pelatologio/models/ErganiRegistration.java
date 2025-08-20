package org.easytech.pelatologio.models;

public class ErganiRegistration {
    private final String program;
    private final String years;
    private final String email;
    private final String entrance;

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
