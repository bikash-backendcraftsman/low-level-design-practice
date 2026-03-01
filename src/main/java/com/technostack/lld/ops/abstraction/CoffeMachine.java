package com.technostack.lld.ops.abstraction;

public class CoffeMachine {

    private String machineModelNum;
    private String brand;
    private int price;

    public CoffeMachine() {

    }

    public CoffeMachine(String machineModelNum, String brand, int price) {
        this.machineModelNum = machineModelNum;
        this.brand = brand;
        this.price = price;
    }

    private void heatWater() {
        System.out.println("Heating water...");
    }

    private void grindBeans() {
        System.out.println("Grinding beans...");
    }

    private void brew() {
        System.out.println("Brewing coffee...");
    }

    public void prepareCoffee(){
        heatWater();
        grindBeans();
        brew();
        System.out.println("Coffee is ready");
    }

    public static void main(String[] args) {
        CoffeMachine machine = new CoffeMachine("CF-0090","Breson Coffee House",2200);
        machine.prepareCoffee();

    }
}
