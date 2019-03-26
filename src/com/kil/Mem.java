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
    private final int DATA_SIZE_ON_PAGE = PAGE_SIZE / 4; // int32 as base type
    private final String KEY_WORDS = "BM";
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

        //fill stock file
        for (int i = 0; i < page_count; i++) {
            bufferPages[0].index = i;
            //for tests
            //
            bufferPages[0].bitmap[1] = 14;
            bufferPages[0].bitmap[BITMAP_SIZE - 2] = 14;
            //
            bufferPages[0].bitmap[0] = i;
            bufferPages[0].bitmap[BITMAP_SIZE - 1] = i;
            bufferPages[0].data[0] = i;
            bufferPages[0].data[DATA_SIZE_ON_PAGE - 1] = i;
            //for tests
            //for tests
            //
            bufferPages[1].bitmap[1] = 14;
            bufferPages[1].bitmap[BITMAP_SIZE - 2] = 14;
            //
            bufferPages[1].bitmap[0] = 1;
            bufferPages[1].bitmap[BITMAP_SIZE - 1] = 1;
            bufferPages[1].data[0] = 1;
            bufferPages[1].data[DATA_SIZE_ON_PAGE - 1] = 1;
            //for tests
            //for tests
            //
            bufferPages[2].bitmap[1] = 14;
            bufferPages[2].bitmap[BITMAP_SIZE - 2] = 14;
            //
            bufferPages[2].bitmap[0] = 2;
            bufferPages[2].bitmap[BITMAP_SIZE - 1] = 2;
            bufferPages[2].data[0] = 2;
            bufferPages[2].data[DATA_SIZE_ON_PAGE - 1] = 2;
            //for tests
            writePageToFile(0);
            bufferPages[0].isModified = true;
        }
    }


    //write the number by index
    void writeNumber(int number, int index) {
        if (index > array_size) {
            log.info("index beyond the array size");
        }

        int pageIndex = (int) Math.floor((double) index / DATA_SIZE_ON_PAGE);
        int indexOnPage = index - (DATA_SIZE_ON_PAGE * pageIndex);

        int bufIndex = findPage(pageIndex);
        bufferPages[bufIndex].data[indexOnPage] = number;
        bufferPages[bufIndex].isModified = true;
        bufferPages[bufIndex].last_access = LocalDateTime.now(clock);
        log.info("number:" + number + " was written with index:" + index + " in page number:" + pageIndex + " with local index:" + indexOnPage);
    }
    // add bitmap redaction!


    //read the number by index
    int read(int index) {

        return 0;
    }


    //find and update page in buffer if it's necessary
    private int findPage(int indexOfPage) {

        //buffer page check
        for (int i = 0; i < bufferPages.length; i++) {
            if (bufferPages[i].index == indexOfPage)
                return i;
        }

        //find latest page in buffer
        LocalDateTime latest = bufferPages[0].last_access;
        int indexBufOfLast = 0;
        for (int i = 0; i < bufferPages.length; i++) {
            if (bufferPages[i].last_access.isBefore(latest)) {
                latest = bufferPages[i].last_access;
                indexBufOfLast = i;
            }
        }

        //load page
        if (bufferPages[indexBufOfLast].isModified) {
            writePageToFile(indexBufOfLast);
            readPageFromFile(indexOfPage, indexBufOfLast);
        } else {
            readPageFromFile(indexOfPage, indexBufOfLast);
        }
        return indexBufOfLast;
    }


    //writing page to file
    private void writePageToFile(int indexInBuffer) {
        if (indexInBuffer >= BUFFER_SIZE)
            System.out.println("index beyond the array size in function 'writePageToFile'");
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
        log.info("write page" + bufferPages[indexInBuffer].index);
    }
    //add bitmap writing!


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
        bufferPages[indexInBuffer].last_access =  LocalDateTime.now(clock);
        bufferPages[indexInBuffer].index = indexOfPage;
        log.info("read page:" + indexOfPage + " to buffer[" + indexInBuffer + "]");
    }
    //add bitmap reading!


    //
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
