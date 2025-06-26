import React, { useState, useEffect } from 'react';

const Header = ({ user, notifications, unreadNotifications, onLogout, onNavigate, currentPage }) => {
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfile, setShowProfile] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  // 외부 클릭 감지
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (showNotifications || showProfile) {
        setShowNotifications(false);
        setShowProfile(false);
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [showNotifications, showProfile]);

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    setShowLogoutModal(false);
    onLogout();
  };

  const navItems = [
    { id: 'home', label: '홈' },
    { id: 'movies', label: '영화' },
    { id: 'series', label: '드라마' },
    { id: 'sports', label: '스포츠' },
    { id: 'curation', label: '큐레이션' },
    { id: 'live', label: '라이브' },
    { id: 'playlists', label: '플레이리스트' }
  ];

  const handleNavClick = (sectionId) => {
    if (onNavigate) {
      onNavigate(sectionId);
    }
  };

  return (
    <>
      <header className="header">
        <nav className="nav">
          <div className="logo" onClick={() => handleNavClick('home')}>모플</div>
          
          <ul className="nav-links">
            {navItems.map(item => (
              <li key={item.id}>
                <a 
                  href={`#${item.id}`} 
                  className={currentPage === item.id ? 'active' : ''}
                  onClick={(e) => {
                    e.preventDefault();
                    handleNavClick(item.id);
                  }}
                >
                  {item.label}
                </a>
              </li>
            ))}
          </ul>

          <div className="nav-actions">
            {/* Notification */}
            <div className="notification-container">
              <div
                className="notification-icon"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowNotifications(!showNotifications);
                  setShowProfile(false);
                }}
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                </svg>
                {unreadNotifications > 0 && (
                  <span className="notification-badge">{unreadNotifications}</span>
                )}
              </div>

              {showNotifications && (
                <div className="notification-dropdown show">
                  <div className="notification-header">
                    <h3>알림</h3>
                    <button className="mark-all-read">모두 읽음</button>
                  </div>
                  <div className="notification-list">
                    {notifications.map(notification => (
                      <div key={notification.id} className={`notification-item ${!notification.read ? 'unread' : ''}`}>
                        <div className="notification-title">{notification.title}</div>
                        <div className="notification-message">{notification.message}</div>
                        <div className="notification-time">{notification.time}</div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* User Profile */}
            <div className="user-profile-container">
              <div
                className="user-profile"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowProfile(!showProfile);
                  setShowNotifications(false);
                }}
              >
                <div className="profile-avatar">{user?.avatar}</div>
                <span className="user-name">{user?.name}</span>
                <svg className="profile-dropdown-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="6,9 12,15 18,9"></polyline>
                </svg>
              </div>

              {showProfile && (
                <div className="user-profile-dropdown show">
                  <div className="profile-dropdown-item" onClick={() => alert('마이페이지 준비 중입니다!')}>
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <span>마이페이지</span>
                  </div>
                  <div className="profile-dropdown-divider"></div>
                  <div className="profile-dropdown-item" onClick={() => setShowLogoutModal(true)}>
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                      <polyline points="16,17 21,12 16,7"></polyline>
                      <line x1="21" y1="12" x2="9" y2="12"></line>
                    </svg>
                    <span>로그아웃</span>
                  </div>
                </div>
              )}
            </div>
          </div>
        </nav>
      </header>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>로그아웃</h2>
            <p>정말로 로그아웃 하시겠습니까?</p>
            <div className="modal-actions">
              <button onClick={handleLogout} className="confirm-button">로그아웃</button>
              <button onClick={() => setShowLogoutModal(false)} className="cancel-button">취소</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Header;