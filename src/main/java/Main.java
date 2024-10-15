import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main {
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final String ACCESS_KEY = "f84bc312-c29d-4f01-a4a1-85b646ac0517";
    private static final String WELCOME_MESSAGE = """
            Вас приветствует Яндекс.Погода.
            Для получения информации о погоде в конкретном месте,
            необходимо ввести ширину и долготу места.
            
            Введите ширину: """;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print(WELCOME_MESSAGE);
        String latitude = scanner.nextLine();
        System.out.print("Введите долготу: ");
        String longitude = scanner.nextLine();
        System.out.println("За сколько дней посчитать среднюю температуру?");
        String limit = scanner.nextLine();
        scanner.close();

        getWeather(latitude, longitude, limit);

    }

    private static void getWeather(String latitude, String longitude, String limit) {
        try {
            URI uri = new URI(String.format("%s?lat=%s&lon=%s&limit=%s", API_URL, latitude, longitude, limit));
            URL url = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Yandex-Weather-Key", ACCESS_KEY);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String responceLine;

                while ((responceLine = reader.readLine()) != null) {
                    response.append(responceLine);
                }

                reader.close();
                JSONObject jsonResponse = new JSONObject(response.toString());

                System.out.println(jsonResponse.toString(2));
                JSONObject fact = jsonResponse.getJSONObject("fact");
                System.out.printf("Текущая температура: %s\n", fact.getInt("temp"));

                JSONArray forecasts = jsonResponse.getJSONArray("forecasts");
                int avgTemp = getAvgTemp(forecasts);
                System.out.printf("Средняя температура за %s дней(я): %s", limit, avgTemp);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getAvgTemp(JSONArray forecasts) throws JSONException {
        int tempSum = 0;
        for (int i=0; i < forecasts.length(); i++) {
            JSONObject day = forecasts.getJSONObject(i);
            JSONArray hours = day.getJSONArray("hours");
            for (int y=0; y < hours.length(); y++) {
                JSONObject hour = hours.getJSONObject(y);
                tempSum += hour.getInt("temp");
            }
        }

        return tempSum / (24 * forecasts.length());
    }
}