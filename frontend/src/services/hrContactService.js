import axios from "axios";

const API_URL = "http://localhost:8080/hrcontacts";

export const getAllHRContacts = () => axios.get(API_URL);
