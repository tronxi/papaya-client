package dev.tronxi.papayaclient.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PartSizeCalculatorTest {

    private final PartSizeCalculator partSizeCalculator = new PartSizeCalculator();

    @Test
    public void calculatePartSize_File_8KB() {
        long fileSize_8KB = 8 * 1024;
        long calculatedPart = partSizeCalculator.calculate(fileSize_8KB);
        assertEquals(fileSize_8KB, calculatedPart);
    }

    @Test
    public void calculatePartSize_File_16KB() {
        long fileSize_16KB = 16 * 1024;
        long expectedPartSize_16KB = 16 * 1024;
        long calculatedPart = partSizeCalculator.calculate(fileSize_16KB);
        assertEquals(expectedPartSize_16KB, calculatedPart);
    }

    @Test
    public void calculatePartSize_File_10MB() {
        long fileSize_10MB = 10 * 1024 * 1024;
        long expectedPartSize_16KB = 16 * 1024;
        long calculatedPart = partSizeCalculator.calculate(fileSize_10MB);
        assertEquals(expectedPartSize_16KB, calculatedPart);
    }

    @Test
    public void calculatePartSize_File_100MB() {
        long fileSize_100MB = 100 * 1024 * 1024; // 100 MB
        long expectedPartSize_10MB = 10 * 1024 * 1024; // 10 MB
        long calculatedPart = partSizeCalculator.calculate(fileSize_100MB);
        assertEquals(expectedPartSize_10MB, calculatedPart);
    }

    @Test
    public void calculatePartSize_File_1GB() {
        long fileSize_1GB = 1L * 1024 * 1024 * 1024; // 1 GB
        long expectedPartSize_32MB = 32 * 1024 * 1024; // 32 MB
        long calculatedPart = partSizeCalculator.calculate(fileSize_1GB);
        assertEquals(expectedPartSize_32MB, calculatedPart);
    }

    @Test
    public void calculatePartSize_File_10GB() {
        long fileSize_10GB = 10L * 1024 * 1024 * 1024;
        long expectedPartSize_64MB = 64 * 1024 * 1024;
        long calculatedPart = partSizeCalculator.calculate(fileSize_10GB);
        assertEquals(expectedPartSize_64MB, calculatedPart);
    }

    @Test
    public void calculatePartSize_File_15GB() {
        long fileSize_15GB = 15L * 1024 * 1024 * 1024;
        long expectedPartSize_128MB = 128 * 1024 * 1024;
        long calculatedPart = partSizeCalculator.calculate(fileSize_15GB);
        assertEquals(expectedPartSize_128MB, calculatedPart);
    }

    @Test
    public void calculatePartSize_File_100GB() {
        long fileSize_100GB = 100L * 1024 * 1024 * 1024;
        long expectedPartSize_128MB = 128 * 1024 * 1024;
        long calculatedPart = partSizeCalculator.calculate(fileSize_100GB);
        assertEquals(expectedPartSize_128MB, calculatedPart);
    }
}
