package com.practice.sakthi_via.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.practice.sakthi_via.constants.Constants;
import com.practice.sakthi_via.exception.ResourceNotFoundException;
import com.practice.sakthi_via.facade.EmployeeFacade;
import com.practice.sakthi_via.model.Employee;
import com.practice.sakthi_via.model.dto.EmployeeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest
public class EmployeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EmployeeFacade employeeFacade;

    Employee employee = new Employee();

    @BeforeEach
    public void setup() {
        employee.setId((long) 40000);
        employee.setName("Sakthi");
        employee.setEmail("sgsakthi1992@gmail.com");
        employee.setUsername("sgsakthi");
        employee.setAge(27);
    }

    @Test
    @DisplayName("Fetch Employee")
    public void testGetEmployee() throws Exception {
        //GIVEN
        when(employeeFacade.getEmployees()).thenReturn(
                Stream.of(employee).collect(Collectors.toList()));

        //WHEN
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employees"));

        //THEN
        validateOkResponse(resultActions)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(employee.getName())));
    }

    @Test
    @DisplayName("Create Employee with valid details")
    public void testPostEmployee() throws Exception {
        //GIVEN
        EmployeeDto employeeDto = new EmployeeDto("Employee 1",
                "employee1", "emp1@gmail.com", 25);

        String requestJson = convertEmployeeDtoToJson(employeeDto);

        when(employeeFacade.createEmployee(Mockito.any(EmployeeDto.class))).thenReturn(employee);
        when(employeeFacade.checkUsername(employeeDto.getUsername())).thenReturn(true);

        //WHEN
        ResultActions resultActions = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        //THEN
        validateOkResponse(resultActions).andExpect(jsonPath("name", is(employee.getName())));
    }

    @Test
    @DisplayName("Create Employee with invalid Email")
    public void testPostEmployeeWithInvalidEmailId() throws Exception {
        //GIVEN
        EmployeeDto employeeDto = new EmployeeDto("Employee 1",
                "employee1", "emp1", 25);

        String requestJson = convertEmployeeDtoToJson(employeeDto);

        //WHEN
        ResultActions resultActions = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        //THEN
        validateBadRequestResponse(resultActions, Constants.EMAIL_VALIDATION_MSG);
    }

    @Test
    @DisplayName("Create Employee with less than allowed Username length")
    public void testPostEmployeeWithMinUsernameSize() throws Exception {
        //GIVEN
        EmployeeDto employeeDto = new EmployeeDto("Employee 1",
                "emp", "emp1", 25);

        String requestJson = convertEmployeeDtoToJson(employeeDto);

        //WHEN
        ResultActions resultActions = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        //THEN
        validateBadRequestResponse(resultActions, Constants.EMPLOYEE_USERNAME_SIZE);
    }

    @Test
    @DisplayName("Create Employee with more than allowed Username length")
    public void testPostEmployeeWithMaxUsernameSize() throws Exception {
        //GIVEN
        EmployeeDto employeeDto = new EmployeeDto("Employee 1",
                "employee1_username", "emp1", 25);

        String requestJson = convertEmployeeDtoToJson(employeeDto);

        //WHEN
        ResultActions resultActions = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        //THEN
        validateBadRequestResponse(resultActions, Constants.EMPLOYEE_USERNAME_SIZE);
    }

    @Test
    @DisplayName("Create Employee with less than allowed Age")
    public void testPostEmployeeWithInvalidAge() throws Exception {
        //GIVEN
        EmployeeDto employeeDto = new EmployeeDto("Employee 1",
                "employee1", "emp1", 0);

        String requestJson = convertEmployeeDtoToJson(employeeDto);

        //WHEN
        ResultActions resultActions = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        //THEN
        validateBadRequestResponse(resultActions, Constants.EMPLOYEE_MINIMUM_AGE);
    }

    @Test
    @DisplayName("Fetch Employee with valid Id")
    public void testGetEmployeeById() throws Exception {
        //GIVEN
        when(employeeFacade.getEmployeeById(employee.getId())).thenReturn(employee);

        //WHEN
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employees/" + employee.getId()));

        //THEN
        validateOkResponse(resultActions).andExpect(jsonPath("name", is(employee.getName())));
    }

    @Test
    @DisplayName("Fetch Employee with invalid Id")
    public void testGetEmployeeWithInvalidId() throws Exception {
        //GIVEN
        Long id = 1L;
        when(employeeFacade.getEmployeeById(id)).thenThrow(
                new ResourceNotFoundException(Constants.EMPLOYEE_ID_NOT_FOUND));

        //WHEN
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employees/" + id));

        //THEN
        validateNotFoundResponse(resultActions, Constants.EMPLOYEE_ID_NOT_FOUND);
    }

    @Test
    @DisplayName("Delete Employee with Id")
    public void testDeleteEmployeeById() throws Exception {
        //GIVEN
        when(employeeFacade.deleteEmployeeById(employee.getId())).thenReturn("Success");

        //WHEN
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/employees/" + employee.getId()));

        //THEN
        validateOkResponse(resultActions).andExpect(content().string(containsString("Success")));
    }

    @Test
    @DisplayName("Delete Employee with Invalid Id")
    public void testDeleteEmployeeWithInvalidId() throws Exception {
        //GIVEN
        Long id = 1L;
        when(employeeFacade.deleteEmployeeById(id)).thenThrow(
                new ResourceNotFoundException(Constants.EMPLOYEE_ID_NOT_FOUND));

        //WHEN
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/employees/" + id));

        //THEN
        validateNotFoundResponse(resultActions, Constants.EMPLOYEE_ID_NOT_FOUND);
    }

    @Test
    @DisplayName("Update Employee Email with Id")
    public void testUpdateEmployeeEmailById() throws Exception {
        //GIVEN
        when(employeeFacade.updateEmployeeEmail(employee.getId(), "newemail@gmail.com")).thenReturn("Success");

        //WHEN
        ResultActions resultActions = mockMvc.perform(put("/api/v1/employees/" + employee.getId())
                .param("email", "newemail@gmail.com"));

        //THEN
        validateOkResponse(resultActions).andExpect(content().string(containsString("Success")));
    }

    @Test
    @DisplayName("Update Employee Email with invalid Id")
    public void testUpdateEmployeeEmailWithInvalidId() throws Exception {
        //GIVEN
        Long id = 1L;
        when(employeeFacade.updateEmployeeEmail(id, "newemail@gmail.com"))
                .thenThrow(new ResourceNotFoundException(Constants.EMPLOYEE_ID_NOT_FOUND));

        //WHEN
        ResultActions resultActions = mockMvc.perform(put("/api/v1/employees/" + id)
                .param("email", "newemail@gmail.com"));

        //THEN
        validateNotFoundResponse(resultActions, Constants.EMPLOYEE_ID_NOT_FOUND);
    }

    @Test
    @DisplayName("Update Employee invalid Email with Id")
    public void testUpdateEmployeeEmailWithInvalidEmail() throws Exception {
        //GIVEN
        //WHEN
        ResultActions resultActions = mockMvc.perform(put("/api/v1/employees/1")
                .param("email", "newemail"));

        //THEN
        validateBadRequestResponse(resultActions, Constants.EMAIL_VALIDATION_MSG);
    }

    @Test
    @DisplayName("Fetch Employee with Email")
    public void testGetEmployeeByEmail() throws Exception {
        //GIVEN
        when(employeeFacade.getEmployeeByEmail(employee.getEmail()))
                .thenReturn(Stream.of(employee).collect(Collectors.toList()));

        //WHEN
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employeesByEmail/" + employee.getEmail()));

        //THEN
        validateOkResponse(resultActions).andExpect(jsonPath("$[0].name", is(employee.getName())));
    }

    @Test
    @DisplayName("Fetch Employee with not available Email")
    public void testGetEmployeeByEmailWithNewEmail() throws Exception {
        //GIVEN
        String email = "newemail@gmail.com";
        when(employeeFacade.getEmployeeByEmail(email)).thenThrow(
                new ResourceNotFoundException(Constants.EMPLOYEE_EMAIL_NOT_FOUND));

        //WHEN
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employeesByEmail/" + email));

        //THEN
        validateNotFoundResponse(resultActions, Constants.EMPLOYEE_EMAIL_NOT_FOUND);
    }

    @Test
    @DisplayName("Fetch Employee with invalid Email")
    public void testGetEmployeeByEmailWithInvalidEmail() throws Exception {
        //GIVEN
        String email = "newemail";

        //WHEN
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employeesByEmail/" + email));

        //THEN
        validateBadRequestResponse(resultActions, Constants.EMAIL_VALIDATION_MSG);
    }

    @Test
    @DisplayName("Fetch Employee with Username or Email")
    public void testGetEmployeeByUsernameOrEmail() throws Exception {
        //GIVEN
        when(employeeFacade.getEmployeeByUsernameOrEmail(employee.getUsername(), employee.getEmail()))
                .thenReturn(Stream.of(employee).collect(Collectors.toList()));

        //WHEN
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employeesByUsernameOrEmail?" +
                "email=" + employee.getEmail() + "&username=" + employee.getUsername()));

        //THEN
        validateOkResponse(resultActions).andExpect(jsonPath("$[0].name", is(employee.getName())));
    }

    @Test
    @DisplayName("Fetch Employee with Username or Email with invalid Email")
    public void testGetEmployeeByUsernameOrEmailWithInvalidEmail() throws Exception {
        //GIVEN
        //WHEN
        ResultActions resultActions = mockMvc.perform(get("/api/v1/employeesByUsernameOrEmail" +
                "?email=newemail"));

        //THEN
        validateBadRequestResponse(resultActions, Constants.EMAIL_VALIDATION_MSG);
    }

    private ResultActions validateOkResponse(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isOk());
    }

    private void validateNotFoundResponse(ResultActions resultActions, String expectedMessage) throws Exception {
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("message", containsStringIgnoringCase(expectedMessage)));
    }

    private void validateBadRequestResponse(ResultActions resultActions, String expectedMessage) throws Exception {
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("message", containsStringIgnoringCase(expectedMessage)));
    }

    private String convertEmployeeDtoToJson(EmployeeDto employeeDto) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(employeeDto);
        return requestJson;
    }
}
