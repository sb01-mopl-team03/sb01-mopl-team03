# 📹 모두의 플리
> 대규모 트래픽이 예상되는 글로벌 컨텐츠 평점 및 큐레이션 플랫폼
</br> 
모무의 플리에서는 다른 사용자와 소통하며 콘텐츠 경험을 확장할 수 있는 서비스를 제공받을 수 있습니다. :partying_face: 
</br>
</br> 1️⃣ 영화, 드라마, 스포츠 등 다양한 콘텐츠를 큐레이팅
</br> 2️⃣ 실시간으로 예고편 같이 보기
</br> 3️⃣ 나만의 콘텐츠 플레이리스트 만들기

</br>
</br>

## :family: 팀원
| 김경린 | 김창우 | 박유진 | 양병운 | 이유빈 |
|:-----:|:------:|:------:|:------:|:-------:|
|<img src="https://avatars.githubusercontent.com/u/133985654?v=4" width="130">|<img src="https://avatars.githubusercontent.com/u/101470043?v=4" width="130">|<img src="https://avatars.githubusercontent.com/u/78692557?v=4" width="130">|<img src="https://avatars.githubusercontent.com/u/143311964?v=4" width="130">|<img src="https://avatars.githubusercontent.com/u/80386881?v=4" width="130">|
| [k01zero](https://github.com/k01zero) | [qwertyuiop4m](https://github.com/qwertyuiop4m)| [yudility](https://github.com/yudility) | [Yang-ByeongUn](https://github.com/Yang-ByeongUn) | [iiyubb](https://github.com/iiyubb) |

</br>
</br>

## 🚀 기술 스택

<div align="center">

  <br>

**☕ Language & Platform ☕**

<img alt="Java" src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white" />  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />  

<br>

**🗄️ Database 🗄️** 

<img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white" />  <img alt="H2" src="https://img.shields.io/badge/H2-1C99E0?style=for-the-badge&logo=h2&logoColor=white" />

<br>

**:package: Storage :package:**

<img alt="AWS S3" src="https://img.shields.io/badge/AWS%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white" />

<br>
<br>


**🌐 Messaging 🌐**

<img alt="WebSocket" src="https://img.shields.io/badge/WebSocket-6A1B9A?style=for-the-badge&logo=websocket&logoColor=white" />

<br>
<br>

**🔐 Security & Auth 🔐**

<img alt="Spring Security" src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" />  <img alt="OAuth2" src="https://img.shields.io/badge/OAuth2-2C4A7A?style=for-the-badge&logo=openid&logoColor=white" />  <img alt="JWT" src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />

<br>

**📦 Build & Test 📦**

<img alt="Gradle" src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" />  <img alt="Jacoco" src="https://img.shields.io/badge/Jacoco-C51A4A?style=for-the-badge&logo=jacoco&logoColor=white" />  <img alt="JUnit5" src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white" />  <img alt="Mockito" src="https://img.shields.io/badge/Mockito-FFCB2B?style=for-the-badge&logo=mockito&logoColor=black" />  

<br>

**🔍 Monitoring & Docs 🔍** 

<img alt="Springdoc OpenAPI" src="https://img.shields.io/badge/OpenAPI-6BA539?style=for-the-badge&logo=swagger&logoColor=white" />  <img alt="Prometheus" src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white" />  <img alt="Actuator" src="https://img.shields.io/badge/Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />

<br>

**:speech_balloon: Tool :speech_balloon:** 

<img alt="GitHub" src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" />  
<img alt="Notion" src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white" />  
<img alt="Discord" src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white" />


</div>

</br>
</br>

## 📁 파일 구조
.. 파일 구조 입력 ..

</br>
</br>

## :pushpin: 주요 기능
### 사용자 관리
- **어드민 기능**
  - 어드민 계정 자동 초기화
  - 사용자 권한 변경 (ADMIN / USER) 및 권한 변경 시 자동 로그아웃
  - 계정 잠금 및 잠금된 계정 자동 로그아웃
- **회원 가입 및 로그인**
  - 이메일 기반 회원 가입
  - JWT 기반 인증/인가
  - 중복 로그인 시 강제 로그아웃 처리
- **비밀번호 초기화**
  - 이메일로 임시 비밀번호 발급 및 만료 시간 적용
  - 임시 비밀번호 로그인 시 새 비밀번호 변경 필수
- **소셜 계정 연동**
  - Google, Kakao 계정 연동 및 연동 계정 로그인 지원
  - Kakao는 이메일 정보 제한으로 가상 이메일 사용

### 콘텐츠 데이터 관리
- TMDB, The Sports DB API를 통한 콘텐츠 수집
- Spring Batch 기반 콘텐츠 수집 및 갱신 배치 작업 자동화

### 콘텐츠 평가 및 큐레이팅
- 콘텐츠 의견 및 평점 등록
- 개인 맞춤 큐레이션 (예: 사랑, 사람 등 키워드 검색)
- 플레이리스트 공유 및 구독, 구독 시 알림 제공

### 실시간 같이 보기
- 동일 콘텐츠 실시간 시청 및 의견 교환
- 실시간 시청자 수 확인 기능

### 팔로우 및 DM
- 사용자 팔로우 및 팔로우한 사용자의 시청 콘텐츠 실시간 확인
- 팔로우 및 DM 수신 시 알림 제공
- 실시간 DM(쪽지) 채팅 기능

### 알림
- 권한 변경, 플레이리스트 구독/등록, 팔로우, DM 수신 시 알림 제공
- Server Sent Event(SSE) 기반 실시간 알림 처리

</br>
</br>


## :pushpin: 구현 기능 상세
.. 각 작동하는 기능의 gif 이미지 입력 ..
