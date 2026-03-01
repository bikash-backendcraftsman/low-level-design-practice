package com.technostack.lld.ops.abstraction;

public class CoffeeVendorSupplier {
    public static void main(String[] args) {
        CoffeMachine machine = new CoffeMachine();
        machine.prepareCoffee();

        /*
        We cat call this method as part of abstration, because ita private method,
         can only be accessible inside the same class it has been written.
         */
       // machine.heatWater();
    }
}
