import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Main from './components/Main';
import './styles/App.css';

function App() {
  const [currentPage, setCurrentPage] = useState('login');
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 로그인 상태 확인
    const checkAuthStatus = () => {
      const token = localStorage.getItem('authToken');
      const userData = localStorage.getItem('userData');
      
      if (token && userData) {
        try {
          const parsedUserData = JSON.parse(userData);
          setUser(parsedUserData);
          setCurrentPage('main');
        } catch (error) {
          console.error('사용자 데이터 파싱 오류:', error);
          // 손상된 데이터 정리
          localStorage.removeItem('authToken');
          localStorage.removeItem('userData');
          setCurrentPage('login');
        }
      } else {
        setCurrentPage('login');
      }
      setIsLoading(false);
    };

    checkAuthStatus();
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
    setCurrentPage('main');
  };

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    setUser(null);
    setCurrentPage('login');
  };

  // 로딩 중일 때 스피너 표시
  if (isLoading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>모플을 로드하고 있습니다...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="App">
      {currentPage === 'login' && <Login onLogin={handleLogin} />}
      {currentPage === 'main' && <Main user={user} onLogout={handleLogout} />}
    </div>
  );
}

export default App;