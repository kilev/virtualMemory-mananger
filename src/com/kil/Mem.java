package com.kil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.Logger;

class Mem {

    private final int PAGE_SIZE = 512; // in bytes
    private final int BITMAP_SIZE = PAGE_SIZE / 8; // 1 data byte -> 1 bitmap bit
    private final int DATA_SIZE_ON_PAGE = PAGE_SIZE / 4; // int32_t as base type
    private final String KEY_WORDS = "BM";
    private final int PAGE_NOINDEX = Integer.MAX_VALUE;
    private final int BUFFER_SIZE = 3;
    private final String path = "bin";

    private FileOutputStream fileOut = null;
    private FileInputStream fileIn = null;
    private int array_size;
    private int page_count;
    private Page[] bufferPages = new Page[BUFFER_SIZE];
    private static Logger log = Logger.getLogger(Mem.class.getName());// logger
    private Clock clock = Clock.system(ZoneId.systemDefault()); //clock


    class Page {
        int index;
        boolean isModified;
        LocalDateTime last_access;
        int[] bitmap = new int[BITMAP_SIZE];
        int[] data = new int[DATA_SIZE_ON_PAGE];

        Page(int index) {
            isModified = true;
            this.index = index;
            Arrays.fill(bitmap, 0);
            Arrays.fill(data, 0);
        }
    }


    Mem(int size) {
        LocalDateTime time = LocalDateTime.now(clock);
        time.format(DateTimeFormatter.ISO_TIME);
        log.info("time was launched, current time: " + time.toString());

        array_size = size;
        page_count = (int) Math.ceil((double) size / DATA_SIZE_ON_PAGE);
        log.info("pages buffer size: " + BUFFER_SIZE);
        log.info("pages count: " + page_count);

        openOutPutFile();//open/create bin file for write
        openInPutFile();// open file to read

        //write "BM" into the file
        try {
            byte[] buffer = KEY_WORDS.getBytes();
            fileOut.write(buffer);
            log.info("write key words to file");
        } catch (IOException e) {
            e.printStackTrace();
        }


        // load our buffer
        for (int i = 0; i < BUFFER_SIZE; i++) {
            bufferPages[i] = new Page(i);
            bufferPages[i].last_access = LocalDateTime.now(clock);
        }
        for (int i = 0; i < page_count; i++) {
            bufferPages[0].index = i;
            //for tests
            //
            bufferPages[0].bitmap[1] = 14;
            //
            bufferPages[0].bitmap[0] = i;
            bufferPages[0].bitmap[BITMAP_SIZE - 1] = i;
            bufferPages[0].data[0] = i;
            bufferPages[0].data[DATA_SIZE_ON_PAGE - 1] = i;
            //for tests
            writePageToFile(0);
            bufferPages[0].isModified = true;
        }
        System.out.println("finish");
    }

    //writing page to file
    private void writePageToFile(int indexInBuffer) {
        //перенести потом этот иф в функцию финдпэйдж!!
        if (!bufferPages[indexInBuffer].isModified) {
            return;
        }
        if (indexInBuffer >= BUFFER_SIZE)
            System.err.println("index beyond the array size");
        try {
            int index = bufferPages[indexInBuffer].index;
            FileChannel channel = fileOut.getChannel().position(BITMAP_SIZE * (index) + PAGE_SIZE * (index) + (KEY_WORDS).length());

            for (int i : bufferPages[indexInBuffer].bitmap) {
                byte[] buff = ByteBuffer.allocate(1).put((byte) i).array();
                channel.write(ByteBuffer.wrap(buff));
            }
            for (int i : bufferPages[indexInBuffer].data) {
                byte[] buff = ByteBuffer.allocate(4).putInt(i).array();
                channel.write(ByteBuffer.wrap(buff));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        bufferPages[indexInBuffer].isModified = false;// reset page properties
        log.info("write " + bufferPages[indexInBuffer].index + " to mem file");
    }

    //reading page from file
    private void readPageFromFile(int indexOfPage, int indexInBuffer) {
        try {
            FileChannel ch = fileIn.getChannel();//get channel
            ch.position(BITMAP_SIZE * (indexOfPage) + PAGE_SIZE * (indexOfPage) + (KEY_WORDS).length());//set position
            for (int i = 0; i < bufferPages[indexInBuffer].bitmap.length; i++) {
                int k = fileIn.read();
                bufferPages[indexInBuffer].bitmap[i] = k;
            }
            for (int i = 0; i < bufferPages[indexInBuffer].data.length; i++) {
                byte[] bt = new byte[4];
                fileIn.read(bt);
                ByteBuffer wrapped = ByteBuffer.wrap(bt); // big-endian by default
                bufferPages[indexInBuffer].data[i] = wrapped.getInt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("read " + indexOfPage + " to buffer page " + indexInBuffer + " mem file");
    }


    private void findPage(int indexOfPage) {
        for (Page page: bufferPages) {
            //if ()
        }
    }


    //write the number
    void writeNumber(int number, int index) {
        if (index > array_size) {
            throw new Error("index beyond the array size");
        }
        int page_index = (int) Math.floor((double) index / DATA_SIZE_ON_PAGE);
    }


    //read index page
    int read(int index) {

        return 0;
    }


    //sync index page
    void _sync_page(int page_index) {

    }

    //System functions:
    //

    //open the output file
    private void openOutPutFile() {
        try {
            fileOut = new FileOutputStream(path, false);
            log.info("open file to write'" + path + "'");

        } catch (FileNotFoundException e) {
            log.info(e.getMessage());
        }
    }

    //open the input file
    private void openInPutFile() {
        try {
            fileIn = new FileInputStream(path);
            log.info("open file to read '" + path + "'");

        } catch (FileNotFoundException e) {
            log.info(e.getMessage());
        }
    }

}
