import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {

    private static final int WINDOW_SIZE = 10;
    private static final List<Integer> window = new LinkedList<>();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        Map<String, Object> result = getNumbers("p");
        System.out.println(result);
        
        Map<String, Object> productsResult = getTopProducts("AMZ", "Laptop", 1, 10000, 10);
        System.out.println(productsResult);
    }

    public static Map<String, Object> getNumbers(String numbeid) {
        long startTime = System.currentTimeMillis();
        List<Integer> prevState;
        List<Integer> newNumbers;

        synchronized (window) {
            prevState = new ArrayList<>(window);
        }

        newNumbers = fetchNumbers(numbeid);

        synchronized (window) {
            for (Integer number : newNumbers) {
                if (!window.contains(number)) {
                    if (window.size() >= WINDOW_SIZE) {
                        window.remove(0); // Remove oldest number
                    }
                    window.add(number);
                }
            }
        }

        double average;
        synchronized (window) {
            average = window.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("windowPrevState", prevState);
        synchronized (window) {
            response.put("windowCurrentState", new ArrayList<>(window));
        }
        response.put("numbers", newNumbers);
        response.put("avg", Math.round(average * 100.0) / 100.0);

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime > 500) {
            response.put("error", "Request timeout");
        }

        return response;
    }

    private static List<Integer> fetchNumbers(String numbeid) {
        // Simulating fetching numbers from third-party server
        // Replace with actual logic to fetch numbers
        List<Integer> numbers = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            numbers.add(random.nextInt(100));
        }
        return numbers;
    }

    public static Map<String, Object> getTopProducts(String company, String category, int minPrice, int maxPrice, int top) {
        String urlString = "http://20.244.56.144/test/companies/" + company +
                "/categories/" + category + "/products?top=" + top +
                "&minPrice=" + minPrice + "&maxPrice=" + maxPrice;

        Future<List<String>> future = executor.submit(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    conn.disconnect();

                    // Assuming the response is a JSON array of product names
                    String jsonString = content.toString();
                    return Arrays.stream(jsonString.replace("[", "").replace("]", "").split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });

        try {
            List<String> products = future.get();
            Map<String, Object> response = new HashMap<>();
            response.put("company", company);
            response.put("category", category);
            response.put("minPrice", minPrice);
            response.put("maxPrice", maxPrice);
            response.put("products", products);
            return response;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}