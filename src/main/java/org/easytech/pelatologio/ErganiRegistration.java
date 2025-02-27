package org.easytech.pelatologio;

public class ErganiRegistration {
    private String program;
    private String years;
    private String email;
    private String entrance;

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
