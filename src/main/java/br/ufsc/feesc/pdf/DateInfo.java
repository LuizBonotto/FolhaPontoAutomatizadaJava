package br.ufsc.feesc.pdf;

public class DateInfo {
    private int month;
    private int year;

    public DateInfo(int month, int year) {
        this.month = month;
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}