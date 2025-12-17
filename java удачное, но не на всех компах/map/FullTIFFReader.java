import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.Iterator;

public class FullTIFFReader {

    private static int[][] fullPixelArray;
    private static int width;
    private static int height;

    public static void main(String[] args) {
        try {
            String filePath = "C:/Users/Ant1dote_7/OneDrive/Рабочий стол/Задание/map/out.tiff";
            File tiffFile = new File(filePath);

            if (!tiffFile.exists()) {
                System.out.println("Файл не найден: " + filePath);
                return;
            }

            boolean success = readTIFFToArray(tiffFile);

            if (success) {
                System.out.println("✓ TIFF файл успешно прочитан!");
                System.out.println("✓ Размеры: " + width + "x" + height);
                System.out.println("✓ Всего элементов: " + (width * height));

                showArrayInfo();
                saveArrayToCSV("height_profile.csv"); // ← теперь CSV (Excel-friendly)
                processFullArray();
                accessSpecificElements();
            }

        } catch (Exception e) {
            System.err.println("Ошибка:");
            e.printStackTrace();
        }
    }

    private static boolean readTIFFToArray(File tiffFile) throws Exception {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIFF");

        if (!readers.hasNext()) {
            System.out.println("Ошибка: TIFF ридер не найден.");
            return false;
        }

        ImageReader reader = readers.next();

        try (ImageInputStream stream = ImageIO.createImageInputStream(tiffFile)) {
            reader.setInput(stream);

            width = reader.getWidth(0);
            height = reader.getHeight(0);

            System.out.println("Чтение файла...");
            System.out.println("Размеры: " + width + "x" + height);

            BufferedImage image = reader.read(0);
            Raster raster = image.getRaster();

            fullPixelArray = new int[height][width];

            System.out.println("Чтение " + (width * height) + " пикселей...");

            int[] pixel = new int[raster.getNumBands()];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    raster.getPixel(x, y, pixel);
                    fullPixelArray[y][x] = pixel[0];
                }

                if (y % 100 == 0) {
                    System.out.printf("Прочитано: %.1f%%\n", (y * 100.0 / height));
                }
            }

            System.out.println("Чтение завершено на 100%");
            return true;

        } finally {
            reader.dispose();
        }
    }

    private static void showArrayInfo() {
        System.out.println("\n=== ИНФОРМАЦИЯ О МАССИВЕ ===");
        System.out.println("Размер массива: " + fullPixelArray.length + " строк");
        System.out.println("Длина строки: " + fullPixelArray[0].length + " столбцов");

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        long sum = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = fullPixelArray[y][x];
                if (value < min)
                    min = value;
                if (value > max)
                    max = value;
                sum += value;
            }
        }

        double average = (double) sum / (width * height);

        System.out.println("Минимальное значение: " + min);
        System.out.println("Максимальное значение: " + max);
        System.out.println("Среднее значение: " + String.format("%.2f", average));
        System.out.println("Сумма всех значений: " + sum);
    }

    // ✅ Экспорт в CSV — открывается в Excel как таблица с X, Y, Height
private static void saveArrayToCSV(String filename) {
    System.out.println("\n=== СОХРАНЕНИЕ В CSV (для Excel) ===");
    System.out.println("Сохранение в файл: " + filename);

    try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
        // Заголовки — используем точку с запятой как разделитель
        writer.println("X;Y;Height");

        // Данные
        int total = width * height;
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.printf("%d;%d;%d%n", x, y, fullPixelArray[y][x]);
                count++;

                if (count % 10000 == 0 || count == total) {
                    System.out.printf("Сохранено: %.1f%%\n", (100.0 * count / total));
                }
            }
        }

        System.out.println("✓ Данные сохранены в: " + filename);
        System.out.println("💡 Откройте файл в Excel — данные отобразятся в 3 колонках!");

    } catch (Exception e) {
        System.err.println("Ошибка при сохранении CSV: " + e.getMessage());
        e.printStackTrace();
    }
}

    private static void processFullArray() {
        System.out.println("\n=== ОБРАБОТКА МАССИВА ===");

        int threshold = 20000;
        int countAboveThreshold = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (fullPixelArray[y][x] > threshold) {
                    countAboveThreshold++;
                }
            }
        }

        System.out.println("Пикселей > " + threshold + ": " + countAboveThreshold +
                " (" + String.format("%.1f", (countAboveThreshold * 100.0 / (width * height))) + "%)");

        int maxValue = Integer.MIN_VALUE;
        int maxX = -1, maxY = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (fullPixelArray[y][x] > maxValue) {
                    maxValue = fullPixelArray[y][x];
                    maxX = x;
                    maxY = y;
                }
            }
        }

        System.out.println("Самый яркий пиксель: значение=" + maxValue +
                " на позиции [Y=" + maxY + ", X=" + maxX + "]");
    }

    private static void accessSpecificElements() {
        System.out.println("\n=== ДОСТУП К ЭЛЕМЕНТАМ ===");

        System.out.println("Первые 10x10 элементов:");
        for (int y = 0; y < Math.min(10, height); y++) {
            for (int x = 0; x < Math.min(10, width); x++) {
                System.out.print(fullPixelArray[y][x] + "\t");
            }
            System.out.println();
        }

        System.out.println("\nПоследние 10x10 элементов:");
        int startY = Math.max(0, height - 10);
        int startX = Math.max(0, width - 10);

        for (int y = startY; y < height; y++) {
            for (int x = startX; x < width; x++) {
                System.out.print(fullPixelArray[y][x] + "\t");
            }
            System.out.println();
        }

        System.out.println("\nЗначения в углах массива:");
        System.out.println("Левый верхний [0][0]: " + fullPixelArray[0][0]);
        System.out.println("Правый верхний [0][" + (width - 1) + "]: " + fullPixelArray[0][width - 1]);
        System.out.println("Левый нижний [" + (height - 1) + "][0]: " + fullPixelArray[height - 1][0]);
        System.out.println("Правый нижний [" + (height - 1) + "][" + (width - 1) + "]: " +
                fullPixelArray[height - 1][width - 1]);

        int centerY = height / 2;
        int centerX = width / 2;
        System.out.println("Центр массива [Y=" + centerY + ", X=" + centerX + "]: " +
                fullPixelArray[centerY][centerX]);
    }

    public static int getPixelValue(int row, int col) {
        if (fullPixelArray != null &&
                row >= 0 && row < height &&
                col >= 0 && col < width) {
            return fullPixelArray[row][col];
        }
        return -1;
    }

    public static int[][] getFullArray() {
        return fullPixelArray;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }
}
