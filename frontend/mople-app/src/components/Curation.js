import React, { useState, useEffect, useRef } from 'react';
import { curationData } from '../data/dummyData';
import '../styles/Curation.css';

const Curation = () => {
  const [selectedKeyword, setSelectedKeyword] = useState('all');
  const [curationContent, setCurationContent] = useState([]);
  const [keywords, setKeywords] = useState([
    { id: 'all', label: '전체', color: '#4ecdc4', isDefault: true },
    { id: 'romance', label: '로맨스', color: '#ff6b6b', isDefault: false },
    { id: 'action', label: '액션', color: '#ffa726', isDefault: false },
    { id: 'comedy', label: '코미디', color: '#66bb6a', isDefault: false },
    { id: 'thriller', label: '스릴러', color: '#ab47bc', isDefault: false },
    { id: 'sf', label: 'SF', color: '#42a5f5', isDefault: false },
    { id: 'fantasy', label: '판타지', color: '#ec407a', isDefault: false },
    { id: 'horror', label: '호러', color: '#8d6e63', isDefault: false },
    { id: 'documentary', label: '다큐멘터리', color: '#26a69a', isDefault: false },
    { id: 'animation', label: '애니메이션', color: '#ffca28', isDefault: false }
  ]);
  const [showAddKeyword, setShowAddKeyword] = useState(false);
  const [newKeywordInput, setNewKeywordInput] = useState('');
  const [loading, setLoading] = useState(false);

  const sectionRefs = useRef({});

  useEffect(() => {
    // 초기 큐레이션 데이터 설정 (4개 섹션으로 제한)
    const limitedCurationData = curationData.slice(0, 4);
    setCurationContent(limitedCurationData);
    
    // 사용자 키워드 목록 불러오기
    loadUserKeywords();
  }, []);

  // 사용자 키워드 목록 조회
  const loadUserKeywords = async () => {
    try {
      // 실제 API 호출 시 주석 해제
      // const response = await fetch('/api/keywords');
      // const userKeywords = await response.json();
      // setKeywords(prev => [...prev.filter(k => k.isDefault), ...userKeywords]);
    } catch (error) {
      console.error('키워드 로딩 실패:', error);
    }
  };

  // 키워드별 콘텐츠 조회
  const loadKeywordContent = async (keywordId) => {
    try {
      setLoading(true);
      if (keywordId === 'all') {
        setCurationContent(curationData.slice(0, 4));
      } else {
        // 실제 API 호출 시 주석 해제
        // const response = await fetch(`/api/keywords/${keywordId}/contents`);
        // const content = await response.json();
        // setCurationContent(content);
        
        // 임시 필터링 로직
        const filteredContent = curationData.slice(0, 4).map(section => ({
          ...section,
          items: section.items.filter(item => 
            item.genres.includes(keywordId) || section.category.includes(keywordId)
          )
        })).filter(section => section.items.length > 0);
        
        setCurationContent(filteredContent);
      }
    } catch (error) {
      console.error('콘텐츠 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleKeywordClick = (keywordId) => {
    setSelectedKeyword(keywordId);
    loadKeywordContent(keywordId);
  };

  // 키워드 추가
  const handleAddKeyword = async () => {
    if (!newKeywordInput.trim()) return;

    try {
      const newKeyword = {
        label: newKeywordInput.trim(),
        color: `#${Math.floor(Math.random()*16777215).toString(16)}`, // 랜덤 색상
        isDefault: false
      };

      // 실제 API 호출 시 주석 해제
      // const response = await fetch('/api/keywords', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify(newKeyword)
      // });
      // const savedKeyword = await response.json();
      
      // 임시로 로컬에 추가
      const savedKeyword = {
        ...newKeyword,
        id: `custom_${Date.now()}`
      };
      
      setKeywords(prev => [...prev, savedKeyword]);
      setNewKeywordInput('');
      setShowAddKeyword(false);
      
      alert('키워드가 추가되었습니다!');
    } catch (error) {
      console.error('키워드 추가 실패:', error);
      alert('키워드 추가에 실패했습니다.');
    }
  };

  // 키워드 삭제
  const handleDeleteKeyword = async (keywordId) => {
    if (window.confirm('이 키워드를 삭제하시겠습니까?')) {
      try {
        // 실제 API 호출 시 주석 해제
        // await fetch(`/api/keywords/${keywordId}`, { method: 'DELETE' });
        
        setKeywords(prev => prev.filter(k => k.id !== keywordId));
        
        // 삭제된 키워드가 현재 선택된 키워드면 '전체'로 변경
        if (selectedKeyword === keywordId) {
          setSelectedKeyword('all');
          loadKeywordContent('all');
        }
        
        alert('키워드가 삭제되었습니다.');
      } catch (error) {
        console.error('키워드 삭제 실패:', error);
        alert('키워드 삭제에 실패했습니다.');
      }
    }
  };

  const scrollLeft = (containerId) => {
    const container = document.getElementById(containerId);
    if (container) {
      container.scrollBy({ left: -300, behavior: 'smooth' });
    }
  };

  const scrollRight = (containerId) => {
    const container = document.getElementById(containerId);
    if (container) {
      container.scrollBy({ left: 300, behavior: 'smooth' });
    }
  };



  return (
    <div className="curation-page">
      {/* 키워드 태그 섹션 */}
      <div className="keyword-section">
        <h2 className="section-title">🎯 취향 맞춤 큐레이션</h2>
        <div className="keyword-container">
          <div className="keyword-tags">
            {keywords.map(keyword => (
              <div key={keyword.id} className="keyword-wrapper">
                <button
                  className={`keyword-tag ${selectedKeyword === keyword.id ? 'active' : ''}`}
                  style={{
                    '--tag-color': keyword.color
                  }}
                  onClick={() => handleKeywordClick(keyword.id)}
                >
                  {keyword.label}
                </button>
                {!keyword.isDefault && (
                  <button
                    className="delete-keyword-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteKeyword(keyword.id);
                    }}
                    title="키워드 삭제"
                  >
                    ×
                  </button>
                )}
              </div>
            ))}
            
            {/* 키워드 추가 버튼 */}
            {showAddKeyword ? (
              <div className="add-keyword-form">
                <input
                  type="text"
                  value={newKeywordInput}
                  onChange={(e) => setNewKeywordInput(e.target.value)}
                  placeholder="키워드 입력"
                  className="keyword-input"
                  onKeyPress={(e) => e.key === 'Enter' && handleAddKeyword()}
                  autoFocus
                />
                <button onClick={handleAddKeyword} className="confirm-btn">✓</button>
                <button onClick={() => {
                  setShowAddKeyword(false);
                  setNewKeywordInput('');
                }} className="cancel-btn">×</button>
              </div>
            ) : (
              <button
                className="add-keyword-btn"
                onClick={() => setShowAddKeyword(true)}
                title="키워드 추가"
              >
                +
              </button>
            )}
          </div>
        </div>
      </div>

      {/* 로딩 인디케이터 */}
      {loading && (
        <div className="loading-indicator">
          <div className="spinner"></div>
          <p>콘텐츠를 불러오는 중...</p>
        </div>
      )}

      {/* 큐레이션 콘텐츠 섹션들 */}
      <div className="curation-sections">
        {curationContent.map(section => (
          <div key={section.id} className="curation-section">
            <div className="section-header">
              <h3 className="section-title">{section.title}</h3>
              <div className="section-controls">
                <button 
                  className="scroll-button left"
                  onClick={() => scrollLeft(`section-${section.id}`)}
                >
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="15,18 9,12 15,6"></polyline>
                  </svg>
                </button>
                <button 
                  className="scroll-button right"
                  onClick={() => scrollRight(`section-${section.id}`)}
                >
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="9,18 15,12 9,6"></polyline>
                  </svg>
                </button>
              </div>
            </div>
            
            <div 
              className="content-row" 
              id={`section-${section.id}`}
            >
              {section.items.map(item => (
                <div 
                  key={item.id} 
                  className="content-item"
                  onClick={() => alert(`"${item.title}" 재생을 시작합니다!`)}
                >
                  <div className="item-thumbnail">
                    <div className="thumbnail-placeholder">
                      {item.icon}
                    </div>
                    <div className="play-overlay">
                      <div className="play-button-large">▶</div>
                    </div>
                  </div>
                  <div className="item-info">
                    <h4 className="item-title">{item.title}</h4>
                    <div className="item-meta">
                      <span className="item-year">{item.year}</span>
                      <span className="item-rating">⭐ {item.rating}</span>
                    </div>
                    <div className="item-genres">
                      {item.displayGenres.map(genre => (
                        <span key={genre} className="genre-tag">{genre}</span>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* 추천 이유 섹션 */}
      <div className="recommendation-info">
        <div className="info-card">
          <h3>🤖 AI 추천 시스템</h3>
          <p>
            시청 기록과 평점을 분석하여 취향에 맞는 콘텐츠를 추천합니다. 
            더 많이 시청할수록 더 정확한 추천을 받을 수 있어요!
          </p>
        </div>
        <div className="info-card">
          <h3>👥 사용자 기반 추천</h3>
          <p>
            비슷한 취향을 가진 다른 사용자들이 좋아한 콘텐츠를 추천합니다. 
            새로운 장르를 발견해보세요!
          </p>
        </div>
      </div>
    </div>
  );
};

export default Curation;