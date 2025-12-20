//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("============================================================");
        System.out.println("ГЕНЕРАТОР ПРОФИЛЯ ВЫСОТ ИЗ TIF ФАЙЛА");
        System.out.println("============================================================");

        String tifFile = "out.tif";
        File file = new File(tifFile);
        if (!file.exists()) {
            System.out.println("ОШИБКА: Файл '" + tifFile + "' не найден!");
            System.out.println("Поместите файл в ту же папку, что и скрипт");
            System.out.print("Нажмите Enter для выхода...");
            scanner.nextLine();
            scanner.close();
            return;
        }

        System.out.println("\nЧтение файла: " + tifFile);
        Map<String, Object> info;
        try {
            info = readTiffInfo(tifFile);
            System.out.println("Файл успешно прочитан");
            System.out.println("Размер: " + info.get("width") + " x " + info.get("height") + " пикселей");
            System.out.printf("Диапазон высот: %.1f - %.1f м%n",
                    (Double) info.get("min_height"), (Double) info.get("max_height"));
        } catch (Exception e) {
            System.out.println("ОШИБКА при чтении файла: " + e.getMessage());
            System.out.print("Нажмите Enter для выхода...");
            scanner.nextLine();
            scanner.close();
            return;
        }

        double[] startPoint, endPoint;
        try {
            double[][] points = getUserCoordinates(info, scanner);
            startPoint = points[0];
            endPoint = points[1];
        } catch (Exception e) {
            System.out.println("Ошибка при вводе координат: " + e.getMessage());
            System.out.print("Нажмите Enter для выхода...");
            scanner.nextLine();
            scanner.close();
            return;
        }

        System.out.println("\n--- НАСТРОЙКИ ПРОФИЛЯ ---");
        int numPoints = 0;
        while (true) {
            System.out.print("Количество точек на профиле (рекомендуется 50-200): ");
            String input = scanner.nextLine();
            try {
                numPoints = Integer.parseInt(input);
                if (numPoints >= 2) {
                    break;
                } else {
                    System.out.println("Ошибка: должно быть минимум 2 точки");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }

        System.out.println("\nВычисление профиля...");
        List<Map<String, Object>> profile = calculateProfile(info, startPoint, endPoint, numPoints);

        if (profile == null || profile.isEmpty()) {
            System.out.println("ОШИБКА: Не удалось получить данные профиля");
            System.out.println("Возможно, точки находятся вне зоны данных");
            System.out.print("Нажмите Enter для выхода...");
            scanner.nextLine();
            scanner.close();
            return;
        }

        System.out.println("Получено точек: " + profile.size());

        String outputFile = "height_profile_csv.csv";
        int pointsSaved = saveToCsv(profile, outputFile);

        try {
            saveToExcel(profile, "height_profile_xl.xlsx");
            System.out.println("Таблица сохранена в файл height_profile.xlsx");
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении Excel файла: " + e.getMessage());
        }

        System.out.println("Данные сохранены в файл: " + outputFile);

        printStatistics(profile);

        System.out.println("\nПервые 5 точек профиля:");
        System.out.println("--------------------------------------------------");
        System.out.printf("%-4s %-12s %-12s %-8s%n", "№", "X", "Y", "Высота");
        System.out.println("--------------------------------------------------");

        for (int i = 0; i < Math.min(5, profile.size()); i++) {
            Map<String, Object> p = profile.get(i);
            System.out.printf("%-4d %-12.1f %-12.1f %-8.1f%n",
                    (Integer) p.get("index"),
                    (Double) p.get("x"),
                    (Double) p.get("y"),
                    (Double) p.get("height"));
        }

        System.out.println("\n============================================================");
        System.out.println("ГОТОВО! CSV файл создан: " + outputFile);
        System.out.println("Всего точек: " + pointsSaved);
        System.out.println("============================================================");

        System.out.print("\nНажмите Enter для выхода...");
        scanner.nextLine();
        scanner.close();
    }

    // Stub for readTiffInfo - to be implemented
    private static Map<String, Object> readTiffInfo(String tifFile) throws Exception {
        // This method should read the TIFF file and return a map with keys:
        // "width" (Integer), "height" (Integer), "min_height" (Double), "max_height" (Double)
        // For now, return dummy data for demonstration
        Map<String, Object> info = new HashMap<>();
        info.put("width", 1000);
        info.put("height", 1000);
        info.put("min_height", 10.0);
        info.put("max_height", 200.0);
        return info;
    }

    // Stub for getUserCoordinates - to be implemented
    private static double[][] getUserCoordinates(Map<String, Object> info, Scanner scanner) {
        // This method should get user input for start and end coordinates
        // For now, return dummy coordinates
        double[] startPoint = {0.0, 0.0};
        double[] endPoint = {100.0, 100.0};
        return new double[][]{startPoint, endPoint};
    }

    // Stub for calculateProfile - to be implemented
    private static List<Map<String, Object>> calculateProfile(Map<String, Object> info, double[] startPoint, double[] endPoint, int numPoints) {
        // This method should calculate the profile points between startPoint and endPoint
        // For now, return dummy data
        List<Map<String, Object>> profile = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("index", i + 1);
            point.put("x", startPoint[0] + (endPoint[0] - startPoint[0]) * i / (numPoints - 1));
            point.put("y", startPoint[1] + (endPoint[1] - startPoint[1]) * i / (numPoints - 1));
            point.put("height", 10.0 + (190.0 * i / (numPoints - 1))); // linear interpolation dummy height
            profile.add(point);
        }
        return profile;
    }

    private static int saveToCsv(List<Map<String, Object>> profile, String outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.append("index,x,y,height\n");
            for (Map<String, Object> p : profile) {
                writer.append(p.get("index").toString()).append(",");
                writer.append(String.format("%.1f", (Double) p.get("x"))).append(",");
                writer.append(String.format("%.1f", (Double) p.get("y"))).append(",");
                writer.append(String.format("%.1f", (Double) p.get("height"))).append("\n");
            }
            return profile.size();
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении CSV файла: " + e.getMessage());
            return 0;
        }
    }

    private static void saveToExcel(List<Map<String, Object>> profile, String filename) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("height_profile");

        // Create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("index");
        header.createCell(1).setCellValue("x");
        header.createCell(2).setCellValue("y");
        header.createCell(3).setCellValue("height");

        // Fill data rows
        int rowNum = 1;
        for (Map<String, Object> p : profile) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((Integer) p.get("index"));
            row.createCell(1).setCellValue((Double) p.get("x"));
            row.createCell(2).setCellValue((Double) p.get("y"));
            row.createCell(3).setCellValue((Double) p.get("height"));
        }

        // Autosize columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileWriter fileOut = new