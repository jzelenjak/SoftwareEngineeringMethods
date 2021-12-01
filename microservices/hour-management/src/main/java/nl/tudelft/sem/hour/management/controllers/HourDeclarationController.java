package nl.tudelft.sem.hour.management.controllers;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.Data;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
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
     * Entry point of the repo, also acts as a sanity check.
     *
     * @return a simple greeting
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
    @GetMapping("/declaration")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<HourDeclaration> getAllDeclarations() {
        List<HourDeclaration> result = hourDeclarationRepository.findAll();

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "There are no declarations in the system.");
        }

        return result;
    }

    /**
     * Allows a user to post a new hour-declaration.
     *
     * @param hourDeclarationRequest hour declaration that will be saved
     *
     * @return an informative message about status of request
     */
    @PostMapping("/declaration")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String
    declareHours(@RequestBody @Valid HourDeclarationRequest hourDeclarationRequest) {
        HourDeclaration hourDeclaration = new HourDeclaration(hourDeclarationRequest);

        try {
            // throws error if there is a problem with the given argument
            long declarationId = hourDeclarationRepository.save(hourDeclaration).getDeclarationId();
            return String.format("Declaration with id %s has been successfully saved.",
                    declarationId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Given declaration is not valid.");
        }
    }

    /**
     * Get a declaration associated with declarationId.
     *
     * @param declarationId id of the desired student
     *
     * @return all declared hours associated with a student
     */
    @GetMapping("/declaration/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody HourDeclaration
    getSpecifiedDeclaration(@PathVariable("id") long declarationId) {
        Optional<HourDeclaration> result = hourDeclarationRepository.findById(declarationId);

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("There are no declarations with id: %d in the system.",
                            declarationId));
        }

        return result.get();
    }


    /**
     * Allows a user to delete an unapproved hour-declaration.
     *
     * @param declarationId id of declaration to be deleted
     *
     * @return an informative message about status of request
     */
    @DeleteMapping("/declaration/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String deleteDeclaredHour(@PathVariable("id") long declarationId) {
        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository
                .findById(declarationId);

        if (hourDeclaration.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Given declaration does not exists.");
        } else if (hourDeclaration.get().isApproved()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Given declaration has been approved.");
        }

        hourDeclarationRepository.delete(hourDeclaration.get());

        return String.format("Declaration with id %s has been successfully deleted.",
                declarationId);
    }

    /**
     * Allows a user to approve an unapproved hour-declaration.
     *
     * @param declarationId id of declaration to be deleted
     * @return an informative message about status of request
     */
    @PutMapping("/declaration/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String approveDeclaredHour(@PathVariable("id") long declarationId) {
        Optional<HourDeclaration> hourDeclaration = hourDeclarationRepository
                .findById(declarationId);

        if (hourDeclaration.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Given declaration does not exists.");
        } else if (hourDeclaration.get().isApproved()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Given declaration has been approved.");
        }

        // approve the declaration
        hourDeclaration.get().setApproved(true);
        hourDeclarationRepository.save(hourDeclaration.get());

        return String.format("Declaration with id %s has been successfully approved.",
                declarationId);
    }

    /**
     * Gets all unapproved declarations in the system.
     *
     * @return all stored unapproved declarations
     */
    @GetMapping("/declaration/unapproved")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<HourDeclaration> getAllUnapprovedDeclarations() {
        List<HourDeclaration> result = hourDeclarationRepository.findByApproved(false);

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "There are no declarations in the system.");
        }

        return result;
    }

    /**
     * Gets all declarations associated with a student.
     *
     * @param studentId id of the desired student
     *
     * @return all declared hours associated with a student
     */
    @GetMapping("/declaration/student/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<HourDeclaration>
    getAllDeclarationsByStudent(@PathVariable("id") long studentId) {
        List<HourDeclaration> result = hourDeclarationRepository.findByStudentId(studentId);

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("There are no declarations by student: %d in the system.",
                            studentId));
        }

        return result;
    }
}
