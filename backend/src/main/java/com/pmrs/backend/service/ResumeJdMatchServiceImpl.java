package com.pmrs.backend.service;

import com.pmrs.backend.dto.DriveSkillsGapSummaryDTO;
import com.pmrs.backend.dto.MlServiceAnalyzeResponse;
import com.pmrs.backend.dto.MlServiceCritiqueResponse;
import com.pmrs.backend.dto.MlServiceMatchResponse;
import com.pmrs.backend.dto.ResumeCritiqueDTO;
import com.pmrs.backend.dto.ResumeJdMatchResultDTO;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.ResumeJdMatch;
import com.pmrs.backend.entity.ResumeJdMatchSkill;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.repository.ResumeJdMatchRepository;
import com.pmrs.backend.repository.ResumeJdMatchSkillRepository;
import com.pmrs.backend.repository.StudentRepository;
import com.pmrs.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates the resume/JD skills-gap analysis: keeps the student's cached
 * resume text fresh, calls ml-service for the actual extraction/matching/
 * write-up, and persists the result as a ResumeJdMatch (+ skill rows). Also
 * hosts the lighter-weight ml-service integrations that don't warrant their
 * own service: fast fit-scoring (no LLM call), cohort skills-gap aggregation
 * (pure DB aggregation, no ml-service call at all), and the standalone resume
 * critique (one LLM call, nothing persisted).
 */
@Service
public class ResumeJdMatchServiceImpl implements ResumeJdMatchService {

    private static final Logger log = LoggerFactory.getLogger(ResumeJdMatchServiceImpl.class);
    private static final int TOP_SKILLS_LIMIT = 5;

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DocumentTextExtractionService documentTextExtractionService;
    private final DriveService driveService;
    private final ResumeJdMatchRepository resumeJdMatchRepository;
    private final ResumeJdMatchSkillRepository resumeJdMatchSkillRepository;
    private final RestTemplate restTemplate;

    @Value("${prms.ml-service.base-url:http://localhost:8000}")
    private String mlServiceBaseUrl;

    public ResumeJdMatchServiceImpl(StudentRepository studentRepository,
                                    UserRepository userRepository,
                                    DocumentTextExtractionService documentTextExtractionService,
                                    DriveService driveService,
                                    ResumeJdMatchRepository resumeJdMatchRepository,
                                    ResumeJdMatchSkillRepository resumeJdMatchSkillRepository,
                                    RestTemplate restTemplate) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.documentTextExtractionService = documentTextExtractionService;
        this.driveService = driveService;
        this.resumeJdMatchRepository = resumeJdMatchRepository;
        this.resumeJdMatchSkillRepository = resumeJdMatchSkillRepository;
        this.restTemplate = restTemplate;
    }

    private Student resolveStudent(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (user.getStudentId() == null) {
            throw new IllegalStateException("No student profile linked to this account");
        }
        return studentRepository.findById(user.getStudentId())
                .orElseThrow(() -> new IllegalStateException("Student record not found"));
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

    @Override
    public ResumeJdMatchResultDTO analyze(String username, Integer driveId) {
        Student student = resolveStudent(username);
        Drive drive = driveService.getDriveById(driveId);

        String resumeText = refreshResumeTextCache(student);
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalStateException("Resume not uploaded yet, or its text couldn't be extracted.");
        }

        String jdText = drive.getJdText();
        if (jdText == null || jdText.isBlank()) {
            throw new IllegalStateException("This drive doesn't have a job description attached yet.");
        }

        MlServiceAnalyzeResponse mlResponse = restTemplate.postForObject(
                mlServiceBaseUrl + "/analyze",
                Map.of("jd_text", jdText, "resume_text", resumeText),
                MlServiceAnalyzeResponse.class);

        ResumeJdMatch match = new ResumeJdMatch();
        match.setStudent(student);
        match.setDrive(drive);
        match.setScore(mlResponse.getScore());
        match.setWriteUp(mlResponse.getWriteUp());
        match.setInterviewQuestions(joinLines(mlResponse.getInterviewQuestions()));
        match = resumeJdMatchRepository.save(match);

        List<ResumeJdMatchSkill> skills = new ArrayList<>();
        for (MlServiceAnalyzeResponse.MatchedSkill matched : mlResponse.getMatchedSkills()) {
            ResumeJdMatchSkill skill = new ResumeJdMatchSkill();
            skill.setMatch(match);
            skill.setSkillName(matched.getJdSkill());
            skill.setStatus("MATCHED");
            skill.setMatchedResumeSkill(matched.getResumeSkill());
            skills.add(skill);
        }
        for (String missing : mlResponse.getMissingSkills()) {
            ResumeJdMatchSkill skill = new ResumeJdMatchSkill();
            skill.setMatch(match);
            skill.setSkillName(missing);
            skill.setStatus("MISSING");
            skills.add(skill);
        }
        resumeJdMatchSkillRepository.saveAll(skills);

        return toDTO(match, skills);
    }

    @Override
    public ResumeJdMatchResultDTO getLatestMatch(String username, Integer driveId) {
        Student student = resolveStudent(username);
        return resumeJdMatchRepository
                .findTopByStudent_StudentIdAndDrive_DriveIdOrderByCreatedAtDesc(
                        student.getStudentId(), driveId)
                .map(match -> toDTO(match, resumeJdMatchSkillRepository.findByMatch_MatchId(match.getMatchId())))
                .orElse(null);
    }

    @Override
    public Integer computeFitScore(Student student, Drive drive) {
        String resumeText = refreshResumeTextCache(student);
        String jdText = drive.getJdText();
        if (resumeText == null || resumeText.isBlank() || jdText == null || jdText.isBlank()) {
            return null;
        }
        try {
            MlServiceMatchResponse response = restTemplate.postForObject(
                    mlServiceBaseUrl + "/match",
                    Map.of("jd_text", jdText, "resume_text", resumeText),
                    MlServiceMatchResponse.class);
            return response != null ? response.getScore() : null;
        } catch (Exception e) {
            log.warn("Fit-score computation failed for student {} / drive {}: {}",
                    student.getStudentId(), drive.getDriveId(), e.getMessage());
            return null;
        }
    }

    @Override
    public DriveSkillsGapSummaryDTO getSkillsGapSummary(Integer driveId) {
        int totalAnalyses = resumeJdMatchRepository.findByDrive_DriveId(driveId).size();
        List<ResumeJdMatchSkill> skills = resumeJdMatchSkillRepository.findByMatch_Drive_DriveId(driveId);

        DriveSkillsGapSummaryDTO dto = new DriveSkillsGapSummaryDTO();
        dto.setDriveId(driveId);
        dto.setTotalAnalyses(totalAnalyses);
        dto.setTopMissingSkills(topSkillCounts(skills, "MISSING"));
        dto.setTopMatchedSkills(topSkillCounts(skills, "MATCHED"));
        return dto;
    }

    private List<DriveSkillsGapSummaryDTO.SkillCountDTO> topSkillCounts(List<ResumeJdMatchSkill> skills, String status) {
        return skills.stream()
                .filter(s -> status.equals(s.getStatus()))
                .collect(Collectors.groupingBy(ResumeJdMatchSkill::getSkillName, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_SKILLS_LIMIT)
                .map(e -> new DriveSkillsGapSummaryDTO.SkillCountDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public ResumeCritiqueDTO critiqueResume(String username) {
        Student student = resolveStudent(username);
        String resumeText = refreshResumeTextCache(student);
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalStateException("Resume not uploaded yet, or its text couldn't be extracted.");
        }

        MlServiceCritiqueResponse response = restTemplate.postForObject(
                mlServiceBaseUrl + "/critique-resume",
                Map.of("resume_text", resumeText),
                MlServiceCritiqueResponse.class);

        return new ResumeCritiqueDTO(response.getOverall(), response.getStrengths(), response.getImprovements());
    }

    private static String joinLines(List<String> lines) {
        return lines == null ? null : String.join("\n", lines);
    }

    private static List<String> splitLines(String text) {
        return (text == null || text.isBlank()) ? List.of() : List.of(text.split("\n"));
    }

    private ResumeJdMatchResultDTO toDTO(ResumeJdMatch match, List<ResumeJdMatchSkill> skills) {
        ResumeJdMatchResultDTO dto = new ResumeJdMatchResultDTO();
        dto.setMatchId(match.getMatchId());
        dto.setScore(match.getScore());
        dto.setWriteUp(match.getWriteUp());
        dto.setInterviewQuestions(splitLines(match.getInterviewQuestions()));
        dto.setCreatedAt(match.getCreatedAt());
        dto.setSkills(skills.stream()
                .map(s -> new ResumeJdMatchResultDTO.SkillDTO(s.getSkillName(), s.getStatus(), s.getMatchedResumeSkill()))
                .toList());
        return dto;
    }
}
