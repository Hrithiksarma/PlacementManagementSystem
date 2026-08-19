import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/penalties`;

export const getPenalties = () => axios.get(API_URL);

export const liftPenalty = (id) => axios.post(`${API_URL}/${id}/lift`);
