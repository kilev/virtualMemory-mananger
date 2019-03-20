package com.kil;

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

    private FileOutputStream file = null;
    private int array_size;
    private int page_count;
    private page localPage;
    private page[] bufferPages = new page[BUFFER_SIZE];
    private static Logger log = Logger.getLogger(Mem.class.getName());// logger
    private Clock clock = Clock.system(ZoneId.systemDefault()); //clock


    class page {
        int index;
        boolean isModified;
        LocalDateTime last_access;
        int[] bitmap = new int[BITMAP_SIZE];
        int[] data = new int[DATA_SIZE_ON_PAGE];

        page(int index) {
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

        openOutPutFile();//open/create bin file

        //write into the file "BM"
        try {
            byte[] buffer = KEY_WORDS.getBytes();
            file.write(buffer);
            log.info("write key words to file");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load our buffer
        for (int i = 0; i < BUFFER_SIZE; i++) {
            bufferPages[i] = new page(PAGE_NOINDEX);
        }
        localPage = bufferPages[0];
        for (int i = 0; i < page_count; i++) {
            localPage.index = i;
            //for tests
            localPage.bitmap[0] = 1;
            localPage.bitmap[BITMAP_SIZE-1] = 1;
            localPage.data[0] = 31;
            localPage.data[DATA_SIZE_ON_PAGE-1] = 31;
            //for tests
            writePageToFile();
            localPage.isModified = true;
        }
    }


    private void writePageToFile() {
        //перенести потом этот иф в функцию финдпэйдж!!
        if (!localPage.isModified) {
            return;
        }
        try {
            int offset = BITMAP_SIZE * localPage.index + PAGE_SIZE * localPage.index + (KEY_WORDS).length();
            FileChannel channel = file.getChannel().position(offset);

            for(int i : localPage.bitmap){
                byte[] buff = ByteBuffer.allocate(1).put((byte) i).array();
                channel.write(ByteBuffer.wrap(buff));
            }
            for(int i : localPage.data){
                byte[] buff = ByteBuffer.allocate(4).putInt(i).array();
                channel.write(ByteBuffer.wrap(buff));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        localPage.isModified = false;// reset page properties
        log.info("write " + localPage.index + " to mem file");
    }


    private void readPageFromFile(int indexOfPage, int indexInBuffer){
        int offset = BITMAP_SIZE * (localPage.index - 1) + PAGE_SIZE * (localPage.index - 1) + (KEY_WORDS).length();
    }


    private void findPage(int indexOfPage){
        readPageFromFile(3,2);
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


    //open the out put file
    private void openOutPutFile() {
        try {
            file = new FileOutputStream(path, false);
            log.info("open file '" + path + "'");

        } catch (FileNotFoundException e) {
            log.info(e.getMessage());
        }
    }

}
