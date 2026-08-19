package com.pmrs.backend.service;

import com.pmrs.backend.dto.DashboardDTO;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.repository.ApplicationRepository;
import com.pmrs.backend.repository.CompanyRepository;
import com.pmrs.backend.repository.DriveRepository;
import com.pmrs.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final DriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;

    public DashboardServiceImpl(StudentRepository studentRepository,
                                CompanyRepository companyRepository,
                                DriveRepository driveRepository,
                                ApplicationRepository applicationRepository) {
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.driveRepository = driveRepository;
        this.applicationRepository = applicationRepository;
    }

    private static boolean isFiltered(String val) {
        return val != null && !val.isEmpty() && !"All".equalsIgnoreCase(val);
    }

    private static boolean isPlaced(Student s) {
        return s.getPlacementTier() != null && !s.getPlacementTier().equalsIgnoreCase("Unplaced");
    }

    /** Returns {highest, average, median} for a list of package/salary values, all 0 if empty. */
    private static double[] packageStats(List<Double> values) {
        if (values.isEmpty()) {
            return new double[]{0, 0, 0};
        }
        List<Double> sorted = values.stream().sorted().collect(Collectors.toList());
        double highest = sorted.get(sorted.size() - 1);
        double avg = sorted.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        int mid = sorted.size() / 2;
        double median = sorted.size() % 2 == 0
                ? (sorted.get(mid - 1) + sorted.get(mid)) / 2.0
                : sorted.get(mid);
        return new double[]{
                Math.round(highest * 10.0) / 10.0,
                Math.round(avg * 10.0) / 10.0,
                Math.round(median * 10.0) / 10.0
        };
    }

    /** Computes registered/higher-studies/jobs-offered/selected/salary stats for one program+branch group. */
    private static DashboardDTO.BranchCategoryStatsDTO computeCategoryStats(
            String label, List<Student> group, List<Application> allApplications) {
        DashboardDTO.BranchCategoryStatsDTO stats = new DashboardDTO.BranchCategoryStatsDTO();
        stats.setBranch(label);
        stats.setTotalRegistered(group.size());

        stats.setOptedForHigherStudies(group.stream()
                .filter(s -> Boolean.TRUE.equals(s.getOptedForHigherStudies()))
                .count());

        Set<Integer> groupIds = group.stream()
                .map(Student::getStudentId)
                .collect(Collectors.toSet());

        List<Application> groupSelectedApps = allApplications.stream()
                .filter(a -> a.getStudent() != null && groupIds.contains(a.getStudent().getStudentId()))
                .filter(a -> "Selected".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());
        stats.setTotalJobsOffered(groupSelectedApps.size());

        long selectedStudents = group.stream().filter(DashboardServiceImpl::isPlaced).count();
        stats.setTotalSelected(selectedStudents);
        stats.setPercentagePlaced(group.isEmpty() ? 0
                : Math.round((selectedStudents * 100.0 / group.size()) * 10.0) / 10.0);

        // Each placed student's best ("Selected") offer — handles students holding
        // multiple offers by taking the highest package among them.
        Map<Integer, Double> bestOfferByStudent = new HashMap<>();
        groupSelectedApps.stream()
                .filter(a -> a.getDrive() != null && a.getDrive().getPackageLpa() != null)
                .forEach(a -> bestOfferByStudent.merge(
                        a.getStudent().getStudentId(),
                        a.getDrive().getPackageLpa().doubleValue(),
                        Math::max));

        double[] salaryStats = packageStats(new ArrayList<>(bestOfferByStudent.values()));
        stats.setHighestSalary(salaryStats[0]);
        stats.setAverageSalary(salaryStats[1]);
        stats.setMedianSalary(salaryStats[2]);

        return stats;
    }

    @Override
    public DashboardDTO getDashboardData(Integer year, String branch, String program) {
        List<Student> allStudents = studentRepository.findAll();
        List<Drive> drives = driveRepository.findAll();
        List<Application> allApplications = applicationRepository.findAll();
        long totalCompanies = companyRepository.count();

        // ── Filter options (always from all students, for dropdown population) ─
        List<Integer> availableYears = allStudents.stream()
                .map(Student::getBatchYear)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> availableBranches = allStudents.stream()
                .filter(s -> s.getDepartment() != null && s.getDepartment().getBranch() != null)
                .map(s -> s.getDepartment().getBranch())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // ── Apply cohort filters to students ──────────────────────────────────
        List<Student> students = allStudents.stream()
                .filter(s -> year == null || year.equals(s.getBatchYear()))
                .filter(s -> !isFiltered(branch) || (s.getDepartment() != null
                        && branch.equals(s.getDepartment().getBranch())))
                .filter(s -> !isFiltered(program) || (s.getDepartment() != null
                        && program.equals(s.getDepartment().getProgram())))
                .collect(Collectors.toList());

        Set<Integer> filteredStudentIds = students.stream()
                .map(Student::getStudentId)
                .collect(Collectors.toSet());

        List<Application> applications = allApplications.stream()
                .filter(a -> a.getStudent() != null
                        && filteredStudentIds.contains(a.getStudent().getStudentId()))
                .collect(Collectors.toList());

        DashboardDTO dto = new DashboardDTO();
        dto.setAvailableYears(availableYears);
        dto.setAvailableBranches(availableBranches);

        // ── KPI ───────────────────────────────────────────────────────────────
        dto.setTotalStudents(students.size());
        dto.setTotalCompanies(totalCompanies);
        dto.setTotalDrives(drives.size());
        dto.setTotalApplications(applications.size());

        long placed = students.stream()
                .filter(s -> s.getPlacementTier() != null
                        && !s.getPlacementTier().equalsIgnoreCase("Unplaced"))
                .count();
        dto.setTotalPlacedStudents(placed);
        dto.setPlacementPercentage(students.isEmpty() ? 0
                : Math.round((placed * 100.0 / students.size()) * 10.0) / 10.0);

        // ── Tier distribution ─────────────────────────────────────────────────
        dto.setTierCCount(students.stream()
                .filter(s -> "C".equals(s.getPlacementTier())).count());
        dto.setTierBCount(students.stream()
                .filter(s -> "B".equals(s.getPlacementTier())).count());
        dto.setTierACount(students.stream()
                .filter(s -> "A".equals(s.getPlacementTier())).count());
        dto.setUnplacedCount(students.stream()
                .filter(s -> s.getPlacementTier() == null
                        || s.getPlacementTier().equalsIgnoreCase("Unplaced"))
                .count());

        // ── Package analytics (from all drives — not cohort-specific) ─────────
        List<Double> packages = drives.stream()
                .filter(d -> d.getPackageLpa() != null)
                .map(d -> d.getPackageLpa().doubleValue())
                .collect(Collectors.toList());

        double[] globalPackageStats = packageStats(packages);
        dto.setHighestPackage(globalPackageStats[0]);
        dto.setAveragePackage(globalPackageStats[1]);
        dto.setMedianPackage(globalPackageStats[2]);

        // ── Drive statistics (all drives, not cohort-filtered) ────────────────
        dto.setUpcomingDrives(drives.stream()
                .filter(d -> "Upcoming".equalsIgnoreCase(d.getStatus())).count());
        dto.setCompletedDrives(drives.stream()
                .filter(d -> "Completed".equalsIgnoreCase(d.getStatus())).count());
        dto.setCancelledDrives(drives.stream()
                .filter(d -> "Cancelled".equalsIgnoreCase(d.getStatus())).count());

        // ── Eligibility snapshot ──────────────────────────────────────────────
        dto.setNoBacklogStudents(students.stream()
                .filter(s -> s.getActiveBacklogs() == null || s.getActiveBacklogs() == 0)
                .count());
        dto.setDreamEligibleStudents(students.stream()
                .filter(s -> (s.getActiveBacklogs() == null || s.getActiveBacklogs() == 0)
                        && s.getCgpa() != null && s.getCgpa() >= 7.0)
                .count());
        dto.setSuperDreamEligibleStudents(students.stream()
                .filter(s -> (s.getActiveBacklogs() == null || s.getActiveBacklogs() == 0)
                        && s.getCgpa() != null && s.getCgpa() >= 8.5)
                .count());
        dto.setStudentsWithBacklogs(students.stream()
                .filter(s -> s.getActiveBacklogs() != null && s.getActiveBacklogs() > 0)
                .count());

        // ── Placement trends (always global — all batch years, all branches) ──
        Map<Integer, List<Student>> byYear = allStudents.stream()
                .filter(s -> s.getBatchYear() != null)
                .collect(Collectors.groupingBy(Student::getBatchYear));

        List<DashboardDTO.TrendPointDTO> trends = byYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    DashboardDTO.TrendPointDTO t = new DashboardDTO.TrendPointDTO();
                    t.setYear(e.getKey());
                    t.setTotalStudents(e.getValue().size());
                    long p = e.getValue().stream()
                            .filter(s -> s.getPlacementTier() != null
                                    && !s.getPlacementTier().equalsIgnoreCase("Unplaced"))
                            .count();
                    t.setPlacedStudents(p);
                    t.setPlacementPercentage(Math.round((p * 100.0 / e.getValue().size()) * 10.0) / 10.0);
                    return t;
                })
                .collect(Collectors.toList());
        dto.setPlacementTrends(trends);

        // ── Branch-wise placement ─────────────────────────────────────────────
        Map<String, List<Student>> byBranch = students.stream()
                .filter(s -> s.getDepartment() != null && s.getDepartment().getBranch() != null)
                .collect(Collectors.groupingBy(s -> s.getDepartment().getBranch()));

        List<DashboardDTO.BranchPlacementDTO> branchList = byBranch.entrySet().stream()
                .map(e -> {
                    DashboardDTO.BranchPlacementDTO b = new DashboardDTO.BranchPlacementDTO();
                    b.setBranch(e.getKey());
                    b.setTotalStudents(e.getValue().size());
                    long placedInBranch = e.getValue().stream()
                            .filter(s -> s.getPlacementTier() != null
                                    && !s.getPlacementTier().equalsIgnoreCase("Unplaced"))
                            .count();
                    b.setPlacedStudents(placedInBranch);
                    b.setPlacementPercentage(Math.round(
                            (placedInBranch * 100.0 / e.getValue().size()) * 10.0) / 10.0);
                    return b;
                })
                .sorted(Comparator.comparingDouble(
                        DashboardDTO.BranchPlacementDTO::getPlacementPercentage).reversed())
                .collect(Collectors.toList());
        dto.setBranchWisePlacement(branchList);

        // ── Program × branch statistics matrix (year-filtered only — branch and
        // program are the pivot dimensions here, so the dashboard's own branch/
        // program dropdowns don't apply to this table) ─────────────────────────
        List<Student> yearPool = allStudents.stream()
                .filter(s -> year == null || year.equals(s.getBatchYear()))
                .collect(Collectors.toList());

        Map<String, List<Student>> byProgram = yearPool.stream()
                .filter(s -> s.getDepartment() != null && s.getDepartment().getProgram() != null)
                .collect(Collectors.groupingBy(s -> s.getDepartment().getProgram()));

        List<DashboardDTO.ProgramStatsDTO> programStatsList = byProgram.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    String programName = e.getKey();
                    List<Student> programStudents = e.getValue();

                    Map<String, List<Student>> byBranchInProgram = programStudents.stream()
                            .filter(s -> s.getDepartment().getBranch() != null)
                            .collect(Collectors.groupingBy(s -> s.getDepartment().getBranch()));

                    List<DashboardDTO.BranchCategoryStatsDTO> categories = byBranchInProgram.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(be -> computeCategoryStats(be.getKey(), be.getValue(), allApplications))
                            .collect(Collectors.toList());
                    categories.add(computeCategoryStats("Overall", programStudents, allApplications));

                    DashboardDTO.ProgramStatsDTO p = new DashboardDTO.ProgramStatsDTO();
                    p.setProgram(programName);
                    p.setCategories(categories);
                    return p;
                })
                .collect(Collectors.toList());
        dto.setProgramStats(programStatsList);

        // ── Top recruiters (filtered — selections from cohort students) ────────
        Map<String, long[]> recruiterData = new LinkedHashMap<>();
        Map<String, String> recruiterTier = new HashMap<>();

        applications.stream()
                .filter(a -> "Selected".equalsIgnoreCase(a.getStatus())
                        && a.getDrive() != null
                        && a.getDrive().getCompany() != null)
                .forEach(a -> {
                    String name = a.getDrive().getCompany().getCompanyName();
                    String tier = a.getDrive().getCompany().getTier();
                    recruiterData.merge(name, new long[]{1}, (existing, val) -> {
                        existing[0]++;
                        return existing;
                    });
                    recruiterTier.putIfAbsent(name, tier);
                });

        List<DashboardDTO.RecruiterDTO> recruiters = recruiterData.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> {
                    DashboardDTO.RecruiterDTO r = new DashboardDTO.RecruiterDTO();
                    r.setCompanyName(e.getKey());
                    r.setTier(recruiterTier.get(e.getKey()));
                    r.setStudentsHired(e.getValue()[0]);
                    return r;
                })
                .collect(Collectors.toList());
        dto.setTopRecruiters(recruiters);

        // ── Full companies-placed list (filtered, unranked, uncapped — unlike topRecruiters) ──
        List<String> placedCompanies = applications.stream()
                .filter(a -> "Selected".equalsIgnoreCase(a.getStatus())
                        && a.getDrive() != null
                        && a.getDrive().getCompany() != null)
                .map(a -> a.getDrive().getCompany().getCompanyName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        dto.setPlacedCompanies(placedCompanies);

        // ── Recent activities (filtered — latest 10 from cohort students) ──────
        List<DashboardDTO.ActivityDTO> activities = applications.stream()
                .filter(a -> a.getStudent() != null
                        && a.getDrive() != null
                        && a.getDrive().getCompany() != null)
                .sorted((a, b) -> {
                    if (a.getApplicationDate() == null) return 1;
                    if (b.getApplicationDate() == null) return -1;
                    return b.getApplicationDate().compareTo(a.getApplicationDate());
                })
                .limit(10)
                .map(a -> {
                    DashboardDTO.ActivityDTO act = new DashboardDTO.ActivityDTO();
                    act.setStudentName(a.getStudent().getName());
                    act.setCompanyName(a.getDrive().getCompany().getCompanyName());
                    act.setStatus(a.getStatus());
                    act.setApplicationDate(
                            a.getApplicationDate() != null ? a.getApplicationDate().toString() : null);
                    return act;
                })
                .collect(Collectors.toList());
        dto.setRecentActivities(activities);

        // ── Most applied company (all statuses, filtered cohort) ──────────────
        Map<String, Long> appliedByCompany = new HashMap<>();
        applications.stream()
                .filter(a -> a.getDrive() != null && a.getDrive().getCompany() != null)
                .forEach(a -> appliedByCompany.merge(
                        a.getDrive().getCompany().getCompanyName(), 1L, Long::sum));

        appliedByCompany.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> {
                    dto.setMostAppliedCompanyName(e.getKey());
                    dto.setMostAppliedCompanyCount(e.getValue());
                });

        // ── Last updated timestamp ─────────────────────────────────────────────
        dto.setLastUpdated(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));

        return dto;
    }
}
