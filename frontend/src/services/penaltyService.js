import axios from "axios";

const API_URL = "http://localhost:8080/penalties";

export const getPenalties = () => axios.get(API_URL);

export const liftPenalty = (id) => axios.post(`${API_URL}/${id}/lift`);
