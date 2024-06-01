// src/services/api.js
import axios from 'axios';

const API_BASE_URL = 'https://test-server.com/api';

const api = axios.create({
  baseURL: API_BASE_URL,
});

export const fetchProducts = async (category, company, filters) => {
  const response = await api.get('/products', {
    params: { category, company, ...filters },
  });
  return response.data;
};

export const fetchProductById = async (productId) => {
  const response = await api.get(`/products/${productId}`);
  return response.data;
};
