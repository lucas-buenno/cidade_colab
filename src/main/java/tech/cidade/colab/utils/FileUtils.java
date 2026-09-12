package tech.cidade.colab.utils;

public final class FileUtils {

    private static final int MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    private FileUtils() {
    }

    public static void validateFile(byte[] file) {
        validateNotEmpty(file);
        validateSize(file);
        validateType(file);
    }

    private static void validateNotEmpty(byte[] file) {
        if (file == null || file.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }
    }

    private static void validateSize(byte[] file) {
        if (file.length > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of 10MB");
        }
    }

    private static void validateType(byte[] file) {
        if (!isSupportedImage(file)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: JPEG, PNG, WEBP, HEIC, HEIF, GIF, BMP"
            );
        }
    }

    private static boolean isSupportedImage(byte[] file) {
        return isJpeg(file)
                || isPng(file)
                || isWebp(file)
                || isHeicOrHeif(file)
                || isGif(file)
                || isBmp(file);
    }

    private static boolean isJpeg(byte[] file) {
        return file.length >= 3
                && (file[0] & 0xFF) == 0xFF
                && (file[1] & 0xFF) == 0xD8
                && (file[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] file) {
        return file.length >= 8
                && (file[0] & 0xFF) == 0x89
                && file[1] == 0x50
                && file[2] == 0x4E
                && file[3] == 0x47
                && file[4] == 0x0D
                && file[5] == 0x0A
                && file[6] == 0x1A
                && file[7] == 0x0A;
    }

    private static boolean isWebp(byte[] file) {
        return file.length >= 12
                && file[0] == 0x52
                && file[1] == 0x49
                && file[2] == 0x46
                && file[3] == 0x46
                && file[8] == 0x57
                && file[9] == 0x45
                && file[10] == 0x42
                && file[11] == 0x50;
    }

    private static boolean isHeicOrHeif(byte[] file) {
        if (file.length < 12) {
            return false;
        }

        return file[4] == 0x66
                && file[5] == 0x74
                && file[6] == 0x79
                && file[7] == 0x70
                && (
                (file[8] == 0x68 && file[9] == 0x65 && file[10] == 0x69 && file[11] == 0x63)
                        || (file[8] == 0x68 && file[9] == 0x65 && file[10] == 0x69 && file[11] == 0x78)
                        || (file[8] == 0x68 && file[9] == 0x65 && file[10] == 0x76 && file[11] == 0x63)
                        || (file[8] == 0x68 && file[9] == 0x65 && file[10] == 0x76 && file[11] == 0x78)
                        || (file[8] == 0x6D && file[9] == 0x69 && file[10] == 0x66 && file[11] == 0x31)
                        || (file[8] == 0x6D && file[9] == 0x73 && file[10] == 0x66 && file[11] == 0x31)
        );
    }

    private static boolean isGif(byte[] file) {
        return file.length >= 6
                && file[0] == 0x47
                && file[1] == 0x49
                && file[2] == 0x46
                && file[3] == 0x38
                && (file[4] == 0x37 || file[4] == 0x39)
                && file[5] == 0x61;
    }

    private static boolean isBmp(byte[] file) {
        return file.length >= 2
                && file[0] == 0x42
                && file[1] == 0x4D;
    }
}