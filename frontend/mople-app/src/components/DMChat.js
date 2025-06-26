import React, { useState, useEffect, useRef } from 'react';
import '../styles/DMChat.css';

const DMChat = ({ 
  contacts, 
  currentChat, 
  isMinimized, 
  user,
  onClose, 
  onMinimize, 
  onStartChat, 
  onBackToContacts 
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const messagesEndRef = useRef(null);

  // 더미 메시지 데이터
  const dummyMessages = {
    1: [
      { id: 1, senderId: 1, content: '안녕하세요!', timestamp: '2024-06-25T10:00:00Z', sent: false },
      { id: 2, senderId: user?.id || 1, content: '안녕하세요! 반갑습니다.', timestamp: '2024-06-25T10:01:00Z', sent: true },
      { id: 3, senderId: 1, content: '오늘 영화 같이 볼까요?', timestamp: '2024-06-25T10:02:00Z', sent: false },
    ],
    2: [
      { id: 4, senderId: 2, content: '내일 스포츠 경기 같이 봐요', timestamp: '2024-06-25T09:00:00Z', sent: false },
      { id: 5, senderId: user?.id || 1, content: '좋아요! 몇 시에 만날까요?', timestamp: '2024-06-25T09:05:00Z', sent: true },
    ],
    3: [
      { id: 6, senderId: 3, content: '플레이리스트 공유 감사합니다!', timestamp: '2024-06-25T08:00:00Z', sent: false },
    ]
  };

  // 현재 채팅의 메시지 로드
  useEffect(() => {
    if (currentChat) {
      setMessages(dummyMessages[currentChat.id] || []);
    }
  }, [currentChat]);

  // 메시지 목록 끝으로 스크롤
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // 검색 필터링
  const filteredContacts = contacts.filter(contact =>
    contact.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // 메시지 전송
  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!newMessage.trim() || !currentChat) return;

    const message = {
      id: Date.now(),
      senderId: user?.id || 1,
      content: newMessage.trim(),
      timestamp: new Date().toISOString(),
      sent: true
    };

    setMessages(prev => [...prev, message]);
    setNewMessage('');

    // 더미 응답 (실제로는 웹소켓으로 실시간 메시지 수신)
    setTimeout(() => {
      const response = {
        id: Date.now() + 1,
        senderId: currentChat.id,
        content: `"${newMessage.trim()}"에 대한 응답입니다!`,
        timestamp: new Date().toISOString(),
        sent: false
      };
      setMessages(prev => [...prev, response]);
    }, 1000);
  };

  // 시간 포맷팅
  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('ko-KR', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false 
    });
  };

  return (
    <div className={`dm-chat-window ${isMinimized ? 'minimized' : ''}`}>
      {/* Header */}
      <div 
        className="chat-window-header"
        onClick={isMinimized ? onMinimize : undefined}
      >
        <div className="chat-window-user">
          {currentChat ? (
            <>
              <button className="back-button" onClick={onBackToContacts}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M19 12H5"></path>
                  <path d="M12 19l-7-7 7-7"></path>
                </svg>
              </button>
              <div className="chat-user-avatar">{currentChat.avatar}</div>
              <span>{currentChat.name}</span>
            </>
          ) : (
            <span>메시지</span>
          )}
        </div>
        <div className="chat-window-controls">
          <button className="minimize-chat" onClick={onMinimize}>
            {isMinimized ? '□' : '−'}
          </button>
          <button className="close-chat" onClick={onClose}>×</button>
        </div>
      </div>

      {!isMinimized && (
        <div className="chat-window-content">
          {!currentChat ? (
            // 연락처 목록
            <div className="dm-contact-list">
              <div className="dm-search-input">
                <input
                  type="text"
                  placeholder="메시지 보낼 사람 검색..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <div className="dm-contacts">
                {filteredContacts.length > 0 ? (
                  filteredContacts.map(contact => (
                    <div
                      key={contact.id}
                      className="dm-contact-item"
                      onClick={() => onStartChat(contact)}
                    >
                      <div className="dm-contact-avatar">{contact.avatar}</div>
                      <div className="dm-contact-info">
                        <div className="dm-contact-name">{contact.name}</div>
                        <div className="dm-contact-status">{contact.status}</div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="no-contacts">
                    {searchQuery ? '검색 결과가 없습니다.' : '연락처가 없습니다.'}
                  </div>
                )}
              </div>
            </div>
          ) : (
            // 채팅 화면
            <div className="dm-chat-content">
              <div className="chat-messages-container">
                {messages.map(message => (
                  <div
                    key={message.id}
                    className={`dm-message ${message.sent ? 'sent' : 'received'}`}
                  >
                    <div className="dm-message-bubble">{message.content}</div>
                    <div className="dm-message-time">{formatTime(message.timestamp)}</div>
                  </div>
                ))}
                <div ref={messagesEndRef} />
              </div>
              
              <form onSubmit={handleSendMessage} className="chat-input-area">
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="메시지를 입력하세요..."
                  autoComplete="off"
                />
                <button type="submit" disabled={!newMessage.trim()}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="22" y1="2" x2="11" y2="13"></line>
                    <polygon points="22,2 15,22 11,13 2,9"></polygon>
                  </svg>
                </button>
              </form>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default DMChat;