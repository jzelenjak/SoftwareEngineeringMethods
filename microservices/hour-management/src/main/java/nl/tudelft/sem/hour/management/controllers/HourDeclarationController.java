package nl.tudelft.sem.hour.management.controllers;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.Data;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/hour-management")
@Data
public class HourDeclarationController {

    private final HourDeclarationRepository hourDeclarationRepository;

    /**
     * Sanity check method of the controller.
     * 
     * @return a greeting
     */
    @GetMapping
    public @ResponseBody String hello() {
        return "Hello from Hour Management";
    }

    /**
     * Gets all the stored declarations in the system.
     *
     * @return all stored declaration in the system
     */
    @GetMapping("/api/declaration")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<HourDeclaration> getAllDeclarations() {
        return hourDeclarationRepository.findAll();
    }

    /**
     * Allows a user to post a new hour-declaration.
     *
     * @param hourDeclaration hour declaration that will be saved
     *
     * @return an informative message about status of request
     */
    @PostMapping("/api/declaration")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String declareHours(@RequestBody @Valid HourDeclaration hourDeclaration) {
        try {
            // throws error if there is a problem with the given argument
            long declarationId = hourDeclarationRepository.save(hourDeclaration).getDeclarationId();
            return String.format("Declaration with id %s has been successfully saved.", declarationId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given declaration is not valid.");
        }
    }

    /**
     * Allows a user to delete an unapproved hour-declaration.
     *
     * @param declarationId id of declaration to be deleted
     *
     * @return an informative message about status of request
     */
    @DeleteMapping("/api/declaration/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String deleteDeclaredHour(@PathVariable("id") long declarationId) {
        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository.findById(declarationId);

        if (hourDeclaration.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given declaration is not valid.");
        } else if (hourDeclaration.get().isApproved()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given declaration has been approved.");
        }

        hourDeclarationRepository.delete(hourDeclaration.get());

        return String.format("Declaration with id %s has been successfully deleted.", declarationId);
    }

    /**
     * Gets all unapproved declarations in the system.
     *
     * @return all stored unapproved declarations
     */
    @GetMapping("/api/declaration/unapproved")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<HourDeclaration> getAllUnapprovedDeclarations() {
        return hourDeclarationRepository.findByApproved(false);
    }

    /**
     * Gets all declarations associated with a student.
     *
     * @param studentId id of the desired student
     *
     * @return all declared hours associated with a student
     */
    @GetMapping("/api/declaration/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<HourDeclaration> getAllDeclarationsByStudent(@PathVariable("id") long studentId) {
        return hourDeclarationRepository.findByStudentId(studentId);
    }
}
