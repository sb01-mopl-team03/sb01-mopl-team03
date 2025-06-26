import React, { useState, useEffect } from 'react';
import Header from './Header';
import Footer from './Footer';
import DMChat from './DMChat';
import Curation from './Curation';
import { dummyData } from '../data/dummyData';
import '../styles/Main.css';

const Main = ({ user, onLogout }) => {
  const [currentContent, setCurrentContent] = useState([]);
  const [liveRooms, setLiveRooms] = useState([]);
  const [playlists, setPlaylists] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [unreadNotifications, setUnreadNotifications] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState('home');
  
  // DM 관련 상태
  const [showDMChat, setShowDMChat] = useState(false);
  const [dmContacts, setDmContacts] = useState([]);
  const [currentChat, setCurrentChat] = useState(null);
  const [isMinimized, setIsMinimized] = useState(false);

  useEffect(() => {
    setCurrentContent(dummyData.content);
    setLiveRooms(dummyData.liveRooms);
    setPlaylists(dummyData.playlists);
    setNotifications(dummyData.notifications);
    setUnreadNotifications(dummyData.notifications.filter(n => !n.read).length);
    setDmContacts(dummyData.dmContacts);
  }, []);

  const filteredContent = currentContent.filter(item =>
    item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    item.category.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // 네비게이션 핸들러
  const handleNavigation = (page) => {
    setCurrentPage(page);
    
    // 페이지별 필터링 로직
    switch (page) {
      case 'movies':
        setCurrentContent(dummyData.content.filter(item => 
          item.category.toLowerCase().includes('영화') || 
          item.category.toLowerCase().includes('sf') ||
          item.icon === '🎬'
        ));
        break;
      case 'series':
        setCurrentContent(dummyData.content.filter(item => 
          item.category.toLowerCase().includes('드라마') || 
          item.category.toLowerCase().includes('코미디') ||
          item.icon === '📺'
        ));
        break;
      case 'sports':
        setCurrentContent(dummyData.content.filter(item => 
          item.category.toLowerCase().includes('스포츠') ||
          item.icon === '⚽'
        ));
        break;
      case 'live':
        // 라이브 섹션으로 스크롤
        const liveSection = document.querySelector('.live-watch');
        if (liveSection) {
          liveSection.scrollIntoView({ behavior: 'smooth' });
        }
        setCurrentPage('home');
        break;
      case 'playlists':
        // 플레이리스트 섹션으로 스크롤
        const playlistSection = document.querySelector('.playlist-grid');
        if (playlistSection) {
          playlistSection.parentElement.scrollIntoView({ behavior: 'smooth' });
        }
        setCurrentPage('home');
        break;
      case 'curation':
        // 큐레이션 페이지는 별도 처리
        break;
      default:
        setCurrentContent(dummyData.content);
        setCurrentPage('home');
    }
  };

  // DM 창 열기
  const openDMChat = () => {
    setShowDMChat(true);
    setIsMinimized(false);
    setCurrentChat(null);
  };

  // DM 창 닫기
  const closeDMChat = () => {
    setShowDMChat(false);
    setCurrentChat(null);
    setIsMinimized(false);
  };

  // DM 창 최소화/복원
  const toggleMinimize = () => {
    setIsMinimized(!isMinimized);
  };

  // 채팅 시작
  const startChat = (contact) => {
    setCurrentChat(contact);
  };

  // 연락처 목록으로 돌아가기
  const backToContacts = () => {
    setCurrentChat(null);
  };

  // 큐레이션 페이지 렌더링
  if (currentPage === 'curation') {
    return (
      <div className="main-page">
        <Header 
          user={user} 
          notifications={notifications}
          unreadNotifications={unreadNotifications}
          onLogout={onLogout}
          onNavigate={handleNavigation}
          currentPage={currentPage}
        />

        <Curation />

        {/* Floating Action Button (DM) */}
        <div className="fab" onClick={openDMChat}>
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
        </div>

        {/* DM Chat Component */}
        {showDMChat && (
          <DMChat
            contacts={dmContacts}
            currentChat={currentChat}
            isMinimized={isMinimized}
            user={user}
            onClose={closeDMChat}
            onMinimize={toggleMinimize}
            onStartChat={startChat}
            onBackToContacts={backToContacts}
          />
        )}

        <Footer />
      </div>
    );
  }

  // 기본 홈 페이지 렌더링
  return (
    <div className="main-page">
      <Header 
        user={user} 
        notifications={notifications}
        unreadNotifications={unreadNotifications}
        onLogout={onLogout}
        onNavigate={handleNavigation}
        currentPage={currentPage}
      />

      <main className="main-content">
        {/* Hero Section */}
        <section className="hero">
          <div className="hero-content">
            <h1>모두의 플레이</h1>
            <p>친구들과 실시간으로 영화, 드라마, 스포츠를 함께 시청하고 공유하세요</p>
            <button className="cta-button">지금 시작하기</button>
          </div>
        </section>

        {/* Search Bar */}
        <div className="search-container">
          <div className="search-wrapper">
            <input
              type="text"
              className="search-bar"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="영화, 드라마, 스포츠 검색..."
            />
            <svg className="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
          </div>
        </div>

        {/* Live Rooms */}
        <section className="section">
          <h2 className="section-title">🔴 실시간 같이 보기</h2>
          <div className="live-watch">
            <p>지금 이 순간 다른 사용자들과 함께 시청 중인 콘텐츠에 참여하세요!</p>
            <div className="live-rooms">
              {liveRooms.map(room => (
                <div key={room.id} className="room-card" onClick={() => alert(`"${room.title}" 라이브 룸에 참여합니다!`)}>
                  <div className="room-status">LIVE</div>
                  <h3>{room.title}</h3>
                  <p>{room.category}</p>
                  <div className="viewer-count">👥 {room.viewers}명 시청 중</div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Trending Content */}
        <section className="section">
          <h2 className="section-title">
            {currentPage === 'movies' && '🎬 영화'}
            {currentPage === 'series' && '📺 드라마'}
            {currentPage === 'sports' && '⚽ 스포츠'}
            {currentPage === 'home' && '🔥 지금 인기'}
          </h2>
          <div className="content-grid">
            {filteredContent.map(item => (
              <div key={item.id} className="content-card" onClick={() => alert(`"${item.title}" 재생을 시작합니다!`)}>
                <div className="card-image">
                  {item.icon}
                  <div className="play-button">▶</div>
                </div>
                <h3 className="card-title">{item.title}</h3>
                <div className="card-meta">
                  <span>{item.year} · {item.category}</span>
                  <div className="rating">⭐ {item.rating}</div>
                </div>
                <p>{item.description}</p>
                <div className="social-stats">
                  <div className="stat" onClick={(e) => {e.stopPropagation(); alert('좋아요!');}}>
                    ❤️ {item.stats.likes}
                  </div>
                  <div className="stat" onClick={(e) => {e.stopPropagation(); alert('댓글!');}}>
                    💬 {item.stats.comments}
                  </div>
                  <div className="stat" onClick={(e) => {e.stopPropagation(); alert('공유!');}}>
                    📤 {item.stats.shares}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* My Playlists */}
        <section className="section">
          <h2 className="section-title">📋 내 플레이리스트</h2>
          <div className="playlist-grid">
            {playlists.map(playlist => (
              <div key={playlist.id} className="playlist-card">
                <div className="playlist-header">
                  <h3>{playlist.title}</h3>
                  <span>{playlist.itemCount}개 항목</span>
                </div>
                <div className="playlist-items">
                  {playlist.items.map(item => (
                    <div key={item.id} className="playlist-item" onClick={() => alert(`"${item.title}" 재생!`)}>
                      <span>{item.icon}</span>
                      <div>
                        <div>{item.title}</div>
                        <small>{item.category} · {item.duration}</small>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>
      </main>

      {/* Floating Action Button (DM) */}
      <div className="fab" onClick={openDMChat}>
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
        </svg>
      </div>

      {/* DM Chat Component */}
      {showDMChat && (
        <DMChat
          contacts={dmContacts}
          currentChat={currentChat}
          isMinimized={isMinimized}
          user={user}
          onClose={closeDMChat}
          onMinimize={toggleMinimize}
          onStartChat={startChat}
          onBackToContacts={backToContacts}
        />
      )}

      <Footer />
    </div>
  );
};

export default Main;