package com.academic.service.service;

import com.academic.service.dto.StudentRequestDTO;
import com.academic.service.dto.StudentResponseDTO;

import java.util.List;

public interface StudentService {

    StudentResponseDTO create(StudentRequestDTO request);

    List<StudentResponseDTO> findAll();

    StudentResponseDTO findByRollNo(String rollNo);

    StudentResponseDTO update(String rollNo, StudentRequestDTO request);

    void delete(String rollNo);
}
