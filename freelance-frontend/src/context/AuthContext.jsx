import { createContext, useState, useContext, useEffect } from 'react';
import apiService from '../api/apiService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load user from localStorage on mount
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (err) {
        console.error('Failed to parse saved user:', err);
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const register = async (userData) => {
    try {
      setError(null);
      console.log('Registering with userData:', userData);
      const response = await apiService.register(userData);
      if (response.success && response.data) {
        const newUser = {
          id: response.data.id,
          email: response.data.email,
          fullName: response.data.fullName,
          phone: response.data.phone,
          role: response.data.role,
        };
        setUser(newUser);
        localStorage.setItem('user', JSON.stringify(newUser));
        return response;
      }
    } catch (err) {
      console.error('Registration error:', err);
      const errorMsg = err.response?.data?.message || err.message || 'Đăng ký thất bại';
      setError(errorMsg);
      throw err;
    }
  };

  const login = async (credentials) => {
    try {
      setError(null);
      const response = await apiService.login(credentials);
      if (response.success && response.data) {
        const newUser = {
          id: response.data.id,
          email: response.data.email,
          fullName: response.data.fullName,
          phone: response.data.phone,
          role: response.data.role,
          status: response.data.status,
        };
        setUser(newUser);
        localStorage.setItem('user', JSON.stringify(newUser));
        return response;
      }
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
  };

  const isAuthenticated = !!user;
  const isStudent = user?.role === 'STUDENT';
  const isEmployer = user?.role === 'EMPLOYER';
  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        error,
        isAuthenticated,
        isStudent,
        isEmployer,
        isAdmin,
        register,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
