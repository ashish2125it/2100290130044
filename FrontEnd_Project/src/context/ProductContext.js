// src/context/ProductContext.js
import React, { createContext, useState, useEffect } from 'react';
import { fetchProducts } from '../services/api';

const ProductContext = createContext();

const ProductProvider = ({ children }) => {
  const [products, setProducts] = useState([]);
  const [filters, setFilters] = useState({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const loadProducts = async () => {
      setLoading(true);
      const data = await fetchProducts(filters.category, filters.company, filters);
      setProducts(data);
      setLoading(false);
    };
    loadProducts();
  }, [filters]);

  return (
    <ProductContext.Provider value={{ products, setFilters, loading }}>
      {children}
    </ProductContext.Provider>
  );
};

export { ProductContext, ProductProvider };
