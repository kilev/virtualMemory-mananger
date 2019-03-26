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

        boolean bool = true;

        while (bool) {
            String inPut = in.next();

            if (inPut.startsWith("exit")) {
                bool = false;

            } else if (inPut.startsWith("info")) {
                System.out.println("functions:");
                System.out.println("\texit     //exit");
                System.out.println("\tread     //use to read a value by index");
                System.out.println("\treadAll  //use to read all pages");
                System.out.println("\twrite    //use to write your value in your index");
                System.out.println("\tsync     //use to sync all buffers pages");

            } else if (inPut.startsWith("sync")) {
                mem.sync();
                System.out.println("synchronization was successful");

            } else if (inPut.startsWith("write")) {
                int number;
                int index;
                System.out.println("please write a number");
                number = in.nextInt();
                System.out.println("please write a index");
                index = in.nextInt();
                mem.writeNumber(number, index);
                System.out.println("number:" + number + " was written with index:" + index);

            } else if (inPut.startsWith("readAll")) {
                readAll();

            } else if (inPut.startsWith("read")) {
                int index;
                System.out.println("please write a index");
                index = in.nextInt();
                System.out.println("at index:" + index + " found number:" + mem.read(index));

            }
        }
    }

    //read all data
    private static void readAll() {
        for (Integer i = 0; i < size; ++i) {
            System.out.println("memory[" + i + "] = " + mem.read(i) + "\n");
        }
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

}