package com.pmrs.backend.service;

import com.pmrs.backend.entity.Company;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.DriveFormSubmission;
import com.pmrs.backend.entity.HRContact;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.CompanyRepository;
import com.pmrs.backend.repository.DriveFormSubmissionRepository;
import com.pmrs.backend.repository.DriveRepository;
import com.pmrs.backend.repository.HRContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Promotes staged Google Form submissions into the real placement tables.
 *
 * <p>Mirrors the Academic ERP import pattern: the staging row is the source,
 * "include" is the human approval step, and everything created goes through
 * the normal Companies/HRContacts/Drives tables so the drive automatically
 * appears in every existing view (Drives page, dashboards, eligible drives).
 */
@Service
public class DriveFormServiceImpl implements DriveFormService {

    private final DriveFormSubmissionRepository submissionRepository;
    private final CompanyRepository             companyRepository;
    private final HRContactRepository           hrContactRepository;
    private final DriveRepository               driveRepository;
    private final PenaltyService                penaltyService;

    public DriveFormServiceImpl(DriveFormSubmissionRepository submissionRepository,
                                CompanyRepository             companyRepository,
                                HRContactRepository           hrContactRepository,
                                DriveRepository               driveRepository,
                                PenaltyService                penaltyService) {
        this.submissionRepository = submissionRepository;
        this.companyRepository    = companyRepository;
        this.hrContactRepository  = hrContactRepository;
        this.driveRepository      = driveRepository;
        this.penaltyService       = penaltyService;
    }

    @Override
    public List<DriveFormSubmission> getSubmissionsByStatus(String status) {
        return submissionRepository.findByStatusOrderBySubmissionIdDesc(status);
    }

    @Override
    public List<Company> getCompaniesWithoutSubmission() {
        Set<String> submittedNames = submissionRepository.findAll().stream()
                .map(DriveFormSubmission::getCompanyName)
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.trim().toLowerCase())
                .collect(Collectors.toSet());

        return companyRepository.findAll().stream()
                .filter(c -> c.getCompanyName() != null
                        && !submittedNames.contains(c.getCompanyName().trim().toLowerCase()))
                .toList();
    }

    @Override
    @Transactional
    public DriveFormSubmission includeSubmission(Integer submissionId) {
        DriveFormSubmission submission = getSubmission(submissionId);

        if (!DriveFormSubmission.STATUS_PENDING.equals(submission.getStatus())) {
            throw new IllegalArgumentException(
                    "Submission " + submissionId + " is already " + submission.getStatus() + ".");
        }
        if (submission.getCompanyName() == null || submission.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Submission has no company name — fix the sheet row and re-sync.");
        }
        if (submission.getRoleOffered() == null || submission.getRoleOffered().isBlank()) {
            throw new IllegalArgumentException("Submission has no role offered — fix the sheet row and re-sync.");
        }
        if (submission.getDriveDate() == null) {
            throw new IllegalArgumentException(
                    "Submission has no valid drive date (use YYYY-MM-DD or DD/MM/YYYY in the form).");
        }

        Company company = findOrCreateCompany(submission);
        HRContact hrContact = findOrCreateHrContact(submission, company);

        Drive drive = new Drive();
        drive.setCompany(company);
        drive.setHrContact(hrContact);
        drive.setDriveDate(submission.getDriveDate());
        drive.setPptDate(submission.getPptDate());
        drive.setResumeSelectionDate(submission.getResumeSelectionDate());
        drive.setFinalSelectionDate(submission.getFinalSelectionDate());
        drive.setRoleOffered(submission.getRoleOffered().trim());
        drive.setPackageLpa(submission.getPackageLpa());
        drive.setDriveType(blankToNull(submission.getDriveType()));
        drive.setStatus("Upcoming");
        drive.setMinCgpa(submission.getMinCgpa());
        drive.setMaxBacklogs(submission.getMaxBacklogs());
        Drive savedDrive = driveRepository.save(drive);
        penaltyService.onDriveCreated(savedDrive);

        submission.setStatus(DriveFormSubmission.STATUS_INCLUDED);
        submission.setDriveId(savedDrive.getDriveId());
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public DriveFormSubmission rejectSubmission(Integer submissionId) {
        DriveFormSubmission submission = getSubmission(submissionId);
        if (!DriveFormSubmission.STATUS_PENDING.equals(submission.getStatus())) {
            throw new IllegalArgumentException(
                    "Submission " + submissionId + " is already " + submission.getStatus() + ".");
        }
        submission.setStatus(DriveFormSubmission.STATUS_REJECTED);
        return submissionRepository.save(submission);
    }

    private DriveFormSubmission getSubmission(Integer submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Form submission not found with id: " + submissionId));
    }

    private Company findOrCreateCompany(DriveFormSubmission submission) {
        String name = submission.getCompanyName().trim();
        return companyRepository.findByCompanyNameIgnoreCase(name)
                .orElseGet(() -> {
                    Company company = new Company();
                    company.setCompanyName(name);
                    company.setSector(blankToNull(submission.getSector()));
                    // Company.tier is mandatory — default new companies to Tier A
                    // when the form left it blank; an admin can retier later.
                    company.setTier(normaliseTier(submission.getTier()));
                    company.setWebsite(normaliseWebsite(submission.getWebsite()));
                    return companyRepository.save(company);
                });
    }

    /**
     * Reuses an existing HR contact (matched by email within the company) or
     * creates one. Returns null when the form didn't provide a full contact —
     * Drive.hrContact is optional, so the drive is still created.
     */
    private HRContact findOrCreateHrContact(DriveFormSubmission submission, Company company) {
        String email = blankToNull(submission.getHrEmail());
        String name  = blankToNull(submission.getHrName());
        String phone = blankToNull(submission.getHrPhone());

        if (email != null) {
            HRContact existing = hrContactRepository
                    .findFirstByCompany_CompanyIdAndHrEmailIgnoreCase(company.getCompanyId(), email)
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        // HRContact requires name, email and phone — skip creation if any is missing.
        if (email == null || name == null || phone == null) {
            return null;
        }

        HRContact contact = new HRContact();
        contact.setCompany(company);
        contact.setHrName(name);
        contact.setHrEmail(email);
        contact.setHrPhone(phone);
        return hrContactRepository.save(contact);
    }

    private static String normaliseTier(String tier) {
        String t = tier == null ? "" : tier.trim().toUpperCase();
        return switch (t) {
            case "A", "B", "C" -> t;
            default            -> "A";
        };
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /**
     * Companies.website has a DB CHECK requiring an http(s) URL, but recruiters
     * often type a bare domain ("acme.com") or, occasionally, an email address
     * in that field. Prefix bare domains with https://; drop values that still
     * can't be a URL (e.g. contain "@") rather than fail the whole import.
     */
    private static String normaliseWebsite(String raw) {
        String website = blankToNull(raw);
        if (website == null) return null;
        if (website.matches("(?i)^https?://.+")) return website;
        if (website.contains("@")) return null;
        return "https://" + website;
    }
}
