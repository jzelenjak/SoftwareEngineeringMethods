package nl.tudelft.sem.hour.management.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

import java.time.ZonedDateTime;
import java.util.List;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StatisticsServiceTest {
    @Autowired
    private transient StatisticsService statisticsService;

    @Autowired
    private transient HourDeclarationRepository hourDeclarationRepository;

    private final transient ZonedDateTime testDate = ZonedDateTime.now();

    private final transient HourDeclarationRequest hourDeclarationRequestOne =
            new HourDeclarationRequest(1234, 5678, 12);
    private final transient HourDeclarationRequest hourDeclarationRequestTwo =
            new HourDeclarationRequest(1234, 5678, 29);
    private final transient HourDeclarationRequest hourDeclarationRequestThree =
            new HourDeclarationRequest(1234, 5678, 42);
    private final transient HourDeclarationRequest hourDeclarationRequestFour =
            new HourDeclarationRequest(1234, 5678, 1337.5);
    private final transient HourDeclarationRequest hourDeclarationRequestInvalidate =
            new HourDeclarationRequest(1234, 5678, -9999);

    private final transient HourDeclaration hourDeclarationOne = new HourDeclaration(1,
            hourDeclarationRequestOne, false, testDate);
    private final transient HourDeclaration hourDeclarationTwo = new HourDeclaration(2,
            hourDeclarationRequestTwo, false, testDate);
    private final transient HourDeclaration hourDeclarationThree = new HourDeclaration(3,
            hourDeclarationRequestThree, false, testDate);
    private final transient HourDeclaration hourDeclarationFour = new HourDeclaration(4,
            hourDeclarationRequestFour, false, testDate);
    private final transient HourDeclaration hourDeclarationInvalidate = new HourDeclaration(5,
            hourDeclarationRequestInvalidate, false, testDate);

    @BeforeEach
    void tearDown() {
        hourDeclarationRepository.deleteAll();
    }

    @Test
    void testCalculateMean() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree);

        assertThat(statisticsService.calculateMean(hourDeclarationList)).isEqualTo(27.6666666667, withPrecision(0.00001));
    }

    @Test
    void testCalculateMeanValidate() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree, hourDeclarationFour);

        assertThat(statisticsService.calculateMean(hourDeclarationList)).isEqualTo(355.125, withPrecision(0.00001));
    }

    @Test
    void testCalculateMeanInvalid() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree, hourDeclarationFour, hourDeclarationInvalidate);

        assertThat(statisticsService.calculateMean(hourDeclarationList)).isEqualTo(-1);
    }

    @Test
    void testCalculateMedianOdd() {
        // Ensure sorted
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne, hourDeclarationTwo, hourDeclarationThree);

        assertThat(statisticsService.calculateMedian(hourDeclarationList)).isEqualTo(29, withPrecision(0.00001));
    }

    @Test
    void testCalculateMedianEven() {
        // Ensure sorted
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree, hourDeclarationFour);

        assertThat(statisticsService.calculateMedian(hourDeclarationList)).isEqualTo(35.5, withPrecision(0.00001));
    }

    @Test
    void testCalculateMedianInvalidate() {
        List<HourDeclaration> hourDeclarationList = List.of();

        assertThat(statisticsService.calculateMedian(hourDeclarationList)).isEqualTo(-1);
    }

    @Test
    void testCalculateStandardDeviationValid() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree);

        double mean = statisticsService.calculateMean(hourDeclarationList);

        assertThat(statisticsService.calculateStandardDeviation(mean, hourDeclarationList)).isEqualTo(12.283683848459, withPrecision(0.00001));
    }

    @Test
    void testCalculateStandardDeviationSkew() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree, hourDeclarationFour);

        double mean = statisticsService.calculateMean(hourDeclarationList);

        assertThat(statisticsService.calculateStandardDeviation(mean, hourDeclarationList)).isEqualTo(567.27422546331, withPrecision(0.00001));
    }

    @Test
    void testCalculateStandardDeviationInvalid() {
        List<HourDeclaration> hourDeclarationList = List.of();

        assertThat(statisticsService.calculateStandardDeviation(0, hourDeclarationList)).isEqualTo(-1);
    }
}
