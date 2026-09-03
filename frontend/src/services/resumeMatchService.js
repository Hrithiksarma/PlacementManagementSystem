import axios from "axios";

const BASE = `${import.meta.env.VITE_API_URL}/api/student`;

export const analyzeResumeMatch = (driveId) => axios.post(`${BASE}/drives/${driveId}/analyze`);

export const getResumeMatch = (driveId) => axios.get(`${BASE}/drives/${driveId}/matches`);

export const getResumeCritique = () => axios.post(`${BASE}/resume-critique`);
