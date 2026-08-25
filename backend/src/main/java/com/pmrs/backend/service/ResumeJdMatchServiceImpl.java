package com.pmrs.backend.service;

import com.pmrs.backend.entity.Student;
import com.pmrs.backend.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Orchestrates the resume/JD skills-gap analysis. Currently only the
 * resume-text caching piece is implemented (Day 3 of the build plan) — the
 * actual analysis (calling the ml-service and persisting a ResumeJdMatch) is
 * added once that service exists.
 */
@Service
public class ResumeJdMatchServiceImpl implements ResumeJdMatchService {

    private static final Logger log = LoggerFactory.getLogger(ResumeJdMatchServiceImpl.class);

    private final StudentRepository studentRepository;
    private final DocumentTextExtractionService documentTextExtractionService;

    public ResumeJdMatchServiceImpl(StudentRepository studentRepository,
                                    DocumentTextExtractionService documentTextExtractionService) {
        this.studentRepository = studentRepository;
        this.documentTextExtractionService = documentTextExtractionService;
    }

    @Override
    public String refreshResumeTextCache(Student student) {
        String resumeUrl = student.getResumeUrl();
        if (resumeUrl == null || resumeUrl.isBlank()) {
            return null;
        }

        // Cache still matches the current resumeUrl — nothing to do.
        if (resumeUrl.equals(student.getResumeTextSourceUrl()) && student.getResumeTextCache() != null) {
            return student.getResumeTextCache();
        }

        String text = documentTextExtractionService.extractText(resumeUrl);
        student.setResumeTextCache(text);
        student.setResumeTextSourceUrl(resumeUrl);
        student.setResumeTextExtractedAt(text != null ? LocalDateTime.now() : null);
        studentRepository.save(student);

        if (text == null) {
            log.warn("Could not extract resume text for student {} (roll {}) from {}",
                    student.getStudentId(), student.getRollNo(), resumeUrl);
        }
        return text;
    }
}
