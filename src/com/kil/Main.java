package com.kil;

import java.util.Random;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Main {

    private static Logger log = null;//logger
    private static Mem mem;
    private static Integer size;

    public static void main(String[] args) {

        launchLogger();

        //scan size of input array.data
        Scanner in = new Scanner(System.in);
        System.out.print("Введите размер массива: ");
        size = in.nextInt();

        //random some number and put into memory
        mem = new Mem(size);
        try {
            final Random random = new Random();

            for (int i = 0; i < size / 2; ++i) {
                int index = (int) (Math.random() * size);
                int number = random.nextInt();
                System.out.println("memory[" + index + "] = " + number + "\n");
                mem.writeNumber(number, index);
            }
            System.out.println("\n\n");
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        readAll();
    }


    //logger launcher
    private static void launchLogger() {
        try {
            LogManager.getLogManager().readConfiguration(
                    Main.class.getResourceAsStream("/logging.properties"));
        } catch (Exception e) {
            System.err.println("Could not setup logger configuration: " + e.toString());
        }
        log = Logger.getLogger(Mem.class.getName());
    }

    //read all data
    private static void readAll() {
        for (Integer i = 0; i < size; ++i) {
            System.out.println("memory[" + i + "] = " + mem.read(i) + "\n");
        }
    }

}