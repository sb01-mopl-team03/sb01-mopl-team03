import React, { useState } from 'react';
import '../styles/Login.css';

const Login = ({ onLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const showError = (message) => {
    setError(message);
    setSuccess('');
  };

  const showSuccess = (message) => {
    setSuccess(message);
    setError('');
  };

  const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!email || !password) {
      showError('이메일과 비밀번호를 모두 입력해주세요.');
      return;
    }

    if (!isValidEmail(email)) {
      showError('올바른 이메일 형식을 입력해주세요.');
      return;
    }

    if (password.length < 4) {
      showError('비밀번호는 최소 4자 이상이어야 합니다.');
      return;
    }

    setLoading(true);

    try {
      // 백엔드 연결 전까지는 더미 로그인 처리
      // 실제 서비스에서는 아래 fetch 코드의 주석을 해제하세요
      /*
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      const data = await response.json();

      if (response.ok) {
        const userData = data.user;
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('userData', JSON.stringify(userData));
        
        showSuccess('로그인 성공! 메인 페이지로 이동합니다...');
        setTimeout(() => onLogin(userData), 1500);
      } else {
        showError(data.message || '로그인에 실패했습니다.');
      }
      */

      // 더미 로그인 처리 - 실제 서비스에서는 제거
      const userData = { 
        id: 1,
        email, 
        name: email.split('@')[0] || '사용자', 
        avatar: (email.split('@')[0] || '사용자').charAt(0).toUpperCase()
      };
      
      const dummyToken = `dummy-token-${Date.now()}`;
      
      localStorage.setItem('authToken', dummyToken);
      localStorage.setItem('userData', JSON.stringify(userData));
      
      showSuccess('로그인 성공! 메인 페이지로 이동합니다...');
      setTimeout(() => onLogin(userData), 1500);

    } catch (error) {
      console.error('로그인 오류:', error);
      showError('서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const handleSocialLogin = (provider) => {
    alert(`${provider} 로그인 기능은 준비 중입니다!`);
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSubmit(e);
    }
  };

  return (
    <div className="login-page">
      <div className="login-container">
        <h1 className="logo">모두의 플레이</h1>
        <p className="subtitle">친구들과 실시간으로 영화, 드라마, 스포츠를 함께 시청하고 공유하세요</p>
        
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="email">이메일</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="이메일을 입력하세요"
              required
              autoComplete="email"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">비밀번호</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="비밀번호를 입력하세요"
              required
              autoComplete="current-password"
              disabled={loading}
            />
          </div>

          <button type="submit" disabled={loading} className="login-button">
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className="auth-links">
          <a href="#" onClick={(e) => {e.preventDefault(); alert('회원가입 페이지 준비 중입니다!');}}>
            회원가입
          </a>
          <a href="#" onClick={(e) => {e.preventDefault(); alert('비밀번호 찾기 기능 준비 중입니다!');}}>
            비밀번호를 잊어버렸나요?
          </a>
        </div>

        <div className="divider">
          <span>또는</span>
        </div>

        <div className="social-login">
          <button 
            onClick={() => handleSocialLogin('kakao')} 
            className="social-button kakao"
            disabled={loading}
          >
            카카오로 로그인
          </button>
          <button 
            onClick={() => handleSocialLogin('google')} 
            className="social-button google"
            disabled={loading}
          >
            Google로 로그인
          </button>
        </div>
        
        <div style={{ marginTop: '20px', fontSize: '0.8rem', color: 'rgba(255, 255, 255, 0.5)', textAlign: 'center' }}>
          💡 데모 버전: 아무 이메일과 비밀번호로 로그인 가능
        </div>
      </div>
    </div>
  );
};

export default Login;