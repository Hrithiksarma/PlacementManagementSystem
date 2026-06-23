import axios from "axios";

const BASE = "http://localhost:8080/api/student";

export const getStudentProfile      = ()         => axios.get(`${BASE}/profile`);
export const getEligibleDrives      = ()         => axios.get(`${BASE}/eligible-drives`);
export const getStudentApplications = ()         => axios.get(`${BASE}/applications`);
export const getPlacementStatus     = ()         => axios.get(`${BASE}/placement-status`);
export const getPlacementOffers     = ()         => axios.get(`${BASE}/placement-offers`);
export const acceptOffer            = (appId)    => axios.post(`${BASE}/applications/${appId}/accept-offer`);
export const rejectOffer            = (appId)    => axios.post(`${BASE}/applications/${appId}/reject-offer`);
export const applyToDrive           = (driveId)  => axios.post(`${BASE}/apply/${driveId}`);
export const getRecentAchievements  = ()         => axios.get(`${BASE}/achievements`);
