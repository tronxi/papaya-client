package dev.tronxi.papayaclient.persistence;

import org.springframework.stereotype.Service;

@Service
public class PartSizeCalculator {

    private static final int PART_SIZE_128_MB = 128 * 1024 * 1024;
    private static final int PART_SIZE_64_MB = 64 * 1024 * 1024;
    private static final int PART_SIZE_32_MB = 32 * 1024 * 1024;
    private static final int PART_SIZE_10_MB = 10 * 1024 * 1024;
    private static final int PART_SIZE_16_KB = 16 * 1024;

    private static final long FILE_SIZE_10_MB = 10 * 1024 * 1024;
    private static final long FILE_SIZE_100_MB = 100 * 1024 * 1024;
    private static final long FILE_SIZE_1_GB = 1L * 1024 * 1024 * 1024;
    private static final long FILE_SIZE_10_GB = 10L * 1024 * 1024 * 1024;

    public long calculate(long fileSize) {
        if (fileSize <= FILE_SIZE_10_MB) {
            return Math.min(fileSize, PART_SIZE_16_KB);
        } else if (fileSize <= FILE_SIZE_100_MB) {
            return PART_SIZE_10_MB;
        } else if (fileSize <= FILE_SIZE_1_GB) {
            return PART_SIZE_32_MB;
        } else if (fileSize <= FILE_SIZE_10_GB) {
            return PART_SIZE_64_MB;
        } else {
            return PART_SIZE_128_MB;
        }
    }
}
