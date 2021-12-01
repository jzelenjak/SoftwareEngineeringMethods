package nl.tudelft.sem.hour.management;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hour-management")
public class HourManagementController {
    @GetMapping
    public @ResponseBody String hello() {
        return "Hello from Hour Management";
    }

    @GetMapping("/hour-management")
    public @ResponseBody Test sad() {
        return new Test(1, ":(");
    }
}
