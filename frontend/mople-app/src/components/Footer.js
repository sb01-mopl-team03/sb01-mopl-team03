import React from 'react';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-section">
          <h3>모플</h3>
          <p>모두의 플레이 - 함께하는 즐거움</p>
        </div>
        <div className="footer-section">
          <h4>서비스</h4>
          <ul>
            <li><a href="#">영화</a></li>
            <li><a href="#">드라마</a></li>
            <li><a href="#">스포츠</a></li>
            <li><a href="#">라이브</a></li>
          </ul>
        </div>
        <div className="footer-section">
          <h4>고객지원</h4>
          <ul>
            <li><a href="#">자주 묻는 질문</a></li>
            <li><a href="#">고객센터</a></li>
            <li><a href="#">개인정보처리방침</a></li>
            <li><a href="#">이용약관</a></li>
          </ul>
        </div>
        <div className="footer-section">
          <h4>회사정보</h4>
          <ul>
            <li><a href="#">회사소개</a></li>
            <li><a href="#">채용정보</a></li>
            <li><a href="#">투자정보</a></li>
            <li><a href="#">보도자료</a></li>
          </ul>
        </div>
      </div>
      <div className="footer-bottom">
        <p>&copy; 2025 모플(Mopl) OT. All rights reserved.</p>
        <p>코드잇 스프린트 SB 01 Team 03</p> 
      </div>
    </footer>
  );
};

export default Footer;