import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class Main {

    public static void main(String[] args) {
        // Создаем объект Scanner для чтения ввода пользователя из консоли
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Запрашиваем у пользователя путь к файлу или команду выхода
            System.out.println("Введите путь до файла-справочника или 'exit' для завершения:");
            String filePath = scanner.nextLine(); // Считываем введенный путь или команду

            // Проверка, ввел ли пользователь "exit" для завершения программы
            if (filePath.equalsIgnoreCase("exit")) {
                System.out.println("Завершение программы.");
                break;
            }

            // Замеряем время начала обработки для отслеживания времени выполнения
            long startTime = System.currentTimeMillis();

            // Переменная для хранения данных, считанных из файла
            List<Map<String, String>> records;

            // Проверяем расширение файла и вызываем соответствующий метод обработки
            if (filePath.endsWith(".csv")) {
                records = loadCsv(filePath); // Загружаем данные из CSV файла
            } else if (filePath.endsWith(".xml")) {
                records = loadXml(filePath); // Загружаем данные из XML файла
            } else {
                // Сообщаем пользователю о неподдерживаемом формате и возвращаемся к началу цикла
                System.out.println("Формат файла не поддерживается. Пожалуйста, используйте CSV или XML.");
                continue;
            }

            // Замеряем время окончания обработки
            long endTime = System.currentTimeMillis();
            // Рассчитываем общее время обработки файла
            long processingTime = endTime - startTime;

            // Если данные были успешно считаны, выводим статистику
            if (!records.isEmpty()) {
                printDuplicates(records); // Поиск и вывод дублирующихся записей
                printFloorStats(records); // Вывод статистики этажей по городам
                // Печатаем общее время, затраченное на обработку файла
                System.out.println("Время обработки файла: " + processingTime + " мс.");
            }
        }
    }


    // Чтение CSV файла
    private static List<Map<String, String>> loadCsv(String filePath) {
        List<Map<String, String>> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Пропустить заголовок
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    Map<String, String> record = new HashMap<>();
                    record.put("city", parts[0].replaceAll("\"", "").trim());
                    record.put("street", parts[1].replaceAll("\"", "").trim());
                    record.put("house", parts[2].trim());
                    record.put("floor", parts[3].trim());
                    data.add(record);
                }
            }
            System.out.println("CSV файл успешно загружен.");
        } catch (IOException e) {
            System.out.println("Ошибка при чтении CSV файла: " + e.getMessage());
        }
        return data;
    }

    // Чтение XML файла
    private static List<Map<String, String>> loadXml(String filePath) {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(new File(filePath), new DefaultHandler() {
                Map<String, String> record;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if ("item".equals(qName)) {
                        record = new HashMap<>();
                        record.put("city", attributes.getValue("city"));
                        record.put("street", attributes.getValue("street"));
                        record.put("house", attributes.getValue("house"));
                        record.put("floor", attributes.getValue("floor"));
                        data.add(record);
                    }
                }
            });
            System.out.println("XML файл успешно загружен.");
        } catch (Exception e) {
            System.out.println("Ошибка при чтении XML файла: " + e.getMessage());
        }
        return data;
    }

    // Поиск и вывод дубликатов
    private static void printDuplicates(List<Map<String, String>> records) {
        Map<String, Integer> duplicates = new HashMap<>();
        for (Map<String, String> record : records) {
            String key = record.get("city") + "|" + record.get("street") + "|" + record.get("house");
            duplicates.put(key, duplicates.getOrDefault(key, 0) + 1);
        }

        System.out.println("Дублирующиеся записи:");
        for (Map.Entry<String, Integer> entry : duplicates.entrySet()) {
            if (entry.getValue() > 1) {
                System.out.println("Адрес: " + entry.getKey().replace("|", ", ") + " - Повторений: " + entry.getValue());
            }
        }
    }
    // Подсчёт зданий по количеству этажей
    private static void printFloorStats(List<Map<String, String>> records) {
        Map<String, Map<String, Integer>> cityFloors = new HashMap<>();

        for (Map<String, String> record : records) {
            String city = record.get("city");
            String floor = record.get("floor");

            cityFloors.putIfAbsent(city, new HashMap<>());
            Map<String, Integer> floors = cityFloors.get(city);
            floors.put(floor, floors.getOrDefault(floor, 0) + 1);
        }

        System.out.println("Статистика этажей по городам:");
        for (Map.Entry<String, Map<String, Integer>> cityEntry : cityFloors.entrySet()) {
            System.out.println("Город: " + cityEntry.getKey());
            for (Map.Entry<String, Integer> floorEntry : cityEntry.getValue().entrySet()) {
                System.out.println("  " + floorEntry.getKey() + "-этажных зданий: " + floorEntry.getValue());
            }
        }
    }
}
