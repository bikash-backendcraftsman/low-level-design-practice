package com.technostack.lld.ops.encapsulation;

public class ProductionHouse {
    private String name;
    private String positionHoldInCompany;
    private int totalEmployeeStrength;

    //Default Constructor
    public ProductionHouse() {

    }

    //Parameterized Constructor
    public ProductionHouse(String name, String positionHoldInCompany, int totalEmployeeStrength) {
        this.name = name;
        this.positionHoldInCompany = positionHoldInCompany;
        this.totalEmployeeStrength = totalEmployeeStrength;
    }

    public static void main(String[] args) {
        Movie m1 = new Movie();
        m1.setTitle("Soldier");
        m1.printMovieInformation();

        //private methods we can not access outside the class.
    }
}
