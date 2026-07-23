package com.pmrs.backend.dto;

import java.util.List;

public class DashboardDTO {

    // KPI
    private long totalStudents;
    private long totalCompanies;
    private long totalDrives;
    private long totalApplications;
    private long totalPlacedStudents;
    private double placementPercentage;

    // Tier distribution
    private long tierCCount;
    private long tierBCount;
    private long tierACount;
    private long unplacedCount;

    // Package analytics
    private double highestPackage;
    private double averagePackage;
    private double medianPackage;

    // Drive stats
    private long upcomingDrives;
    private long completedDrives;
    private long cancelledDrives;

    // Eligibility snapshot (renamed from eligibleStudents)
    private long noBacklogStudents;
    private long dreamEligibleStudents;
    private long superDreamEligibleStudents;
    private long studentsWithBacklogs;

    // Filter option lists (always unfiltered — for populating dropdowns)
    private List<Integer> availableYears;
    private List<String> availableBranches;

    // Quick insight fields
    private String mostAppliedCompanyName;
    private long mostAppliedCompanyCount;
    private String lastUpdated;

    // Charts / lists
    private List<TrendPointDTO> placementTrends;
    private List<BranchPlacementDTO> branchWisePlacement;
    private List<RecruiterDTO> topRecruiters;
    private List<ActivityDTO> recentActivities;

    // ─── Getters / Setters ─────────────────────────────────────────────────────

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(long totalCompanies) { this.totalCompanies = totalCompanies; }

    public long getTotalDrives() { return totalDrives; }
    public void setTotalDrives(long totalDrives) { this.totalDrives = totalDrives; }

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getTotalPlacedStudents() { return totalPlacedStudents; }
    public void setTotalPlacedStudents(long totalPlacedStudents) { this.totalPlacedStudents = totalPlacedStudents; }

    public double getPlacementPercentage() { return placementPercentage; }
    public void setPlacementPercentage(double placementPercentage) { this.placementPercentage = placementPercentage; }

    public long getTierCCount() { return tierCCount; }
    public void setTierCCount(long tierCCount) { this.tierCCount = tierCCount; }

    public long getTierBCount() { return tierBCount; }
    public void setTierBCount(long tierBCount) { this.tierBCount = tierBCount; }

    public long getTierACount() { return tierACount; }
    public void setTierACount(long tierACount) { this.tierACount = tierACount; }

    public long getUnplacedCount() { return unplacedCount; }
    public void setUnplacedCount(long unplacedCount) { this.unplacedCount = unplacedCount; }

    public double getHighestPackage() { return highestPackage; }
    public void setHighestPackage(double highestPackage) { this.highestPackage = highestPackage; }

    public double getAveragePackage() { return averagePackage; }
    public void setAveragePackage(double averagePackage) { this.averagePackage = averagePackage; }

    public double getMedianPackage() { return medianPackage; }
    public void setMedianPackage(double medianPackage) { this.medianPackage = medianPackage; }

    public long getUpcomingDrives() { return upcomingDrives; }
    public void setUpcomingDrives(long upcomingDrives) { this.upcomingDrives = upcomingDrives; }

    public long getCompletedDrives() { return completedDrives; }
    public void setCompletedDrives(long completedDrives) { this.completedDrives = completedDrives; }

    public long getCancelledDrives() { return cancelledDrives; }
    public void setCancelledDrives(long cancelledDrives) { this.cancelledDrives = cancelledDrives; }

    public long getNoBacklogStudents() { return noBacklogStudents; }
    public void setNoBacklogStudents(long noBacklogStudents) { this.noBacklogStudents = noBacklogStudents; }

    public long getDreamEligibleStudents() { return dreamEligibleStudents; }
    public void setDreamEligibleStudents(long dreamEligibleStudents) { this.dreamEligibleStudents = dreamEligibleStudents; }

    public long getSuperDreamEligibleStudents() { return superDreamEligibleStudents; }
    public void setSuperDreamEligibleStudents(long v) { this.superDreamEligibleStudents = v; }

    public long getStudentsWithBacklogs() { return studentsWithBacklogs; }
    public void setStudentsWithBacklogs(long studentsWithBacklogs) { this.studentsWithBacklogs = studentsWithBacklogs; }

    public String getMostAppliedCompanyName() { return mostAppliedCompanyName; }
    public void setMostAppliedCompanyName(String v) { this.mostAppliedCompanyName = v; }

    public long getMostAppliedCompanyCount() { return mostAppliedCompanyCount; }
    public void setMostAppliedCompanyCount(long v) { this.mostAppliedCompanyCount = v; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public List<Integer> getAvailableYears() { return availableYears; }
    public void setAvailableYears(List<Integer> availableYears) { this.availableYears = availableYears; }

    public List<String> getAvailableBranches() { return availableBranches; }
    public void setAvailableBranches(List<String> availableBranches) { this.availableBranches = availableBranches; }

    public List<TrendPointDTO> getPlacementTrends() { return placementTrends; }
    public void setPlacementTrends(List<TrendPointDTO> placementTrends) { this.placementTrends = placementTrends; }

    public List<BranchPlacementDTO> getBranchWisePlacement() { return branchWisePlacement; }
    public void setBranchWisePlacement(List<BranchPlacementDTO> branchWisePlacement) { this.branchWisePlacement = branchWisePlacement; }

    public List<RecruiterDTO> getTopRecruiters() { return topRecruiters; }
    public void setTopRecruiters(List<RecruiterDTO> topRecruiters) { this.topRecruiters = topRecruiters; }

    public List<ActivityDTO> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<ActivityDTO> recentActivities) { this.recentActivities = recentActivities; }

    // ─── Nested DTOs ───────────────────────────────────────────────────────────

    public static class TrendPointDTO {
        private int year;
        private long totalStudents;
        private long placedStudents;
        private double placementPercentage;

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

        public long getPlacedStudents() { return placedStudents; }
        public void setPlacedStudents(long placedStudents) { this.placedStudents = placedStudents; }

        public double getPlacementPercentage() { return placementPercentage; }
        public void setPlacementPercentage(double placementPercentage) { this.placementPercentage = placementPercentage; }
    }

    public static class BranchPlacementDTO {
        private String branch;
        private long totalStudents;
        private long placedStudents;
        private double placementPercentage;

        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }

        public long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

        public long getPlacedStudents() { return placedStudents; }
        public void setPlacedStudents(long placedStudents) { this.placedStudents = placedStudents; }

        public double getPlacementPercentage() { return placementPercentage; }
        public void setPlacementPercentage(double v) { this.placementPercentage = v; }
    }

    public static class RecruiterDTO {
        private String companyName;
        private String tier;
        private long studentsHired;

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getTier() { return tier; }
        public void setTier(String tier) { this.tier = tier; }

        public long getStudentsHired() { return studentsHired; }
        public void setStudentsHired(long studentsHired) { this.studentsHired = studentsHired; }
    }

    public static class ActivityDTO {
        private String studentName;
        private String companyName;
        private String status;
        private String applicationDate;

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getApplicationDate() { return applicationDate; }
        public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }
    }
}
