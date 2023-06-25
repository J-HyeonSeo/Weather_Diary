package jhsfully.weather.service;

import jhsfully.weather.WeatherApplication;
import jhsfully.weather.domain.DateWeather;
import jhsfully.weather.domain.Diary;
import jhsfully.weather.error.InvalidDate;
import jhsfully.weather.repository.DateWeatherRepository;
import jhsfully.weather.repository.DiaryRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DiaryService {

    @Value("${openweathermap.key}")
    private String apiKey;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);
    //constructor.
    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    //service codes.

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate(){
        dateWeatherRepository.save(getWeatherFromApi());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text){
        logger.info("createDiary function started.");
        //날씨 데이터 가져오기 API or DB
        DateWeather dateWeather = getDateWeather(date);

        //파싱된 데이터 + 일기 값 우리 db에 넣기.

        diaryRepository.save(Diary.builder()
                        .weather(dateWeather.getWeather())
                        .icon(dateWeather.getIcon())
                        .temperature(dateWeather.getTemperature())
                        .text(text)
                        .date(date)
                        .build());
        logger.info("createDiary function ended successfully");
    }

    public List<Diary> readDiary(LocalDate date){
        logger.debug("called readDiary function. date : " + date.toString());

        if(date.isAfter(LocalDate.ofYearDay(3050, 1))){
            throw new InvalidDate();
        }

        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate){
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    @Transactional
    public void updateDiary(LocalDate date, String text){
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
    }

    @Transactional
    public void deleteDiary(LocalDate date){
        diaryRepository.deleteAllByDate(date);
    }

    //==================================== Util fuction =========================================
    private DateWeather getDateWeather(LocalDate date){
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if(dateWeatherListFromDB.size() == 0){
            //새로 api에서 날씨 정보를 가져와야 한다.
            return getWeatherFromApi();
        }
        return dateWeatherListFromDB.get(0);
    }

    private DateWeather getWeatherFromApi(){
        //open weather map에서 날씨데이터 가져오기.
        String weatherData = getWeatherString();
        //받아온 날씨 json파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);

        return DateWeather.builder()
                        .date(LocalDate.now())
                        .weather(parsedWeather.get("main").toString())
                        .icon(parsedWeather.get("icon").toString())
                        .temperature((Double) parsedWeather.get("temp"))
                        .build();
    }
    private String getWeatherString(){
        String apiurl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiurl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            if(responseCode == 200){
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }else{
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = br.readLine()) != null){
                response.append(inputLine);
            }
            br.close();
            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString){

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try{
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        }catch (ParseException e){
            throw new RuntimeException();
        }
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        return resultMap;
    }
}
